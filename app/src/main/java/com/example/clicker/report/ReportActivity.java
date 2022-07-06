package com.example.clicker.report;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.clicker.ObjectBoxApp;
import com.example.clicker.R;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.Point_;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;

public class ReportActivity extends AppCompatActivity {


    ArrayList<Point> dataModels;
    ListView listView;
    private static CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        //    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //   setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list);

        dataModels = new ArrayList<>();
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        Calendar today = GregorianCalendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, 0 - tripLength);
        int flags = QueryBuilder.NULLS_LAST | QueryBuilder.DESCENDING;
        List<Point> points = pointBox.query()
                .order(Point_.timeStamp,flags)
                .greater(Point_.timeStamp, today.getTime())
                .build().find();
        for (Point p : points) {
            if (!p.getName().equalsIgnoreCase("Label"))
                dataModels.add(p);
        }

        adapter = new CustomAdapter(dataModels, getApplicationContext());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Point dataModel = dataModels.get(position);
                Snackbar.make(view, dataModel.getName() + "\n" + dataModel.getTimeStamp().toString() + " " + dataModel.getFishSize(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //  if (id == R.id.action_settings) {
        //       return true;
        //  }

        return super.onOptionsItemSelected(item);
    }
}
