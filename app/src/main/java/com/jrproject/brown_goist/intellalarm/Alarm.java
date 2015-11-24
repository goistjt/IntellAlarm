package com.jrproject.brown_goist.intellalarm;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Alarm model object.
 */
public class Alarm {
    private String name;
    private GregorianCalendar alarmTime;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long i) {
        id = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public GregorianCalendar getAlarmTime() {
        return alarmTime;
    }

    public int getHour() {
        return alarmTime.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return alarmTime.get(Calendar.MINUTE);
    }

    public void setAlarmTime(int hour, int minute) {
        alarmTime = new GregorianCalendar(0, 0, 0, hour, minute);
    }

    public String toString() {
        return getName() + " " + getAlarmTime();
    }
}
