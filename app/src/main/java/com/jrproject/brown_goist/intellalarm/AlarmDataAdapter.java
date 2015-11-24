package com.jrproject.brown_goist.intellalarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlarmDataAdapter {

    // Becomes the filename of the database
    private static final String DATABASE_NAME = "alarms.db";
    // Only one table in this database
    private static final String TABLE_NAME = "alarms";
    // We increment this every time we change the database schema which will
    // kick off an automatic upgrade
    private static final int DATABASE_VERSION = 1;
    // TODO: Implement a SQLite database

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_MINUTE = "minute";
    public static final String KEY_STATUS = "status";

    private static String DROP_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static String CREATE_STATEMENT;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + TABLE_NAME + " (");
        sb.append(KEY_ID + " integer primary key autoincrement, ");
        sb.append(KEY_NAME + " text, ");
        sb.append(KEY_HOUR + " integer, ");
        sb.append(KEY_MINUTE + " integer, ");
        sb.append(KEY_STATUS + " integer");
        sb.append(")");
        CREATE_STATEMENT = sb.toString();
    }

    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;

    public AlarmDataAdapter(Context context) {
        //Create a SQLiteOpenHelper
        openHelper = new AlarmDBHelper(context);

    }

    public void open() {
        // Open the database
        database = openHelper.getWritableDatabase();
    }

    public long addAlarm(Alarm alarm) {
        ContentValues row = getContentValuesFromAlarm(alarm);
        long newId = database.insert(TABLE_NAME, null, row);
        alarm.setId(newId);
        return newId;
    }

    private ContentValues getContentValuesFromAlarm(Alarm alarm) {
        ContentValues row = new ContentValues();
        row.put(KEY_NAME, alarm.getName());
        row.put(KEY_HOUR, alarm.getHour());
        row.put(KEY_MINUTE, alarm.getMinute());
        row.put(KEY_STATUS, alarm.getStatus());
        return row;
    }

    public Cursor getAlarmsCursor() {
        String[] projection = new String[] {KEY_ID, KEY_NAME, KEY_HOUR, KEY_MINUTE, KEY_STATUS};
        return database.query(TABLE_NAME, null, null, null, null, null, KEY_HOUR + " ASC, "
                + KEY_MINUTE + " ASC, " + KEY_NAME + " ASC");
    }

    public Alarm getAlarm(long id) {
        String[] projection = new String[] {KEY_ID, KEY_NAME, KEY_HOUR, KEY_MINUTE, KEY_STATUS};
        String selection = KEY_ID + " = " + id;
        Cursor c =  database.query(TABLE_NAME, projection, selection, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            return getAlarmFromCursor(c);
        }
        return null;
    }

    private Alarm getAlarmFromCursor(Cursor c) {
        Alarm a = new Alarm();
        a.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
        a.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
        a.setAlarmTime(c.getInt(c.getColumnIndexOrThrow(KEY_HOUR)),
                c.getInt(c.getColumnIndexOrThrow(KEY_MINUTE)));
        a.setStatus(c.getInt(c.getColumnIndexOrThrow(KEY_STATUS)));
        return a;
    }

    public void updateAlarm(Alarm alarm) {
        ContentValues row = getContentValuesFromAlarm(alarm);
        String selection = KEY_ID + " = " + alarm.getId();
        database.update(TABLE_NAME, row, selection, null);
    }

    public boolean removeAlarm(long id) {
        String selection = KEY_ID + " = " + id;
        return database.delete(TABLE_NAME, selection, null) > 0;
    }

    public boolean removeAlarm(Alarm alarm) {
        return removeAlarm(alarm.getId());
    }

    public void close(){
        // Close the database
        database.close();
    }

    private class AlarmDBHelper extends SQLiteOpenHelper {

        public AlarmDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_STATEMENT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("DAA", "Upgrading the database from version " + oldVersion +
                    " to " + newVersion + ", which will destroy old table(s).");
            db.execSQL(DROP_STATEMENT);
            onCreate(db);
        }
    }
}
