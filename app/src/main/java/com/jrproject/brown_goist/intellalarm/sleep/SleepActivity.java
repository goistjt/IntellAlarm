package com.jrproject.brown_goist.intellalarm.sleep;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.AlarmActivity;
import com.jrproject.brown_goist.intellalarm.BaseActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.SensorData;
import com.jrproject.brown_goist.intellalarm.alert.AlarmAlertBroadcastReceiver;
import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;
import com.jrproject.brown_goist.intellalarm.database.SensorDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SleepActivity extends Activity implements View.OnLongClickListener, SensorEventListener {

    static Activity parentActivity;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private PowerManager.WakeLock wakeLock;

    private float threshold;
    public static final int SENSOR_DELAY = 40000; //40ms = 25/second

    private float prev = 0;
    private static final int VALS_PER_MIN = 25 * 60;
    private int vals = 0;
    private int events = 0;
    private long priorMin;
    private long alarmTime;
    private Alarm nextAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_activity);

        parentActivity = this;

        AlarmDatabase.init(this);
        SensorDatabase.init(this);

        TextView alarmTimeText = (TextView) findViewById(R.id.sleep_activity_alarm_text);
        nextAlarm = AlarmDatabase.getNextAlarm();
        if (nextAlarm != null) {
            alarmTime = nextAlarm.getAlarmTime().getTimeInMillis();
            alarmTimeText.setText(nextAlarm.getAlarmTimeString());
        }

        Button awakeButton = (Button) findViewById(R.id.sleep_activity_awake_button);
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

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sleep Screen Data Collection");
        wakeLock.acquire();

        threshold = getApplicationContext().getSharedPreferences("IntellAlarm", 0).getFloat("threshold", .01F);
        Log.v("Sensor Threshold", "" + threshold);

        Intent data = getIntent();
        int prior = data.getIntExtra(BaseActivity.KEY_PRIOR_MINUTES, -1);
        priorMin = alarmTime - (prior * 60 * 1000);
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
        menu.findItem(R.id.menu_item_alarm_list).setVisible(false);
        menu.findItem(R.id.menu_item_calibrate).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        wakeLock.release();
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

    public void checkSleepStatus(SensorData sd) {
        //check to see if user is restless or awake within specified time prior to alarm
        //if so, activate alarm now rather than wait
        if (sd.getStatus() != SensorData.Status.ASLEEP) {
            //set off alarm
            Log.d("SleepSensor", "SET OFF ALARM!!");

            //Cancelling the previously set alarm
            Intent cancelAlarm = new Intent(getApplicationContext(), AlarmAlertBroadcastReceiver.class);
            cancelAlarm.putExtra("alarm", nextAlarm);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cancelAlarm, PendingIntent
                    .FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);

            //Making a new alarm and setting it off
            Alarm copy = nextAlarm;
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            Calendar c = Calendar.getInstance();
            Log.v("Blargh", "Current c.getTime(): " + c.getTime().toString());
            c.add(Calendar.SECOND, 5);
            Log.v("Blargh", "Current c.getTime(): " + c.getTime().toString());
            String date = df.format(c.getTime());
            copy.setAlarmTime(date);
            copy.schedule(getApplicationContext());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorData sensorData = new SensorData();
        float absSize = (float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] +
                event.values[2] * event.values[2]);
        absSize = absSize > threshold ? absSize : 0;
        float deriv = absSize - prev;
        if (deriv != 0) {
            events++;
        }
        vals++;
        if (vals >= VALS_PER_MIN) {
            sensorData.setNumEvents(events);
            sensorData.setTimeStamp();
            SensorDatabase.create(sensorData);
            if (System.currentTimeMillis() > priorMin) {
                checkSleepStatus(sensorData);
            }
            vals = 0;
            events = 0;
        }
        prev = absSize;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        wakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
        wakeLock.acquire();
    }
}
