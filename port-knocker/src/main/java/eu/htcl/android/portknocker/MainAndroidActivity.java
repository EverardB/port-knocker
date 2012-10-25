package eu.htcl.android.portknocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.*;

import java.util.ArrayList;

import eu.htcl.android.common.checkboxlist.CheckboxListRow;
import eu.htcl.android.common.checkboxlist.SelectArrayAdapter;
import eu.htcl.android.common.checkboxlist.SelectViewHolder;
import eu.htcl.android.portknocker.DBAdapter.HostData;
import java.util.Arrays;

/**
 *
 * @author everard
 */
public class MainAndroidActivity extends Activity {

    private static final int ACTIVITY_SETTINGS = 0;
    private static String TAG = "port-knocker";
    //private CheckboxListView hostlistView;
    private ListView hostlistView;
    private ArrayAdapter<CheckboxListRow> listAdapter;
    private CheckboxListRow[] listItems;
    HostData[] hostData;
    private DBAdapter dbadapter;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialised after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
     * is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "MainAndroidActivity::onCreate");
        setContentView(R.layout.main);

        // Connect to the database
        dbadapter = new DBAdapter(this.getApplicationContext());
        dbadapter.open();

        // Get host list
        hostData = dbadapter.getHostList();

        // Create and populate host list.
        listItems = (CheckboxListRow[]) getLastNonConfigurationInstance();

        ArrayList<CheckboxListRow> checkboxList = new ArrayList<CheckboxListRow>();
        for (int i = 0; i < hostData.length; i++) {
            checkboxList.add(hostData[i]);
        }

        // Initialise the ListView resource.
//        hostlistView = (CheckboxListView) findViewById(R.id.hostlistView);
//        hostlistView.initialise(this, checkboxList);

        hostlistView = (ListView) findViewById(R.id.hostlistView);

        // Register the ListView to handle a context menu
        registerForContextMenu(hostlistView);

        // When item is tapped, toggle 'selected' property of CheckBox and hostname.
        hostlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                CheckboxListRow row = listAdapter.getItem(position);
                row.toggleSelected();
                SelectViewHolder viewHolder = (SelectViewHolder) item.getTag();
                viewHolder.getCheckBox().setChecked(row.isSelected());
            }
        });

        // Set our custom array adapter as the ListView's adapter.
        listAdapter = new SelectArrayAdapter(this, checkboxList);
        hostlistView.setAdapter(listAdapter);

        Button knockButton = (Button) findViewById(R.id.KnockButton);

        knockButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startKnocking(view, DBAdapter.START_MODE);
            }
        });

        /*
         * Button abortButton = (Button) findViewById(R.id.AbortButton);
         *
         * abortButton.setOnClickListener(new View.OnClickListener() { public
         * void onClick(View view) { launchKnocking(view, DBAdapter.STOP_MODE);
         * } });
         *
         */
    }

    /*
     * Context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checkbox_list_row_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit_host:
                editHost(info.id);
                return true;
            case R.id.delete_host:
                deleteHost(info.id);
                return true;
            case R.id.up:
                dbadapter.upHost(info.id);
                updateList();
                return true;
            case R.id.down:
                dbadapter.downHost(info.id);
                updateList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    void editHost(long id) {
        int dataId = hostData[(int) id].getId();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Port knocking");
        alert.setMessage("RowId: " + String.valueOf(id) + ", DataId: " + String.valueOf(dataId));
        alert.show();
    }

    void deleteHost(long id) {
        int dataId = hostData[(int) id].getId();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Port knocking");
        alert.setMessage("RowId: " + String.valueOf(id) + ", DataId: " + String.valueOf(dataId));
        alert.show();
    }

    public void updateList() {
        // TBD: Update is OK but causes NPE onItemClick()
        if (false) {
            hostData = dbadapter.getHostList();

            // Create and populate host list.
            listItems = (CheckboxListRow[]) getLastNonConfigurationInstance();

            ArrayList<CheckboxListRow> checkboxList = new ArrayList<CheckboxListRow>();
            checkboxList.addAll(Arrays.asList(hostData));

            Cursor hostsCursor = dbadapter.getRawHosts();
            startManagingCursor(hostsCursor);

            String[] from = new String[]{"host", "selected"};
            int[] to = new int[]{R.id.rowText, R.id.selectedCheckBox};

            SimpleCursorAdapter hosts = new SimpleCursorAdapter(this, R.layout.checkbox_list_row, hostsCursor, from, to);
            hostlistView.setAdapter(hosts);
        }
    }

    /*
     * Knocker thread
     */
    private Handler mHandler = new Handler();
    private ProgressDialog d;
    private boolean success = true;

    public void startKnocking(View view, int startstop) {

        d = ProgressDialog.show(view.getContext(), "Port knocking ...", "Please wait ...", true);

        new Thread(new Runnable() {

            public void run() {
                HostData[] hostdata = dbadapter.getSelectedHosts();

                for (int i = 0; i < hostdata.length; i++) {

                    final String host = hostdata[i].getName();
                    //final String host = "192.168.0.102";
                    //final String host = "127.0.0.1";

                    final String knockSequence = hostdata[i].getKnockSequence();
                    //final String knockSequence = "3333:tcp,1111:udp,4444:udp";
                    //final String String knockSequence = "3333:tcp,1111:tcp,4444:udp";

                    //d.setMessage(String.format("Pinging %s:%s ...", host, knockSequence));

                    success = true;
                    String[] knockSequencePortList = knockSequence.split(",");
                    for (int sequenceId = 0; sequenceId < knockSequencePortList.length; sequenceId++) {
                        String[] portInfo = knockSequencePortList[sequenceId].split(":");
                        final int port = Integer.parseInt(portInfo[0]);
                        final String protocol = portInfo[1];

                        mHandler.post(new Runnable() {

                            public void run() {
                                Log.i("PortKnocker", String.format("Pinging %s:%d:%s ...", host, port, protocol));
                            }
                        });

                        if (!Knocker.doKnock(host, port, protocol)) {
                            success = false;
                        }
                    }

                    if (success) {
                        dbadapter.setSelected(hostdata[i].getId(), false);
                    }
                }

                mHandler.post(new Runnable() {

                    public void run() {
                        d.dismiss();
                        stopKnocking();
                    }
                });
            }
        }).start();
    }

    public void stopKnocking() {
        updateList();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Port knocking");

        if (success) {
            alert.setMessage("Knocking complete !");
        } else {
            alert.setMessage("An error occured during knocking !");
        }

        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem menuit = menu.add(0, Menu.FIRST, 0, "Settings");
        menuit.setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                Intent i = new Intent(this, Settings.class);
                startActivityForResult(i, ACTIVITY_SETTINGS);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return listItems;
    }
}
