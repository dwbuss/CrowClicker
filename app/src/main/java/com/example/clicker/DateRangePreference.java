package com.example.clicker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateRangePreference extends Preference {

    public static final String DATE_RANGE_FORMAT = "yyyy-MM-dd";
    private String startDate;
    private String endDate;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private final SimpleDateFormat dateFormat;

    public DateRangePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(DATE_RANGE_FORMAT, Locale.US);
        setTitle("Date Range");
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(true);
    }

    @Override
    protected void onClick() {
        showStartDatePicker();
    }

    private void showStartDatePicker() {
        int year = startCalendar.get(Calendar.YEAR);
        int month = startCalendar.get(Calendar.MONTH);
        int day = startCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDatePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    startCalendar.set(year1, monthOfYear, dayOfMonth, 0, 0, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    startDate = dateFormat.format(startCalendar.getTime());
                    showEndDatePicker();
                },
                year,
                month,
                day
        );
        startDatePickerDialog.show();
    }

    private void showEndDatePicker() {
        int year = endCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH);
        int day = endCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDatePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    endCalendar.set(year1, monthOfYear, dayOfMonth, 23, 59, 59);
                    endCalendar.set(Calendar.MILLISECOND, 999);
                    endDate = dateFormat.format(endCalendar.getTime());
                    if (callChangeListener(new Pair<>(startDate, endDate))) {
                        persistString(startDate + "," + endDate);
                        setSummary(startDate + " - " + endDate);
                        notifyChanged();
                    }
                },
                year,
                month,
                day
        );
        endDatePickerDialog.show();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        String value = getPersistedString((String) defaultValue);

        if (value != null && !value.isEmpty()) {
            String[] dates = value.split(",");
            if (dates.length == 2) {
                startDate = dates[0];
                endDate = dates[1];
                updateCalendarsAndSummary();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    private void updateCalendarsAndSummary() {
        try {
            if (startDate != null && endDate != null) {
                // Parse the dates
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);

                if (start != null && end != null) {
                    // Update start calendar
                    startCalendar.setTime(start);
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);

                    // Update end calendar
                    endCalendar.setTime(end);
                    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    endCalendar.set(Calendar.MINUTE, 59);
                    endCalendar.set(Calendar.SECOND, 59);
                    endCalendar.set(Calendar.MILLISECOND, 999);

                    setSummary(startDate + " - " + endDate);
                }
            } else {
                setSummary("Select a start and end date");
            }
        } catch (ParseException e) {
            setSummary("Invalid date format");
        }
    }

    public static final Calendar[] parseDateRange(String dateRange) {
        String[] dates = dateRange.split(",");
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_RANGE_FORMAT, Locale.US);
        Calendar[] calendars = new Calendar[2];
        calendars[0] = Calendar.getInstance(Locale.US);
        calendars[1] = Calendar.getInstance(Locale.US);

        try {
            Date start = dateFormat.parse(dates[0]);
            Date end = dateFormat.parse(dates[1]);

            // Update start calendar
            calendars[0].setTime(start);
            calendars[0].set(Calendar.HOUR_OF_DAY, 0);
            calendars[0].set(Calendar.MINUTE, 0);
            calendars[0].set(Calendar.SECOND, 0);
            calendars[0].set(Calendar.MILLISECOND, 0);

            // Update end calendar
            calendars[1].setTime(end);
            calendars[1].set(Calendar.HOUR_OF_DAY, 23);
            calendars[1].set(Calendar.MINUTE, 59);
            calendars[1].set(Calendar.SECOND, 59);
            calendars[1].set(Calendar.MILLISECOND, 999);
        }
        catch (ParseException e) {
            throw ( new RuntimeException("Invalid date format", e));
        }
        return calendars;
    }
}