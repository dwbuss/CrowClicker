package com.example.clicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    ActivityResultLauncher<Intent> importFromTSVActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    PointsHelper helper = new PointsHelper(getApplicationContext());
                    try ( Reader reader = new InputStreamReader(getContentResolver().openInputStream(data.getData())))
                    {
                        List<String> lines = IOUtils.readLines(reader);
                        lines.remove(0);
                        for (String line : lines) {
                            Log.d(TAG, line);
                            helper.addOrUpdatePoint(new Point(line));
                        }
                        updateCounts();
                        Toast.makeText(getApplicationContext(), String.format("Imported %d points", lines.size()), Toast.LENGTH_SHORT).show();
                    } catch (IOException | ParseException e) {
                        Toast.makeText(getApplicationContext(), "File Import " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failure to import from CSV", e);
                    }
                }
            });

    ActivityResultLauncher<Intent> exportToTSVActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        List<Point> points = new PointsHelper(getApplicationContext()).getAll();
                        int counter = 0;

                        try (OutputStreamWriter os = new OutputStreamWriter(SettingsActivity.this.getContentResolver().openOutputStream(result.getData().getData()))) {
                            os.write(Point.CSV_HEADER());
                            for (Point point : points) {
                                if (!point.getName().trim().equals("label")) {
                                    os.write(point + "\n");
                                    counter++;
                                }
                            }
                        }
                        catch (IOException e) {
                            Log.e(TAG, "Failure writing out file of applications.", e);
                            Toast.makeText(SettingsActivity.this, "Failure exporting applications.", Toast.LENGTH_LONG).show();
                        }
                        finally {
                            Toast.makeText(SettingsActivity.this, String.format("Exported %d points.", counter), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    static int convertPoints(Box<Point> pointBox, JSONArray points) throws ParseException, JSONException {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("epsg:3857");
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("epsg:4326");
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform toWgs84 = ctFactory.createTransform(epsg3857, wgs84);
        ProjCoordinate southWestInWgs84 = new ProjCoordinate();

        int counter = 0;
        for (int i = 0, size = points.length(); i < size; i++) {
            JSONObject point = points.getJSONObject(i);
            String name = point.getJSONObject("properties").getString("name");
            String length = point.getJSONObject("properties").getString("size");
            String date = point.getJSONObject("properties").getString("date");
            double lon = point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
            double lat = point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);

            toWgs84.transform(new ProjCoordinate(lon, lat), southWestInWgs84);
            Double newLat = southWestInWgs84.y;
            Double newLon = southWestInWgs84.x;
            Point pt = new Point(0, name, length, date, newLon, newLat);
            if (pointBox != null)
                pointBox.put(pt);
            else
                Log.d(TAG, pt.toString());
            counter++;
        }
        return counter;
    }

    @SuppressLint("Range")
    static final Map<String, String> GET_CONTACT_LIST(int groupID, final ContentResolver contentResolver) {
        LinkedHashMap<String, String> contactList = new LinkedHashMap<String, String>();
        Uri groupURI = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};

        Cursor c = contentResolver.query(
                groupURI,
                projection,
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                        + "=" + groupID, null, null);

        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
            Cursor pCur = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);

            while (pCur.moveToNext()) {
                String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                contactList.putIfAbsent(name, phone);
            }
            pCur.close();
        }
        return contactList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        updateCounts();
    }

    private void updateCounts() {
        PointsHelper pointsHelper;
        pointsHelper = new PointsHelper(this.getApplicationContext());

        ((TextView) findViewById(R.id.dailyCatch)).setText(pointsHelper.getDailyCatch());
        ((TextView) findViewById(R.id.dailyContact)).setText(pointsHelper.getDailyContact());
        ((TextView) findViewById(R.id.dailyFollow)).setText(pointsHelper.getDailyFollow());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));

        ((TextView) findViewById(R.id.tripCatch)).setText(pointsHelper.getTripCatch(tripLength));
        ((TextView) findViewById(R.id.tripContact)).setText(pointsHelper.getTripContact(tripLength));
        ((TextView) findViewById(R.id.tripFollow)).setText(pointsHelper.getTripFollow(tripLength));

        ((TextView) findViewById(R.id.totalCatch)).setText(pointsHelper.getTotalCatch());
        ((TextView) findViewById(R.id.totalContact)).setText(pointsHelper.getTotalContact());
        ((TextView) findViewById(R.id.totalFollow)).setText(pointsHelper.getTotalFollow());
    }

    public void clearPoints(View view) {
        AlertDialog dialogDelete = new AlertDialog.Builder(view.getContext())
                .setTitle("Warning!!")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Are you sure to delete all points?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    PointsHelper helper = new PointsHelper(getApplicationContext());
                    helper.clearPoints();
                    updateCounts();
                    Toast.makeText(getApplicationContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public void importPoints(View view) {
        SheetAccess sheet = new SheetAccess(getApplicationContext());
        sheet.syncSheet();
        Toast.makeText(this, "Background sync started.", Toast.LENGTH_LONG).show();
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void importFromTSV(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile.putExtra(Intent.EXTRA_TITLE, "points.tsv");
        chooseFile = Intent.createChooser(chooseFile, "Select Import TSV File");
        importFromTSVActivity.launch(chooseFile);
    }

    public void exportPointsToTSV(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        chooseFile.setType("text/tsv");
        chooseFile.putExtra(Intent.EXTRA_TITLE, "points.tsv");
        chooseFile = Intent.createChooser(chooseFile, "Select Export TSV File");
        exportToTSVActivity.launch(chooseFile);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        ActivityResultLauncher<Intent> catchNotificationActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                    }
                });
        private boolean isScanning = false;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            findPreference("scan_for_buttons").setOnPreferenceClickListener(preference -> scanForButtons());
            configureCatchNotificationChoices("Catch Notification");
            configureCatchNotificationChoices("Follow Notification");
            configureCatchNotificationChoices("Lost Notification");
        }

        private void configureCatchNotificationChoices(String key) {
            ListPreference contactGroups = findPreference(key);
            List<String> entries = new LinkedList<String>();
            List<String> values = new LinkedList<String>();

            entries.add("NONE");
            values.add("");

            Uri uri = ContactsContract.Groups.CONTENT_URI;
            Log.d(TAG, "URI: " + uri);
            String[] projection = new String[]{
                    ContactsContract.Groups._ID,
                    ContactsContract.Groups.TITLE,
                    ContactsContract.Groups.ACCOUNT_TYPE
            };
            //Loader<Cursor> loader = new CursorLoader(getContext(),  uri, projection, null, null, null);
            Cursor results = getActivity().getContentResolver().query(uri, projection, null, null, null);
            while (results.moveToNext()) {
                if (results.getString(2).equals("com.google")) {
                    entries.add(results.getString(1));
                    values.add(results.getString(0));
                }
            }
            contactGroups.setEntries(entries.toArray(new CharSequence[entries.size()]));
            contactGroups.setEntryValues(values.toArray(new CharSequence[entries.size()]));
        }

        @TargetApi(31)
        private boolean scanForButtons() {
            FragmentActivity activity = requireActivity();
            if (isScanning) {
                Flic2Manager.getInstance().stopScan();
                isScanning = false;
            } else {
                if (Build.VERSION.SDK_INT < 31 || activity.getApplicationInfo().targetSdkVersion < 31) {
                    if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        Log.d(TAG, "Failure to get ACCESS_FINE_LOCATION permission.");
                        return false;
                    }
                } else {
                    if (getContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                            getContext().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        activity.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);
                        Log.d(TAG, "Failure to get BLUETOOTH_SCAN or BLUETOOTH_CONNECT permissions");
                        return false;
                    }
                }
                Toast.makeText(activity, "Press and hold down your Clicker until it connects.", Toast.LENGTH_SHORT).show();
                isScanning = true;

                Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
                    @Override
                    public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                        Toast.makeText(getContext(), "Found an already paired clicker. Try another clicker.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDiscovered(String bdAddr) {
                        Toast.makeText(getContext(), "Found clicker, now connecting...", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onConnected() {
                        Toast.makeText(getContext(), "Connected. Now pairing...", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAskToAcceptPairRequest() {
                        Toast.makeText(getContext(), "Please press \"Pair & Connect\" in the system dialog...", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete(int result, int subCode, Flic2Button button) {
                        isScanning = false;

                        if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                            Toast.makeText(getContext(), "Scan success!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Scan failed with code " + Flic2Manager.errorCodeToString(result), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            return true;
        }
    }
}

