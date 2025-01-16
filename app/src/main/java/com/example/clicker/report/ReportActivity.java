package com.example.clicker.report;

import static com.example.clicker.Constants.DELETE_ACTION;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clicker.DateRangePreference;
import com.example.clicker.databinding.ActivityReportBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = "ReportActivity";
    private RecyclerView pointsView;
    private LinearLayoutManager layoutManager;
    private PointAdapter adapter;
    private ArrayList<Point> dataModels;
    ActivityResultLauncher<Intent> editPointActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                Point point = data.getParcelableExtra("point");
                if (point != null && dataModels.contains(point)) {
                    int position = dataModels.indexOf(point);
                    if (result.getResultCode() == DELETE_ACTION) {
                        dataModels.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        dataModels.set(position, point);
                        adapter.notifyItemChanged(position);
                    }
                }
            });
    private ActivityReportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar[] trip_range = DateRangePreference.parseDateRange(prefs.getString("my_date_range_preference", "2020-01-01,2020-12-31"));
        dataModels = new PointsHelper(this).getPointsForTrip(trip_range, prefs.getString("Lake", ""));

        adapter = new PointAdapter(dataModels, editPointActivity);
        layoutManager = new LinearLayoutManager(this);
        pointsView = binding.pointsRecyclerView;
        pointsView.setLayoutManager(layoutManager);
        pointsView.setAdapter(adapter);
    }
}
