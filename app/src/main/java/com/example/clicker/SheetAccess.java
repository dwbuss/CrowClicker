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
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    int sheetId = 1890696516;
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

                    List<List<Object>> values = getRows();
                    if (values == null || values.isEmpty()) {
                        Log.d(TAG, "No data found.");
                    } else {
                        int counter = 0;
                        for (List row : values) {
                            try {
                                pointBox.put(new Point(row));
                                counter++;
                            } catch (Exception e) {
                                Log.d(TAG, "Invalid Point Row:" + row);
                            }
                        }
                        Log.d(TAG, String.format("Imported %s points", counter));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failure during import.", e);
                }
            }
        });
    }

    public List<List<Object>> getRows() throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();
        return response.getValues();
    }

    String get(List row, int id) {
        try {
            return (String) row.get(id);
        } catch (Exception e) {
            return "";
        }
    }

    public void syncSheet() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) context).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);

                    List<List<Object>> values = getRows();
                    if (values == null || values.isEmpty()) {
                        Log.d(TAG, "No data found.");
                    } else {
                        int counter = 0;
                        List<Point> points = pointBox.query()
                                .build().find();
                        for (List row : values) {
                            try {
                                Point storedPoint = new Point(row);
                                Optional<Point> sheetPoint = points.stream().filter(p -> p.getSheetId() == storedPoint.getSheetId()).findFirst();
                                if (sheetPoint.isPresent())
                                    sheetPoint.get().refresh(row);
                                else
                                    sheetPoint = Optional.of(new Point(row));
                                pointBox.put(sheetPoint.get());
                                counter++;
                            } catch (Exception e) {
                                Log.d(TAG, "Invalid Point Row:" + row);
                            }
                        }
                        Log.d(TAG, String.format("Updated %s points", counter));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failure during sync.", e);
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
                    if (row.isEmpty())
                        Log.d(TAG, "No row found for ID " + point.getSheetId());
                    else {
                        Request request = new Request()
                                .setDeleteDimension(new DeleteDimensionRequest()
                                                            .setRange(new DimensionRange()
                                                                              .setSheetId(sheetId)
                                                                              .setDimension("ROWS")
                                                                              .setStartIndex(Integer.parseInt(row) - 1)
                                                                              .setEndIndex(Integer.parseInt(row))
                                                            )
                                );
                        List<Request> requests = new ArrayList<Request>();
                        requests.add(request);
                        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
                        content.setRequests(requests);
                        Sheets.Spreadsheets.BatchUpdate update = service.spreadsheets().batchUpdate(spreadsheetId, content);
                        update.execute();
                        Log.d(TAG, "Deleted point from row " + row);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failure during deleting row " + row, e);
                }
            }
        });
    }

    public void storePoint(Point point, String lake) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String row = findRow(point);
                    ValueRange body = new ValueRange()
                            .setValues(point.getSheetBody(lake));
                    if (row.isEmpty()) {
                        service.spreadsheets().values()
                                .append(spreadsheetId, sheetName, body)
                                .setValueInputOption("USER_ENTERED")
                                .execute();
                        Log.d(TAG, "Created new row " + point.getSheetBody(lake));
                    } else {
                        service.spreadsheets().values()
                                .update(spreadsheetId, sheetName + "!A" + row, body)
                                .setValueInputOption("USER_ENTERED")
                                .execute();
                        Log.d(TAG, "Updated row " + row);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failure during store " + point.getSheetBody(lake), e);
                }
            }
        });
    }

    private String findRow(Point point) throws IOException {
        String row = "";
        List<List<Object>> values = getRows();
        if (values == null || values.isEmpty()) {
            Log.d(TAG, "No data found." + sheetName);
            return null;
        } else {
            int index = 1;
            for (int i = 0; i < values.size(); i++) {
                if (values.size() > 0 && values.get(i).size() > 0 && ((String) values.get(i).get(0)).equalsIgnoreCase(Long.toString(point.getSheetId()))) {
                    row = Integer.toString(index);
                }
                index++;
            }
        }
        return row;
    }
}
