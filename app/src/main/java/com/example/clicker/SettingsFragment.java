package com.example.clicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.LinkedList;
import java.util.List;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";
    private boolean isScanning = false;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference versionPreference = findPreference("pref_version");
        String versionName = BuildConfig.VERSION_NAME;
        versionPreference.setSummary(String.format(versionPreference.getSummary().toString(), versionName));

        findPreference("scan_for_buttons").setOnPreferenceClickListener(preference -> scanForButtons());
        findPreference("advanced").setOnPreferenceClickListener(preference -> {
            Intent transfer = new Intent(getActivity(), TransferActivity.class);
            startActivity(transfer);
            return true;
        });
        Preference pref = findPreference("manage_data_storage");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            pref.setVisible(true);
            pref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return true;
            });
        } else {
            pref.setVisible(false);
        }
        Preference notifications = findPreference("manage_notifications");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            notifications.setVisible(true);
            notifications.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                startActivity(intent);
                return true;
            });
        } else {
            notifications.setVisible(false);
        }
        configureCatchNotificationChoices("Catch Notification");
        configureCatchNotificationChoices("Follow Notification");
        configureCatchNotificationChoices("Lost Notification");
    }

    public boolean scanForButtons() {
        if (isScanning) {
            Flic2Manager.getInstance().stopScan();
            isScanning = false;
        } else {
            if (Build.VERSION.SDK_INT < 31 || getActivity().getApplicationInfo().targetSdkVersion < 31) {
                if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationPermissionRequest.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION});
                    Log.d(TAG, "Failure to get ACCESS_FINE_LOCATION permission.");
                    return false;
                }
            } else {
                if (getContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                        getContext().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    getActivity().requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    Log.d(TAG, "Failure to get BLUETOOTH_SCAN or BLUETOOTH_CONNECT permissions");
                    return false;
                }
            }
            Toast.makeText(getActivity(), "Press and hold down your Clicker until it connects.", Toast.LENGTH_SHORT).show();
            isScanning = true;

            Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
                @Override
                public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                    Toast.makeText(getActivity(), "Found an already paired clicker. Try another clicker.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDiscovered(String bdAddr) {
                    Toast.makeText(getActivity(), "Found clicker, now connecting...", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onConnected() {
                    Toast.makeText(getActivity(), "Connected. Now pairing...", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAskToAcceptPairRequest() {
                    Toast.makeText(getActivity(), "Please press \"Pair & Connect\" in the system dialog...", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onComplete(int result, int subCode, Flic2Button button) {
                    isScanning = false;

                    if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                        Toast.makeText(getActivity(), "Scan success!  You will need to stop and restart the application.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Scan failed with code " + Flic2Manager.errorCodeToString(result), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        return true;
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

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                                              .RequestMultiplePermissions(), result -> {
                                          Boolean fineLocationGranted = result.getOrDefault(
                                                  Manifest.permission.ACCESS_FINE_LOCATION, false);
                                          if (fineLocationGranted != null && fineLocationGranted) {
                                              scanForButtons();
                                          } else
                                              Toast.makeText(getActivity(), "Scanning needs permissions for FINE accurace locations, which you have rejected", Toast.LENGTH_LONG).show();
                                      }
            );


}
