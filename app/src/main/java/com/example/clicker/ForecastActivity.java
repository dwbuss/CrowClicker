package com.example.clicker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class ForecastActivity extends AppCompatActivity implements View.OnClickListener {
    private Calendar cal;
    private Button btnDatePicker;
    private Button btnTimePicker;

    private CombinedChart mChart;
    private Weather weather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weather = new Weather();
        setContentView(R.layout.activity_forecast);
        Button homeBtn = findViewById(R.id.home);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        cal = Calendar.getInstance(Locale.US);

        mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setDrawBarShadow(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        });
        renderData();
        btnDatePicker = findViewById(R.id.btn_date);
        btnTimePicker = findViewById(R.id.btn_time);

        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        setDate();
    }

    public void renderData() {

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new xFormatter());
        xAxis.setLabelRotationAngle(90f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.removeAllLimitLines();
        rightAxis.setDrawZeroLine(false);
        rightAxis.setDrawLimitLinesBehindData(false);
        rightAxis.setAxisMinimum(-70f);

    }

    private void setData() {
        ArrayList<CandleEntry> pressureValues = weather.pressurePoints;
        Collections.sort(pressureValues, new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry t1) {
                if (entry.getX() > t1.getX())
                    return 1;
                else
                    return -1;
            }
        });
        ArrayList<BarEntry> sunValues = weather.sunPoints;
        Collections.sort(pressureValues, new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry t1) {
                if (entry.getX() > t1.getX())
                    return 1;
                else
                    return -1;
            }
        });
        ArrayList<Entry> moonValues = weather.moonDegrees;
        Collections.sort(moonValues, new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry t1) {
                if (entry.getX() > t1.getX())
                    return 1;
                else
                    return -1;
            }
        });
        CandleDataSet pressuerSet;
        LineDataSet moonSet;
        BarDataSet sunSet;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            pressuerSet = (CandleDataSet) mChart.getData().getCandleData().getDataSetByIndex(0);
            pressuerSet.setValues(pressureValues);
            moonSet = (LineDataSet) mChart.getData().getLineData().getDataSetByIndex(0);
            moonSet.setValues(moonValues);
            sunSet = (BarDataSet) mChart.getData().getBarData().getDataSetByIndex(0);
            sunSet.setValues(sunValues);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            pressuerSet = new CandleDataSet(pressureValues, "Pressure");
            pressuerSet.setDrawIcons(false);
            pressuerSet.enableDashedHighlightLine(10f, 5f, 0f);
            pressuerSet.setColor(Color.DKGRAY);
            pressuerSet.setValueTextSize(9f);
            pressuerSet.setColor(Color.rgb(80, 80, 80));
            pressuerSet.setShadowColor(Color.RED);
            pressuerSet.setIncreasingColor(Color.RED);
            pressuerSet.setDecreasingColor(Color.BLUE);
            pressuerSet.setNeutralColor(Color.LTGRAY);
            pressuerSet.setShadowWidth(0.8f);
            pressuerSet.setFormLineWidth(1f);
            pressuerSet.setFormSize(15.f);
            pressuerSet.setDrawValues(false);

            moonSet = new LineDataSet(moonValues, "Moon Data");
            moonSet.setDrawIcons(false);
            moonSet.enableDashedLine(10f, 5f, 0f);
            moonSet.enableDashedHighlightLine(10f, 5f, 0f);
            moonSet.setColor(Color.BLACK);
            moonSet.setCircleColor(Color.BLACK);
            moonSet.setLineWidth(1f);
            moonSet.setCircleRadius(3f);
            moonSet.setDrawCircleHole(false);
            moonSet.setValueTextSize(9f);
            moonSet.setDrawFilled(true);
            moonSet.setFormLineWidth(1f);
            moonSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            moonSet.setFormSize(15.f);
            moonSet.setDrawValues(false);
            moonSet.setDrawCircles(false);
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
                moonSet.setFillDrawable(drawable);
            } else {
                moonSet.setFillColor(Color.DKGRAY);
            }

            sunSet = new BarDataSet(sunValues, "Sun");
            sunSet.setColor(Color.YELLOW);
            sunSet.setBarBorderColor(Color.BLACK);

            BarData barData = new BarData();
            barData.setBarWidth(0.45f);
            sunSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            barData.addDataSet(sunSet);

            CandleData candleData = new CandleData();
            pressuerSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            candleData.addDataSet(pressuerSet);

            LineData moonData = new LineData();
            moonSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            moonData.addDataSet(moonSet);
            CombinedData data = new CombinedData();
            data.setData(barData);
            data.setData(candleData);
            data.setData(moonData);
            mChart.setData(data);
        }
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void setDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        ((TextView) findViewById(R.id.in_date)).setText(simpleDateFormat.format(cal.getTime()));
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm a");
        ((TextView) findViewById(R.id.in_time)).setText(simpleTimeFormat.format(cal.getTime()));
        showSolunar();
        showWeather();
    }

    public void showSolunar() {
        Solunar solunar = new Solunar();
        solunar.populate(LocationHelper.CURRENT_LOCATION(this), cal);
        ((TextView) findViewById(R.id.sunRise)).setText(solunar.sunRise);
        ((TextView) findViewById(R.id.sunSet)).setText(solunar.sunSet);
        ((TextView) findViewById(R.id.moonRise)).setText(solunar.moonRise);
        ((TextView) findViewById(R.id.moonSet)).setText(solunar.moonSet);
        ((TextView) findViewById(R.id.moonTransit)).setText(solunar.moonOverHead);
        ((TextView) findViewById(R.id.moonUnder)).setText(solunar.moonUnderFoot);
        ((TextView) findViewById(R.id.moonPhase)).setText(solunar.moonPhase);
        ((TextView) findViewById(R.id.minor)).setText(solunar.minor);
        ((TextView) findViewById(R.id.major)).setText(solunar.major);
        ((ImageView) findViewById(R.id.moonView)).setImageResource(solunar.moonPhaseIcon);
    }

    public void showWeather() {
        Location loc = LocationHelper.CURRENT_LOCATION(this);
        weather.populate(loc.getLatitude(), loc.getLongitude(), cal.getTime(), getApplicationContext(), new ClickerCallback() {
            @Override
            public void onSuccess() {
                ((TextView) findViewById(R.id.temperature)).setText(weather.temperature);
                ((TextView) findViewById(R.id.apparentTemperature)).setText(weather.feelsLike);
                ((TextView) findViewById(R.id.dewPoint)).setText(weather.dewPoint);
                ((TextView) findViewById(R.id.windSpeed)).setText(weather.windSpeed);
                ((TextView) findViewById(R.id.windGust)).setText(weather.windGust);
                ((TextView) findViewById(R.id.humidity)).setText(weather.humidity);
                ((TextView) findViewById(R.id.pressure)).setText(weather.pressure);
                ((TextView) findViewById(R.id.cloudCover)).setText(weather.cloudCover);
            }

            @Override
            public void onFailure() {

            }
        });
        weather.populatePressure(loc.getLatitude(), loc.getLongitude(), cal.getTime(), getApplicationContext(), new ClickerCallback() {
            @Override
            public void onSuccess() {
                setData();
            }

            @Override
            public void onFailure() {

            }
        });
    }

    public void nextDay(View view) {
        cal.add(Calendar.DATE, 1);
        setDate();
    }

    public void prevDay(View view) {
        cal.add(Calendar.DATE, -1);
        setDate();
    }

    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            cal.set(year, monthOfYear, dayOfMonth);
                            setDate();
                        }
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            cal.set(Calendar.MINUTE, minute);
                            setDate();
                        }
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), DateFormat.is24HourFormat(ForecastActivity.this));
            timePickerDialog.show();
        }
    }
}
