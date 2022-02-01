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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;

public class PointTest {

    @Test
    public void testCreatePoint() throws ParseException {
        String csvRecord = "237\tDan\t-93.85824702680111\t49.21601569265263\tThu Sep 02 09:11:00 CDT 2021\tFOLLOW\t61.5°\t\tdusa\t\t\t'''''9.72 mph\tSE\t0.99\t54.06°\t1017.2 mb\t0.77";
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
                    URL url = new URL("https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + Double.toString(lat) + "," + Double.toString(lon) + "," + date);
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
                    int day = cal.get(Calendar.DAY_OF_MONTH);
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