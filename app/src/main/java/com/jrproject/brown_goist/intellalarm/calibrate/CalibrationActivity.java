package com.jrproject.brown_goist.intellalarm.calibrate;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.jrproject.brown_goist.intellalarm.R;

public class CalibrationActivity extends Activity {
    private SensorManager sensorManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate_activity);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            Log.d("SleepSensor", "Linear Accelerometer Exists");
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        } else {
            Log.d("SleepSensor", "Linear Accelerometer Does Not Exist");
        }
    }
}
