package com.example.clicker;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shredzone.commons.suncalc.SunTimes;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

public class Weather {

    private static final String TAG = "weather";
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
    ArrayList<BarEntry> sunPoints;


    public void populate(double lat, double lon, Date cal, Context context, final ClickerCallback callback) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + (cal.getTime() / 1000);
        populate(url, context, callback);
    }

    public void populate(String url, Context context, final ClickerCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = pullWeather(url, callback);
        queue.add(stringRequest);
    }

    public StringRequest pullWeather(String url, ClickerCallback callback) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject main = reader.getJSONObject("currently");
                            temperature = ((int) Double.parseDouble(main.getString("temperature"))) + "";
                            feelsLike = ((int) Double.parseDouble(main.getString("apparentTemperature"))) + "";
                            dewPoint = ((int) Double.parseDouble(main.getString("dewPoint"))) + "";
                            windSpeed = ((int) Double.parseDouble(main.getString("windSpeed"))) + "";
                            windDir = getCardinalDirection(main.getDouble("windBearing"));
                            windGust = ((int) Double.parseDouble(main.getString("windGust"))) + "";
                            date = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(new Date(1000 * Long.parseLong(main.getString("time"))));
                            precipProbability = main.getString("precipProbability");
                            humidity = main.getString("humidity");
                            pressure = ((int) Double.parseDouble(main.getString("pressure"))) + "";
                            cloudCover = main.getString("cloudCover");
                        } catch (JSONException e) {
                            Log.e(TAG, "Failure to create SheetAccess", e);
                        }

                        callback.onSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure();
            }
        });
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
        RequestQueue queue = Volley.newRequestQueue(context);
        Date yesterday = new Date(today.getTime() - Duration.ofDays(1).toMillis());
        Date tomorrow = new Date(today.getTime() + Duration.ofDays(1).toMillis());
        queue.add(pullPressure("https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + (yesterday.getTime() / 1000), callback, lat, lon));
        queue.add(pullPressure("https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + (today.getTime() / 1000), callback, lat, lon));
        queue.add(pullPressure("https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + (tomorrow.getTime() / 1000), callback, lat, lon));
    }

    private StringRequest pullPressure(String url, ClickerCallback callback, double lat, double lon) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject daily = reader.getJSONObject("daily");
                            JSONArray dailyData = daily.getJSONArray("data");

                            Date r = new Date(1000 * Long.parseLong(dailyData.getJSONObject(0).getString("sunriseTime")));
                            sunPoints.add(new BarEntry(r.getTime(), 50));
                            Date s = new Date(1000 * Long.parseLong(dailyData.getJSONObject(0).getString("sunsetTime")));
                            sunPoints.add(new BarEntry(s.getTime(), 70));
                            JSONObject hourly = reader.getJSONObject("hourly");
                            JSONArray data = hourly.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                Date d = new Date(1000 * Long.parseLong(data.getJSONObject(i).getString("time")));
                                pressurePoints.add(new CandleEntry(d.getTime(), (float) data.getJSONObject(i).getDouble("pressure") + 0.5f,
                                        (float) data.getJSONObject(i).getDouble("pressure") - 0.5f,
                                        (float) data.getJSONObject(i).getDouble("pressure") + 0.001f,
                                        (float) data.getJSONObject(i).getDouble("pressure") - 0.001f));
                                moonDegrees.add(new Entry(d.getTime(), (float) Math.toDegrees(SunCalc4JavaUtils.getMoonPosition(d, lat, lon).get("altitude"))));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Failure to create SheetAccess", e);
                        }

                        callback.onSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure();
            }
        });
    }
}
