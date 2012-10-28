package eu.htcl.android.portknocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import eu.htcl.android.common.checkboxlist.CheckboxListRow;

/**
 *
 * @author everard
 */
public class DBAdapter {

    private Context ctx;
    private HostdataOpenHelper helper;
    private SQLiteDatabase db;

    /*
     * DB field constants
     */
    public static final String KEY_ID = "_id";
    public static final String KEY_HOST = "host";
    public static final String KEY_KNOCK_SEQUENCE = "knock_sequence";
    public static final String KEY_SELECTED = "selected";

    /*
     * Opertaions
     */
    public static final int START_MODE = 0;
    public static final int STOP_MODE = 1;

    public class HostData extends CheckboxListRow {

        private DBAdapter db;
        private String knock_sequence = "";

        public HostData(DBAdapter db) {
            this.db = db;
        }

        public HostData(DBAdapter db, int id, String name) {
            super(id, name);
            this.db = db;
        }

        public HostData(DBAdapter db, int id, String name, String knock_sequence, int selected) {
            super(id, name);
            super.setSelected((selected == 0) ? false : true);
            this.db = db;
            this.knock_sequence = knock_sequence;
        }

        @Override
        public void setName(String name) {
            super.setName(name);
            db.setHost(id, name);
        }

        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            db.setSelected(id, selected);
        }

        public String getKnockSequence() {
            return knock_sequence;
        }

        public void setKnockSequence(String knock_sequence) {
            this.knock_sequence = knock_sequence;
            db.setKnockSequence(id, knock_sequence);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class HostdataOpenHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "hostdata.db";
        private static final int DATABASE_VERSION = 1;

        /*
         * hosts table.
         */
        private static final String CREATE_TABLE =
                "CREATE TABLE hosts ("
                + "_id INTEGER PRIMARY KEY,"
                + "host STRING,"
                + "knock_sequence STRING,"
                + "selected INTEGER DEFAULT 0,"
                + "popularity INTEGER DEFAULT 0"
                + ");";

        /*
         * Triggers for computing popularity after DELETE/UPDATE-ing an host.
         */
        private static final String DELETE_TRIGGER =
                "CREATE TRIGGER recalcpos_after_delete AFTER DELETE ON hosts "
                + "FOR EACH ROW BEGIN "
                + " UPDATE hosts "
                + " SET popularity=popularity-1 WHERE popularity>old.popularity; "
                + "END;";
        private static final String UPDATE_TRIGGER =
                "CREATE TRIGGER recalcpos_before_update BEFORE UPDATE OF popularity ON hosts "
                + "FOR EACH ROW BEGIN "
                + " UPDATE hosts "
                + " SET popularity=old.popularity WHERE popularity=new.popularity; "
                + "END;";

        public HostdataOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);

            db.execSQL(DELETE_TRIGGER);
            db.execSQL(UPDATE_TRIGGER);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //db.execSQL("DROP TABLE IF EXISTS hosts");
            //onCreate(db);
        }
    }

    public DBAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void open() {
        helper = new HostdataOpenHelper(this.ctx);
        db = helper.getWritableDatabase();
    }

    public void close() {
        db.close();
    }

    public HostData[] getHostList() {
        HostData[] hostData = null;

        // Get hosts (most popular first)
        Cursor c = getRawHosts();

        if (c != null) {
            hostData = new HostData[c.getCount()];

            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String knock_sequence = c.getString(2);
                int selected = c.getInt(3);
                hostData[i] = new HostData(this, id, name, knock_sequence, selected);
                c.moveToNext();
            }
        }

        c.close();

        return hostData;
    }

    public HostData[] getSelectedHosts() {
        HostData[] hostData = null;

        // Get 'selected' hosts (most popular first)
        Cursor c = getRawHosts(1);

        if (c != null) {
            hostData = new HostData[c.getCount()];

            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String knock_sequence = c.getString(2);
                int selected = c.getInt(3);
                hostData[i] = new HostData(this, id, name, knock_sequence, selected);
                c.moveToNext();
            }
        }

        c.close();

        return hostData;
    }

    public HostData getHostData(int id) {
        Cursor c = getRawHost(id);

        //int id = c.getInt(0);
        String name = c.getString(1);
        String knock_sequence = c.getString(2);
        int selected = c.getInt(3);
        HostData hostData = new HostData(this, id, name, knock_sequence, selected);

        c.close();

        return hostData;
    }

    public int[] getHosts(long selected) {
        Cursor c = getRawHosts(1, KEY_ID);

        int[] data = new int[c.getCount()];

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            data[i] = c.getInt(0);
            c.moveToNext();
        }

        c.close();

        return data;
    }

    public String getHost(int id) {
        Cursor c = getRawHost(id, KEY_HOST);
        String host = c.getString(0);
        c.close();

        return host;
    }

    public void setHost(long id, String host) {
        ContentValues args = new ContentValues();
        args.put(KEY_HOST, host);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public String getKnockSequence(int id) {
        Cursor c = getRawHost(id, KEY_KNOCK_SEQUENCE);
        String knock_sequence = c.getString(0);
        c.close();

        return knock_sequence;
    }

    public void setKnockSequence(long id, String knock_sequence) {
        ContentValues args = new ContentValues();
        args.put(KEY_KNOCK_SEQUENCE, knock_sequence);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public boolean getSelected(long id) {
        Cursor c = getRawHost(id, KEY_SELECTED);
        boolean selected = (c.getInt(0) == 0) ? false : true;
        c.close();

        return selected;
    }

    public void setSelected(long id, boolean selected) {
        ContentValues args = new ContentValues();
        args.put(KEY_SELECTED, (selected) ? 1 : 0);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public Cursor getRawHost(long id) {
        Cursor c = db.query("hosts", null, "_id = " + String.valueOf(id), null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getRawHost(long id, String key) {
        Cursor c = db.query("hosts", new String[]{key}, "_id = " + String.valueOf(id), null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getRawHosts() {
        Cursor c = db.query("hosts",
                new String[]{"_id", "host", "knock_sequence", "selected"}, null, null, null, null, "popularity DESC");
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getRawHosts(int selected) {
        Cursor c = db.query("hosts", null, "selected=" + selected, null, null, null, "popularity DESC");
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getRawHosts(int selected, String key) {
        Cursor c = db.query("hosts", new String[]{key}, "selected=" + selected, null, null, null, "popularity DESC");
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public void createHost(String host, String knock_sequence) {
        db.execSQL("INSERT INTO hosts (host, knock_sequence, popularity, selected)"
                + " VALUES (?, ?, ?, ?)",
                new Object[]{host, knock_sequence, 0, 0});
    }

    public void updateHost(long id, String host, String knock_sequence) {
        ContentValues args = new ContentValues();
        args.put(KEY_HOST, host);
        args.put(KEY_KNOCK_SEQUENCE, knock_sequence);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public void updateHost(long id, String host, String knock_sequence, boolean selected) {
        ContentValues args = new ContentValues();
        args.put(KEY_HOST, host);
        args.put(KEY_KNOCK_SEQUENCE, knock_sequence);
        args.put(KEY_SELECTED, (selected) ? 1 : 0);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public void deleteHost(long id) {
        db.delete("hosts", KEY_ID + "=" + id, null);
    }

    public void upHost(long id) {
        db.execSQL("UPDATE hosts SET popularity = popularity+1 WHERE _id=?", new Object[]{id});
    }

    public void downHost(long id) {
        db.execSQL("UPDATE hosts SET popularity = popularity-1 WHERE _id=?", new Object[]{id});
    }
}
