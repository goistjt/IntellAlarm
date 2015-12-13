package com.jrproject.brown_goist.intellalarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import com.jrproject.brown_goist.intellalarm.calibrate.CalibrationActivity;
import com.jrproject.brown_goist.intellalarm.graph.BarChartActivity;
import com.jrproject.brown_goist.intellalarm.preferences.AlarmPreferencesActivity;
import com.jrproject.brown_goist.intellalarm.service.AlarmServiceBroadcastReceiver;
import com.jrproject.brown_goist.intellalarm.sleep.SleepActivity;

import java.lang.reflect.Field;

public abstract class BaseActivity extends Activity implements android.view.View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new:
                Intent newAlarmIntent = new Intent(this, AlarmPreferencesActivity.class);
                startActivity(newAlarmIntent);
                break;
            case R.id.menu_item_sleep:
                callSleepActivity();
                break;
            case R.id.menu_item_graph:
                Intent graphIntent = new Intent(this, BarChartActivity.class);
                startActivity(graphIntent);
                break;
            case R.id.menu_item_alarm_list:
                Intent alarmListIntent = new Intent(this, AlarmActivity.class);
                startActivity(alarmListIntent);
                break;
            case R.id.menu_item_calibrate:
                callCalibrateActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void callCalibrateActivity() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(BaseActivity.this);
        dialog.setTitle("Sensor Calibration");
        dialog.setMessage("Please set phone face up on a flat surface before pressing 'Ok'");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startCalibrationIntent = new Intent(BaseActivity.this, CalibrationActivity.class);
                startActivity(startCalibrationIntent);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    protected void callAlarmScheduleService() {
        Intent alarmServiceIntent = new Intent(this, AlarmServiceBroadcastReceiver.class);
        sendBroadcast(alarmServiceIntent, null);
    }

    protected void callSleepActivity() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(BaseActivity.this);
        dialog.setTitle("Sleep");
        dialog.setMessage("Start sleep monitoring now?");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent startSleepIntent = new Intent(BaseActivity.this, SleepActivity.class);
                startActivity(startSleepIntent);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
