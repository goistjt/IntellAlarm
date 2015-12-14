package com.jrproject.brown_goist.intellalarm.graph;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class MyBarDataSet extends BarDataSet {
    public MyBarDataSet(List<BarEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getColor(int index) {
        if (getEntryForXIndex(index).getVal() >= 200) {
            return mColors.get(0);
        }
        else if (getEntryForXIndex(index).getVal() < 200 && getEntryForXIndex(index).getVal() >= 50) {
            return mColors.get(1);
        }
        else {
            return mColors.get(2);
        }
    }
}
