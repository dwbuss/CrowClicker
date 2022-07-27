package com.example.clicker;

import static java.util.stream.Collectors.joining;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.clicker.databinding.ActivityStatisticsBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.Point_;
import com.example.clicker.objectbo.PointsHelper;
import com.google.android.gms.common.util.MapUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private static final String TAG = "StatisticsActivity";
    private PointsHelper pointsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pointsHelper = new PointsHelper(this);
        updateCounts();
        buildLeaderBoard();
    }

    private void updateCounts() {
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

    private void buildLeaderBoard() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int tripLength = Integer.parseInt(prefs.getString("TripLength", "0"));

        List<Point> points = pointsHelper.getPointsForTrip(tripLength);

        // Top anglers by inches caught
        String mostInches = mostInches(points).entrySet().stream().limit(3).map(entry -> String.format("%s: %.2f\"", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostInches.setText(mostInches);

        // Top anglers by catches
        String mostCatches = mostCatches(points).entrySet().stream().limit(3).map(entry -> String.format("%s: %d\"", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostCatches.setText(mostCatches);

        // Top anglers who lost fish
        String mostLosses = mostLosses(points).entrySet().stream().limit(3).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostLosses.setText(mostLosses);

        // Top anglers by follows
        String mostFollows = mostFollows(points).entrySet().stream().limit(3).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostFollows.setText(mostFollows);
    }

    private Map<String, Double> mostInches(List<Point> points) {
        Map<String, Double> topInches = new LinkedHashMap<>();
        Map<String, Double> intermediateInches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(),
                                               Collectors.summingDouble(point -> point.getFishSizeAsDouble())));
        intermediateInches.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue()
                                .reversed()).forEachOrdered(e -> topInches.put(e.getKey(), e.getValue()));
        return topInches;
    }

    private Map<String, Long> mostCatches(List<Point> points) {
        Map<String, Long> intermediateCatches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(), Collectors.counting()));
        Map<String, Long> topCatches = new LinkedHashMap<>();
        intermediateCatches.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                                .reversed()).forEachOrdered(e -> topCatches.put(e.getKey(), e.getValue()));
        return topCatches;
    }

    private Map<String, Long> mostLosses(List<Point> points) {
        Map<String, Long> intermediateLosses = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CONTACT.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(), Collectors.counting()));
        Map<String, Long> topLosses = new LinkedHashMap<>();
        intermediateLosses.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                                .reversed()).forEachOrdered(e -> topLosses.put(e.getKey(), e.getValue()));
        Log.d(TAG, "Most losses: "+topLosses.toString());
        return topLosses;
    }

    private Map<String, Long> mostFollows(List<Point> points) {
        Map<String, Long> intermediateFollows = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.FOLLOW.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(), Collectors.counting()));
        Map<String, Long> topFollows = new LinkedHashMap<>();
        intermediateFollows.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                                .reversed()).forEachOrdered(e -> topFollows.put(e.getKey(), e.getValue()));
        return topFollows;
    }
}