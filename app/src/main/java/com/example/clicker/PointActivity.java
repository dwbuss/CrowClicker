package com.example.clicker;

import static com.example.clicker.Constants.CANCEL_ACTION;
import static com.example.clicker.Constants.DELETE_ACTION;
import static com.example.clicker.Constants.PUSH_ACTION;
import static com.example.clicker.Constants.SAVE_ACTION;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.clicker.databinding.ActivityPointBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PointActivity extends AppCompatActivity {

    private static final String TAG = "PointActivity";
    private Point point;
    private ActivityPointBinding binding;
    private PointsHelper helper;
    private SheetAccess sheets;
    private boolean shouldNotifyDefault;
    private int lastAction;

    public static void SEND_MESSAGE(String message, ContactType action, SharedPreferences prefs, ContentResolver resolver) {
        String notification;
        switch (action) {
            case CATCH:
                notification = prefs.getString("Catch Notification", "");
                break;
            case FOLLOW:
                notification = prefs.getString("Follow Notification", "");
                break;
            case CONTACT:
                notification = prefs.getString("Lost Notification", "");
                break;
            default:
                notification = "";
        }
        if (!notification.trim().isEmpty()) {
            SmsManager smgr = SmsManager.getDefault();
            Map<String, String> contacts = SettingsActivity.GET_CONTACT_LIST(Integer.parseInt(notification), resolver);
            contacts.forEach((name, number) -> {
                smgr.sendTextMessage(number, null, message, null, null);
                Log.d(TAG, String.format("%s message sent to %s ( %s )", action, name, number));
            });
        } else
            Log.d(TAG, "No notifications found.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPointBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        point = getIntent().getParcelableExtra("point");

        binding.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance(Locale.US);
                cal.setTime(point.getTimeStamp());
                DatePickerDialog datePickerDialog = new DatePickerDialog(PointActivity.this,
                                                                         new DatePickerDialog.OnDateSetListener() {

                                                                             @Override
                                                                             public void onDateSet(DatePicker view, int year,
                                                                                                   int monthOfYear, int dayOfMonth) {
                                                                                 cal.set(year, monthOfYear, dayOfMonth);
                                                                                 point.setTimeStamp(cal.getTime());
                                                                                 binding.timeStamp.setText(point.timeStampAsString());
                                                                             }
                                                                         }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        binding.btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance(Locale.US);
                cal.setTime(point.getTimeStamp());
                TimePickerDialog timePickerDialog = new TimePickerDialog(PointActivity.this,
                                                                         new TimePickerDialog.OnTimeSetListener() {

                                                                             @Override
                                                                             public void onTimeSet(TimePicker view, int hourOfDay,
                                                                                                   int minute) {

                                                                                 cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                                                 cal.set(Calendar.MINUTE, minute);
                                                                                 point.setTimeStamp(cal.getTime());
                                                                                 binding.timeStamp.setText(point.timeStampAsString());
                                                                             }
                                                                         }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), DateFormat.is24HourFormat(PointActivity.this));
                timePickerDialog.show();
            }
        });

        helper = new PointsHelper(this);

        shouldNotifyDefault = getIntent().getBooleanExtra("shouldNotify", false);
        binding.setPoint(point);
        sheets = new SheetAccess(this);
        populateForm();
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("point", point);
        returnIntent.putExtra("lastAction", lastAction);
        setResult(lastAction, returnIntent);
        super.finish();
    }

    public void cancelButton(View v) {
        lastAction = CANCEL_ACTION;
        finish();
    }

    public void saveButton(View v) {
        boolean shouldNotify = updatePoint();
        helper.addOrUpdatePoint(point);
        if (shouldNotify) {
            ContentResolver contentResolver = getContentResolver();
            SEND_MESSAGE(point.getMessage(),
                         ContactType.valueOf(point.getContactType()),
                         PreferenceManager.getDefaultSharedPreferences(this),
                         contentResolver);
        }
        lastAction = SAVE_ACTION;
        finish();
    }

    public void deleteButton(View v) {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this);
        dialogDelete.setTitle("Warning!!");
        dialogDelete.setMessage("Are you sure to delete this point?");
        dialogDelete.setPositiveButton("Yes", (dialogInterface, i) -> {
            helper.deletePoint(point.getId());
            sheets.deletePoint(point);
            lastAction = DELETE_ACTION;
            dialogInterface.dismiss();
            finish();
        });
        dialogDelete.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        dialogDelete.show();
    }

    public void pushButton(View v) {
        boolean shouldNotify = updatePoint();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ClickerCallback callback = new ClickerCallback() {
            @Override
            public void onSuccess() {
                helper.addOrUpdatePoint(point);
                if (shouldNotify) {
                    SEND_MESSAGE(point.getMessage(), ContactType.valueOf(point.getContactType()), prefs, getContentResolver());
                }
                lastAction = PUSH_ACTION;
                finish();
            }

            @Override
            public void onFailure() {
                helper.addOrUpdatePoint(point);
                lastAction = PUSH_ACTION;
                finish();
            }
        };
        sheets.storePoint(point, prefs.getString("Lake", "").trim(), callback);
        lastAction = PUSH_ACTION;
    }

    public void weatherButton(View v) {
        updatePoint();
        try {
            final Weather weather = new Weather();
            weather.populate(point.getLat(), point.getLon(), point.getTimeStamp(), this, new ClickerCallback() {
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
                    Toast.makeText(getApplicationContext(), "Weather data updated.  Remember to SAVE or SAVE/PUSH to store the new weather data.", Toast.LENGTH_LONG).show();
                    binding.invalidateAll();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getApplicationContext(), "Unable to retrieve weather data.  Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failure to update weather", e);
        }
    }

    private void populateForm() {
        String[] contactTypes = ContactType.asStringArray();
        ArrayAdapter<String> contactAdapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, contactTypes);
        contactAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.contactType.setAdapter(contactAdapter);
        binding.contactType.setSelection(Arrays.asList(contactTypes).indexOf(point.getContactType()));

        String[] baits = getResources().getStringArray(R.array.bait_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, baits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.bait.setAdapter(adapter);
        binding.bait.setSelection(Arrays.asList(baits).indexOf(point.getBait()));

        binding.notify.setChecked(shouldNotifyDefault);
    }

    private boolean updatePoint() {
        point.setName(binding.name.getText().toString().trim());
        point.setContactType(binding.contactType.getSelectedItem().toString().trim());
        String timeStampStr = binding.timeStamp.getText().toString().trim();

        try {
            Date timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").parse(timeStampStr);
            point.setTimeStamp(new Timestamp(timeStamp.getTime()));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time", e);
        }

        point.setBait(binding.bait.getSelectedItem().toString().trim());
        String fishSize = binding.fishSize.getText().toString().trim();
        if (!fishSize.isEmpty())
            point.setFishSize(String.format("%.2f", Double.parseDouble(fishSize)));
        point.setAirTemp(binding.airtemp.getText().toString().trim());
        point.setWaterTemp(binding.watertemp.getText().toString().trim());
        point.setWindSpeed(binding.windSpeed.getText().toString().trim());
        point.setWindDir(binding.windDir.getText().toString().trim());
        point.setCloudCover(binding.cloudCover.getText().toString().trim());
        point.setDewPoint(binding.dewPoint.getText().toString().trim());
        point.setPressure(binding.pressure.getText().toString().trim());
        point.setHumidity(binding.humidity.getText().toString().trim());
        point.setNotes(binding.notes.getText().toString().trim());
        return binding.notify.isChecked();
    }
}