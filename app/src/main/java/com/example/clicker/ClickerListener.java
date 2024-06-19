package com.example.clicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;

public class ClickerListener extends Flic2ButtonListener {
    private static final String TAG = "ClickerListener";
    private final Context context;
    private final PointsHelper helper;

    public ClickerListener(Context context) {
        this.context = context;
        this.helper = new PointsHelper(context);
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        super.onButtonSingleOrDoubleClickOrHold(button, wasQueued, lastQueued, timestamp, isSingleClick, isDoubleClick, isHold);

        if (wasQueued && button.getReadyTimestamp() - timestamp > 15000) {
            // Drop the event if it's more than 15 seconds old
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        ButtonPressActions singleClickType = ButtonPressActions.valueOf(prefs.getString("single_click", "FOLLOW"));
        ButtonPressActions doubleClickType = ButtonPressActions.valueOf(prefs.getString("double_click", "CONTACT"));
        ButtonPressActions longClickType = ButtonPressActions.valueOf(prefs.getString("long_click", "CONTACT"));

        Log.d(TAG, String.format("Single-click is %s, Double-click is %s, Long-click is %s", singleClickType, doubleClickType, longClickType));

        if (isSingleClick) addFromButton(singleClickType);
        if (isDoubleClick) addFromButton(doubleClickType);
        if (isHold) addFromButton(longClickType);
    }

    private void addFromButton(ButtonPressActions actionType) {
        Location loc = LocationHelper.CURRENT_LOCATION(context);
        if (loc == null) {
            Toast.makeText(context, "Failed create point, could not retrieve location.", Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean mature = prefs.getBoolean("Mature", false);

        ContactType contactType = actionType.getType();
        int soundBite = contactType.lookupSoundBite(mature);
        MediaPlayer song = MediaPlayer.create(context, soundBite);
        song.start();

        String lure = prefs.getString("CurrentBait", "");
        if (actionType.equals(ButtonPressActions.FOLLOW_ON_BLADES))
            lure = "Blades";
        if (actionType.equals(ButtonPressActions.FOLLOW_ON_RUBBER))
            lure = "Rubber";

        final Point point = new Point(0, prefs.getString("Username", null), contactType.toString(), loc.getLongitude(), loc.getLatitude(), lure, prefs.getString("Lake", ""));
        helper.addOrUpdatePoint(point);
        PointActivity.SEND_MESSAGE(point.getMessage(), contactType, prefs, context.getContentResolver());
    }
}
