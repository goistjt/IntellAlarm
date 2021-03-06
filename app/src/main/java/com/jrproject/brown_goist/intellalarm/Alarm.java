package com.jrproject.brown_goist.intellalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import com.jrproject.brown_goist.intellalarm.alert.AlarmAlertBroadcastReceiver;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Model class for representing the data needed to store an alarm
 */
public class Alarm implements Serializable {

    public enum Day {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY;

        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Sunday";
                case 1:
                    return "Monday";
                case 2:
                    return "Tuesday";
                case 3:
                    return "Wednesday";
                case 4:
                    return "Thursday";
                case 5:
                    return "Friday";
                case 6:
                    return "Saturday";
            }
            return super.toString();
        }
    }

    private int id;
    private Boolean alarmActive = true;
    private Calendar alarmTime = Calendar.getInstance();
    private Day[] days = {Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY};
    private String alarmTonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
    private Boolean vibrate = true;
    private String alarmName = "Alarm Clock";
    private Boolean progressive = false;

    public Alarm() {
    }

    /**
     * @return the alarmActive
     */
    public Boolean getAlarmActive() {
        return alarmActive;
    }

    /**
     * @param alarmActive the alarmActive to set
     */
    public void setAlarmActive(Boolean alarmActive) {
        this.alarmActive = alarmActive;
    }

    /**
     * @return the alarmTime
     */
    public Calendar getAlarmTime() {
        if (alarmTime.before(Calendar.getInstance())) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        while (!Arrays.asList(getDays()).contains(Day.values()[alarmTime.get(Calendar.DAY_OF_WEEK) - 1])) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        return alarmTime;
    }

    /**
     * @return the alarmTime
     */
    public String getAlarmTimeString() {
        StringBuilder sb = new StringBuilder();
        if (alarmTime.get(Calendar.HOUR_OF_DAY) <= 9) {
            sb.append("0");
        }
        sb.append(String.valueOf(alarmTime.get(Calendar.HOUR_OF_DAY)));
        sb.append(":");

        if (alarmTime.get(Calendar.MINUTE) <= 9) {
            sb.append("0");
        }
        sb.append(String.valueOf(alarmTime.get(Calendar.MINUTE)));

        return sb.toString();
    }

    /**
     * @param alarmTime the alarmTime to set
     */
    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    /**
     * @param alarmTime the alarmTime to set
     */
    public void setAlarmTime(String alarmTime) {
        String[] timePieces = alarmTime.split(":");

        Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePieces[0]));
        newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
        newAlarmTime.set(Calendar.SECOND, 0);
        setAlarmTime(newAlarmTime);
    }

    /**
     * @return the repeatDays
     */
    public Day[] getDays() {
        return days;
    }

    /**
     * @param days the repeatDays to set
     */
    public void setDays(Day[] days) {
        this.days = days;
    }

    public void addDay(Day day) {
        boolean contains = false;
        for (Day d : getDays()) {
            if (d.equals(day)) {
                contains = true;
            }
            if (!contains) {
                List<Day> result = new LinkedList<>();
                Collections.addAll(result, getDays());
                result.add(day);
                setDays(result.toArray(new Day[result.size()]));
                return;
            }
        }
    }

    public void removeDay(Day day) {
        List<Day> result = new LinkedList<>();
        for (Day d : getDays()) {
            if (!d.equals(day)) {
                result.add(d);
            }
        }
        setDays(result.toArray(new Day[result.size()]));
    }

    /**
     * @return the alarmTonePath
     */
    public String getAlarmTonePath() {
        return alarmTonePath;
    }

    /**
     * @param alarmTonePath the alarmTonePath to set
     */
    public void setAlarmTonePath(String alarmTonePath) {
        this.alarmTonePath = alarmTonePath;
    }

    /**
     * @return the progressive
     */
    public Boolean getProgressive() {
        return progressive;
    }

    /**
     * @param progressive the progressive to set
     */
    public void setProgressive(Boolean progressive) {
        this.progressive = progressive;
    }

    /**
     * @return the vibrate
     */
    public Boolean getVibrate() {
        return vibrate;
    }

    /**
     * @param vibrate the vibrate to set
     */
    public void setVibrate(Boolean vibrate) {
        this.vibrate = vibrate;
    }

    /**
     * @return the alarmName
     */
    public String getAlarmName() {
        return alarmName;
    }

    /**
     * @param alarmName the alarmName to set
     */
    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRepeatDaysString() {
        StringBuilder daysStringBuilder = new StringBuilder();
        if (getDays().length == Day.values().length) {
            daysStringBuilder.append("Every Day");
        } else {
            Arrays.sort(getDays(), new Comparator<Day>() {
                @Override
                public int compare(Day lhs, Day rhs) {
                    return lhs.ordinal() - rhs.ordinal();
                }
            });
            for (Day d : getDays()) {
                daysStringBuilder.append(d.toString().substring(0, 3));
                daysStringBuilder.append(',');
            }
            daysStringBuilder.setLength(daysStringBuilder.length() - 1);
        }
        return daysStringBuilder.toString();
    }

    public void schedule(Context context) {
        setAlarmActive(true);
        Intent myIntent = new Intent(context, AlarmAlertBroadcastReceiver.class);
        myIntent.putExtra("alarm", this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime().getTimeInMillis(), pendingIntent);
    }

    public String getTimeUntilNextAlarmMessage() {
        long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
        long days = timeDifference / (1000 * 60 * 60 * 24);
        long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
        long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
        long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
        StringBuilder sb = new StringBuilder();
        sb.append("Alarm will sound in ");
        if (days > 0) {
            sb.append(String.format("%d days, %d hours, %d minutes and %d seconds", days, hours, minutes, seconds));
        } else {
            if (hours > 0) {
                sb.append(String.format("%d hours, %d minutes and %d seconds", hours, minutes, seconds));
            } else {
                if (minutes > 0) {
                    sb.append(String.format("%d minutes, %d seconds", minutes, seconds));
                } else {
                    sb.append(String.format("%d seconds", seconds));
                }
            }
        }
        return sb.toString();
    }
}
