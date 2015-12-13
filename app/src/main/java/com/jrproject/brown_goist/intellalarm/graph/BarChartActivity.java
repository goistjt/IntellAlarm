package com.jrproject.brown_goist.intellalarm.graph;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.utils.ColorTemplate;
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

public class BarChartActivity extends BaseActivity {

    private BarChart mChart;
    private Button dayButton, hours6Button, hours12Button;

    private DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorDatabase.init(BarChartActivity.this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barchart_activity);

        hours6Button = (Button) findViewById(R.id.button6Hours);
        hours6Button.setOnClickListener(this);

        hours12Button = (Button) findViewById(R.id.button12Hours);
        hours12Button.setOnClickListener(this);

        dayButton = (Button) findViewById(R.id.buttonDay);
        dayButton.setOnClickListener(this);

        mChart = (BarChart) findViewById(R.id.chart1);

        mChart.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(600);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);

        mChart.getAxisLeft().setDrawGridLines(false);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setTextColor(Color.WHITE);
        mChart.getAxisRight().setEnabled(false);

        // add a nice and smooth animation
        mChart.animateY(2500);

        mChart.getLegend().setEnabled(false);

        // Legend l = mChart.getLegend();
        // l.setPosition(LegendPosition.BELOW_CHART_CENTER);
        // l.setFormSize(8f);
        // l.setFormToTextSpace(4f);
        // l.setXEntrySpace(6f);

        // mChart.setDrawLegend(false);
        updateGraph("hourly", 1, 0);
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

    public void updateGraph(String graphType, int start, int end) {
        df.setTimeZone(TimeZone.getDefault());
        SensorDatabase.init(BarChartActivity.this);
        mChart.resetTracking();

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals = new ArrayList<>();

        List<SensorData> sd = graphType.equals("hourly") ? SensorDatabase.getHourly(start, end) :
                graphType.equals("6hours") ? SensorDatabase.get6Hours() : graphType.equals("12Hours") ?
                        SensorDatabase.get12Hours() : SensorDatabase.getDay();
        if (sd.size() == 0) {
            return;
        }

        SensorData prev = null;
        long startTime = graphType.equals("hourly") ? System.currentTimeMillis() - 3600 * start * 1000 :
                graphType.equals("6hours") ? System.currentTimeMillis() - 3600 * 6 * 1000 :
                        graphType.equals("12hours") ? System.currentTimeMillis() - 3600 * 12 * 1000 :
                                System.currentTimeMillis() - 3600 * 24 * 1000;
        long endTime = graphType.equals("hourly") ? System.currentTimeMillis() - 3600 * end * 1000 :
                System.currentTimeMillis();

        List<SensorData> fillerData = new ArrayList<>();

        long curTime = sd.get(0).getTimeStamp();
        while (startTime <= curTime) {
            SensorData addSD = new SensorData();
            xVals.add(df.format(new Date(startTime)).substring(11));

            //Adding in a dummy value for spacing
            addSD.setNumEvents(0);
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
                    xVals.add(df.format(new Date(prevTime)).substring(11));

                    //Adding in a dummy value for spacing
                    addSD.setNumEvents(0);
                    addSD.setTimeStamp(prevTime);
                    fillerData.add(addSD);
                    prevTime += 60000;
                }
            }
            xVals.add(df.format(new Date(curTime)).substring(11));
            prev = s;
        }
        long prevTime = prev.getTimeStamp();
        while (endTime >= prevTime) {
            SensorData addSD = new SensorData();
            xVals.add(df.format(new Date(prevTime)).substring(11));

            //Adding in a dummy value for spacing
            addSD.setNumEvents(0);
            addSD.setTimeStamp(prevTime);
            fillerData.add(addSD);
            prevTime += 60000;
        }

        //Add filler data to list of SensorData
        sd.addAll(fillerData);

        //Sorting data chronological after adding dummy values
        Collections.sort(sd);

        for (int i = 0; i < sd.size(); i++) {
            SensorData d = sd.get(i);
            yVals.add(new BarEntry(d.getNumEvents(), i));
        }

        BarDataSet set1 = new BarDataSet(yVals, "Data Set");
        set1.setColor(Color.rgb(192, 255, 140));
        set1.setValueTextColor(Color.WHITE);
        set1.setDrawValues(false);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        mChart.setData(data);
        mChart.invalidate();
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
        updateGraph("hourly", 1, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button6Hours:
                Log.d("BarChartActivity", "6 Hours button pressed");
                updateGraph("6hours", 0, 0);
                break;
            case R.id.button12Hours:
                Log.d("BarChartActivity", "12 Hours button pressed");
                updateGraph("12hours", 0, 0);
                break;
            case R.id.buttonDay:
                Log.d("BarChartActivity", "Day button pressed");
                updateGraph("day", 0, 0);
                break;
        }
    }
}
