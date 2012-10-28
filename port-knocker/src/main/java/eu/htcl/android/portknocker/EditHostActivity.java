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

    private DBAdapter dbadapter;
    private int operation;
    private Long id;
    private EditText wHost;
    private EditText wKnockSequence;

    /*
     * operation constants
     */
    public static final String OPERATION = "operation";
    public static final int NEW = 0;
    public static final int EDIT = 1;
    public static final int COPY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostedit);

        dbadapter = new DBAdapter(this.getApplicationContext());
        dbadapter.open();

        wHost = (EditText) findViewById(R.id.hostText);
        wKnockSequence = (EditText) findViewById(R.id.knockSequenceText);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                commit();

                setResult(RESULT_OK);
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(OPERATION)) {
                operation = extras.getInt(OPERATION);
                switch (operation) {
                    case NEW:
                        this.setTitle("New host");
                        id = null;
                        break;

                    case EDIT:
                        if (extras.containsKey(DBAdapter.KEY_ID)) {
                            this.setTitle("Edit host");
                            id = extras.getLong(DBAdapter.KEY_ID);
                            populateFields();
                        }
                        break;

                    case COPY:
                        if (extras.containsKey(DBAdapter.KEY_ID)) {
                            this.setTitle("New host");
                            id = extras.getLong(DBAdapter.KEY_ID);
                            populateFields();
                            id = null;
                        }
                        break;
                }
            }
        }
    }

    private void populateFields() {
        if (id != null) {
            Cursor host = dbadapter.getRawHost(id);

            startManagingCursor(host);

            wHost.setText(host.getString(host.getColumnIndexOrThrow(DBAdapter.KEY_HOST)));
            wKnockSequence.setText(host.getString(host.getColumnIndexOrThrow(DBAdapter.KEY_KNOCK_SEQUENCE)));
        }
    }

    private void commit() {
        if (id == null) {
            dbadapter.createHost(wHost.getText().toString(), wKnockSequence.getText().toString());
        } else {
            dbadapter.updateHost(id, wHost.getText().toString(), wKnockSequence.getText().toString());
        }
    }
}
