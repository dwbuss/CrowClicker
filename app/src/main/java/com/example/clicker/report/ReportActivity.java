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

import com.example.clicker.ObjectBoxApp;
import com.example.clicker.databinding.ActivityReportBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.Point_;
import com.example.clicker.objectbo.PointsHelper;

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
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));
        dataModels = new PointsHelper(this).getPointsForTrip(tripLength);

        adapter = new PointAdapter(dataModels, editPointActivity);
        layoutManager = new LinearLayoutManager(this);
        pointsView = binding.pointsRecyclerView;
        pointsView.setLayoutManager(layoutManager);
        pointsView.setAdapter(adapter);
    }
}
