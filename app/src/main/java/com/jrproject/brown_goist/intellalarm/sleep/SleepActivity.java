package com.jrproject.brown_goist.intellalarm.sleep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.AlarmActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.SensorData;
import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;
import com.jrproject.brown_goist.intellalarm.database.SensorDatabase;

public class SleepActivity extends Activity implements View.OnLongClickListener, SensorEventListener {

    Button awakeButton;
    TextView alarmTime;
    static Activity parentActivity;
    SensorManager sensorManager;
    Sensor accelerometer;

    public static final int SENSOR_DELAY = 200000; //200ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_activity);

        parentActivity = this;

        AlarmDatabase.init(this);
        SensorDatabase.init(this);

        alarmTime = (TextView) findViewById(R.id.sleep_activity_alarm_text);
        Alarm nextAlarm = AlarmDatabase.getNextAlarm();
        if (nextAlarm != null) {
            alarmTime.setText(nextAlarm.getAlarmTimeString());
        }

        awakeButton = (Button) findViewById(R.id.sleep_activity_awake_button);
        awakeButton.setLongClickable(true);
        awakeButton.setOnLongClickListener(this);

        // Setting up sensor. Automatically registers listener as defined in onResume()
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            Log.d("SleepSensor", "Linear Accelerometer Exists");
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        } else {
            Log.d("SleepSensor", "Linear Accelerometer Does Not Exist");
        }
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
        menu.findItem(R.id.menu_item_graph).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorData sensorData = new SensorData();
        sensorData.setxValue(event.values[0]);
        sensorData.setyValue(event.values[1]);
        sensorData.setzValue(event.values[2]);
        sensorData.setTimeStamp();
        SensorDatabase.create(sensorData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
    }
}
