package com.jrproject.brown_goist.intellalarm.preferences;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.BaseActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;
import com.jrproject.brown_goist.intellalarm.preferences.AlarmPreference.Key;

import java.util.Calendar;

/**
 * Screen to select options for an alarm and then save/delete it
 */
public class AlarmPreferencesActivity extends BaseActivity {
    private Alarm alarm;
    private MediaPlayer mediaPlayer;
    private ListAdapter listAdapter;
    private ListView listView;
    private CountDownTimer alarmToneTimer;

    /**
     * Intializes the view and sets listeners for each list item so that they may change states or show further options
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.alarm_preferences);

        Bundle bundle = getIntent().getExtras();
        //Load information with a selected alarm or with a new alarm
        if (bundle != null && bundle.containsKey("alarm")) {
            setAlarm((Alarm) bundle.getSerializable("alarm"));
        } else {
            setAlarm(new Alarm());
        }
        if (bundle != null && bundle.containsKey("adapter")) {
            setListAdapter((AlarmPreferenceListAdapter) bundle.getSerializable("adapter"));
        } else {
            setListAdapter(new AlarmPreferenceListAdapter(this, getAlarm()));
        }

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                final AlarmPreferenceListAdapter alarmPreferenceListAdapter = (AlarmPreferenceListAdapter) getListAdapter();
                final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter.getItem(position);

                AlertDialog.Builder alert;
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                switch (alarmPreference.getType()) {
                    case BOOLEAN:
                        CheckedTextView checkedTextView = (CheckedTextView) v;
                        boolean checked = !checkedTextView.isChecked();
                        ((CheckedTextView) v).setChecked(checked);
                        switch (alarmPreference.getKey()) {
                            case ALARM_ACTIVE:
                                alarm.setAlarmActive(checked);
                                break;
                            case ALARM_VIBRATE:
                                alarm.setVibrate(checked);
                                if (checked) {
                                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                }
                                break;
                            case ALARM_PROGRESSIVE:
                                alarm.setProgressive(checked);
                                break;
                        }
                        alarmPreference.setValue(checked);
                        break;
                    case STRING:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                        alert.setTitle(alarmPreference.getTitle());

                        // Set an EditText view to get user input
                        final EditText input = new EditText(AlarmPreferencesActivity.this);
                        input.setText(alarmPreference.getValue().toString());

                        alert.setView(input);
                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                alarmPreference.setValue(input.getText().toString());
                                if (alarmPreference.getKey() == Key.ALARM_NAME) {
                                    alarm.setAlarmName(alarmPreference.getValue().toString());
                                }
                                alarmPreferenceListAdapter.setAlarm(getAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }
                        });
                        alert.show();
                        break;
                    case LIST:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                        alert.setTitle(alarmPreference.getTitle());

                        CharSequence[] items = new CharSequence[alarmPreference.getOptions().length];
                        System.arraycopy(alarmPreference.getOptions(), 0, items, 0, items.length);

                        alert.setItems(items, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (alarmPreference.getKey()) {
                                    case ALARM_TONE:
                                        alarm.setAlarmTonePath(alarmPreferenceListAdapter.getAlarmTonePaths()[which]);
                                        if (alarm.getAlarmTonePath() != null) {
                                            if (mediaPlayer == null) {
                                                mediaPlayer = new MediaPlayer();
                                            } else {
                                                if (mediaPlayer.isPlaying())
                                                    mediaPlayer.stop();
                                                mediaPlayer.reset();
                                            }
                                            try {
                                                mediaPlayer.setVolume(0.2f, 0.2f);
                                                mediaPlayer.setDataSource(AlarmPreferencesActivity.this, Uri.parse(alarm.getAlarmTonePath
                                                        ()));
                                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                                                mediaPlayer.setLooping(false);
                                                mediaPlayer.prepare();
                                                mediaPlayer.start();

                                                // Force the mediaPlayer to stop after 3 seconds
                                                if (alarmToneTimer != null)
                                                    alarmToneTimer.cancel();
                                                alarmToneTimer = new CountDownTimer(3000, 3000) {
                                                    @Override
                                                    public void onTick(long millisUntilFinished) {
                                                        //Do nothing
                                                    }

                                                    @Override
                                                    public void onFinish() {
                                                        if (mediaPlayer.isPlaying()) {
                                                            mediaPlayer.stop();
                                                        }
                                                    }
                                                };
                                                alarmToneTimer.start();
                                            } catch (Exception e) {
                                                if (mediaPlayer.isPlaying()) {
                                                    mediaPlayer.stop();
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                alarmPreferenceListAdapter.setAlarm(getAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }

                        });

                        alert.show();
                        break;
                    case MULTIPLE_LIST:
                        alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                        alert.setTitle(alarmPreference.getTitle());

                        CharSequence[] multiListItems = new CharSequence[alarmPreference.getOptions().length];
                        System.arraycopy(alarmPreference.getOptions(), 0, multiListItems, 0, multiListItems.length);

                        boolean[] checkedItems = new boolean[multiListItems.length];
                        for (Alarm.Day day : getAlarm().getDays()) {
                            checkedItems[day.ordinal()] = true;
                        }
                        alert.setMultiChoiceItems(multiListItems, checkedItems, new OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which, boolean isChecked) {
                                Alarm.Day thisDay = Alarm.Day.values()[which];
                                if (isChecked) {
                                    alarm.addDay(thisDay);
                                } else {
                                    // Only remove the day if there is more than 1 selected
                                    if (alarm.getDays().length > 1) {
                                        alarm.removeDay(thisDay);
                                    } else {
                                        // If the last day was unchecked, re-check it
                                        ((AlertDialog) dialog).getListView().setItemChecked(which, true);
                                    }
                                }
                            }
                        });
                        alert.setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                alarmPreferenceListAdapter.setAlarm(getAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }
                        });
                        alert.show();
                        break;
                    case TIME:
                        TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmPreferencesActivity.this, new OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                                Calendar newAlarmTime = Calendar.getInstance();
                                newAlarmTime.set(Calendar.HOUR_OF_DAY, hours);
                                newAlarmTime.set(Calendar.MINUTE, minutes);
                                newAlarmTime.set(Calendar.SECOND, 0);
                                alarm.setAlarmTime(newAlarmTime);
                                alarmPreferenceListAdapter.setAlarm(getAlarm());
                                alarmPreferenceListAdapter.notifyDataSetChanged();
                            }
                        }, alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY), alarm.getAlarmTime().get(Calendar.MINUTE), true);
                        timePickerDialog.setTitle(alarmPreference.getTitle());
                        timePickerDialog.show();
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_new).setVisible(false);
        menu.findItem(R.id.menu_item_sleep).setVisible(false);
        menu.findItem(R.id.menu_item_graph).setVisible(false);
        menu.findItem(R.id.menu_item_alarm_list).setVisible(false);
        menu.findItem(R.id.menu_item_calibrate).setVisible(false);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                AlarmDatabase.init(getApplicationContext());
                if (getAlarm().getId() < 1) {
                    AlarmDatabase.create(getAlarm());
                } else {
                    AlarmDatabase.update(getAlarm());
                }
                callAlarmScheduleService();
                Toast.makeText(AlarmPreferencesActivity.this, getAlarm().getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.menu_item_delete:
                AlertDialog.Builder dialog = new AlertDialog.Builder(AlarmPreferencesActivity.this);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete this alarm?");
                dialog.setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlarmDatabase.init(getApplicationContext());
                        if (getAlarm().getId() >= 1) {
                            AlarmDatabase.deleteEntry(alarm);
                            callAlarmScheduleService();
                        } // Alarm not saved if id < 1
                        finish();
                    }
                });
                dialog.setNegativeButton("Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("alarm", getAlarm());
        outState.putSerializable("adapter", (AlarmPreferenceListAdapter) getListAdapter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public ListAdapter getListAdapter() {
        return listAdapter;
    }

    public void setListAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;
        getListView().setAdapter(listAdapter);

    }

    public ListView getListView() {
        if (listView == null) {
            listView = (ListView) findViewById(android.R.id.list);
        }
        return listView;
    }

    @Override
    public void onClick(View v) {
        //Overriding BaseActivity's OnClick to make it do nothing
    }
}
