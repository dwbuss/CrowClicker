package com.example.clicker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.clicker.objectbo.Point;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class SheetAccess {

    private static final String TAG = "SheetAccess";
    private static final String APPLICATION_NAME = "crowapp-257113";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    final String spreadsheetId = "1xgnjh0SvHrU44OLXb3z_2PHsIe5AjeCoBEyVE8IRGuo";
    Sheets service;
    String sheetName = "test";
    Context context;

    public SheetAccess(Context appContext) {
        try {
            this.context = appContext;
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Bundle metaData = appContext.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
            GoogleCredential credentials = GoogleCredential.fromStream(IOUtils.toInputStream(metaData.getString("com.google.api.credentials"), StandardCharsets.UTF_8)).createScoped(SCOPES);
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Failure to create SheetAccess", e);
        }
    }

    public void importSheet() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) context).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);

                    ValueRange response = service.spreadsheets().values()
                            .get(spreadsheetId, sheetName)
                            .execute();
                    List<List<Object>> values = response.getValues();
                    if (values == null || values.isEmpty()) {
                        Log.d(TAG, "No data found.");
                    } else {
                        int counter = 0;
                        for (List row : values) {
                            try {
                                pointBox.put(new Point(row));
                                counter++;
                            } catch (Exception e) {
                                Log.d(TAG, String.format("Invalid Point at row %s", row.get(0)));
                            }
                            Log.d(TAG, String.format("Imported %s points", Integer.toString(counter)));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failure during import.", e);
                }
            }
        });
    }

    public void deletePoint(Point point) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String row = "";
                try {
                    row = findRow(point);
                    String range = sheetName + "!" + row + ":" + row;
                    service.spreadsheets().values()
                            .clear(spreadsheetId, range, new ClearValuesRequest())
                            .execute();
                    Log.d(TAG, "Deleted point from row " + row);
                } catch (IOException e) {
                    Log.e(TAG, "Failure during deleting row " + row, e);
                }
            }
        });
    }

    public void storePoint(Point point) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String row = findRow(point);
                    ValueRange body = new ValueRange()
                            .setValues(point.getSheetBody());
                    if (row.isEmpty()) {
                        service.spreadsheets().values()
                                .append(spreadsheetId, sheetName, body)
                                .setValueInputOption("RAW")
                                .execute();
                        Log.d(TAG, "Created new row");
                    } else {
                        service.spreadsheets().values()
                                .update(spreadsheetId, sheetName + "!A" + row, body)
                                .setValueInputOption("RAW")
                                .execute();
                        Log.d(TAG, "Updated row " + row);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failure during store.", e);
                }
            }
        });
    }

    private String findRow(Point point) throws IOException {
        String row = "";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Log.d(TAG, "No data found." + sheetName);
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
}
