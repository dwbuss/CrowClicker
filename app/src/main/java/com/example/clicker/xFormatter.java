package com.example.clicker;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class xFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        SimpleDateFormat f = new SimpleDateFormat("MM/dd h:mm a");
        return f.format(new Date((long) value));
    }
}
