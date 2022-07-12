package com.example.clicker.report;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clicker.ObjectBoxApp;
import com.example.clicker.R;
import com.example.clicker.databinding.ActivityPointBinding;
import com.example.clicker.databinding.ActivityReportBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.Point_;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = "ReportActivity";
    private RecyclerView pointsView;
    private LinearLayoutManager layoutManager;
    private PointAdapter adapter;
    private ArrayList<Point> dataModels;
    private ActivityReportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                .order(Point_.timeStamp, flags)
                .greater(Point_.timeStamp, today.getTime())
                .build().find();
        for (Point p : points) {
            if (!p.getName().equalsIgnoreCase("Label"))
                dataModels.add(p);
        }

        adapter = new PointAdapter(dataModels, editPointActivity);
        layoutManager = new LinearLayoutManager(this);
        pointsView = binding.pointsRecyclerView;
        pointsView.setLayoutManager(layoutManager);
        pointsView.setAdapter(adapter);
    }

    ActivityResultLauncher<Intent> editPointActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Point point = data.getParcelableExtra("point");
                    if ( dataModels.contains(point) ) {
                        int position = dataModels.indexOf(point);
                        dataModels.set(position, point);
                        adapter.notifyItemChanged(position);
                    }
            }});
}
