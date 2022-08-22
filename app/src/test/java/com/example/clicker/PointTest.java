package com.example.clicker;

import static org.junit.Assert.assertEquals;

import com.example.clicker.objectbo.Point;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class PointTest {
    @Test
    public void testGetMessageFollow() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tFOLLOW\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
        Point point = new Point(csvRecord);
        assertEquals("Dan saw one on a dusa.\n\nhttp://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage());
    }

    @Test
    public void testGetMessageCatch() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tCATCH\t61.5°\t\tdusa\t39\tTypical pike!\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
        Point point = new Point(csvRecord);
        assertEquals("Dan caught a 39 on a dusa.\nTypical pike!\nhttp://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage());
    }

    @Test
    public void testGetMessageContact() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tCONTACT\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
        Point point = new Point(csvRecord);
        assertEquals("Dan lost one on a dusa.\n\nhttp://maps.google.com/maps?q=49.216016,-93.858247", point.getMessage());
    }

    @Test
    public void testCreatePoint() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\t09-02-2021 09:11 AM\tFOLLOW\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
        Point point = new Point(csvRecord);
        assertEquals("Dan", point.getName());
    }

    @Test
    public void split() {
        String row = "101\tBlair\t8/9/2019\t\t49.22377105\t-93.89106709\t\t\t\t\t\t";
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
        List row = Arrays.asList("2, ,Tony,35.75, , Crow, 9/17/2021, 9:25 AM, blade blade, 4, -10447030.528943 6306926.152734499, 49.18861458, -93.84727198,   , 54, 54, 14, 27, NW, 1013, 0.64, 42, 0.11, 0, 4 - Waxing Gibbous, FALSE, FALSE".split(","));
        Point point = new Point(row);
        assertEquals("49.18861458", Double.toString(point.getLat()));
        assertEquals("-93.84727198", Double.toString(point.getLon()));
        assertEquals("Tony", point.getName());
        assertEquals("35.75", point.getFishSize());
        assertEquals("09-17-2021 9:25 AM", point.timeStampAsString());
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
    @Ignore
    public void testWeather() {
        try {
            File file = new File("C:\\Users\\Buss\\Downloads\\missing2.csv");    //creates a new file instance
            File ofile = new File("C:\\Users\\Buss\\Downloads\\output.csv");    //creates a new file instance
            BufferedWriter bw = new BufferedWriter(new FileWriter(ofile));
            BufferedReader br = new BufferedReader(new FileReader(file));  //creates a buffering character input stream
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    String dateS = fields[2];
                    String time = "";
                    if (fields.length == 3) {
                        time = "12:00 PM";
                    } else {
                        time = fields[3];
                        if (time.trim().equals(""))
                            time = "12:00 PM";
                    }
                    String latStr = "";
                    String lonStr = "";
                    if (fields.length < 5) {
                        latStr = "49.202587191134256";
                        lonStr = "-93.95591373128072";
                    } else if (fields[4] == null || fields[5].trim().equals("")) {
                        latStr = "49.202587191134256";
                        lonStr = "-93.95591373128072";
                    } else {
                        latStr = fields[4];
                        lonStr = fields[5];
                    }

                    //  latStr = "48.59691460358734";
                    //  lonStr = "-93.3763311889763";
                    latStr = "49.53751341665286";
                    lonStr = "-94.06379027359617";
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    Date dateD = new SimpleDateFormat("MM/dd/yyyy h:mm a").parse(dateS + " " + time);

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
                    String outputStr = line + "\t" + temperature + "\t" + feelsLike + "\t" + windSpeed + "\t" + windGust + "\t" + windDir + "\t" + pressure + "\t" + humidity + "\t" + dewPoint + "\t" + cloudCover + "\t" + precipProbability + "\r\n";
                    bw.write(outputStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testSolunar() {
        try {
            File file = new File("C:\\Users\\Buss\\Downloads\\golden.csv");    //creates a new file instance
            File ofile = new File("C:\\Users\\Buss\\Downloads\\golden2.csv");    //creates a new file instance
            BufferedWriter bw = new BufferedWriter(new FileWriter(ofile));
            BufferedReader br = new BufferedReader(new FileReader(file));  //creates a buffering character input stream
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    String dateS = fields[1];
                    String time = "";
                    boolean defualttime = false;
                    if (fields.length == 2) {
                        time = "12:00 PM";
                        defualttime = true;
                    } else {
                        time = fields[2];
                        if (time.trim().equals("")) {
                            defualttime = true;
                            time = "12:00 PM";
                        }
                    }
                    String latStr = "";
                    String lonStr = "";
                    if (fields.length < 2) {
                        latStr = "49.20255592261585";
                        lonStr = "-93.95784642864542";
                    } else if (fields[3] == null || fields[4].trim().equals("")) {
                        latStr = "49.21819937673365";
                        lonStr = "-93.85738357924455";
                    } else {
                        latStr = fields[3];
                        lonStr = fields[4];
                    }

                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    Date dateD = new SimpleDateFormat("MM/dd/yyyy h:mm a").parse(dateS + " " + time);
                    Solunar solunar = new Solunar();
                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(dateD);
                    solunar.populate(lon, lat, cal);
                    if (defualttime) {
                        String outputStr = line + "\t" + solunar.moonPhase + "\t" + " " + "\t" + " " + "\r\n";
                        bw.write(outputStr);
                    } else {
                        String outputStr = line + "\t" + solunar.moonPhase + "\t" + solunar.isMajor + "\t" + solunar.isMinor + "\r\n";
                        bw.write(outputStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            bw.close();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    String getDouble(JSONObject obj, String field) {
        try {
            return obj.getDouble(field) + "";
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