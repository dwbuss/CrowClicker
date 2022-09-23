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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        access = new SheetAccess(appContext);
    }

    @Test
    public void canCreateSheetAccess() {
        assertNotNull(access);
    }

    @Test
    public void canGetAllRows() throws IOException {
        String TAG = "SheetAccess";
        String APPLICATION_NAME = "crowapp-257113";
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        final String spreadsheetId = "1xgnjh0SvHrU44OLXb3z_2PHsIe5AjeCoBEyVE8IRGuo";
        Context context;
        Sheets service = null;
        String sheetName = "test";
        int sheetId = 1890696516;
        String token;
        try {
            Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Bundle metaData = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData;
            GoogleCredential credentials = GoogleCredential.fromStream(IOUtils.toInputStream(metaData.getString("com.google.api.credentials"), StandardCharsets.UTF_8)).createScoped(SCOPES);
            token = credentials.getAccessToken();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Failure to create SheetAccess", e);
        }
        List<List<Object>> rows = access.getRowsFromSpreadSheet();
        for (List row : rows) {
            try {
                Point point = new Point(row);
                if (!point.getName().equalsIgnoreCase("label") && !((String) row.get(7)).trim().isEmpty()) {
                    System.err.println("********** Updating point " + point.getSheetId() + " " + point.getTimeStamp());
                    ValueRange body = new ValueRange().setValues(point.getSheetBody(row));
                    System.err.println(body);
                    String rowNumber = access.findRowNumberFromSpreadSheetForPointBySheetId(point);
                    service.spreadsheets().values()
                            .update(spreadsheetId, sheetName + "!A" + rowNumber, body)
                            .setValueInputOption("USER_ENTERED")
                            .execute();
                }
            } catch (Exception e) {
            }
        }
        assertTrue("We should have more than 500 rows returned.", rows.size() > 500);
        assertEquals("First row should have these columns.", 27, rows.get(0).size());
    }

    @Ignore
    public void canRowGivenSheetId() throws Exception {
        Point paulIn2022 = new Point(222, "Paul", "48", "07-08-2022 07:48 PM", -93.82745682, 49.22014097);
        paulIn2022.setSheetId(1618);
        String row = access.findRowNumberFromSpreadSheetForPointBySheetId(paulIn2022);
        assertEquals("1060", row);
    }
}
