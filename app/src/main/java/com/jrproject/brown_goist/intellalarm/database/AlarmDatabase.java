package com.jrproject.brown_goist.intellalarm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jrproject.brown_goist.intellalarm.Alarm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * usage:
 * DatabaseSetup.init(egActivityOrContext);
 * DatabaseSetup.createEntry() or DatabaseSetup.getContactNames() or DatabaseSetup.getDb()
 * DatabaseSetup.deactivate() then job done
 */

public class AlarmDatabase extends SQLiteOpenHelper {
    static AlarmDatabase instance = null;
    static SQLiteDatabase database = null;

    static final String DATABASE_NAME = "DB";
    static final int DATABASE_VERSION = 1;

    public static final String ALARM_TABLE = "alarm";
    public static final String COLUMN_ALARM_ID = "_id";
    public static final String COLUMN_ALARM_ACTIVE = "alarm_active";
    public static final String COLUMN_ALARM_TIME = "alarm_time";
    public static final String COLUMN_ALARM_DAYS = "alarm_days";
    public static final String COLUMN_ALARM_TONE = "alarm_tone";
    public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
    public static final String COLUMN_ALARM_NAME = "alarm_name";
    public static final String COLUMN_ALARM_PROGRESSIVE = "alarm_progressive";

    public static void init(Context context) {
        if (instance == null) {
            instance = new AlarmDatabase(context);
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

    public static long create(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            cv.put(COLUMN_ALARM_DAYS, buff);

        } catch (IOException e) {
            e.printStackTrace();
        }

        cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
        cv.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
        cv.put(COLUMN_ALARM_PROGRESSIVE, alarm.getProgressive());

        return getDatabase().insert(ALARM_TABLE, null, cv);
    }

    public static int update(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            cv.put(COLUMN_ALARM_DAYS, buff);

        } catch (IOException e) {
            e.printStackTrace();
        }

        cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
        cv.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
        cv.put(COLUMN_ALARM_PROGRESSIVE, alarm.getProgressive());

        return getDatabase().update(ALARM_TABLE, cv, "_id=" + alarm.getId(), null);
    }

    public static int deleteEntry(Alarm alarm) {
        return deleteEntry(alarm.getId());
    }

    public static int deleteEntry(int id) {
        return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
    }

    public static int deleteAll() {
        return getDatabase().delete(ALARM_TABLE, "1", null);
    }

    public static Alarm getAlarm(int id) {
        // TODO Auto-generated method stub
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_TONE,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_PROGRESSIVE
        };
        Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID + "=" + id, null, null, null,
                null);
        Alarm alarm = null;

        if (c.moveToFirst()) {
            alarm = new Alarm();
            alarm.setId(c.getInt(1));
            alarm.setAlarmActive(c.getInt(2) == 1);
            alarm.setAlarmTime(c.getString(3));
            byte[] repeatDaysBytes = c.getBlob(4);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Alarm.Day[] repeatDays;
                Object object = objectInputStream.readObject();
                if (object instanceof Alarm.Day[]) {
                    repeatDays = (Alarm.Day[]) object;
                    alarm.setDays(repeatDays);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            alarm.setAlarmTonePath(c.getString(6));
            alarm.setVibrate(c.getInt(7) == 1);
            alarm.setAlarmName(c.getString(8));
            alarm.setProgressive(c.getInt(9) == 1);
        }
        c.close();
        return alarm;
    }

    public static Cursor getCursor() {
        // TODO Auto-generated method stub
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_TONE,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_PROGRESSIVE
        };
        return getDatabase().query(ALARM_TABLE, columns, null, null, null, null,
                null);
    }

    AlarmDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
                + COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
                + COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_TIME + " TEXT NOT NULL, "
                + COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
                + COLUMN_ALARM_TONE + " TEXT NOT NULL, "
                + COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_NAME + " TEXT NOT NULL, "
                + COLUMN_ALARM_PROGRESSIVE + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        onCreate(db);
    }

    public static List<Alarm> getAll() {
        List<Alarm> alarms = new ArrayList<>();
        Cursor cursor = AlarmDatabase.getCursor();
        if (cursor.moveToFirst()) {

            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALARM_ID)));
                alarm.setAlarmActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALARM_ACTIVE)) == 1);
                alarm.setAlarmTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALARM_TIME)));
                byte[] repeatDaysBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ALARM_DAYS));

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        repeatDaysBytes);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(
                            byteArrayInputStream);
                    Alarm.Day[] repeatDays;
                    Object object = objectInputStream.readObject();
                    if (object instanceof Alarm.Day[]) {
                        repeatDays = (Alarm.Day[]) object;
                        alarm.setDays(repeatDays);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                alarm.setAlarmTonePath(cursor.getString(cursor.getColumnIndex(COLUMN_ALARM_TONE)));
                alarm.setVibrate(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALARM_VIBRATE)) == 1);
                alarm.setAlarmName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALARM_NAME)));
                alarm.setProgressive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALARM_PROGRESSIVE)) == 1);

                alarms.add(alarm);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }

    public static Alarm getNextAlarm() {
        List<Alarm> alarms = getAll();
        if(alarms.isEmpty()) {
            return null;
        }
        Collections.sort(alarms, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                long lhsTimeDiff = lhs.getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
                long rhsTimeDiff = rhs.getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
                return lhsTimeDiff > rhsTimeDiff ? -1 : rhsTimeDiff > lhsTimeDiff ? 1 : 0;
            }
        });
        return alarms.get(0);
    }
}