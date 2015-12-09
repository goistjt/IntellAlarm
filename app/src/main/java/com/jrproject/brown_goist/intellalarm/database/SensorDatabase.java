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
    public static final String COLUMN_SENSOR_X = "sensor_x";
    public static final String COLUMN_SENSOR_Y = "sensor_y";
    public static final String COLUMN_SENSOR_Z = "sensor_z";
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
        cv.put(COLUMN_SENSOR_X, data.getxValue());
        cv.put(COLUMN_SENSOR_Y, data.getyValue());
        cv.put(COLUMN_SENSOR_Z, data.getzValue());
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
                COLUMN_SENSOR_X,
                COLUMN_SENSOR_Y,
                COLUMN_SENSOR_Z,
                COLUMN_SENSOR_TIMESTAMP
        };
        Cursor c = getDatabase().query(SENSOR_TABLE, columns, COLUMN_SENSOR_ID + "=" + id, null, null, null,
                null);
        SensorData data = null;

        if (c.moveToFirst()) {
            data = new SensorData();
            data.setId(c.getInt(c.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
            data.setxValue(c.getFloat(c.getColumnIndexOrThrow(COLUMN_SENSOR_X)));
            data.setyValue(c.getFloat(c.getColumnIndexOrThrow(COLUMN_SENSOR_Y)));
            data.setzValue(c.getFloat(c.getColumnIndexOrThrow(COLUMN_SENSOR_Z)));
            data.setTimeStamp(c.getLong(c.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

        }
        c.close();
        return data;
    }

    public static Cursor getCursorAll() {
        // TODO Auto-generated method stub
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_SENSOR_X,
                COLUMN_SENSOR_Y,
                COLUMN_SENSOR_Z,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns, null, null, null, null,
                null);
    }

    public static Cursor getCursorDay() {
        // TODO Auto-generated method stub
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 24 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_SENSOR_X,
                COLUMN_SENSOR_Y,
                COLUMN_SENSOR_Z,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns,
                COLUMN_SENSOR_TIMESTAMP + " >= " + prevDateTime + " AND " +
                        COLUMN_SENSOR_TIMESTAMP + " <= " + currDateTime, null, null, null, null);
    }

    public static Cursor getCursorWeek() {
        // TODO Auto-generated method stub
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 24 * 7 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_SENSOR_X,
                COLUMN_SENSOR_Y,
                COLUMN_SENSOR_Z,
                COLUMN_SENSOR_TIMESTAMP
        };
        return getDatabase().query(SENSOR_TABLE, columns,
                COLUMN_SENSOR_TIMESTAMP + " >= " + prevDateTime + " AND " +
                        COLUMN_SENSOR_TIMESTAMP + " <= " + currDateTime, null, null, null, null);
    }

    public static Cursor getCursorMonth() {
        // TODO Auto-generated method stub
        long currDateTime = System.currentTimeMillis();
        long prevDateTime = System.currentTimeMillis() - 3600 * 24 * 1000;
        String[] columns = new String[]{
                COLUMN_SENSOR_ID,
                COLUMN_SENSOR_X,
                COLUMN_SENSOR_Y,
                COLUMN_SENSOR_Z,
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
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SENSOR_TABLE + " ( "
                + COLUMN_SENSOR_ID + " INTEGER primary key autoincrement, "
                + COLUMN_SENSOR_X + " REAL NOT NULL, "
                + COLUMN_SENSOR_Y + " REAL NOT NULL, "
                + COLUMN_SENSOR_Z + " REAL NOT NULL, "
                + COLUMN_SENSOR_TIMESTAMP + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SENSOR_TABLE);
        onCreate(db);
    }

    public static List<SensorData> getAll() {
        List<SensorData> sensors = new ArrayList<>();
        Cursor cursor = SensorDatabase.getCursorAll();
        if (cursor.moveToFirst()) {

            do {
                SensorData sensorData = new SensorData();
                sensorData.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
                sensorData.setxValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_X)));
                sensorData.setyValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Y)));
                sensorData.setzValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Z)));
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
                sensorData.setxValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_X)));
                sensorData.setyValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Y)));
                sensorData.setzValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Z)));
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
                sensorData.setxValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_X)));
                sensorData.setyValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Y)));
                sensorData.setzValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Z)));
                sensorData.setTimeStamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

                sensors.add(sensorData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sensors;
    }

    public static List<SensorData> getMonth() {
        List<SensorData> sensors = new ArrayList<>();
        Cursor cursor = SensorDatabase.getCursorMonth();
        if (cursor.moveToFirst()) {

            do {
                SensorData sensorData = new SensorData();
                sensorData.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_ID)));
                sensorData.setxValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_X)));
                sensorData.setyValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Y)));
                sensorData.setzValue(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_Z)));
                sensorData.setTimeStamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIMESTAMP)));

                sensors.add(sensorData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return sensors;
    }
}