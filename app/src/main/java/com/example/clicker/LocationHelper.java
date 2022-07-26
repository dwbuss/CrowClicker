package com.example.clicker;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.List;

class LocationHelper {
    private static final String TAG = "LocationHelper";

    public static final Location CURRENT_LOCATION(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            return bestLocation;
        } catch (SecurityException e) {
            Log.e(TAG, "This is bad.", e);
            return null;
        }
    }

}
