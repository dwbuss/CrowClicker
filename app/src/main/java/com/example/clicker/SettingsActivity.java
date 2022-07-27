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

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.IOException;
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

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

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
            findPreference("advanced").setOnPreferenceClickListener(preference -> {
                Intent transfer = new Intent(getActivity(), TransferActivity.class);
                startActivity(transfer);
                return true;} );
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

