package com.example.clicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;

public class ClickerListener extends Flic2ButtonListener {
    private final Context context;
    private final PointsHelper helper;

    public ClickerListener(Context context) {
        this.context = context;
        this.helper = new PointsHelper(context);
    }

    @Override
    public void onButtonSingleOrDoubleClick(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick) {
        super.onButtonSingleOrDoubleClick(button, wasQueued, lastQueued, timestamp, isSingleClick, isDoubleClick);

        if (wasQueued && button.getReadyTimestamp() - timestamp > 15000) {
            // Drop the event if it's more than 15 seconds old
            return;
        }

        if (isSingleClick) addFromButton(ContactType.FOLLOW);
        if (isDoubleClick) addFromButton(ContactType.CONTACT);
    }

    private void addFromButton(ContactType contactType) {
        Location loc = LocationHelper.CURRENT_LOCATION(context);
        if (loc == null) {
            Toast.makeText(context, "Failed create point, could not retrieve location.", Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean friendly = prefs.getBoolean("Friendly", true);

        int soundBite = contactType.lookupSoundBite(friendly);
        MediaPlayer song = MediaPlayer.create(context, soundBite);
        song.start();

        String username = prefs.getString("Username", null);
        String defaultBait = prefs.getString("CurrentBait", "");
        final Point point = new Point(0, username, contactType.toString(), loc.getLongitude(), loc.getLatitude());
        point.setBait(defaultBait);
        point.setName(username);
        helper.addOrUpdatePoint(point);
        PointActivity.SEND_MESSAGE(point.getMessage(), contactType, prefs, context.getContentResolver());
    }
}
