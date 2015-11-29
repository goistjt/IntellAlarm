package com.jrproject.brown_goist.intellalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;

import java.util.ArrayList;
import java.util.List;

public class AlarmListAdapter extends BaseAdapter {

    private AlarmActivity alarmActivity;
    private List<Alarm> alarms = new ArrayList<>();

    public static final String ALARM_FIELDS[] = {AlarmDatabase.COLUMN_ALARM_ACTIVE,
            AlarmDatabase.COLUMN_ALARM_TIME, AlarmDatabase.COLUMN_ALARM_DAYS};

    public AlarmListAdapter(AlarmActivity alarmActivity) {
        this.alarmActivity = alarmActivity;
    }

    @Override
    public int getCount() {
        return alarms.size();
    }

    @Override
    public Object getItem(int position) {
        return alarms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (null == view)
            view = LayoutInflater.from(alarmActivity).inflate(R.layout.alarm_list_element, viewGroup, false);

        Alarm alarm = (Alarm) getItem(position);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox_alarm_active);
        checkBox.setChecked(alarm.getAlarmActive());
        checkBox.setTag(position);
        checkBox.setOnClickListener(alarmActivity);

        TextView alarmTimeView = (TextView) view.findViewById(R.id.textView_alarm_time);
        alarmTimeView.setText(alarm.getAlarmTimeString());

        TextView alarmDaysView = (TextView) view.findViewById(R.id.textView_alarm_days);
        alarmDaysView.setText(alarm.getRepeatDaysString());

        return view;
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
    }

}
