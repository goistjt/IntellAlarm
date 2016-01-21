package com.jrproject.brown_goist.intellalarm.graph;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.jrproject.brown_goist.intellalarm.BaseActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.SensorData;
import com.jrproject.brown_goist.intellalarm.database.SensorDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Uses MPAndroidChart to draw a bar graph detailing movement data collected during Sleep mode
 */
public class BarChartActivity extends BaseActivity {

    private BarChart mChart;
    private TextView sleepText, awakeText, restlessText, tableHeaderText;
    private int minAsleep, minAwake, minRestless = 0;
    //Red/pink, yellow/orange, and light blue color definitions
    private int[] colors = {Color.rgb(250, 140, 140), Color.rgb(242, 238, 109), Color.rgb(0, 188, 212)};

    private DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorDatabase.init(BarChartActivity.this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barchart_activity);

        Button hours8Button = (Button) findViewById(R.id.button8Hours);
        hours8Button.setOnClickListener(this);

        Button hours12Button = (Button) findViewById(R.id.button12Hours);
        hours12Button.setOnClickListener(this);

        Button dayButton = (Button) findViewById(R.id.buttonDay);
        dayButton.setOnClickListener(this);

        Button weekButton = (Button) findViewById(R.id.buttonWeek);
        weekButton.setOnClickListener(this);

        tableHeaderText = (TextView) findViewById(R.id.tableHeaderText);
        sleepText = (TextView) findViewById(R.id.timeSleptEditText);
        awakeText = (TextView) findViewById(R.id.timeAwakeEditText);
        restlessText = (TextView) findViewById(R.id.timeRestlessEditText);

        mChart = (BarChart) findViewById(R.id.chart1);

        mChart.setDescription("");

        mChart.setMaxVisibleValueCount(10100);

        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setTextColor(Color.rgb(255, 176, 30));
        xAxis.setDrawGridLines(false);

        mChart.getAxisLeft().setDrawGridLines(false);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setTextColor(Color.rgb(255, 176, 30));
        yAxis.setAxisMinValue(0);
        mChart.getAxisRight().setEnabled(false);

        // add a nice and smooth animation
        mChart.animateY(2500);

        mChart.getLegend().setEnabled(false);

        //Initially show data for 8 hours
        updateGraph("8hours");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_new).setVisible(false);
        menu.findItem(R.id.menu_item_save).setVisible(false);
        menu.findItem(R.id.menu_item_delete).setVisible(false);
        menu.findItem(R.id.menu_item_graph).setVisible(false);
        menu.findItem(R.id.menu_item_calibrate).setVisible(false);
        return result;
    }

    public void updateGraph(String graphType) {
        df.setTimeZone(TimeZone.getDefault());
        SensorDatabase.init(BarChartActivity.this);
        mChart.resetTracking();

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals = new ArrayList<>();

        List<SensorData> sd = graphType.equals("8hours") ? SensorDatabase.get8Hours() :
                graphType.equals("12hours") ? SensorDatabase.get12Hours() :
                        graphType.equals("day") ? SensorDatabase.getDay() : SensorDatabase.getWeek();

        if (sd.size() != 0) {

            SensorData prev = null;
            long startTime = System.currentTimeMillis() - (graphType.equals("8hours") ? 3600 * 8 * 1000 :
                    graphType.equals("12hours") ? 3600 * 12 * 1000 :
                            graphType.equals("day") ? 3600 * 24 * 1000 : 3600 * 24 * 7 * 1000);
            long endTime = System.currentTimeMillis();

            List<SensorData> fillerData = new ArrayList<>();

            long curTime = sd.get(0).getTimeStamp();
            while (startTime <= curTime) {
                SensorData addSD = new SensorData();
                xVals.add(df.format(new Date(startTime)));

                //Adding in a dummy value for spacing
                addSD.setNumEvents(-1);
                addSD.setTimeStamp(startTime);
                fillerData.add(addSD);
                startTime += 60000;
            }
            for (SensorData s : sd) {
                curTime = s.getTimeStamp();
                if (prev != null) {
                    long prevTime = prev.getTimeStamp();
                    while (curTime >= prevTime + 70000) {
                        SensorData addSD = new SensorData();
                        xVals.add(df.format(new Date(prevTime)));

                        //Adding in a dummy value for spacing
                        addSD.setNumEvents(-1);
                        addSD.setTimeStamp(prevTime);
                        fillerData.add(addSD);
                        prevTime += 60000;
                    }
                }
                xVals.add(df.format(new Date(curTime)));
                prev = s;
            }
            long prevTime = prev.getTimeStamp();
            while (endTime >= prevTime) {
                SensorData addSD = new SensorData();
                xVals.add(df.format(new Date(prevTime)));

                //Adding in a dummy value for spacing
                addSD.setNumEvents(-1);
                addSD.setTimeStamp(prevTime);
                fillerData.add(addSD);
                prevTime += 60000;
            }

            //Add filler data to list of SensorData
            sd.addAll(fillerData);

            //Sorting data chronologically after adding dummy values
            Collections.sort(sd);
        }

        for (int i = 0; i < sd.size(); i++) {
            SensorData d = sd.get(i);
            BarEntry b = new BarEntry(d.getNumEvents(), i);

            switch (d.getStatus()) {
                case AWAKE:
                    minAwake++;
                    break;
                case RESTLESS:
                    minRestless++;
                    break;
                case ASLEEP:
                    minAsleep++;
                    break;
            }
            yVals.add(b);
        }

        MyBarDataSet set1 = new MyBarDataSet(yVals, "Data Set");
        set1.setColors(colors);
        set1.setDrawValues(false);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        mChart.setData(data);
        mChart.invalidate();

        setTableValues(graphType);
    }

    private void setTableValues(String type) {
        int hours = minAsleep / 60;
        int min = minAsleep % 60;
        String s = hours + " hrs " + min + " min";
        sleepText.setText(s);

        s = minAwake + " min";
        awakeText.setText(s);

        s = minRestless + " min";
        restlessText.setText(s);

        minAsleep = minAwake = minRestless = 0;

        String time = type.equals("8hours") ? "8 Hours" : type.equals("12hours") ? "12 Hours" :
                type.equals("day") ? "Day" : "Week";
        s = "Times For Past " + time;
        tableHeaderText.setText(s);
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
        updateGraph("8hours");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button8Hours:
                Log.d("BarChartActivity", "8 Hours button pressed");
                updateGraph("8hours");
                break;
            case R.id.button12Hours:
                Log.d("BarChartActivity", "12 Hours button pressed");
                updateGraph("12hours");
                break;
            case R.id.buttonDay:
                Log.d("BarChartActivity", "Day button pressed");
                updateGraph("day");
                break;
            case R.id.buttonWeek:
                Log.d("BarChartActivity", "Week button pressed");
                updateGraph("week");
                break;
        }
    }
}
