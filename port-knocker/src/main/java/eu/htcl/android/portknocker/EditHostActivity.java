package eu.htcl.android.portknocker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 * @author everard
 */
public class EditHostActivity extends Activity {

    private Long rowid;
    private DBAdapter dbadapter;
    private EditText wHost;
    private EditText wKnockSequence;
    private Button wCancelButton;
    private int activity_mode = DBAdapter.START_MODE;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostedit);
        this.setTitle("Add a host");

        dbadapter = new DBAdapter(this.getApplicationContext());
        dbadapter.open();

        wHost = (EditText) findViewById(R.id.EditText01);
        wKnockSequence = (EditText) findViewById(R.id.EditText02);

        wCancelButton = (Button) findViewById(R.id.Button02);
        wCancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Button savebutton = (Button) findViewById(R.id.Button01);
        savebutton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                commit();

                setResult(RESULT_OK);
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            activity_mode = extras.getInt(DBAdapter.KEY_SELECTED);
            if (extras.containsKey(DBAdapter.KEY_ID)) {
                rowid = extras.getLong(DBAdapter.KEY_ID);
                populateFields();
            }
        }
    }

    private void populateFields() {
        if (rowid != null) {
            Cursor host = dbadapter.getRawHost(rowid);
            startManagingCursor(host);

            wHost.setText(host.getString(host.getColumnIndexOrThrow(DBAdapter.KEY_HOST)));
            wKnockSequence.setText(host.getString(host.getColumnIndexOrThrow(DBAdapter.KEY_KNOCK_SEQUENCE)));
        }
    }

    private void commit() {
        if (rowid == null) {
            dbadapter.createHost(wHost.getText().toString(), wKnockSequence.getText().toString(), activity_mode);
        } else {
            dbadapter.updateHost(rowid, wHost.getText().toString(), wKnockSequence.getText().toString());
        }
    }
}
