package com.example.clicker;

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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SheetsTest {
    private static final String APPLICATION_NAME = "crowapp-257113";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\Buss\\AndroidStudioProjects\\CrowClicker\\credentials.json";
    Sheets service;

    final String spreadsheetId = "1xgnjh0SvHrU44OLXb3z_2PHsIe5AjeCoBEyVE8IRGuo";

    @Before
    public void setUp() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = new FileInputStream(new File(CREDENTIALS_FILE_PATH));
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleCredential credentials = GoogleCredential.fromStream(in).createScoped(SCOPES);

        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Test
    @Ignore
    public void testReadSheets() throws IOException, GeneralSecurityException {
        String range = "test!A:AA";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                System.err.println("ROW: " + row);
            }
        }
    }

    @Test
    @Ignore
    public void testWriteSheets() throws IOException {
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList("10", "", "Dan", "54.00", "", "Crow", "9/13/2021", "2:04 PM", "blades", "4", "-10431566.362659,6310076.194329302", "49.20710543", "-93.70835501"),
                        Arrays.asList("11", "", "Tony", "54.00", "", "Crow", "9/13/2021", "2:04 PM", "blades", "4", "-10431566.362659,6310076.194329302", "49.20710543", "-93.70835501")));
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, "test!A10", body)
                .setValueInputOption("RAW")
                .execute();
    }

    @Test
    @Ignore
    public void testUpdateSheets() throws IOException {
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList("10", "", "Dan", "54.00", "", "LOTW", "9/13/2021", "2:04 PM", "blades", "4", "-10431566.362659,6310076.194329302", "49.20710543", "-93.70835501"),
                        Arrays.asList("11", "", "Tony", "24.00", "", "Crow", "9/13/2021", "2:04 PM", "blades", "4", "-10431566.362659,6310076.194329302", "49.20710543", "-93.70835501")));
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, "test!A10", body)
                .setValueInputOption("RAW")
                .execute();
    }

    @Test
    @Ignore
    public void testDeleteSheets() throws IOException {
       service.spreadsheets().values()
                .clear(spreadsheetId, "test!10:11", new ClearValuesRequest())
                .execute();
    }
}
