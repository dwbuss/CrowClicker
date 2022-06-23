package com.example.clicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.example.clicker.objectbo.PointListAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    ActivityResultLauncher<Intent> importActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        try {
                            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                            Box<Point> pointBox = boxStore.boxFor(Point.class);

                            InputStream ifile = getContentResolver().openInputStream(data.getData());
                            InputStreamReader is = new InputStreamReader(ifile);
                            BufferedReader bufferedReader = new BufferedReader(is);
                            int counter = 0;
                            if (bufferedReader.ready()) {
                                //skip header
                                bufferedReader.readLine();
                            }
                            while (bufferedReader.ready()) {
                                String line = bufferedReader.readLine();
                                pointBox.put(new Point(line));
                                counter++;
                            }
                            updateCounts();
                            Toast.makeText(getApplicationContext(), "Imported " + counter + " points", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "File Import " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }
            });


    static int convertPoints(Box<Point> pointBox, JSONArray points) throws JSONException {
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
        PointListAdapter pointListAdapter;
        pointListAdapter = new PointListAdapter(this.getApplicationContext());

        ((TextView) findViewById(R.id.dailyCatch)).setText(pointListAdapter.getDailyCatch());
        ((TextView) findViewById(R.id.dailyContact)).setText(pointListAdapter.getDailyContact());
        ((TextView) findViewById(R.id.dailyFollow)).setText(pointListAdapter.getDailyFollow());

        ((TextView) findViewById(R.id.tripCatch)).setText(pointListAdapter.getTripCatch());
        ((TextView) findViewById(R.id.tripContact)).setText(pointListAdapter.getTripContact());
        ((TextView) findViewById(R.id.tripFollow)).setText(pointListAdapter.getTripFollow());

        ((TextView) findViewById(R.id.totalCatch)).setText(pointListAdapter.getTotalCatch());
        ((TextView) findViewById(R.id.totalContact)).setText(pointListAdapter.getTotalContact());
        ((TextView) findViewById(R.id.totalFollow)).setText(pointListAdapter.getTotalFollow());
    }

    public void clearPoints(View view) {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(view.getContext());
        dialogDelete.setTitle("Warning!!");
        dialogDelete.setMessage("Are you sure to delete all points?");
        dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);
                    List<Point> points = pointBox.query().build().find();
                    for (Point p : points) {
                        pointBox.remove(p);
                    }
                    updateCounts();
                    Toast.makeText(getApplicationContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
                dialogInterface.dismiss();
            }
        });
        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogDelete.show();
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

    public void sendMessage(View view) {
        String msg = ((EditText) findViewById(R.id.messageTxt)).getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String value = prefs.getString("Message Notification", "");
        if (!value.isEmpty()) {
            Location loc = getLastKnownLocation();
            String message = msg + "\r\nhttp://maps.google.com/maps?q=" + loc.getLatitude() + "," + loc.getLongitude();
            SmsManager smgr = SmsManager.getDefault();
            Map<String, String> contacts = GET_CONTACT_LIST(Integer.parseInt(value), getContentResolver());
            contacts.forEach((name, number) -> {
                smgr.sendTextMessage(number, null, message, null, null);
                Log.d(TAG, String.format("Message sent to %s ( %s )", name, number));
            });
            Snackbar.make(view, "Message sent to " + contacts.keySet(), Snackbar.LENGTH_LONG).show();
        } else
            Snackbar.make(view, "Group not set.", Snackbar.LENGTH_LONG).show();
    }

    public void exportPoints(View view) {
        try {
            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
            Box<Point> pointBox = boxStore.boxFor(Point.class);

            // File file = new File("/mnt/sdcard/", "points.txt");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "points.csv");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            List<Point> points = pointBox.query().build().find();
            int counter = 0;

            PointListAdapter pointListAdapter;
            pointListAdapter = new PointListAdapter(this.getApplicationContext());

            outputStreamWriter.write("id\tname\tlon\tlat\ttimeStamp\tcontactType\tairTemp\twaterTemp\tbait\tfishSize\tnotes\twindSpeed\twindDir\tcloudCover\tdewPoint\tpressure\thumidity\n");
            for (Point point : points) {
                if (point.getAirTemp().trim().isEmpty()) {
                    Weather weather = new Weather();
                    weather.populate(point.getLat(), point.getLon(), point.getTimeStamp(), getApplicationContext(), new VolleyCallBack() {
                        @Override
                        public void onSuccess() {
                            point.setAirTemp(weather.temperature);
                            point.setDewPoint(weather.dewPoint);
                            point.setWindSpeed(weather.windSpeed);
                            point.setHumidity(weather.humidity);
                            point.setPressure(weather.pressure);
                            point.setCloudCover(weather.cloudCover);
                            point.setWindDir(weather.windDir);
                            pointListAdapter.addOrUpdatePoint(point);
                            pointListAdapter.updatePoints();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(getApplicationContext(), "Failed to locate weather", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                outputStreamWriter.write(point + "\n");
                counter++;
            }
            updateCounts();
            outputStreamWriter.flush();
            outputStreamWriter.close();
            Toast.makeText(getApplicationContext(), "Exported " + counter + " points as " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Export file Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        ActivityResultLauncher<Intent> catchNotificationActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                        }
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
            configureCatchNotificationChoices("Message Notification");
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

