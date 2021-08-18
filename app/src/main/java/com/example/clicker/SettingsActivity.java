package com.example.clicker;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointListAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;

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
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        List<Point> points = pointBox.query().build().find();
        for (Point p : points) {
            pointBox.remove(p);
        }
        updateCounts();
    }

    public void importPoints(View view) {
        try {
            JSONObject obj = null;
            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
            Box<Point> pointBox = boxStore.boxFor(Point.class);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "points.txt");
            FileInputStream ifile = new FileInputStream(file);
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

    public void exportPoints(View view) {
        try {
            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
            Box<Point> pointBox = boxStore.boxFor(Point.class);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "points.txt");
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
            Toast.makeText(getApplicationContext(), "Exported " + counter + " points", Toast.LENGTH_SHORT).show();
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