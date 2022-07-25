package com.example.clicker;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.clicker.objectbo.Point;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SheetAccessTest {

    private SheetAccess access;

    @Before
    public void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        access = new SheetAccess(appContext);
    }

    @Test
    public void canCreateSheetAccess() {
        assertNotNull(access);
    }

    @Test
    public void canGetAllRows() throws IOException {
        List<List<Object>> rows = access.getRowsFromSpreadSheet();
        assertTrue("We should have more than 500 rows returned.", rows.size() > 500);
        assertEquals("First row should have these columns.", 27, rows.get(0).size());
    }
}
