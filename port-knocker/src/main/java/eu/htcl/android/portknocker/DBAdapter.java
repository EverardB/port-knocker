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
    public static final String KEY_POPULARITY = "popularity";

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
        private static final String LIMIT_POPULARITY_TRIGGER =
                "CREATE TRIGGER limit_popularity_before_update "
                + "BEFORE INSERT OR UPDATE OF popularity "
                + "ON hosts "
                + "BEGIN "
                + " UPDATE hosts "
                + " SET popularity=old.popularity WHERE _id=new._id AND popularity=(SELECT MAX(popularity)); "
                + "END;";

        public HostdataOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);

            //db.execSQL(DELETE_TRIGGER);
            //db.execSQL(UPDATE_TRIGGER);
            //db.execSQL(LIMIT_POPULARITY_TRIGGER);
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

    public int getNumHosts() {
        int retval = 0;
        Cursor c = db.query("hosts", new String[]{"COUNT(*)"}, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            retval = c.getInt(0);
        }
        c.close();
        return retval;
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
        String retval = c.getString(0);
        c.close();
        return retval;
    }

    public void setHost(long id, String host) {
        ContentValues args = new ContentValues();
        args.put(KEY_HOST, host);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public String getKnockSequence(int id) {
        Cursor c = getRawHost(id, KEY_KNOCK_SEQUENCE);
        String retval = c.getString(0);
        c.close();
        return retval;
    }

    public void setKnockSequence(long id, String knock_sequence) {
        ContentValues args = new ContentValues();
        args.put(KEY_KNOCK_SEQUENCE, knock_sequence);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public boolean getSelected(long id) {
        Cursor c = getRawHost(id, KEY_SELECTED);
        boolean retval = (c.getInt(0) == 0) ? false : true;
        c.close();
        return retval;
    }

    public void setSelected(long id, boolean selected) {
        ContentValues args = new ContentValues();
        args.put(KEY_SELECTED, (selected) ? 1 : 0);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public int getPopularity(long id) {
        Cursor c = getRawHost(id, KEY_POPULARITY);
        int retval = c.getInt(0);
        c.close();
        return retval;
    }

    public void setPopularity(long id, int popularity) {
        ContentValues args = new ContentValues();
        args.put(KEY_POPULARITY, popularity);
        db.update("hosts", args, KEY_ID + "=" + id, null);
    }

    public int getMaxPopularity() {
        int retval = 0;
        Cursor c = db.query("hosts", new String[]{"MAX(popularity)"}, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            retval = c.getInt(0);
        }
        c.close();
        return retval;
    }

    private int getNumAtPopularity(int popularity) {
        int retval = 0;
        Cursor c = db.query("hosts", new String[]{"COUNT(*)"}, "popularity = " + String.valueOf(popularity), null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            retval = c.getInt(0);
        }
        c.close();
        return retval;
    }

    public int getNumAtMaxPopularity() {
        int retval = 0;
        int maxPopularity = getMaxPopularity();

        retval = getNumAtPopularity(maxPopularity);

        return retval;
    }

    public int getSecondHighestPopularity() {
        int maxPopularity = getMaxPopularity();
        int popularity = maxPopularity;
        int count = 0;

        while (popularity > 0 && count == 0) {
            popularity--;
            count = getNumAtPopularity(popularity);
        }

        return popularity;
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
                new Object[]{host, knock_sequence, getMaxPopularity(), 0});
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

    public void upHostPopularity(long id) {
        int totalHosts = getNumHosts();
        int currentPopularity = getPopularity(id);
        int maxPopularity = getMaxPopularity();
        int secondHighestPopularity = getSecondHighestPopularity();
        int numAtMaxPopularity = getNumAtMaxPopularity();
        //int numAtSecondHighestPopularity = getNumAtPopularity(secondHighestPopularity);

        if ( // TBD: This is logic needs to be reviewed
                (currentPopularity < totalHosts) // absolute maximum limit
                && !(currentPopularity == maxPopularity && secondHighestPopularity == (currentPopularity - 1) && numAtMaxPopularity == 1) // Avoid breakaway items
                ) {
            setPopularity(id, (currentPopularity + 1));
        }
    }

    public void downHostPopularity(long id) {
        db.execSQL("UPDATE hosts SET popularity = MAX(popularity-1,0) WHERE _id=?", new Object[]{id});
    }
}
