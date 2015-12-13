package com.jrproject.brown_goist.intellalarm.graph;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

public class GraphActivity extends BaseActivity implements OnChartValueSelectedListener {

    private LineChart mChart;
    private Button dayButton, weekButton, hours12Button;
    private int[] mColors = new int[]{
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[3],
            ColorTemplate.VORDIPLOM_COLORS[4]
    };

    private DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorDatabase.init(GraphActivity.this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.graph_activity);

        hours12Button = (Button) findViewById(R.id.button12Hours);
        hours12Button.setOnClickListener(this);

        dayButton = (Button) findViewById(R.id.buttonDay);
        dayButton.setOnClickListener(this);

        weekButton = (Button) findViewById(R.id.buttonWeek);
        weekButton.setOnClickListener(this);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawGridBackground(false);
        mChart.setDescription("");
        mChart.setDrawBorders(false);

        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisLeft().setTextColor(Color.WHITE);

        mChart.getAxisRight().setEnabled(false);

        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setTextColor(Color.WHITE);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        Legend l = mChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

        updateGraph("hourly", 5, 4);
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

    public void updateGraph(String graphType, int start, int end) {
        df.setTimeZone(TimeZone.getDefault());
        SensorDatabase.init(GraphActivity.this);
        mChart.resetTracking();

        ArrayList<String> xVals = new ArrayList<>();

        List<SensorData> sd = graphType.equals("hourly") ? SensorDatabase.getHourly(start, end) :
                graphType.equals("12hours") ? SensorDatabase.get12Hours() : graphType.equals("day") ?
                SensorDatabase.getDay() : SensorDatabase.getWeek();
        if (sd.size() == 0) {
            return;
        }

        List<SensorData> fillerData = new ArrayList<>();

        SensorData prev = null;
        long startTime = graphType.equals("hourly") ? System.currentTimeMillis() - 3600 * start * 1000 :
                graphType.equals("12hours") ? System.currentTimeMillis() - 3600 * 12 * 1000 :
                graphType.equals("day") ? System.currentTimeMillis() - 3600 * 24 * 1000 :
                        System.currentTimeMillis() - 3600 * 24 * 7 * 1000;
        long endTime = graphType.equals("hourly") ? System.currentTimeMillis() - 3600 * end * 1000 :
                System.currentTimeMillis();

        long curTime = sd.get(0).getTimeStamp();
        while (startTime <= curTime) {
            SensorData addSD = new SensorData();
            startTime += 10000;
            xVals.add(df.format(new Date(startTime)).substring(11));

            //Adding in a dummy value for spacing
            addSD.setxValue(0);
            addSD.setyValue(0);
            addSD.setzValue(0);
            addSD.setTimeStamp(startTime);
            fillerData.add(addSD);
        }
        for (SensorData s : sd) {
            curTime = s.getTimeStamp();
            if (prev != null) {
                long prevTime = prev.getTimeStamp();
                while (curTime >= prevTime + 60000) {
                    SensorData addSD = new SensorData();
                    prevTime += 10000;
                    xVals.add(df.format(new Date(prevTime)).substring(11));

                    //Adding in a dummy value for spacing
                    addSD.setxValue(0);
                    addSD.setyValue(0);
                    addSD.setzValue(0);
                    addSD.setTimeStamp(prevTime);
                    fillerData.add(addSD);
                }
            }
            xVals.add(df.format(new Date(curTime)).substring(11));
            prev = s;
        }
        long prevTime = prev.getTimeStamp();
        while (endTime >= prevTime) {
            SensorData addSD = new SensorData();
            prevTime += 10000;
            xVals.add(df.format(new Date(prevTime)).substring(11));

            //Adding in a dummy value for spacing
            addSD.setxValue(0);
            addSD.setyValue(0);
            addSD.setzValue(0);
            addSD.setTimeStamp(prevTime);
            fillerData.add(addSD);
        }

        //Add filler data to list of SensorData
        sd.addAll(fillerData);

        //Sorting data chronological after adding dummy values
        Collections.sort(sd);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();

        for (int z = 0; z < 3; z++) {

            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < sd.size(); i++) {
                SensorData d = sd.get(i);

                double val = (z == 0 ? d.getxValue() : z == 1 ? d.getyValue() : d.getzValue());
                values.add(new Entry((float) val, i));
            }

            LineDataSet d = new LineDataSet(values, "DataSet " + (z + 1));
            d.setLineWidth(2.5f);

            int color = mColors[z % mColors.length];
            d.setColor(color);
            d.setValueTextColor(Color.WHITE);
            d.setDrawCircles(false);
            dataSets.add(d);
        }
        dataSets.get(0).setLabel("X Values");
        dataSets.get(1).setLabel("Y Values");
        dataSets.get(2).setLabel("Z Values");

        LineData data = new LineData(xVals, dataSets);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button12Hours:
                Log.d("GraphActivity", "12 Hours button pressed");
                updateGraph("12hours", 0, 0);
                break;
            case R.id.buttonDay:
                Log.d("GraphActivity", "Day button pressed");
                updateGraph("day", 0, 0);
                break;
            case R.id.buttonWeek:
                Log.d("GraphActivity", "Week button pressed");
                updateGraph("week", 0, 0);
                break;
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("VAL SELECTED",
                "Value: " + e.getVal() + ", xIndex: " + e.getXIndex()
                        + ", DataSet index: " + dataSetIndex);
    }

    @Override
    public void onNothingSelected() {

    }
}
