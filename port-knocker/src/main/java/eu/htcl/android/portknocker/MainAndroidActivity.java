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

    private static String TAG = "port-knocker";
    //private CheckboxListView hostlistView;
    private ListView hostlistView;
    private ArrayAdapter<CheckboxListRow> listAdapter;
    private DBAdapter dbadapter;
    private ArrayList<CheckboxListRow> checkboxListRows = new ArrayList<CheckboxListRow>();
    private HostData[] hostData;

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

        // Initialise the ListView resource.
        // hostlistView = (CheckboxListView) findViewById(R.id.hostlistView);
        // hostlistView.initialise(this, checkboxList);
        hostlistView = (ListView) findViewById(R.id.hostlistView);

        // Register the ListView to handle a context menu
        registerForContextMenu(hostlistView);

        // When item is tapped, toggle 'selected' property of CheckBox and hostname.
        hostlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelectViewHolder viewHolder = (SelectViewHolder) view.getTag();
                CheckboxListRow checkboxListRow = listAdapter.getItem(position);
                checkboxListRow.toggleSelected();
                viewHolder.getCheckBox().setChecked(checkboxListRow.isSelected());
            }
        });

        // Initialise the view data
        initialiseListView();

        Button knockButton = (Button) findViewById(R.id.KnockButton);
        knockButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startKnocking(hostlistView);
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
                editHost((long) hostData[(int) info.id].getId());
                updateListView();
                return true;
            case R.id.copy_host:
                copyHost((long) hostData[(int) info.id].getId());
                updateListView();
                return true;
            case R.id.delete_host:
                deleteHost((long) hostData[(int) info.id].getId());
                updateListView();
                return true;
            case R.id.up:
                dbadapter.upHostPopularity((long) hostData[(int) info.id].getId());
                updateListView();
                return true;
            case R.id.down:
                dbadapter.downHostPopularity((long) hostData[(int) info.id].getId());
                updateListView();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    void addHost() {
        Intent intent = new Intent(this, EditHostActivity.class);
        intent.putExtra(EditHostActivity.OPERATION, EditHostActivity.NEW);
        startActivityForResult(intent, 0);
    }

    void editHost(long id) {
        //AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //alert.setTitle("Port knocking");
        //alert.setMessage("RowId: " + String.valueOf(id) + ", DataId: " + String.valueOf(dataId));
        //alert.show();

        Intent intent = new Intent(this, EditHostActivity.class);
        intent.putExtra(DBAdapter.KEY_ID, id);
        intent.putExtra(EditHostActivity.OPERATION, EditHostActivity.EDIT);
        startActivityForResult(intent, 0);
    }

    void copyHost(long id) {
        //AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //alert.setTitle("Port knocking");
        //alert.setMessage("RowId: " + String.valueOf(id) + ", DataId: " + String.valueOf(dataId));
        //alert.show();

        Intent intent = new Intent(this, EditHostActivity.class);
        intent.putExtra(DBAdapter.KEY_ID, id);
        intent.putExtra(EditHostActivity.OPERATION, EditHostActivity.COPY);
        startActivityForResult(intent, 0);
    }

    void deleteHost(long id) {
        //AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //alert.setTitle("Port knocking");
        //alert.setMessage("RowId: " + String.valueOf(id) + ", DataId: " + String.valueOf(dataId));
        //alert.show();

        // Are u sure ?
        dbadapter.deleteHost(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem menuit = menu.add(0, Menu.FIRST, 0, "Add host");
        menuit.setIcon(android.R.drawable.ic_menu_add);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                addHost();
                updateListView();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateListView();
    }

    public void initialiseListView() {
        if (true) {
            //CheckboxListRow[] listItems;

            // Get host list
            hostData = dbadapter.getHostList();

            // Create and populate host list.
            //listItems = (CheckboxListRow[]) getLastNonConfigurationInstance();
            //checkboxListRows.clear();
            checkboxListRows.addAll(Arrays.asList(hostData));

            // Set our custom array adapter as the ListView's adapter.
            listAdapter = new SelectArrayAdapter(this, R.layout.checkbox_list_row, R.id.rowText, R.id.selectedCheckBox, checkboxListRows);
            hostlistView.setAdapter(listAdapter);
        } else {
            // TBD: Update is OK but causes NPE onItemClick()
            Cursor hostsCursor = dbadapter.getRawHosts();
            startManagingCursor(hostsCursor);

            String[] from = new String[]{"host", "selected"};
            int[] to = new int[]{R.id.rowText, R.id.selectedCheckBox};

            SimpleCursorAdapter hosts = new SimpleCursorAdapter(this, R.layout.checkbox_list_row, hostsCursor, from, to);
            hostlistView.setAdapter(hosts);
        }

        updateListView();
    }

    public void updateListView() {
        // Get host list
        hostData = dbadapter.getHostList();

        // Create and populate host list.
        //listItems = (CheckboxListRow[]) getLastNonConfigurationInstance();
        checkboxListRows.clear();
        checkboxListRows.addAll(Arrays.asList(hostData));

        listAdapter.notifyDataSetChanged();
        hostlistView.invalidateViews();
    }

    /*
     * Knocker thread
     */
    private Handler mHandler = new Handler();
    private ProgressDialog d;
    private boolean success = true;

    public void startKnocking(final View view) {

        d = ProgressDialog.show(view.getContext(), "Port knocking ...", "Please wait ...", true);

        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < hostData.length; i++) {

                    final String host = hostData[i].getName();
                    final String knockSequence = hostData[i].getKnockSequence();
                    final boolean selected = hostData[i].isSelected();

                    if ( selected ) {
                        success = true;
                        String[] knockSequencePortList = knockSequence.split(",");
                        for (int sequenceId = 0; sequenceId < knockSequencePortList.length; sequenceId++) {
                            String[] portInfo = knockSequencePortList[sequenceId].split(":");
                            final int port = Integer.parseInt(portInfo[0]);
                            final String protocol = portInfo[1];

                            mHandler.post(new Runnable() {
                                public void run() {
                                    Log.i("PortKnocker", String.format("Knocking %s:%d:%s ...", host, port, protocol));
                                    //d.setMessage(String.format("Knocking %s:%s ...", host, knockSequence));
                                }
                            });

                            if (!Knocker.doKnock(host, port, protocol)) {
                                success = false;
                            }
                        }

                        if (success) {
                            hostData[i].setSelected(false);
                            dbadapter.upHostPopularity(hostData[i].getId());
                        }
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
        updateListView();

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
    public Object onRetainNonConfigurationInstance() {
        return hostData;
    }
}
