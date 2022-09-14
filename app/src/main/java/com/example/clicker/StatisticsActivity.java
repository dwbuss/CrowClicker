package com.example.clicker;

import static java.util.stream.Collectors.joining;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.clicker.databinding.ActivityStatisticsBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private ActivityStatisticsBinding binding;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("Username", "Angler");

        binding.userDailyLabel.setText( String.format("%s's Daily Count", username) );
        binding.userDailyCatch.setText(pointsHelper.getDailyCatch(username));
        binding.userDailyContact.setText(pointsHelper.getDailyContact(username));
        binding.userDailyFollow.setText(pointsHelper.getDailyFollow(username));

        binding.dailyCatch.setText(pointsHelper.getDailyCatch());
        binding.dailyContact.setText(pointsHelper.getDailyContact());
        binding.dailyFollow.setText(pointsHelper.getDailyFollow());

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
        String max = maxLen(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %.2f\"", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.maxLen.setText(max);

        // Top anglers by average caught
        String averageLen = average(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %.2f\"", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.averageLen.setText(averageLen);

        // Top anglers by catches
        String mostCatches = mostCatches(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostCatches.setText(mostCatches);

        // Top anglers who lost fish
        String mostLosses = mostLosses(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostLosses.setText(mostLosses);

        // Top anglers by follows
        String mostFollows = mostFollows(points).entrySet().stream().limit(10).map(entry -> String.format("%s: %d", entry.getKey(), entry.getValue())).collect(joining("\n"));
        binding.mostFollows.setText(mostFollows);
    }

    private Map<String, Double> average(List<Point> points) {
        Map<String, Double> average = new LinkedHashMap<>();
        Map<String, Double> intermediateInches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.groupingBy(point -> point.getName(),
                                               Collectors.averagingDouble(point -> point.getFishSizeAsDouble())));
        intermediateInches.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue()
                                .reversed()).forEachOrdered(e -> average.put(e.getKey(), e.getValue()));
        return average;
    }

    private Map<String, Double> maxLen(List<Point> points) {
        Map<String, Double> maxLens = new LinkedHashMap<>();
        Map<String, Double> finalMaxLens = new LinkedHashMap<>();
        Map<String, Point> intermediateInches = points.stream()
                .filter(point -> point.getContactType().equals(ContactType.CATCH.toString()))
                .collect(Collectors.toMap(Point::getName, Function.identity(),
                                          BinaryOperator.maxBy(Comparator.comparing(Point::getFishSize))));
        intermediateInches.keySet().stream().forEach(k -> maxLens.put(k, intermediateInches.get(k).getFishSizeAsDouble()));

        maxLens.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue()
                                .reversed()).forEachOrdered(e -> finalMaxLens.put(e.getKey(), e.getValue()));
        return finalMaxLens;
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
        Log.d(TAG, "Most losses: " + topLosses);
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