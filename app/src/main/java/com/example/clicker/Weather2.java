package com.example.clicker;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Weather2 {

    public static final String KEY = "LA5HBECPGDNRF7CSZUEG7U55K";
    private static final String TAG = "Weather2";
    public static final String VISUAL_CROSSING = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/%f,%f/%d?key=%s";
    public String temperature;
    public String feelsLike;
    public String dewPoint;
    public String windSpeed;
    public String windDir;
    public String windGust;
    public String date;
    public String precipProbability;
    public String humidity;
    public String pressure;
    public String cloudCover;
    ArrayList<CandleEntry> pressurePoints;
    ArrayList<Entry> moonDegrees;
    ArrayList<Long> sunPoints;
    ArrayList<BarEntry> windPoints;
    ArrayList<BarEntry> gustPoints;
    ArrayList<Point> contactPoints;


    public void populate(double lat, double lon, Date cal, Context context, final ClickerCallback callback) {
        String url = String.format(VISUAL_CROSSING, lat, lon, (cal.getTime() / 1000), KEY);
        populate(url, context, callback);
    }

    public void populate(String url, Context context, final ClickerCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = pullWeather(url, callback);
        queue.add(stringRequest);
    }

    public StringRequest pullWeather(String url, ClickerCallback callback) {
        return new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject reader = new JSONObject(response);
                        JSONObject main = reader.getJSONArray("days").getJSONObject(0);
                        temperature = ((int) Double.parseDouble(main.getString("temp"))) + "";
                        feelsLike = ((int) Double.parseDouble(main.getString("feelslike"))) + "";
                        dewPoint = ((int) Double.parseDouble(main.getString("dew"))) + "";
                        windSpeed = ((int) Double.parseDouble(main.getString("windspeed"))) + "";
                        windDir = getCardinalDirection(main.getDouble("winddir"));
                        windGust = ((int) Double.parseDouble(main.getString("windgust"))) + "";
                        date = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(new Date(1000 * Long.parseLong(main.getString("datetimeEpoch"))));
                        precipProbability = main.getString("precipprob");
                        humidity = main.getString("humidity");
                        pressure = ((int) Double.parseDouble(main.getString("pressure"))) + "";
                        cloudCover = main.getString("cloudcover");
                    } catch (JSONException e) {
                        Log.e(TAG, "Failure to create SheetAccess", e);
                    }

                    callback.onSuccess();
                }, error -> callback.onFailure());
    }

    String getCardinalDirection(double input) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        int index = (int) Math.floor(((input - 22.5) % 360) / 45);
        return directions[index + 1];
    }

    public void populatePressure(double lat, double lon, Date today, Context context, ClickerCallback callback) {
        pressurePoints = new ArrayList<>();
        moonDegrees = new ArrayList<>();
        sunPoints = new ArrayList<>();
        windPoints = new ArrayList<>();
        gustPoints = new ArrayList<>();
        contactPoints = new ArrayList<>();

        PointsHelper helper = new PointsHelper(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        Date yesterday = new Date(today.getTime() - Duration.ofDays(1).toMillis());
        Date tomorrow = new Date(today.getTime() + Duration.ofDays(1).toMillis());

        AtomicInteger totalRequests = new AtomicInteger(3);
        queue.addRequestEventListener((request, event) -> {
            if (event == RequestQueue.RequestEvent.REQUEST_FINISHED) {
                if (totalRequests.decrementAndGet() == 0) {
                    Log.d(TAG, "Pressure callbacks complete, calling success!");
                    callback.onSuccess();
                }
            }
        });

        queue.add(pullPressure(String.format(VISUAL_CROSSING, lat, lon, (yesterday.getTime() / 1000), KEY), callback, lat, lon));
        queue.add(pullPressure(String.format(VISUAL_CROSSING, lat, lon, (today.getTime() / 1000), KEY), callback, lat, lon));
        queue.add(pullPressure(String.format(VISUAL_CROSSING, lat, lon, (tomorrow.getTime() / 1000), KEY), callback, lat, lon));
    }

    private StringRequest pullPressure(String url, ClickerCallback callback, double lat, double lon) {
        Log.i(TAG, url);
        return new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.i(TAG, response);
                        JSONObject reader = new JSONObject(response);
                        JSONObject dailyData = reader.getJSONArray("days").getJSONObject(0);
                        JSONArray data = dailyData.getJSONArray("hours");

                        sunPoints.add(new Date(1000 * Long.parseLong(dailyData.getString("sunriseEpoch"))).getTime());
                        sunPoints.add(new Date(1000 * Long.parseLong(dailyData.getString("sunsetEpoch"))).getTime());
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject hour = data.getJSONObject(i);
                            Date d = new Date(1000 * Long.parseLong(hour.getString("datetimeEpoch")));
                            pressurePoints.add(new CandleEntry(d.getTime(), (float) hour.getDouble("pressure") + 0.5f,
                                    (float) hour.getDouble("pressure") - 0.5f,
                                    (float) hour.getDouble("pressure") + 0.001f,
                                    (float) hour.getDouble("pressure") - 0.001f));
                            moonDegrees.add(new Entry(d.getTime(), (float) Math.toDegrees(SunCalc4JavaUtils.getMoonPosition(d, lat, lon).get("altitude")) / 10));
                            windPoints.add(new BarEntry(d.getTime(), ((Double) hour.getDouble("windspeed")).floatValue(), getCardinalDirection(hour.getDouble("winddir"))));
                            gustPoints.add(new BarEntry(d.getTime(), ((Double) hour.getDouble("windgust")).floatValue(), getCardinalDirection(hour.getDouble("winddir"))));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failure to create SheetAccess", e);
                    }
                }, error -> callback.onFailure());
    }
}