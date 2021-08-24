package com.example.clicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import geotrellis.proj4.CRS;
import geotrellis.proj4.Transform;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import scala.Function2;
import scala.Tuple2;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        updateCounts();
    }

    private void updateCounts() {
        PointListAdapter pointListAdapter;
        pointListAdapter = new PointListAdapter(this.getApplicationContext());

        ((TextView) findViewById(R.id.dailyCatch)).setText(pointListAdapter.getDailyCatch());
        ((TextView) findViewById(R.id.dailyContact)).setText(pointListAdapter.getDailyContact());
        ((TextView) findViewById(R.id.dailyFollow)).setText(pointListAdapter.getDailyFollow());

        ((TextView) findViewById(R.id.tripCatch)).setText(pointListAdapter.getTripCatch());
        ((TextView) findViewById(R.id.tripContact)).setText(pointListAdapter.getTripContact());
        ((TextView) findViewById(R.id.tripFollow)).setText(pointListAdapter.getTripFollow());

        ((TextView) findViewById(R.id.totalCatch)).setText(pointListAdapter.getTotalCatch());
        ((TextView) findViewById(R.id.totalContact)).setText(pointListAdapter.getTotalContact());
        ((TextView) findViewById(R.id.totalFollow)).setText(pointListAdapter.getTotalFollow());
    }

    public void clearPoints(View view) {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(view.getContext());
        dialogDelete.setTitle("Warning!!");
        dialogDelete.setMessage("Are you sure to delete all points?");
        dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);
                    List<Point> points = pointBox.query().build().find();
                    for (Point p : points) {
                        pointBox.remove(p);
                    }
                    updateCounts();
                    Toast.makeText(getApplicationContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
                dialogInterface.dismiss();
            }
        });
        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogDelete.show();
    }

    ActivityResultLauncher<Intent> importActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        try {
                            JSONObject obj = null;
                            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                            Box<Point> pointBox = boxStore.boxFor(Point.class);
                            InputStream ifile = getContentResolver().openInputStream(data.getData());
                            InputStreamReader is = new InputStreamReader(ifile);
                            BufferedReader bufferedReader = new BufferedReader(is);
                            int counter = 0;
                            while (bufferedReader.ready()) {
                                String line = bufferedReader.readLine();
                                obj = new JSONObject(line);
                                pointBox.put(new Point(obj.getJSONObject("Point")));
                                counter++;
                            }
                            updateCounts();
                            Toast.makeText(getApplicationContext(), "Imported " + counter + " points", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Write file Import " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }
            });

    public void importPoints(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Select Import File");
        importActivity.launch(chooseFile);
    }

    ActivityResultLauncher<Intent> importGeoActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        try {
                            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                            Box<Point> pointBox = boxStore.boxFor(Point.class);
                            InputStream ifile = getContentResolver().openInputStream(data.getData());
                            int counter = 0;
                            BufferedReader streamReader = new BufferedReader(new InputStreamReader(ifile, "UTF-8"));
                            StringBuilder responseStrBuilder = new StringBuilder();
                            String inputStr;
                            while ((inputStr = streamReader.readLine()) != null) {
                                responseStrBuilder.append(inputStr);
                            }
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
                            CRS epsg3857 = CRS.fromEpsgCode(3857);
                            CRS wgs84 = CRS.fromEpsgCode(4326);
                            JSONObject jsonObject = new JSONObject(json);
                            JSONArray points = jsonObject.getJSONArray("features");
                            for (int i = 0, size = points.length(); i < size; i++) {
                                JSONObject point = points.getJSONObject(i);
                                String name = point.getJSONObject("properties").getString("name");
                                String length = point.getJSONObject("properties").getString("size");
                                String date = point.getJSONObject("properties").getString("date");
                                double lon = point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
                                double lat = point.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);

                                Function2<Object, Object, Tuple2<Object, Object>> toWgs84 = Transform.apply(epsg3857, wgs84);
                                Tuple2<Object, Object> southWestInWgs84 = toWgs84.apply(lon, lat);
                                Double newLat = (Double) southWestInWgs84._2();
                                Double newLon = (Double) southWestInWgs84._1();

                                pointBox.put(new Point(0, name, length, date, newLon, newLat));
                                counter++;
                            }
                            Toast.makeText(getApplicationContext(), "Imported " + counter + " points", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Geo Import failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }
            });

    public void importGeoJson(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Select Geo Import File");
        importGeoActivity.launch(chooseFile);
    }

    public void exportPoints(View view) {
        try {
            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
            Box<Point> pointBox = boxStore.boxFor(Point.class);

            File file = new File("/mnt/sdcard/", "points.txt");
          //ile file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "points.txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            List<Point> points = pointBox.query().build().find();
            int counter = 0;
            for (Point p : points) {
                outputStreamWriter.write(p.toString() + "\n");
                counter++;
            }
            updateCounts();
            outputStreamWriter.flush();
            outputStreamWriter.close();
            Toast.makeText(getApplicationContext(), "Exported " + counter + " points as " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Export file Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}