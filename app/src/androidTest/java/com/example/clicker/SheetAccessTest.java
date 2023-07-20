package com.example.clicker;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.clicker.objectbo.Point;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shredzone.commons.suncalc.MoonPosition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SheetAccessTest {

    private SheetAccess access;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        access = new SheetAccess(appContext);
    }

    @Test
    public void canCreateSheetAccess() {
        assertNotNull(access);
    }

    @Test
    @Ignore
    public void canGetAllRows() throws IOException {
        String TAG = "SheetAccess";
        String APPLICATION_NAME = "crowapp-257113";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        final String spreadsheetId = "1xgnjh0SvHrU44OLXb3z_2PHsIe5AjeCoBEyVE8IRGuo";
        Sheets service = null;
        try {
            Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Bundle metaData = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData;
            GoogleCredential credentials = GoogleCredential.fromStream(IOUtils.toInputStream(metaData.getString("com.google.api.credentials"), StandardCharsets.UTF_8)).createScoped(SCOPES);

            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Failure to create SheetAccess", e);
        }
        List<List<Object>> rows = access.getRowsFromSpreadSheet("Pewaukee");
        for (List row : rows) {
            try {
                Point point = new Point(row);
                if (!point.getName().equalsIgnoreCase("label") && !((String) row.get(7)).trim().isEmpty()) {
                    System.err.println("********** Updating point " + point.getSheetId() + " " + point.getTimeStamp());
                    ValueRange body = new ValueRange().setValues(point.getSheetBody(row));
                    System.err.println(body);
                    String rowNumber = access.findRowNumberFromSpreadSheetForPointBySheetId(point);
                    service.spreadsheets().values()
                            .update(spreadsheetId, "Pewaukee!A" + rowNumber, body)
                            .setValueInputOption("USER_ENTERED")
                            .execute();
                }
            } catch (Exception e) {
            }
        }
        assertTrue("We should have more than 500 rows returned.", rows.size() > 500);
        assertEquals("First row should have these columns.", 27, rows.get(0).size());
    }

    @Test
    @Ignore
    public void updateMoonDistance() throws Exception {
        String TAG = "SheetAccess";
        String APPLICATION_NAME = "crowapp-257113";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        final String spreadsheetId = "1xgnjh0SvHrU44OLXb3z_2PHsIe5AjeCoBEyVE8IRGuo";
        Sheets service = null;
        try {
            Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Bundle metaData = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData;
            GoogleCredential credentials = GoogleCredential.fromStream(IOUtils.toInputStream(metaData.getString("com.google.api.credentials"), StandardCharsets.UTF_8)).createScoped(SCOPES);

            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Failure to create SheetAccess", e);
        }
        List<List<Object>> rows = access.getRowsFromSpreadSheet("Data");
        Calendar cal = Calendar.getInstance(Locale.US);
        int updatedRows = 0;
        for (int i = 0; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            int updateRow = i + 1;
            try {
                if (updatedRows > 59) {
                    Log.i(TAG, "Update limit exceeded, sleeping ");
                    Thread.sleep(60000);
                    updatedRows = 0;
                }
                if (!((String) row.get(0)).equalsIgnoreCase("Row") && !((String) row.get(2)).equalsIgnoreCase("label") && !((String) row.get(2)).isEmpty()) {

                    Point point = new Point(row);
                    cal.setTime(point.getTimeStamp());

                    double distance = MoonPosition.compute()
                            .at(-89.1627949, 46.0258947)
                            .on(cal.getTime())
                            .execute().getDistance();

                    ValueRange body = new ValueRange().setValues(List.of(List.of((long) distance)));
                    String range = "Data!AD" + updateRow;
                    service.spreadsheets().values()
                            .update(spreadsheetId, range, body)
                            .setValueInputOption("USER_ENTERED")
                            .execute();
                    Log.i(TAG, "Updated row " + updateRow);
                    updatedRows++;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to update row " + updateRow, e);
            }
        }
    }

    @Ignore
    public void canRowGivenSheetId() throws Exception {
        Point paulIn2022 = new Point(222, "Paul", "CATCH", -93.82745682, 49.22014097, "BUCKTAIL", "CROW");
        paulIn2022.setSheetId(1618);
        String row = access.findRowNumberFromSpreadSheetForPointBySheetId(paulIn2022);
        assertEquals("1060", row);
    }
}
