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

import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.sleep.SleepActivity;

public class CalibrationActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float currentMax = 0;

    private int readings = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate_activity);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            Log.d("SleepSensor", "Linear Accelerometer Exists");
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SleepActivity.SENSOR_DELAY);
        } else {
            Log.d("SleepSensor", "Linear Accelerometer Does Not Exist");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float absSize = (float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] +
                event.values[2] * event.values[2]);
        if(absSize > currentMax) {
            currentMax = absSize;
        }
        readings++;
        if(readings == 125) {
            SharedPreferences settings= getApplicationContext().getSharedPreferences("IntellAlarm", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("threshold", currentMax);
            editor.apply();
            this.finish();
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
