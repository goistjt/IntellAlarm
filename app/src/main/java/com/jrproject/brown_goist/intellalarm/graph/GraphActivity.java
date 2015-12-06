package com.jrproject.brown_goist.intellalarm.graph;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.jrproject.brown_goist.intellalarm.BaseActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.database.SensorDatabase;

public class GraphActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_save).setVisible(false);
        menu.findItem(R.id.menu_item_delete).setVisible(false);
        menu.findItem(R.id.menu_item_graph).setVisible(false);
        return result;
    }

    @Override
    protected void onPause() {
        SensorDatabase.deactivate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SensorDatabase.deactivate();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGraphs();
    }

    public void updateGraphs() {
        SensorDatabase.init(GraphActivity.this);
//        final List<Alarm> alarms = AlarmDatabase.getAll();
//        alarmListAdapter.setAlarms(alarms);
//
//        runOnUiThread(new Runnable() {
//            public void run() {
//                AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
//                if (alarms.size() > 0) {
//                    findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
//                } else {
//                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
//                }
//            }
//        });
    }

    @Override
    public void onClick(View v) {

    }
}
