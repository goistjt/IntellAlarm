package com.jrproject.brown_goist.intellalarm.graph;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    private LineChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorDatabase.init(GraphActivity.this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.graph_activity);

        tvX = (TextView) findViewById(R.id.tvXMax);
        tvY = (TextView) findViewById(R.id.tvYMax);

        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBarX.setOnSeekBarChangeListener(this);

        mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);
        mSeekBarY.setOnSeekBarChangeListener(this);

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

        mSeekBarX.setProgress(20);
        mSeekBarY.setProgress(100);

        Legend l = mChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
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
        updateGraphs();
    }

    public void updateGraphs() {
        SensorDatabase.init(GraphActivity.this);
    }

    @Override
    public void onClick(View v) {

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

    private int[] mColors = new int[] {
            ColorTemplate.VORDIPLOM_COLORS[0],
            ColorTemplate.VORDIPLOM_COLORS[3],
            ColorTemplate.VORDIPLOM_COLORS[4]
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mChart.resetTracking();

        tvX.setText("" + (mSeekBarX.getProgress()));
        tvY.setText("" + (mSeekBarY.getProgress()));

        ArrayList<String> xVals = new ArrayList<>();
        List<SensorData> sd = SensorDatabase.getDay();

        for (int i = 0; i < sd.size(); i++) {
            xVals.add(sd.get(i).getTimeStamp().substring(11));
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
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
