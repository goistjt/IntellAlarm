package com.jrproject.brown_goist.intellalarm.calibrate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.sleep.SleepActivity;

/**
 * Calibration activity should be run before using Sleep the first time, while phone is at 100% charge. Accounts for sensor noise
 */
public class CalibrationActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView countdown;

    private float currentMax = 0;
    private int readings = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate_activity);

        countdown = (TextView) findViewById(R.id.calibration_countdown_text_view);

        //Checking for and initializing the linear accelerometer, which removes force of gravity from values returned
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            Log.d("SleepSensor", "Linear Accelerometer Exists");
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SleepActivity.SENSOR_DELAY);
        } else {
            Log.d("SleepSensor", "Linear Accelerometer Does Not Exist");
        }
    }


    /**
     * Each time sensor reads during 5 second period, it updates the max noise value, and eventually saves it as a SharedPreference setting
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float absSize = (float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] +
                event.values[2] * event.values[2]);
        if(absSize > currentMax) {
            currentMax = absSize;
        }
        readings++;
        switch (readings) {
            case 25:
                countdown.setText("Calibration Complete In: 4");
                break;
            case 50:
                countdown.setText("Calibration Complete In: 3");
                break;
            case 75:
                countdown.setText("Calibration Complete In: 2");
                break;
            case 100:
                countdown.setText("Calibration Complete In: 1");
                break;
            case 125:
                countdown.setText("Calibration Complete In: 0");
                SharedPreferences settings= getApplicationContext().getSharedPreferences("IntellAlarm", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putFloat("threshold", currentMax);
                editor.apply();
                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        sensorManager.unregisterListener(this);
        super.finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this, accelerometer, SleepActivity.SENSOR_DELAY);
        super.onResume();
    }
}
