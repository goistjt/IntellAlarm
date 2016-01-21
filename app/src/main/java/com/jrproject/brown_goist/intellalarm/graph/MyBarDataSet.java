package com.jrproject.brown_goist.intellalarm.graph;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

/**
 * Extending the standard MPAndroidChart in order to determine bar color by value
 */
public class MyBarDataSet extends BarDataSet {
    public MyBarDataSet(List<BarEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getColor(int index) {
        if (getEntryForXIndex(index).getVal() >= 150) {
            return mColors.get(0);
        }
        else if (getEntryForXIndex(index).getVal() < 150 && getEntryForXIndex(index).getVal() >= 50) {
            return mColors.get(1);
        }
        else {
            return mColors.get(2);
        }
    }
}
