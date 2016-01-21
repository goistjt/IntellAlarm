package com.jrproject.brown_goist.intellalarm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jrproject.brown_goist.intellalarm.SensorData;

import java.util.ArrayList;
import java.util.List;

/*
 * usage:
 * DatabaseSetup.init(egActivityOrContext);
 * DatabaseSetup.createEntry() or DatabaseSetup.getContactNames() or DatabaseSetup.getDb()
 * DatabaseSetup.deactivate() then job done
 */

public class SensorDatabase extends SQLiteOpenHelper {
    static SensorDatabase instance = null;
    static SQLiteDatabase database = null;

    static final String DATABASE_NAME = "DB2";
    static final int DATABASE_VERSION = 1;

    public static final String SENSOR_TABLE = "sensor";
    public static final String COLUMN_SENSOR_ID = "_id";
    public static final String COLUMN_EVENTS = "events";
    public static final String COLUMN_SENSOR_TIMESTAMP = "sensor_timestamp";

    public static void init(Context context) {
        if (instance == null) {
            instance = new SensorDatabase(context);
        }
    }

    public static SQLiteDatabase getDatabase() {
        if (database == null) {
            database = instance.getWritableDatabase();
        }
        return database;
    }

    public static void deactivate() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        database = null;
        instance = null;
    }

    public static long create(SensorData data) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EVENTS, data.getNumEvents());
        cv.put(COLUMN_SENSOR_TIMESTAMP, data.getTimeStamp());

        return getDatabase().insert(SENSOR_TABLE, null, cv);
    }

    public static int deleteEntry(SensorData data) {
        return deleteEntry(data.getId());
    }

    public static int deleteEntry(int id) {
        return getDatabase().delete(SENSOR_TABLE, COLUMN_SENSOR_ID + "=" + id, null);
    }

    public static int deleteAll() {
        return getDatabase().delete(SENSOR_TABLE, "1", null);
    }

    public static SensorData getSensorData(int id) {
        // TODO Auto-generated method stub
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_EVENTS,
                COLUMN_SENSOR_TIMESTAMP
        };
        Cursor c = getDatabase().query(SENSOR_TABLE, columns, COLUMN_SENSOR_ID + "=" + id, null, null, null,
                null);
        SensorData data = null;

        if (c.moveToFirst()) {
            data = new SensorData();
            data.setId(c.getInt(c.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
            data.setNumEvents(c.getInt(c.getColumnIndexOrThrow(COLUMN_EVENTS)));
            data.setTimeStamp(c.getLong(c.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

        }
        c.close();
        return data;
    }

    public static Cursor getCursor8Hours() {
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 8 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_EVENTS,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns,
                COLUMN_SENSOR_TIMESTAMP + " >= " + prevDateTime + " AND " +
                        COLUMN_SENSOR_TIMESTAMP + " <= " + currDateTime, null, null, null, null);
    }

    public static Cursor getCursor12Hours() {
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 12 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_EVENTS,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns,
                COLUMN_SENSOR_TIMESTAMP + " >= " + prevDateTime + " AND " +
                        COLUMN_SENSOR_TIMESTAMP + " <= " + currDateTime, null, null, null, null);
    }

    public static Cursor getCursorDay() {
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 24 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_EVENTS,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns,
                COLUMN_SENSOR_TIMESTAMP + " >= " + prevDateTime + " AND " +
                        COLUMN_SENSOR_TIMESTAMP + " <= " + currDateTime, null, null, null, null);
    }

    public static Cursor getCursorWeek() {
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 24 * 7 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_EVENTS,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns,
                COLUMN_SENSOR_TIMESTAMP + " >= " + prevDateTime + " AND " +
                        COLUMN_SENSOR_TIMESTAMP + " <= " + currDateTime, null, null, null, null);
    }

    SensorDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SENSOR_TABLE + " ( "
                + COLUMN_SENSOR_ID + " INTEGER primary key autoincrement, "
                + COLUMN_EVENTS + " INTEGER NOT NULL, "
                + COLUMN_SENSOR_TIMESTAMP + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SENSOR_TABLE);
        onCreate(db);
    }

    public static List<SensorData> get8Hours() {
        List<SensorData> sensors = new ArrayList<>();
        Cursor cursor = SensorDatabase.getCursor8Hours();
        if (cursor.moveToFirst()) {

            do {
                SensorData sensorData = new SensorData();
                sensorData.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
                sensorData.setNumEvents(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENTS)));
                sensorData.setTimeStamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

                sensors.add(sensorData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sensors;
    }

    public static List<SensorData> get12Hours() {
        List<SensorData> sensors = new ArrayList<>();
        Cursor cursor = SensorDatabase.getCursor12Hours();
        if (cursor.moveToFirst()) {

            do {
                SensorData sensorData = new SensorData();
                sensorData.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
                sensorData.setNumEvents(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENTS)));
                sensorData.setTimeStamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

                sensors.add(sensorData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sensors;
    }

    public static List<SensorData> getDay() {
        List<SensorData> sensors = new ArrayList<>();
        Cursor cursor = SensorDatabase.getCursorDay();
        if (cursor.moveToFirst()) {

            do {
                SensorData sensorData = new SensorData();
                sensorData.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
                sensorData.setNumEvents(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENTS)));
                sensorData.setTimeStamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

                sensors.add(sensorData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sensors;
    }

    public static List<SensorData> getWeek() {
        List<SensorData> sensors = new ArrayList<>();
        Cursor cursor = SensorDatabase.getCursorWeek();
        if (cursor.moveToFirst()) {

            do {
                SensorData sensorData = new SensorData();
                sensorData.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
                sensorData.setNumEvents(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENTS)));
                sensorData.setTimeStamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

                sensors.add(sensorData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sensors;
    }
}