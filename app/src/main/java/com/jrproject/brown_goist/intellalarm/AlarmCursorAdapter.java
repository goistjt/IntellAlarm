package com.jrproject.brown_goist.intellalarm;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

public class AlarmCursorAdapter extends CursorAdapter {
    private LayoutInflater inflater;

    public AlarmCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.alarm_list_item, parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView hour = (TextView) view.findViewById(R.id.alarm_list_time_hour_text_view);
        TextView minute = (TextView) view.findViewById(R.id.alarm_list_time_minute_text_view);
        TextView name = (TextView) view.findViewById(R.id.alarm_list_name_text_view);
        Switch toggle = (Switch) view.findViewById(R.id.alarm_on_switch);

        hour.setText(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmDataAdapter.KEY_HOUR)));
        minute.setText(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmDataAdapter.KEY_MINUTE)));
        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(AlarmDataAdapter.KEY_NAME)));
        toggle.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(AlarmDataAdapter.KEY_STATUS)
        ) == 1);
    }
}
