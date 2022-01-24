package com.example.clicker;

import static org.junit.Assert.assertEquals;

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

        Calendar goodDay = Calendar.getInstance(Locale.US);
        goodDay.setTimeZone(TimeZone.getTimeZone("CST"));
        goodDay.set(2021, 8, 15, 9, 30, 00);

        solunar.populate(loc, goodDay);

        //MINOR: 11:34 PM - 12:34 AM    4:50 PM - 5:50 PM
        //MAJOR: 7:45 AM - 9:45 AM    8:17 PM - 10:17 PM

        String minorMessage = String.format("Failed, minor [%s]",solunar.minor);
        String majorMessage = String.format("Failed, major [%s]",solunar.major);

        assertEquals(minorMessage,"Minor disappointment 4:50 PM - 5:50 PM is starting - time to drink!", solunar.getEventNotification("4:50 PM"));
        assertEquals(minorMessage,"Minor 4:50 PM - 5:50 PM has ended - all clear!", solunar.getEventNotification("5:50 PM"));
        assertEquals(minorMessage,"Minor disappointment 11:34 PM - 12:34 AM is starting - time to drink!", solunar.getEventNotification("11:34 PM"));
        assertEquals(minorMessage,"Minor 11:34 PM - 12:34 AM has ended - all clear!", solunar.getEventNotification("12:34 AM"));

        assertEquals(majorMessage,"Major disappointment 7:45 AM - 9:45 AM is starting - time to drink!", solunar.getEventNotification("7:45 AM"));
        assertEquals(majorMessage,"Major 7:45 AM - 9:45 AM has ended - all clear!", solunar.getEventNotification("9:45 AM"));
        assertEquals(majorMessage,"Major disappointment 8:17 PM - 10:17 PM is starting - time to drink!", solunar.getEventNotification("8:17 PM"));
        assertEquals(majorMessage, "Major 8:17 PM - 10:17 PM has ended - all clear!", solunar.getEventNotification("10:17 PM"));
    }
}
