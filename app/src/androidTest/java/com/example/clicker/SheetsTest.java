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
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SheetsTest {
    private static final String APPLICATION_NAME = "crowapp-257113";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
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

    @Test
    public void testReadSheets() throws IOException, GeneralSecurityException {
        String range = "Data";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            int added = 0;
            for (List row : values) {

                try {
                    new Point(row);
                    added++;
                } catch (NumberFormatException x) {
                } catch (Exception e) {
                    System.err.println("ROW " + row.get(0) + " " + row);
                    e.printStackTrace();
                }
            }
            System.err.println("ROWS: " + added);
        }
    }

    @Test
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
    public void testDeleteSheets() throws IOException {
        service.spreadsheets().values()
                .clear(spreadsheetId, "test!10:11", new ClearValuesRequest())
                .execute();
    }
}
