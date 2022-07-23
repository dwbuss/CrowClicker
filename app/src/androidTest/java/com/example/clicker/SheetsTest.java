package com.example.clicker;

import static org.junit.Assert.assertEquals;

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
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//@RunWith(AndroidJUnit4.class)
public class SheetsTest {
    private static final String APPLICATION_NAME = "crowapp-257113";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TAG = "SheetsTest";
    final String spreadsheetId = "1xgnjh0SvHrU44OLXb3z_2PHsIe5AjeCoBEyVE8IRGuo";
    Sheets service;

    @Before
    public void setUp() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bundle metaData = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA).metaData;
        GoogleCredential credentials = GoogleCredential.fromStream(IOUtils.toInputStream(metaData.getString("com.google.api.credentials"), StandardCharsets.UTF_8)).createScoped(SCOPES);
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Ignore
    public void testReadSheets() throws IOException {
        String range = "test";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Log.d(TAG, "No data found.");
        } else {
            int added = 0;
            for (List row : values) {

                try {
                    new Point(row);
                    added++;
                } catch (NumberFormatException x) {
                } catch (Exception e) {
                    Log.e(TAG, String.format("ROW %s %s", row.get(0), row));
                }
            }
            Log.i(TAG, String.format("ROWS: %d",added));
        }
    }

    @Ignore
    public void testWriteSheets() throws IOException {
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList("10", "", "Dan", "54.00", "", "Crow", "09/13/2021", "2:04 PM", "blades", "4", "-10431566.362659,6310076.194329302", "49.20710543", "-93.70835501"),
                        Arrays.asList("11", "", "Tony", "54.00", "", "Crow", "09/13/2021", "2:04 PM", "blades", "4", "-10431566.362659,6310076.194329302", "49.20710543", "-93.70835501")));
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, "test!A10", body)
                .setValueInputOption("RAW")
                .execute();
    }

    @Ignore
    public void testUpdateSheets() throws IOException, ParseException {
        Point point = getPoint("11");
        assertEquals("11", Long.toString(point.getSheetId()));
        assertEquals("54.00", point.getFishSize());
        point.setFishSize("50.50");
        Point storedPoint = storePoint(point);
        assertEquals("50.50", storedPoint.getFishSize());
        point.setFishSize("54.00");
        storedPoint = storePoint(point);
        assertEquals("54.00", storedPoint.getFishSize());
    }

    @Ignore
    public void testCreateAndDeleteSheets() throws IOException, ParseException {
        Point point = getPoint("11");
        assertEquals("11", Long.toString(point.getSheetId()));
        point.setSheetId(5);
        point.setFishSize("50.50");
        Point storedPoint = storePoint(point);
        assertEquals("50.50", storedPoint.getFishSize());
        deletePoint(point);
    }

    private void deletePoint(Point point) throws IOException {
        String row = findRow(point);
        String range = "test!" + row + ":" + row;
        service.spreadsheets().values()
                .clear(spreadsheetId, range, new ClearValuesRequest())
                .execute();
    }

    private String findRow(Point point) throws IOException {
        String row = "";
        String range = "test";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Log.d(TAG, "No data found.");
            return null;
        } else {
            int index = 1;
            for (int i = 0; i < values.size(); i++) {
                if (((String) values.get(i).get(0)).equalsIgnoreCase(Long.toString(point.getSheetId()))) {
                    row = Integer.toString(index);
                }
                index++;
            }
        }
        return row;
    }

    private Point storePoint(Point point) throws IOException, ParseException {
        String row = findRow(point);
        ValueRange body = new ValueRange()
                .setValues(point.getSheetBody("Test"));
        if (row.isEmpty())
            service.spreadsheets().values()
                    .append(spreadsheetId, "test", body)
                    .setValueInputOption("RAW")
                    .execute();
        else
            service.spreadsheets().values()
                    .update(spreadsheetId, "test!A" + row, body)
                    .setValueInputOption("RAW")
                    .execute();
        return getPoint(Long.toString(point.getSheetId()));
    }

    private Point getPoint(String rowId) throws IOException, ParseException {
        String range = "test";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Log.d(TAG, "No data found.");
            return null;
        } else {
            Optional<List<Object>> matched = values.stream().filter(row -> {
                return ((String) row.get(0)).equalsIgnoreCase(rowId);
            }).findFirst();
            return new Point(matched.get());
        }
    }

}
