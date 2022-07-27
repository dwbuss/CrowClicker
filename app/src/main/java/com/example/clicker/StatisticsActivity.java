package com.example.clicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.clicker.databinding.ActivityStatisticsBinding;
import com.example.clicker.objectbo.PointsHelper;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateCounts();
    }

    private void updateCounts() {
        PointsHelper pointsHelper = new PointsHelper(this);

        binding.dailyCatch.setText(pointsHelper.getDailyCatch());
        binding.dailyContact.setText(pointsHelper.getDailyContact());
        binding.dailyFollow.setText(pointsHelper.getDailyFollow());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));

        binding.tripCatch.setText(pointsHelper.getTripCatch(tripLength));
        binding.tripContact.setText(pointsHelper.getTripContact(tripLength));
        binding.tripFollow.setText(pointsHelper.getTripFollow(tripLength));

        binding.totalCatch.setText(pointsHelper.getTotalCatch());
        binding.totalContact.setText(pointsHelper.getTotalContact());
        binding.totalFollow.setText(pointsHelper.getTotalFollow());
    }
}