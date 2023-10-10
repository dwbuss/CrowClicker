package com.example.clicker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;

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
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
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

    public void importSheet(String lake) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) context).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);

                    List<List<Object>> values = getRowsFromSpreadSheet(lake);
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


    public void getSheets(ListPreference lakes) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
                    List<Sheet> sheets = sp.getSheets();
                    List<String> sheetNames = sheets.stream().map(sheet -> sheet.getProperties().getTitle()).collect(Collectors.toList());

                    List<String> entries = new LinkedList<String>();
                    List<String> values = new LinkedList<String>();
                    sheetNames.forEach(sheet -> {
                        if (!sheet.equalsIgnoreCase("data") &&
                                !sheet.equalsIgnoreCase("test")) {
                            entries.add(sheet);
                            values.add(sheet);
                        }
                    });

                    lakes.setEntries(entries.toArray(new CharSequence[entries.size()]));
                    lakes.setEntryValues(values.toArray(new CharSequence[entries.size()]));

                } catch (Exception e) {
                }
            }
        });
    }

    public List<List<Object>> getRowsFromSpreadSheet(String lake) throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, lake)
                .execute();
        return response.getValues();
    }

    //used to fix moon phase logic on all data.
    public void updateMoonOnAllRows(String lake) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<List<Object>> rows = getRowsFromSpreadSheet(lake);
                    Calendar cal = Calendar.getInstance(Locale.US);
                    Solunar solunar = new Solunar();
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

                                String stringLat = (String) row.get(11);
                                String stringLon = (String) row.get(12);
                                if (stringLat.isEmpty() || stringLon.isEmpty()) {
                                    row.set(12, -93.863248);
                                    row.set(11, 49.217314);
                                }
                                Point point = new Point(row);
                                cal.setTime(point.getTimeStamp());
                                String orgPhase = (String) row.get(24);
                                solunar.populate(point.getLon(), point.getLat(), cal);
                                row.set(24, solunar.moonPhase);
                                if (row.size() > 25) {
                                    row.set(25, Boolean.toString(solunar.isMajor));
                                    row.set(26, Boolean.toString(solunar.isMinor));
                                }
                                if (!orgPhase.equalsIgnoreCase(solunar.moonPhase)) {
                                    ValueRange body = new ValueRange().setValues(List.of(row));
                                    service.spreadsheets().values()
                                            .update(spreadsheetId, point.getLake() + "!A" + updateRow, body)
                                            .setValueInputOption("USER_ENTERED")
                                            .execute();
                                    Log.i(TAG, "Updated row " + updateRow);
                                    updatedRows++;
                                } else {
                                    Log.i(TAG, "No change in moon for row " + updateRow);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to update row " + updateRow, e);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failure during update.", e);
                }
            }
        });
    }

    public void syncSheet(String lake) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) context).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);
                    List<List<Object>> spreadSheetRows = getRowsFromSpreadSheet(lake);
                    if (spreadSheetRows == null || spreadSheetRows.isEmpty()) {
                        Log.d(TAG, "No data found.");
                    } else {
                        int counter = 0;
                        List<Point> localPoints = pointBox.query().build().find();
                        localPoints.stream().filter(p -> p.getSheetId() != 0 && p.getLake().equalsIgnoreCase(lake)).forEach(p -> pointBox.remove(p.getId()));
                        for (List row : spreadSheetRows) {
                            try {
                                pointBox.put(new Point(row));
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
                        int rowNumber = Integer.parseInt(findRowNumberFromSpreadSheetForPointBySheetId(point));
                        Request request = new Request()
                                .setDeleteDimension(new DeleteDimensionRequest()
                                                            .setRange(new DimensionRange()
                                                                              .setSheetId(Integer.parseInt(getSheetId(point.getLake())))
                                                                              .setDimension("ROWS")
                                                                              .setStartIndex(rowNumber - 1)
                                                                              .setEndIndex(rowNumber)
                                                            )
                                );
                        List<Request> requests = new ArrayList<Request>();
                        requests.add(request);
                        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
                        content.setRequests(requests);
                        Sheets.Spreadsheets.BatchUpdate update = service.spreadsheets().batchUpdate(spreadsheetId, content);
                        update.execute();
                        Log.d(TAG, "Deleted point from row " + row);

                    } catch (IOException e) {
                        Log.e(TAG, "Failure during deleting row " + row, e);
                    }
                }
            }
        });
    }

    private String getSheetId(String lake) throws IOException {
        AtomicReference<Integer> sheetId = new AtomicReference<>(0);
        Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sp.getSheets();
        sheets.stream().forEach(sheet -> {
            if (sheet.getProperties().getTitle().equalsIgnoreCase(lake))
                sheetId.set(sheet.getProperties().getSheetId());
        });
        return Integer.toString(sheetId.get());
    }

    public void storePoint(Point point, ClickerCallback callback) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long orgSheetId = point.getSheetId();
                try {
                    String rowNumber = findRowNumberFromSpreadSheetForPointBySheetId(point);
                    if (point.getSheetId() == 0 || rowNumber.isEmpty()) {
                        if (point.getContactType().equalsIgnoreCase("CATCH")) {
                            ValueRange body = new ValueRange().setValues(point.getSheetBodyWithOutId());
                            // store new row
                            AppendValuesResponse reponse = service.spreadsheets().values()
                                    .append(spreadsheetId, point.getLake(), body)
                                    .setValueInputOption("USER_ENTERED")
                                    .execute();
                            // returns new row with generated sheetid
                            ValueRange newRow = service.spreadsheets().values()
                                    .get(spreadsheetId, reponse.getUpdates().getUpdatedRange())
                                    .execute();
                            point.setSheetId(Long.parseLong((String) newRow.getValues().get(0).get(0)));
                            Log.d(TAG, "Created new row " + point.getSheetBody());
                        } else {
                            Log.d(TAG, "Can only store catches");
                        }
                    } else {
                        ValueRange body = new ValueRange().setValues(point.getSheetBody());
                        service.spreadsheets().values()
                                .update(spreadsheetId, point.getLake() + "!A" + rowNumber, body)
                                .setValueInputOption("USER_ENTERED")
                                .execute();
                        Log.d(TAG, "Updated row " + rowNumber);
                    }
                    callback.onSuccess();
                } catch (IOException e) {
                    point.setSheetId(orgSheetId);
                    Log.e(TAG, "Failure during store " + point.getSheetBody(), e);
                    callback.onFailure();
                }
            }
        });
    }

    String findRowNumberFromSpreadSheetForPointBySheetId(Point point) throws IOException {
        String row = "";
        List<List<Object>> values = getRowsFromSpreadSheet(point.getLake());
        if (values == null || values.isEmpty()) {
            Log.d(TAG, "No data found." + point.getLake());
            return null;
        } else {
            for (int i = values.size() - 1; i >= 0; i--) {
                Log.d(TAG, "Checking " + i);
                if (values.get(i).size() > 0 && ((String) values.get(i).get(0)).equalsIgnoreCase(Long.toString(point.getSheetId()))) {
                    row = Integer.toString(i + 1);
                    break;
                }
            }
        }
        return row;
    }
}
