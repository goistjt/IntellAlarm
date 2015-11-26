package com.jrproject.brown_goist.intellalarm.sleep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.AlarmActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.database.Database;

public class SleepActivity extends Activity implements View.OnLongClickListener {

    Button awakeButton;
    TextView alarmTime;
    static Activity parentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_activity);

        parentActivity = this;

        Database.init(this);

        alarmTime = (TextView) findViewById(R.id.sleep_activity_alarm_text);
        Alarm nextAlarm = Database.getNextAlarm();
        if (nextAlarm != null) {
            alarmTime.setText(nextAlarm.getAlarmTimeString());
        }

        awakeButton = (Button) findViewById(R.id.sleep_activity_awake_button);
        awakeButton.setLongClickable(true);
        awakeButton.setOnLongClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.menu_item_new).setVisible(false);
        menu.findItem(R.id.menu_item_sleep).setVisible(false);
        menu.findItem(R.id.menu_item_save).setVisible(false);
        menu.findItem(R.id.menu_item_delete).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        stopSleepTracking();
        super.onDestroy();
    }

    private void stopSleepTracking() {
        //stop data collection
    }

    @Override
    public boolean onLongClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Intent alarmActivityIntent = new Intent(SleepActivity.this, AlarmActivity.class);
        startActivity(alarmActivityIntent);
        return true;
    }

    public static Activity getActivity() {
        return parentActivity;
    }
}
