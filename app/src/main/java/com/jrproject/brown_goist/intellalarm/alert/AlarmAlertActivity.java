package com.jrproject.brown_goist.intellalarm.alert;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.sleep.SleepActivity;

public class AlarmAlertActivity extends Activity implements OnClickListener {
    private Alarm alarm;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    public AudioManager audioManager;
    final Handler handler = new Handler();
    private boolean alarmActive;

    /**
     * Initializing alarm alert screen and calling start alarm
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.alarm_alert);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Bundle bundle = this.getIntent().getExtras();
        alarm = (Alarm) bundle.getSerializable("alarm");

        assert alarm != null;
        this.setTitle(alarm.getAlarmName());

        findViewById(R.id.alarm_alert_off).setOnClickListener(this);

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(getClass().getSimpleName(), "Incoming call: " + incomingNumber);
                        mediaPlayer.pause();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(getClass().getSimpleName(), "Call State Idle");
                        mediaPlayer.start();
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        startAlarm();

    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmActive = true;
    }

    private void startAlarm() {
        //Only do all this if the Alarm path isn't null
        if (!alarm.getAlarmTonePath().equals("")) {
            mediaPlayer = new MediaPlayer();
            if (alarm.getVibrate()) {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {1000, 200, 200, 200};
                vibrator.vibrate(pattern, 0);
            }
            try {
                //Setting initial volume based on progressive flag
                if (alarm.getProgressive()) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 7, 0);
                }
                //Initializing media player
                mediaPlayer.setDataSource(this,
                        Uri.parse(alarm.getAlarmTonePath()));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
                //Asynchronous handler activation
                handler.post(new VolumeRunnable(audioManager, handler));
            } catch (Exception e) {
                mediaPlayer.release();
                alarmActive = false;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //Only allow back to work if alarm is off (this should never be hit)
        if (!alarmActive)
            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(this);
    }

    /**
     * Ending/releasing the vibrator and media player on screen close
     */
    @Override
    protected void onDestroy() {
        if (vibrator != null)
            vibrator.cancel();
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * On click handler for the "off" button to turn off alarm and return to previous screen
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (!alarmActive) {
            return;
        }
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        alarmActive = false;
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (SleepActivity.getActivity() != null) {
            SleepActivity.getActivity().finish();
        }
        finish();
    }

    /**
     * Runnable that will increment the current alarm volume level by one every ten seconds until max volume is reached
     */
    public class VolumeRunnable implements Runnable {

        private AudioManager mAudioManager;
        private Handler mHandlerThatWillIncreaseVolume;
        private final static int DELAY_UNTIL_NEXT_INCREASE = 10 * 1000;//10 seconds between each increment

        VolumeRunnable(AudioManager audioManager, Handler handler) {
            this.mAudioManager = audioManager;
            this.mHandlerThatWillIncreaseVolume = handler;
        }

        @Override
        public void run() {
            int currentAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            if (currentAlarmVolume != mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)) { //if we haven't reached the max
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentAlarmVolume + 1, 0);
                mHandlerThatWillIncreaseVolume.postDelayed(this, DELAY_UNTIL_NEXT_INCREASE); //"recursively call this runnable again
                // with some delay between each increment of the volume, until the condition above is satisfied.
            }

        }
    }
}
