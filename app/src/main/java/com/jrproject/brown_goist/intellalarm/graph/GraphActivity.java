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
import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.BaseActivity;
import com.jrproject.brown_goist.intellalarm.R;
import com.jrproject.brown_goist.intellalarm.SensorData;
import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;
import com.jrproject.brown_goist.intellalarm.database.SensorDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GraphActivity extends BaseActivity implements OnChartValueSelectedListener {

    private LineChart mChart;
    private Button dayButton, weekButton, monthButton;
    private int[] mColors = new int[] {
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

        dayButton = (Button) findViewById(R.id.buttonDay);
        dayButton.setOnClickListener(this);

        weekButton = (Button) findViewById(R.id.buttonWeek);
        weekButton.setOnClickListener(this);

        monthButton = (Button) findViewById(R.id.buttonMonth);
        monthButton.setOnClickListener(this);

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

        updateGraph("day");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_new).setVisible(false);
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
        updateGraph("day");
    }

    public void updateGraph(String graphType) {
        df.setTimeZone(TimeZone.getDefault());
        SensorDatabase.init(GraphActivity.this);
        mChart.resetTracking();

        ArrayList<String> xVals = new ArrayList<>();

        List<SensorData> sd = graphType.equals("day") ? SensorDatabase.getDay() : graphType.equals("week") ?
                SensorDatabase.getWeek() : SensorDatabase.getMonth();

//        for (int i = 0; i < sd.size(); i++) {
//            long time = sd.get(i).getTimeStamp();
//            Date d = new Date(time);
//            xVals.add(df.format(d).substring(11));
//        }

        SensorData prev = null;
        for(SensorData s : sd) {
            long curTime = s.getTimeStamp();
            if (prev != null) {
                long prevTime = prev.getTimeStamp();
                while(curTime >= prevTime + 1000) {
                    xVals.add(df.format(new Date(curTime)).substring(11));
                    prevTime+=200;
                }
            } else {
                xVals.add(df.format(new Date(curTime)).substring(11));
            }
            prev = s;
        }

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
            case R.id.buttonDay:
                Log.d("GraphActivity", "Day button pressed");
                updateGraph("day");
                break;
            case R.id.buttonWeek:
                Log.d("GraphActivity", "Week button pressed");
                updateGraph("week");
                break;
            case R.id.buttonMonth:
                Log.d("GraphActivity", "Month button pressed");
                updateGraph("month");
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
