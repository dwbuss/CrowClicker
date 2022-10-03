package com.example.clicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.location.Location;

import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SolunarTest {
    @Test
    public void testNotification() {

        Solunar solunar = new Solunar();
        Location loc = new Location("") {
            @Override
            public double getLatitude() {
                return 49.217314;
            }

            @Override
            public double getLongitude() {
                return -93.863248;
            }
        };

        Calendar goodDay = Calendar.getInstance();
        goodDay.setTimeZone(TimeZone.getTimeZone("CST"));
        goodDay.set(2021, 8, 15, 9, 30, 00);

        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("-64", solunar.moonDegree);
        //MINOR: 11:34 PM - 12:34 AM    4:50 PM - 5:50 PM
        //MAJOR: 7:45 AM - 9:45 AM    8:17 PM - 10:17 PM

        String minorMessage = String.format("Failed, minor [%s]", solunar.minor);
        String majorMessage = String.format("Failed, major [%s]", solunar.major);

        assertEquals(minorMessage, "Minor disappointment 4:50 PM - 5:50 PM is starting - time to drink!", solunar.getEventNotification("4:50 PM"));
        assertEquals(minorMessage, "Minor 4:50 PM - 5:50 PM has ended - all clear!", solunar.getEventNotification("5:50 PM"));
        assertEquals(minorMessage, "Minor disappointment 11:34 PM - 12:34 AM is starting - time to drink!", solunar.getEventNotification("11:34 PM"));
        assertEquals(minorMessage, "Minor 11:34 PM - 12:34 AM has ended - all clear!", solunar.getEventNotification("12:34 AM"));

        assertEquals(majorMessage, "Major disappointment 7:45 AM - 9:45 AM is starting - time to drink!", solunar.getEventNotification("7:45 AM"));
        assertEquals(majorMessage, "Major 7:45 AM - 9:45 AM has ended - all clear!", solunar.getEventNotification("9:45 AM"));
        assertEquals(majorMessage, "Major disappointment 8:17 PM - 10:17 PM is starting - time to drink!", solunar.getEventNotification("8:17 PM"));
        assertEquals(majorMessage, "Major 8:17 PM - 10:17 PM has ended - all clear!", solunar.getEventNotification("10:17 PM"));
    }

    @Test
    public void testDailyStats() {
// site to validate moon times https://www.timeanddate.com/moon/@6087172?month=9&year=2022
        Solunar solunar = new Solunar();
        Location loc = new Location("") {
            @Override
            public double getLatitude() {
                return 49.217314;
            }

            @Override
            public double getLongitude() {
                return -93.863248;
            }
        };

        Calendar goodDay = Calendar.getInstance();
        goodDay.setTimeZone(TimeZone.getTimeZone("CST"));
        goodDay.set(2021, 8, 15, 9, 30, 00);

        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("5:20 PM", solunar.moonRise);
        assertEquals("9:17 PM", solunar.moonOverHead);
        assertEquals("12:04 AM", solunar.moonSet);
        assertEquals("8:45 AM", solunar.moonUnderFoot);
        assertEquals("-64", solunar.moonDegree);
        assertEquals("MU", solunar.moonState);


        goodDay.set(2022, 8, 15, 22, 00, 00);
        solunar.populate(loc, goodDay);
        assertEquals("4:10 AM - 6:10 AM    4:28 PM - 6:28 PM", solunar.major);
        assertEquals("12:38 PM - 1:38 PM    9:15 PM - 10:15 PM", solunar.minor);
        assertTrue(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("1", solunar.moonDegree);
        assertEquals("MR", solunar.moonState);

        goodDay.set(2022, 8, 15, 2, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("42", solunar.moonDegree);
        assertEquals("M1", solunar.moonState);

        goodDay.set(2022, 8, 15, 5, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("59", solunar.moonDegree);
        assertEquals("MO", solunar.moonState);

        goodDay.set(2022, 8, 15, 8, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("45", solunar.moonDegree);
        assertEquals("M2", solunar.moonState);

        goodDay.set(2022, 8, 15, 13, 00, 00);
        solunar.populate(loc, goodDay);
        assertTrue(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("1", solunar.moonDegree);
        assertEquals("MS", solunar.moonState);

        goodDay.set(2022, 8, 15, 16, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("-16", solunar.moonDegree);
        assertEquals("M3", solunar.moonState);

        goodDay.set(2022, 8, 15, 17, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("-18", solunar.moonDegree);
        assertEquals("MU", solunar.moonState);

        goodDay.set(2022, 8, 15, 19, 44, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("-13", solunar.moonDegree);
        assertEquals("M4", solunar.moonState);

        goodDay.set(2022, 8, 15, 23, 44, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("16", solunar.moonDegree);
        assertEquals("M1", solunar.moonState);

        goodDay.set(2022, 8, 6, 18, 28, 00);
        solunar.populate(loc, goodDay);
        assertTrue(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("0", solunar.moonDegree);
        assertEquals("MR", solunar.moonState);

        goodDay.set(2022, 8, 6, 20, 20, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("10", solunar.moonDegree);
        assertEquals("M1", solunar.moonState);

        goodDay.set(2022, 8, 6, 22, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("15", solunar.moonDegree);
        assertEquals("MO", solunar.moonState);

        goodDay.set(2022, 8, 6, 23, 59, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("14", solunar.moonDegree);
        assertEquals("M2", solunar.moonState);

        goodDay.set(2022, 8, 6, 1, 00, 00);
        solunar.populate(loc, goodDay);
        assertTrue(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("2", solunar.moonDegree);
        assertEquals("MS", solunar.moonState);

        goodDay.set(2022, 8, 6, 5, 14, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("-32", solunar.moonDegree);
        assertEquals("M3", solunar.moonState);

        goodDay.set(2022, 8, 6, 9, 46, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("-66", solunar.moonDegree);
        assertEquals("MU", solunar.moonState);

        goodDay.set(2022, 8, 6, 12, 44, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("-51", solunar.moonDegree);
        assertEquals("M4", solunar.moonState);


        goodDay.set(2022, 8, 29, 11, 43, 00);
        solunar.populate(loc, goodDay);
        assertTrue(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("1", solunar.moonDegree);
        assertEquals("MR", solunar.moonState);

        goodDay.set(2022, 8, 29, 14, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("15", solunar.moonDegree);
        assertEquals("M1", solunar.moonState);

        goodDay.set(2022, 8, 29, 16, 25, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("20", solunar.moonDegree);
        assertEquals("MO", solunar.moonState);

        goodDay.set(2022, 8, 29, 19, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("11", solunar.moonDegree);
        assertEquals("M2", solunar.moonState);

        goodDay.set(2022, 8, 29, 20, 37, 00);
        solunar.populate(loc, goodDay);
        assertTrue(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("0", solunar.moonDegree);
        assertEquals("MS", solunar.moonState);

        goodDay.set(2022, 8, 29, 23, 30, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("-25", solunar.moonDegree);
        assertEquals("M3", solunar.moonState);

        goodDay.set(2022, 8, 29, 3, 33, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertTrue(solunar.isMajor);
        assertEquals("-57", solunar.moonDegree);
        assertEquals("MU", solunar.moonState);

        goodDay.set(2022, 8, 29, 7, 00, 00);
        solunar.populate(loc, goodDay);
        assertFalse(solunar.isMinor);
        assertFalse(solunar.isMajor);
        assertEquals("-40", solunar.moonDegree);
        assertEquals("M4", solunar.moonState);
    }

    @Test
    public void checkDayWithNoMoonSet() {
        Location waterfordWI = new Location("") {
            @Override
            public double getLatitude() { return 42.762890; }

            @Override
            public double getLongitude() {
                return -88.212830;
            }
        };

        Calendar noMoonSet = Calendar.getInstance(Locale.US);
        noMoonSet.setTimeZone(TimeZone.getTimeZone("CST"));
        noMoonSet.set(2022, 9, 3, 12, 52, 00);

        Solunar solunar = new Solunar();
        solunar.populate(waterfordWI, noMoonSet);
        assertEquals("N/A", solunar.moonSet);
        assertEquals("M4", solunar.moonState);
        assertEquals( "3:31 PM", solunar.moonRise);
        assertEquals( "6:52 AM", solunar.sunRise);
        assertEquals( "6:30 PM", solunar.sunSet);
    }
}
