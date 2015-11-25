package com.jrproject.brown_goist.intellalarm.sleep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.jrproject.brown_goist.intellalarm.AlarmActivity;
import com.jrproject.brown_goist.intellalarm.R;

public class SleepActivity extends Activity {

    Button awakeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_activity);

        awakeButton = (Button) findViewById(R.id.sleep_activity_awake_button);
        awakeButton.setLongClickable(true);
        awakeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Intent alarmActivityIntent = new Intent(SleepActivity.this, AlarmActivity.class);
                startActivity(alarmActivityIntent);
                return true;
            }
        });
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
        super.onDestroy();
    }
}
