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
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private final Context context;
    Sheets service;
    String sheetName = "test";
    int sheetId = 1890696516;
    private String token;

    public SheetAccess(Context appContext) {
        this.context = appContext;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Bundle metaData = appContext.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
            GoogleCredential credentials = GoogleCredential.fromStream(IOUtils.toInputStream(metaData.getString("com.google.api.credentials"), StandardCharsets.UTF_8)).createScoped(SCOPES);
            token = credentials.getAccessToken();
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

                    List<List<Object>> values = getRowsFromSpreadSheet();
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

    public List<List<Object>> getRowsFromSpreadSheet() throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, sheetName)
                .execute();
        return response.getValues();
    }

    public void syncSheet() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) context).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);
                    List<List<Object>> spreadSheetRows = getRowsFromSpreadSheet();
                    if (spreadSheetRows == null || spreadSheetRows.isEmpty()) {
                        Log.d(TAG, "No data found.");
                    } else {
                        int counter = 0;
                        List<Point> localPoints = pointBox.query().build().find();
                        localPoints.stream().filter(p -> p.getSheetId() != 0).forEach(p -> pointBox.remove(p.getId()));
                        for (List row : spreadSheetRows) {
                            try {
                                pointBox.put( new Point(row));
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
                if (point.getSheetId() != 0) {
                    try {
                        if (point.getSheetId() == 0)
                            Log.d(TAG, "No row found for ID " + point.getSheetId());
                        else {
                            Request request = new Request()
                                    .setDeleteDimension(new DeleteDimensionRequest()
                                            .setRange(new DimensionRange()
                                                    .setSheetId(sheetId)
                                                    .setDimension("ROWS")
                                                    .setStartIndex((int) (point.getSheetId() - 1))
                                                    .setEndIndex((int) point.getSheetId())
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
            }
        });
    }

    public void storePoint(Point point, String lake, VolleyCallBack callback) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long orgSheetId = point.getSheetId();
                try {
                    if (point.getSheetId() == 0) {
                        if (point.getContactType().equalsIgnoreCase("CATCH")) {
                            ValueRange body = new ValueRange().setValues(point.getSheetBodyWithOutId(lake));
                            // store new row
                            AppendValuesResponse reponse = service.spreadsheets().values()
                                    .append(spreadsheetId, sheetName, body)
                                    .setValueInputOption("USER_ENTERED")
                                    .execute();
                            // returns new row with generated sheetid
                            ValueRange newRow = service.spreadsheets().values()
                                    .get(spreadsheetId, reponse.getUpdates().getUpdatedRange())
                                    .execute();
                            point.setSheetId(Long.parseLong((String) newRow.getValues().get(0).get(0)));
                            Log.d(TAG, "Created new row " + point.getSheetBody(lake));
                        } else {
                            Log.d(TAG, "Can only store catches");
                        }
                    } else {
                        long rowNumber = point.getSheetId();
                        ValueRange body = new ValueRange().setValues(point.getSheetBody(lake));
                        service.spreadsheets().values()
                                .update(spreadsheetId, sheetName + "!A" + rowNumber, body)
                                .setValueInputOption("USER_ENTERED")
                                .execute();
                        Log.d(TAG, "Updated row " + rowNumber);
                    }
                    callback.onSuccess();
                } catch (IOException e) {
                    point.setSheetId(orgSheetId);
                    Log.e(TAG, "Failure during store " + point.getSheetBody(lake), e);
                    callback.onFailure();
                }
            }
        });
    }

    private String printCollection(Collection<?> c) {
        String s = c.stream().map(Object::toString).collect(Collectors.joining(","));
        return String.format("[%s]", s);
    }
}
