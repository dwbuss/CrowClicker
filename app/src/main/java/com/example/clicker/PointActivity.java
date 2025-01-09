package com.example.clicker;

import static com.example.clicker.Constants.CANCEL_ACTION;
import static com.example.clicker.Constants.DELETE_ACTION;
import static com.example.clicker.Constants.PUSH_ACTION;
import static com.example.clicker.Constants.SAVE_ACTION;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
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
import java.util.LinkedList;
import java.util.List;
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
    private FantasyFishing ff;
    private String[] ffLocations = new String[]{""};
    private String[] ffOwners = new String[]{""};
    ;

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
        List<List<Object>> ffData = (List<List<Object>>) getIntent().getSerializableExtra("ffdata");
        List<List<Object>> ffStandings = (List<List<Object>>) getIntent().getSerializableExtra("ffstandings");
        ff = new FantasyFishing();
        ff.setStandings(ffStandings);
        ff.loadAnglers(ffData);
        ffLocations = ff.getLocations();
        ffOwners = ff.getOwners();
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
        if (point.getAirTemp().isEmpty() && !point.getName().equals("FF"))
            setWeather(this);
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
        List<Object> ffResult = new LinkedList<>();
        if (!binding.ffOwnerSpots.getSelectedItem().toString().trim().isEmpty() &&
                !binding.ffSpots.getSelectedItem().toString().trim().isEmpty()) {
            String location = binding.ffSpots.getSelectedItem().toString().trim();
            ffResult = ff.scoreCatch(point.getName(), location.substring(0, location.indexOf(":")).trim(), point.getFishSize(), binding.ffOwnerSpots.getSelectedItem().toString().trim(),
                                     point.timeStampAsString(), binding.video.isChecked(), binding.northern.isChecked(), binding.vest.isChecked());
        }
        sheets.storePoint(point, ffResult, callback);
        lastAction = PUSH_ACTION;
    }

    public void weatherButton(View v) {
        updatePoint();
        setWeather(v.getContext());
    }

    private void setWeather(Context context) {
        try {
            // TODO: check for internet connection
            /*
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
Warning: If you are connected to a WiFi network that doesn't include internet access or requires browser-based authentication, connected will still be true.

You will need this permission in your manifest:

<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
             */
            final Weather weather = new Weather(context);
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
                    Toast.makeText(getApplicationContext(), "Weather data updated.", Toast.LENGTH_SHORT).show();
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

        String[] species = getResources().getStringArray(R.array.species_array);
        ArrayAdapter<String> species_adapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, species);
        species_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.species.setAdapter(species_adapter);
        binding.species.setSelection(Arrays.asList(species).indexOf(point.getSpecies()));

        String[] baits = getResources().getStringArray(R.array.bait_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, baits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.bait.setAdapter(adapter);
        binding.bait.setSelection(Arrays.asList(baits).indexOf(point.getBait()));

        ArrayAdapter<String> ffadapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, ffLocations);
        ffadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.ffSpots.setAdapter(ffadapter);

        ArrayAdapter<String> ffOwneradapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_item, ffOwners);
        ffOwneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.ffOwnerSpots.setAdapter(ffOwneradapter);

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
        point.setSpecies(binding.species.getSelectedItem().toString().trim());

        String fishSize = binding.fishSize.getText().toString().trim();
        if (!fishSize.isEmpty())
            point.setFishSize(String.format("%.2f", Double.parseDouble(fishSize)));

        String girth = binding.girth.getText().toString().trim();
        if (!girth.isEmpty())
            point.setGirth(String.format("%.2f", Double.parseDouble(girth)));

        point.setAirTemp(binding.airtemp.getText().toString().trim());
        point.setWaterTemp(binding.watertemp.getText().toString().trim());
        point.setWindSpeed(binding.windSpeed.getText().toString().trim());
        point.setWindDir(binding.windDir.getText().toString().trim());
        point.setCloudCover(binding.cloudCover.getText().toString().trim());
        point.setDewPoint(binding.dewPoint.getText().toString().trim());
        point.setPressure(binding.pressure.getText().toString().trim());
        point.setHumidity(binding.humidity.getText().toString().trim());
        point.setNotes(binding.notes.getText().toString().trim());
        point.setLake(binding.lake.getText().toString().trim());
        return binding.notify.isChecked();
    }
}