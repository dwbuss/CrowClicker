package com.example.clicker;

import static org.junit.Assert.assertEquals;

import com.example.clicker.objectbo.Point;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PointTest {
    @Test
    public void testGetPluralMessageFollow() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tFOLLOW\t61.5°\t\tRubber\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\t \t ";
        Point point = new Point(csvRecord);
        assertEquals("Dan saw one on Rubber.\n\n8:02 pm\nhttps://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage("8:02 pm"));
    }

    @Test
    public void testGetPluralMessageCatch() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tCATCH\t61.5°\t\tBlades\t39\tTypical pike!\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\tMuskellunge\t20";
        Point point = new Point(csvRecord);
        assertEquals("Dan caught a 39\" Muskellunge on Blades.\nTypical pike!\n8:04 pm\nhttps://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage("8:04 pm"));
    }

    @Test
    public void testGetPluralMessageContact() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tCONTACT\t61.5°\t\tT.I.T.S\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\t \t ";
        Point point = new Point(csvRecord);
        assertEquals("Dan lost one on T.I.T.S.\n\n8:03 pm\nhttps://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage("8:03 pm"));
    }

    public void testGetSingularMessageFollow() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tFOLLOW\t61.5°\t\tGlider\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\t \t ";
        Point point = new Point(csvRecord);
        assertEquals("Dan saw one on a Glider.\n\nhttps://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage());
    }

    @Test
    public void testGetSingluarMessageCatch() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tCATCH\t61.5°\t\tSpoon\t39\tTypical pike!\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\tPike\t \t ";
        Point point = new Point(csvRecord);
        assertEquals("Dan caught a 39\" Pike on a Spoon.\nTypical pike!\n8:03 pm\nhttps://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage("8:03 pm"));
    }

    @Test
    public void testGetSingluarMessageContact() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tCONTACT\t61.5°\t\tJig\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\t \t ";
        Point point = new Point(csvRecord);
        assertEquals("Dan lost one on a Jig.\n\n7:55 pm\nhttps://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage("7:55 pm"));
    }

    @Test
    public void testCreatePoint() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tFOLLOW\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77\tCrow\t \t ";
        Point point = new Point(csvRecord);
        assertEquals("Dan", point.getName());
    }

    @Test
    public void testReadTSVWeather() throws Exception {
        String tsvRecord = "44938\tMatt\t-93.90544229\t49.22708605\t08-09-2022 9:12 AM\tCONTACT\t69\t\tRubber\t\t\t13\tSW\t0.25\t65\t1012\t0.89\tCrow\t \t ";
        Point p = new Point(tsvRecord);
        assertEquals("Matt", p.getName());
        assertEquals("", p.getFishSize());
    }

    @Test
    public void testReadTSVNoWeather() throws Exception {
        //                 "44938\tMatt\t-93.90544229\t49.22708605\t08-09-2022 9:12 AM\tCONTACT\t69\t\tRubber\t\t\t13\tSW\t0.25\t65\t1012\t0.89";
        String tsvRecord = "35145\tJeff\t-93.95221551\t49.21974587\t08-07-2022 2:26 PM\tCONTACT\t\t\tBlades\t\t\t\t\t\t\t\t\t \t \t \t ";
        Point p = new Point(tsvRecord);
        assertEquals("Jeff", p.getName());
        assertEquals("", p.getFishSize());
    }

    @Test
    public void split() {
        String row = "101\tBlair\t8/9/2019\t\t49.22377105\t-93.89106709\t\t\t\t\t\t\t ";
        String[] fields = row.split("\t");
        System.err.println(fields.length);
        System.err.println(fields[2]);
        System.err.println(fields[3]);
        System.err.println(fields[4]);
        System.err.println(fields[5]);
    }

    @Test
    public void importRow() throws ParseException, InvalidObjectException {
        //  [Row, Verified, Angler, Length, Girth, Lake, Date, Time, Bait, Anglers, Coordinates, Latitude, Longitude, Notes, Temperature, Feels Like, Wind Speed, Wind Gust, Wind Dir, Pressure, Humidity, Dew Point, Cloud Cover, Precip %, Moon Phase, Is Major, Is Minor]
        List row = Arrays.asList("2, ,Tony,35.75,15, Crow, 9/17/2021, 9:25 AM, blade blade, 4,Muskellunge, 49.18861458, -93.84727198,   , 54, 54, 14, 27, NW, 1013, 0.64, 42, 0.11, 0, 4 - Waxing Gibbous, FALSE, FALSE".split(","));
        Point point = new Point(row);
        assertEquals("49.18861458", Double.toString(point.getLat()));
        assertEquals("-93.84727198", Double.toString(point.getLon()));
        assertEquals("Tony", point.getName());
        assertEquals("35.75", point.getFishSize());
        assertEquals("09-17-2021 9:25 AM", point.timeStampAsString());
        assertEquals("15", point.getGirth());
        assertEquals("Muskellunge", point.getSpecies());
        //row = Arrays.asList("Row, Verified, Angler, Length, Girth, Lake, Date, Time, Bait, Anglers, Coordinates, Latitude, Longitude, Notes, Temperature, Feels Like, Wind Speed, Wind Gust, Wind Dir, Pressure, Humidity, Dew Point, Cloud Cover, Precip %, Moon Phase, Is Major, Is Minor".split(","));
        //point = new Point(row);
        //row = Arrays.asList("2, , Tony, 35.75, , Crow, 9/17/2021, 9:25 AM, blade blade, 4, -10447030.528943,6306926.152734499, , ,   , 54, 54, 14, 27, NW, 1013, 0.64, 42, 0.11, 0, 4 - Waxing Gibbous, FALSE, FALSE".split(","));
        //point = new Point(row);
        //row = Arrays.asList("2, , label, 35.75, , Crow, 9/17/2021, 9:25 AM, blade blade, 4, -10447030.528943,6306926.152734499, , ,   , 54, 54, 14, 27, NW, 1013, 0.64, 42, 0.11, 0, 4 - Waxing Gibbous, FALSE, FALSE".split(","));
        //point = new Point(row);
    }

    @Test
    public void parseDateTime() throws Exception {
        List row = Arrays.asList("225, , Tony, 46.5, , Crow, 08/11/2022, 6:47 PM, Blades, , , 49.20323573, -93.81247379, , 72, , 4, 6, S, 1021, 0.51, 52, 0.66, 0, 5 - Full Moon, FALSE, FALSE".split(","));
        Point point = new Point(row);
        assertEquals("08-11-2022 6:47 PM", point.timeStampAsString());
    }

    @Test
    public void testWeather() {
        try {
            String time = "12:00 PM";
            String latStr = "49.202587191134256";
            String lonStr = "-93.95591373128072";

            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);
            Date dateD = new SimpleDateFormat("MM/dd/yyyy h:mm a").parse("09/20/2022" + " " + time);

            long date = (dateD.getTime() / 1000);
            URL url = new URL("https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + date);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                System.err.println(con.getResponseMessage());
            }
            String text = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            JSONObject reader = new JSONObject(text);
            JSONObject currently = reader.getJSONObject("currently");
            String temperature = getDouble(currently, "temperature");
            String feelsLike = getDouble(currently, "apparentTemperature");
            String dewPoint = getDouble(currently, "dewPoint");
            String windSpeed = getDouble(currently, "windSpeed");
            String windDir = getCardinalDirection(getDouble(currently, "windBearing"));
            String windGust = getDouble(currently, "windGust");
            String precipProbability = getDouble(currently, "precipProbability");
            String humidity = getDouble(currently, "humidity");
            String pressure = getDouble(currently, "pressure");
            String cloudCover = getDouble(currently, "cloudCover");

            JSONObject hourly = reader.getJSONObject("hourly");
            JSONArray data = hourly.getJSONArray("data");

            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                values.add(new Entry(data.getJSONObject(i).getInt("time"), (float) data.getJSONObject(i).getDouble("pressure")));
            }
            pressure = String.valueOf((int) Double.parseDouble(hourly.getString("pressure")));
            String outputStr = "\t" + temperature + "\t" + feelsLike + "\t" + windSpeed + "\t" + windGust + "\t" + windDir + "\t" + pressure + "\t" + humidity + "\t" + dewPoint + "\t" + cloudCover + "\t" + precipProbability + "\r\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getDouble(JSONObject obj, String field) {
        try {
            return String.valueOf(obj.getDouble(field));
        } catch (Exception e) {
            return "";
        }
    }

    String getCardinalDirection(String input) {
        try {
            double d = Double.parseDouble(input);
            String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
            int index = (int) Math.floor(((d - 22.5) % 360) / 45);
            return directions[index + 1];
        } catch (Exception e) {
            return "";
        }
    }
}