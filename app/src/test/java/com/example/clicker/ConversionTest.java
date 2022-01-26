package com.example.clicker;

import static org.junit.Assert.assertEquals;

import com.example.clicker.objectbo.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import io.objectbox.Box;

public class ConversionTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testConversion() throws JSONException {

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

        JSONObject object = new JSONObject(json);
        JSONArray points = object.getJSONArray("features");
        Box<Point> pointBox = null;//BoxStoreBuilder.createDebugWithoutModel().build().boxFor(Point.class);
        assertEquals(3, SettingsActivity.convertPoints(pointBox, points));
    }
}