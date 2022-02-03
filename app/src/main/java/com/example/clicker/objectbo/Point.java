package com.example.clicker.objectbo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Point {

    @Id
    private long id;
    private String name;
    private double lon;
    private double lat;
    private Date timeStamp;
    private String contactType;
    private String airTemp;
    private String waterTemp;
    private String bait;
    private String fishSize;
    private String notes;
    private String windSpeed;
    private String windDir;
    private String cloudCover;
    private String dewPoint;
    private String pressure;
    private String humidity;

    public Point(JSONObject jsonObject) throws ParseException, JSONException {
        // id =jsonObject.getLong("id");
        name = jsonObject.optString("name");
        lon = jsonObject.optDouble("lon");
        lat = jsonObject.optDouble("lat");
        DateFormat osLocalizedDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
        timeStamp = osLocalizedDateFormat.parse(jsonObject.getString("timeStamp"));
        contactType = jsonObject.optString("contactType");
        airTemp = jsonObject.optString("airTemp");
        waterTemp = jsonObject.optString("waterTemp");
        bait = jsonObject.optString("bait");
        fishSize = jsonObject.optString("fishSize");
        notes = jsonObject.optString("notes");
        windSpeed = jsonObject.optString("windSpeed");
        windDir = jsonObject.optString("windDir");
        cloudCover = jsonObject.optString("cloudCover");
        dewPoint = jsonObject.optString("dewPoint");
        pressure = jsonObject.optString("pressure");
        humidity = jsonObject.optString("humidity");
    }

    public Point(String csvRecord) throws ParseException {
        String[] parts = csvRecord.split("\t");
        name = parts[1];
        lon = new Double(parts[2]);
        lat = new Double(parts[3]);
        DateFormat osLocalizedDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
        timeStamp = osLocalizedDateFormat.parse(parts[4]);
        contactType = parts[5];
        airTemp = parts[6];
        waterTemp = parts[7];
        bait = parts[8];
        fishSize = parts[9];
        notes = parts[10];
        windSpeed = parts[11];
        windDir = parts[12];
        cloudCover = parts[13];
        dewPoint = parts[14];
        pressure = parts[15];
        humidity = parts[16];
    }

    public Point(long id, String name, String contactType, double lon, double lat) {
        this.id = id;
        this.name = name;
        this.timeStamp = Calendar.getInstance().getTime();
        this.lat = lat;
        this.lon = lon;
        this.contactType = contactType;
    }

    public Point(long id, String name, String length, String datetime, double lon, double lat) {
        this.id = id;
        this.name = name;
        this.contactType = "CATCH";
        this.fishSize = length;
        this.timeStamp = Calendar.getInstance().getTime();
        this.lat = lat;
        this.lon = lon;
    }

    public Point(List row) throws ParseException {
//  [Row, Verified, Angler, Length, Girth, Lake, Date, Time, Bait, Anglers, Coordinates, Latitude, Longitude, Notes, Temperature, Feels Like, Wind Speed, Wind Gust, Wind Dir, Pressure, Humidity, Dew Point, Cloud Cover, Precip %, Moon Phase, Is Major, Is Minor]
//  [2, , Tony, 35.75, , Crow, 9/17/2021, 9:25 AM, blade blade, 4, -10447030.528943,6306926.152734499, 49.18861458, -93.84727198,   , 54, 54, 14, 27, NW, 1013, 0.64, 42, 0.11, 0, 4 - Waxing Gibbous, FALSE, FALSE]
        name = (String) row.get(2);
        lon = (double) row.get(12);
        lat = (double) row.get(11);
  //      DateFormat osLocalizedDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
  //      timeStamp = osLocalizedDateFormat.parse((String) row.get(6));
        contactType = "CATCH";
        airTemp = (String) row.get(13);
      //  waterTemp = (String) row.get(13);
        bait = (String) row.get(8);
        fishSize = (String) row.get(3);
        //notes =(String) row.get(13);
        windSpeed = (String) row.get(14);
        windDir = (String) row.get(16);
        cloudCover =(String) row.get(20);
        dewPoint =(String) row.get(19);
        pressure = (String) row.get(17);
        humidity =(String) row.get(18);
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getBait() {
        return bait;
    }

    public void setBait(String bait) {
        this.bait = bait;
    }

    public String getFishSize() {
        return fishSize;
    }

    public void setFishSize(String fishSize) {
        this.fishSize = fishSize;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAirTemp() {
        return airTemp;
    }

    public void setAirTemp(String temp) {
        this.airTemp = temp;
    }

    public String getWaterTemp() {
        return waterTemp;
    }

    public void setWaterTemp(String temp) {
        this.waterTemp = temp;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDir() {
        return windDir;
    }

    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    public String getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(String cloudCover) {
        this.cloudCover = cloudCover;
    }

    public String getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(String dewPoint) {
        this.dewPoint = dewPoint;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    @Override
    public String toString() {
        return id + "\t" + name + "\t" + lon + "\t" + lat + "\t" + timeStamp + "\t" + contactType + "\t" +
                airTemp + "\t" + waterTemp + "\t" + bait + "\t" + fishSize + "\t" + notes + "\t" +
                windSpeed + "\t" + windDir + "\t" + cloudCover + "\t" + dewPoint + "\t" +
                pressure + "\t" + humidity;
    }

    public String getMessage() {
        if (contactType.equalsIgnoreCase("CATCH"))
            return getName() + " caught a " + getFishSize() + " on a " + getBait() + "\r\nhttp://maps.google.com/maps?q=" + getLat() + "," + getLon();
        else if (contactType.equalsIgnoreCase("FOLLOW"))
            return getName() + " saw one on a " + getBait() + "\r\nhttp://maps.google.com/maps?q=" + getLat() + "," + getLon();
        else
            return getName() + " lost one on a " + getBait() + "\r\nhttp://maps.google.com/maps?q=" + getLat() + "," + getLon();
    }
}
