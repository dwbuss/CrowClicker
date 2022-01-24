package com.example.clicker;

import static org.junit.Assert.assertEquals;

import com.example.clicker.objectbo.Point;

import org.junit.Test;

import java.text.ParseException;

public class PointTest {
    @Test
    public void testCreatePoint() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\tThu Sep 02 09:11:00 CDT 2021\tFOLLOW\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
        Point point = new Point(csvRecord);
        assertEquals("Dan", point.getName());
    }
}