package com.jrproject.brown_goist.intellalarm;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;
import com.jrproject.brown_goist.intellalarm.preferences.AlarmPreferencesActivity;

import java.util.List;

/**
 * Activity for viewing available alarms and setting listeners to click them -> move to edit screen or delete option
 */
public class AlarmActivity extends BaseActivity {

    ListView alarmListView;
    AlarmListAdapter alarmListAdapter;

    /**
     * Initialize screen, call alarm scheduler to make sure correct alarm will go off next
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

        alarmListView = (ListView) findViewById(android.R.id.list);
        alarmListView.setLongClickable(true);
        alarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                Builder dialog = new AlertDialog.Builder(AlarmActivity.this);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete this alarm?");
                dialog.setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlarmDatabase.init(AlarmActivity.this);
                        AlarmDatabase.deleteEntry(alarm);
                        AlarmActivity.this.callAlarmScheduleService();
                        updateAlarmList();
                    }
                });
                dialog.setNegativeButton("Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });

        callAlarmScheduleService();

        alarmListAdapter = new AlarmListAdapter(this);
        this.alarmListView.setAdapter(alarmListAdapter);
        alarmListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                Intent intent = new Intent(AlarmActivity.this, AlarmPreferencesActivity.class);
                intent.putExtra("alarm", alarm);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_save).setVisible(false);
        menu.findItem(R.id.menu_item_delete).setVisible(false);
        menu.findItem(R.id.menu_item_alarm_list).setVisible(false);
        return result;
    }

    @Override
    protected void onPause() {
        AlarmDatabase.deactivate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        AlarmDatabase.deactivate();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAlarmList();
    }

    public void updateAlarmList() {
        AlarmDatabase.init(AlarmActivity.this);
        final List<Alarm> alarms = AlarmDatabase.getAll();
        alarmListAdapter.setAlarms(alarms);

        runOnUiThread(new Runnable() {
            public void run() {
                AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
                if (alarms.size() > 0) {
                    findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkBox_alarm_active) {
            CheckBox checkBox = (CheckBox) v;
            Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox.getTag());
            alarm.setAlarmActive(checkBox.isChecked());
            AlarmDatabase.update(alarm);
            AlarmActivity.this.callAlarmScheduleService();
            if (checkBox.isChecked()) {
                Toast.makeText(AlarmActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}