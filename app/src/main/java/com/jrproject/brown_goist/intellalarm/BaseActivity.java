package com.jrproject.brown_goist.intellalarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.NumberPicker;

import com.jrproject.brown_goist.intellalarm.calibrate.CalibrationActivity;
import com.jrproject.brown_goist.intellalarm.graph.BarChartActivity;
import com.jrproject.brown_goist.intellalarm.preferences.AlarmPreferencesActivity;
import com.jrproject.brown_goist.intellalarm.service.AlarmServiceBroadcastReceiver;
import com.jrproject.brown_goist.intellalarm.sleep.SleepActivity;

import java.lang.reflect.Field;

public abstract class BaseActivity extends Activity implements android.view.View.OnClickListener {

    public static final String KEY_PRIOR_MINUTES = "KEY_PRIOR_MINUTES";
    private int priorMin = 0;

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
        DialogFragment df = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //Inflate view
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.custom_dialog, null);
                builder.setView(view);
                NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.customDialogPriorNumberPicker);
                numberPicker.setMinValue(0);
                numberPicker.setMaxValue(6);
                String[] vals = {"0", "5", "10", "15", "20", "25", "30"};
                numberPicker.setDisplayedValues(vals);
                numberPicker.setValue(0);
                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        // TODO Auto-generated method stub
                        switch (newVal) {
                            case 0:
                                priorMin = 0;
                                break;
                            case 1:
                                priorMin = 5;
                                break;
                            case 2:
                                priorMin = 10;
                                break;
                            case 3:
                                priorMin = 15;
                                break;
                            case 4:
                                priorMin = 20;
                                break;
                            case 5:
                                priorMin = 25;
                                break;
                            case 6:
                                priorMin = 30;
                                break;
                        }
                    }
                });

                Button cancelButton = (Button) view.findViewById(R.id.buttonCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                Button confirmButton = (Button) view.findViewById(R.id.buttonConfirm);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //add collecting prior time and passing it on
                        Intent startSleepIntent = new Intent(BaseActivity.this, SleepActivity.class);
                        startSleepIntent.putExtra(KEY_PRIOR_MINUTES, priorMin);

                        startActivity(startSleepIntent);
                    }
                });

                return builder.create();
            }
        };
        df.show(getFragmentManager(), "");
    }
}
