package com.example.clicker;

import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import geotrellis.proj4.CRS;
import geotrellis.proj4.Transform;
import scala.Function2;
import scala.Tuple2;

public class ConversionTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testConversion() {

        String json = "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "        \"name\": \"dan\",\n" +
                "        \"date\": \"07/25/2012\",\n" +
                "        \"pic\": \"https://lh3.googleusercontent.com/-axnFS0HbSxM/UBVRwpYcYNI/AAAAAAAAKbY/09zXl45h84A/s400-Ic42/IMG_1381.JPG\",\n" +
                "        \"size\": \"45\",\n" +
                "        \"notes\": \"\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n" +
                "          -10452505.442318,\n" +
                "          6310627.924908303\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "        \"name\": \"carey\",\n" +
                "        \"date\": \"07/24/2012\",\n" +
                "        \"pic\": \"https://lh3.googleusercontent.com/-dw0_d9nTRHE/UBVRqs0ajdI/AAAAAAAAKbA/aAkRAAGqNlc/s400-Ic42/DSCF0037.JPG\",\n" +
                "        \"size\": \"\",\n" +
                "        \"notes\": \"\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n" +
                "          -10452663.09369,\n" +
                "          6310698.390294398\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "        \"name\": \"dan\",\n" +
                "        \"date\": \"09/11/2019\",\n" +
                "        \"pic\": \"\",\n" +
                "        \"size\": \"37\",\n" +
                "        \"notes\": \"\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [\n" +
                "          -10450730.55884,\n" +
                "          6314111.233392801\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        InputStream inputStreamObject = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }

            JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
            JSONArray points = jsonObject.getJSONArray("features");
            for (int i = 0, size = points.length(); i < size; i++) {
                JSONObject point = points.getJSONObject(i);
                System.err.println(point.getJSONObject("properties").getString("name"));
                System.err.println(point.getJSONObject("properties").getString("size"));
                System.err.println(point.getJSONObject("properties").getString("date"));
                System.err.println(Double.valueOf(point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)));
                System.err.println(Double.valueOf(point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)));
                double lon = point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
                double lat = point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);


                CRS epsg3857 = CRS.fromEpsgCode(3857);
                CRS wgs84 = CRS.fromEpsgCode(4326);

                Function2<Object, Object, Tuple2<Object, Object>> toWgs84 = Transform.apply(epsg3857, wgs84);
                Tuple2<Object, Object> southWestInWgs84 = toWgs84.apply(lon, lat);
                Double newLat = (Double) southWestInWgs84._2();
                Double newLon = (Double) southWestInWgs84._1();
                System.out.println("Point in WGS 84: " + newLat + "," + newLon);
                //Function2<Object, Object, Tuple2<Object, Object>> fromWgs84 = Transform.apply(wgs84, epsg3857);
                //Tuple2<Object, Object> southWestBackToEpsg27700 = fromWgs84.apply(southWestInWgs84._1(), southWestInWgs84._2());
                //System.out.println("South-West corner back to EPSG 27700: " + southWestBackToEpsg27700._1() + "," + southWestBackToEpsg27700._2());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}