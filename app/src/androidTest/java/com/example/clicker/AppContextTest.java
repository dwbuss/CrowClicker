package com.example.clicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.clicker.objectbo.Point;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AppContextTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.clicker", appContext.getPackageName());
    }

    @Test
    public void checkMetaDataAvailable() throws PackageManager.NameNotFoundException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bundle metaData = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData;
        assertNotNull(metaData);
        assertNotEquals("MISSING", metaData.getString("com.google.android.maps.v2.API_KEY"));
        assertNotEquals("MISSING", metaData.getString("com.google.api.credentials"));
    }

    @Test
    public void testPointParcelable() throws Exception {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\tThu Sep 02 09:11:00 CDT 2021\tFOLLOW\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
        Point point = new Point(csvRecord);

        Parcel parcel = Parcel.obtain();
        point.writeToParcel(parcel, point.describeContents());
        parcel.setDataPosition(0);
        Point createdFromParcel = Point.CREATOR.createFromParcel(parcel);
        assertEquals(point.toString(), createdFromParcel.toString());
    }
}
