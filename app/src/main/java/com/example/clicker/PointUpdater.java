package com.example.clicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;
import com.google.android.gms.maps.model.Marker;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class PointUpdater {

    private final Context activity;
    private final SheetAccess sheets;
    private final PointsHelper pointsHelper;
    private String TAG = "PointUpdater";

    public PointUpdater(Context context, PointsHelper adapter, SheetAccess sheets) {
        this.activity = context;
        this.pointsHelper = adapter;
        this.sheets = sheets;
    }

    public void showDialogUpdate(final Point point, final Marker marker) {
        final Dialog dialog = new Dialog(activity);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        dialog.setContentView(R.layout.update_dialog);

        ((EditText) dialog.findViewById(R.id.name)).setText(point.getName());

        Spinner contactType = dialog.findViewById(R.id.contactType);
        String[] contactTypes = activity.getResources().getStringArray(R.array.contact_array);
        ArrayAdapter<String> contactAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, contactTypes);
        contactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactType.setAdapter(contactAdapter);
        contactType.setSelection(Arrays.asList(contactTypes).indexOf(point.getContactType()));

        String timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp());
        ((TextView) dialog.findViewById(R.id.timeStamp)).setText(timeStamp);

        Spinner baitEntry = dialog.findViewById(R.id.bait);
        String[] baits = activity.getResources().getStringArray(R.array.bait_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, baits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        baitEntry.setAdapter(adapter);
        baitEntry.setSelection(Arrays.asList(baits).indexOf(point.getBait()));

        ((EditText) dialog.findViewById(R.id.fishSize)).setText(point.getFishSize());
        ((TextView) dialog.findViewById(R.id.airtemp)).setText(point.getAirTemp());
        ((EditText) dialog.findViewById(R.id.watertemp)).setText(point.getWaterTemp());
        ((TextView) dialog.findViewById(R.id.windSpeed)).setText(point.getWindSpeed());
        ((TextView) dialog.findViewById(R.id.windDir)).setText(point.getWindDir());
        ((TextView) dialog.findViewById(R.id.cloudCover)).setText(point.getCloudCover());
        ((TextView) dialog.findViewById(R.id.dewPoint)).setText(point.getDewPoint());
        ((TextView) dialog.findViewById(R.id.pressure)).setText(point.getPressure());
        ((TextView) dialog.findViewById(R.id.humidity)).setText(point.getHumidity());
        ((TextView) dialog.findViewById(R.id.notes)).setText(point.getNotes());

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.95);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(view -> dialog.dismiss());
        Button btnPush = dialog.findViewById(R.id.btnPush);
        btnPush.setOnClickListener(view -> {
            if (point.getSheetId() <= 0) {
                point.setSheetId(point.getId());
            }
            savePoint(point, dialog);
            sheets.storePoint(point, prefs.getString("Lake", ""));
        });
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(view -> {
            AlertDialog.Builder dialogDelete = new AlertDialog.Builder(activity);
            dialogDelete.setTitle("Warning!!");
            dialogDelete.setMessage("Are you sure to delete this point?");
            dialogDelete.setPositiveButton("Yes", (dialogInterface, i) -> {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) activity.getApplicationContext()).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);
                    pointBox.remove(point);
                    if (marker != null) marker.remove();

                    sheets.deletePoint(point);
                    Toast.makeText(activity, "Delete successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                dialogInterface.dismiss();
                dialog.dismiss();
            });
            dialogDelete.setNegativeButton("Cancel", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                dialog.dismiss();
            });
            dialogDelete.show();
        });

        Button btnSave = dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> savePoint(point, dialog));
        Button weatherUpdate = dialog.findViewById(R.id.btnWeather);
        weatherUpdate.setOnClickListener(view -> {
            try {
                final Weather weather = new Weather();
                weather.populate(point.getLat(), point.getLon(), point.getTimeStamp(), activity, new VolleyCallBack() {
                    @Override
                    public void onSuccess() {
                        point.setAirTemp(weather.temperature);
                        point.setDewPoint(weather.dewPoint);
                        point.setWindSpeed(weather.windSpeed);
                        point.setHumidity(weather.humidity);
                        point.setPressure(weather.pressure);
                        point.setCloudCover(weather.cloudCover);
                        point.setWindDir(weather.windDir);
                        point.setWindGust(weather.windGust);
                        point.setPrecipProbability(weather.precipProbability);
                        pointsHelper.addOrUpdatePoint(point);
                    }

                    @Override
                    public void onFailure() {
                    }
                });
                ((EditText) dialog.findViewById(R.id.watertemp)).setText(point.getWaterTemp());
            } catch (Exception error) {
                Log.e("Update Weather error", error.getMessage());
            }
        });
    }

    private void savePoint(Point point, Dialog dialog) {
        try {
            point.setName(((EditText) dialog.findViewById(R.id.name)).getText().toString().trim());
            point.setContactType(((Spinner) dialog.findViewById(R.id.contactType)).getSelectedItem().toString().trim());
            String timeStampStr = ((EditText) dialog.findViewById(R.id.timeStamp)).getText().toString().trim();

            Date timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").parse(timeStampStr);
            point.setTimeStamp(new Timestamp(timeStamp.getTime()));
            point.setBait(((Spinner) dialog.findViewById(R.id.bait)).getSelectedItem().toString().trim());
            String fishSize = ((EditText) dialog.findViewById(R.id.fishSize)).getText().toString().trim();
            if (!fishSize.isEmpty())
                point.setFishSize(String.format("%.2f", Double.parseDouble(fishSize)));
            point.setAirTemp(((TextView) dialog.findViewById(R.id.airtemp)).getText().toString().trim());
            point.setWaterTemp(((EditText) dialog.findViewById(R.id.watertemp)).getText().toString().trim());
            point.setWindSpeed(((TextView) dialog.findViewById(R.id.windSpeed)).getText().toString().trim());
            point.setWindDir(((TextView) dialog.findViewById(R.id.windDir)).getText().toString().trim());
            point.setCloudCover(((TextView) dialog.findViewById(R.id.cloudCover)).getText().toString().trim());
            point.setDewPoint(((TextView) dialog.findViewById(R.id.dewPoint)).getText().toString().trim());
            point.setPressure(((TextView) dialog.findViewById(R.id.pressure)).getText().toString().trim());
            point.setHumidity(((TextView) dialog.findViewById(R.id.humidity)).getText().toString().trim());
            point.setNotes(((EditText) dialog.findViewById(R.id.notes)).getText().toString().trim());
            boolean notify = ((CheckBox) dialog.findViewById(R.id.notify)).isChecked();
            storeAndNotify(point, notify);
            dialog.dismiss();
            Toast.makeText(activity.getApplicationContext(), "Save Successful", Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Log.e("Update error", error.getMessage(), error);
        }
    }

    void storeAndNotify(Point point, boolean notify) {
        BoxStore boxStore = ((ObjectBoxApp) activity.getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        pointBox.put(point);
        if (notify) {
            sendMessage(point.getMessage(), point.getContactType());
        }
    }

    private void sendMessage(String message, String action) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String notification = "";
        if (action.equalsIgnoreCase("CATCH"))
            notification = prefs.getString("Catch Notification", "");
        if (action.equalsIgnoreCase("FOLLOW"))
            notification = prefs.getString("Follow Notification", "");
        if (action.equalsIgnoreCase("CONTACT"))
            notification = prefs.getString("Lost Notification", "");
        if (!notification.isEmpty()) {
            SmsManager smgr = SmsManager.getDefault();
            Map<String, String> contacts = SettingsActivity.GET_CONTACT_LIST(Integer.parseInt(notification), activity.getContentResolver());
            contacts.forEach((name, number) -> {
                smgr.sendTextMessage(number, null, message, null, null);
                Log.d(TAG, String.format("%s message sent to %s ( %s )", action, name, number));
            });
        }
    }
}
