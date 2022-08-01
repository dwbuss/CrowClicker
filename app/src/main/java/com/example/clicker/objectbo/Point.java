package com.example.clicker.objectbo;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.clicker.Solunar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public final class Point implements Parcelable {
    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };
    public static final String CSV_TIMESTAMP_FORMAT = "MM-dd-yyyy h:mm a";

    @Id
    private long id;
    private long sheetId;
    private String name = "";
    private double lon;
    private double lat;
    private Date timeStamp;
    private String contactType = "";
    private String airTemp = "";
    private String waterTemp = "";
    private String bait = "";
    private String fishSize = "";
    private String notes = "";
    private String windSpeed = "";
    private String windGust = "";
    private String windDir = "";
    private String precipProbability = "";
    private String cloudCover = "";
    private String dewPoint = "";
    private String pressure = "";
    private String humidity = "";

    protected Point(Parcel in) {
        id = in.readLong();
        sheetId = in.readLong();
        name = in.readString();
        lon = in.readDouble();
        lat = in.readDouble();
        timeStamp = new Date();
        timeStamp.setTime(in.readLong());
        contactType = in.readString();
        airTemp = in.readString();
        waterTemp = in.readString();
        bait = in.readString();
        fishSize = in.readString();
        notes = in.readString();
        windSpeed = in.readString();
        windDir = in.readString();
        windGust = in.readString();
        precipProbability = in.readString();
        cloudCover = in.readString();
        dewPoint = in.readString();
        pressure = in.readString();
        humidity = in.readString();
    }

    public Point(JSONObject jsonObject) throws ParseException, JSONException {
        // id =jsonObject.getLong("id");
        name = jsonObject.optString("name");
        lon = jsonObject.optDouble("lon");
        lat = jsonObject.optDouble("lat");
        DateFormat osLocalizedDateFormat = new SimpleDateFormat(CSV_TIMESTAMP_FORMAT, Locale.US);
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
        lon = Double.valueOf(parts[2]);
        lat = Double.valueOf(parts[3]);
        DateFormat osLocalizedDateFormat = new SimpleDateFormat(CSV_TIMESTAMP_FORMAT, Locale.US);
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

    public Point(long id, String name, String length, String datetime, double lon, double lat) throws ParseException {
        this.id = id;
        this.name = name;
        this.contactType = "CATCH";
        this.fishSize = length;
        DateFormat osLocalizedDateFormat = new SimpleDateFormat(CSV_TIMESTAMP_FORMAT, Locale.US);
        this.timeStamp = osLocalizedDateFormat.parse(datetime);
        this.lat = lat;
        this.lon = lon;
    }

    public Point(List row) throws ParseException, InvalidObjectException {
//  [Row, Verified, Angler, Length, Girth, Lake, Date, Time, Bait, Anglers, Coordinates, Latitude, Longitude, Notes, Temperature, Feels Like, Wind Speed, Wind Gust, Wind Dir, Pressure, Humidity, Dew Point, Cloud Cover, Precip %, Moon Phase, Is Major, Is Minor]
//  [2, , Tony, 35.75, , Crow, 9/17/2021, 9:25 AM, blade blade, 4, -10447030.528943,6306926.152734499, 49.18861458, -93.84727198,   , 54, 54, 14, 27, NW, 1013, 0.64, 42, 0.11, 0, 4 - Waxing Gibbous, FALSE, FALSE]
        populatePoint(row);
    }

    public static final String CSV_HEADER() {
        return "id\tname\tlon\tlat\ttimeStamp\tcontactType\tairTemp\twaterTemp\tbait\tfishSize\tnotes\twindSpeed\twindDir\tcloudCover\tdewPoint\tpressure\thumidity\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return getId() == point.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(sheetId);
        parcel.writeString(name);
        parcel.writeDouble(lon);
        parcel.writeDouble(lat);
        parcel.writeLong(timeStamp.getTime());
        parcel.writeString(contactType);
        parcel.writeString(airTemp);
        parcel.writeString(waterTemp);
        parcel.writeString(bait);
        parcel.writeString(fishSize);
        parcel.writeString(notes);
        parcel.writeString(windSpeed);
        parcel.writeString(windDir);
        parcel.writeString(windGust);
        parcel.writeString(precipProbability);
        parcel.writeString(cloudCover);
        parcel.writeString(dewPoint);
        parcel.writeString(pressure);
        parcel.writeString(humidity);
    }

    private void populatePoint(List row) throws InvalidObjectException {
        sheetId = Long.parseLong(get(row, 0));
        name = get(row, 2).trim();
        if (name.equalsIgnoreCase("scenery") ||
                name.equalsIgnoreCase("NoFish") ||
                name.equalsIgnoreCase("ftony") ||
                name.equalsIgnoreCase("fdan") ||
                name.equalsIgnoreCase("fblair") ||
                name.equalsIgnoreCase("fchris"))
            throw new InvalidObjectException("Invalid point " + name);
        if (!get(row, 11).isEmpty())
            lat = Double.parseDouble(get(row, 11));
        if (!get(row, 12).isEmpty())
            lon = Double.parseDouble(get(row, 12));
        try {
            if (((String) row.get(7)).trim().isEmpty()) {
                DateFormat osLocalizedDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                timeStamp = osLocalizedDateFormat.parse(((String) row.get(6)).trim());
            } else {
                DateFormat osLocalizedDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a", Locale.US);
                timeStamp = osLocalizedDateFormat.parse(((String) row.get(6)).trim() + " " + ((String) row.get(7)).trim());
            }
        } catch (Exception e) {
            timeStamp = GregorianCalendar.getInstance().getTime();
        }
        if (!name.equalsIgnoreCase("label"))
            contactType = "CATCH";
        else
            contactType = " ";
        airTemp = get(row, 14);
        bait = get(row, 8);
        fishSize = get(row, 3);
        notes = get(row, 13);
        windSpeed = get(row, 16);
        windGust = get(row, 17);
        windDir = get(row, 18);
        cloudCover = get(row, 22);
        dewPoint = get(row, 21);
        pressure = get(row, 19);
        humidity = get(row, 20);
    }

    String get(List row, int id) {
        try {
            return (String) row.get(id);
        } catch (Exception e) {
            return "";
        }
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

    public String getWindGust() {
        return windGust;
    }

    public void setWindGust(String windGust) {
        this.windGust = windGust;
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

    public long getSheetId() {
        return sheetId;
    }

    public void setSheetId(long sheetId) {
        this.sheetId = sheetId;
    }

    public String getPrecipProbability() {
        return precipProbability;
    }

    public void setPrecipProbability(String precipProbability) {
        this.precipProbability = precipProbability;
    }

    @Override
    public String toString() {
        DateFormat osLocalizedDateFormat = new SimpleDateFormat(CSV_TIMESTAMP_FORMAT, Locale.US);
        return id + "\t" + name + "\t" + lon + "\t" + lat + "\t" + osLocalizedDateFormat.format(timeStamp) + "\t" + contactType + "\t" +
                airTemp + "\t" + waterTemp + "\t" + bait + "\t" + fishSize + "\t" + notes + "\t" +
                windSpeed + "\t" + windDir + "\t" + cloudCover + "\t" + dewPoint + "\t" +
                pressure + "\t" + humidity;
    }

    public String getMessage() {
        if (contactType.equalsIgnoreCase("CATCH"))
            return String.format("%s caught a %s on a %s.%n%s%nhttp://maps.google.com/maps?q=%f,%f", getName().trim(), getFishSize().trim(), getBait(), getNotes().trim(), getLat(), getLon());
        else if (contactType.equalsIgnoreCase("FOLLOW"))
            return String.format("%s saw one on a %s.%n%s%nhttp://maps.google.com/maps?q=%f,%f", getName().trim(), getBait(), getNotes().trim(), getLat(), getLon());
        else
            return String.format("%s lost one on a %s.%n%s%nhttp://maps.google.com/maps?q=%f,%f", getName().trim(), getBait(), getNotes().trim(), getLat(), getLon());
    }

    public List<List<Object>> getSheetBody(String lake) {
        DateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        DateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String day = dayFormat.format(getTimeStamp());
        String time = timeFormat.format(getTimeStamp());
        if (sheetId <= 0)
            sheetId = getId();
        Solunar solunar = new Solunar();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(getTimeStamp());
        solunar.populate(lon, lat, cal);
        return Arrays.asList(
                Arrays.asList(sheetId, "", name, fishSize, "", lake, day, time, bait, "", "", lat, lon, notes, airTemp, "", windSpeed, windGust, windDir, pressure, humidity, dewPoint, cloudCover, precipProbability, solunar.moonPhase, Boolean.toString(solunar.isMajor), Boolean.toString(solunar.isMinor)));
    }

    public void refresh(List row) throws InvalidObjectException {
        populatePoint(row);
    }

    public String timeStampAsString() {
        return new SimpleDateFormat(CSV_TIMESTAMP_FORMAT, Locale.US).format(getTimeStamp());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<List<Object>> getSheetBodyWithOutId(String lake) {
        DateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        DateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String day = dayFormat.format(getTimeStamp());
        String time = timeFormat.format(getTimeStamp());
        if (sheetId <= 0)
            sheetId = getId();
        Solunar solunar = new Solunar();
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(getTimeStamp());
        solunar.populate(lon, lat, cal);
        return Arrays.asList(
                Arrays.asList("=ROW()", "", name, fishSize, "", lake, day, time, bait, "", "", lat, lon, notes, airTemp, "", windSpeed, windGust, windDir, pressure, humidity, dewPoint, cloudCover, precipProbability, solunar.moonPhase, Boolean.toString(solunar.isMajor), Boolean.toString(solunar.isMinor)));

    }

    public double getFishSizeAsDouble() {
        if (fishSize.isEmpty()) return 0;
        return Double.parseDouble(fishSize);
    }
}
