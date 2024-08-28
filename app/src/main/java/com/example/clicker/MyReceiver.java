package com.example.clicker;

import static com.example.clicker.Constants.NOTIFICATION_CHANNEL_ID;
import static com.example.clicker.Constants.NOTIFICATION_CHANNEL_NAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private final Location loc;

    public MyReceiver(Location loc) {
        this.loc = loc;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
            Solunar solunar = new Solunar();
            Calendar cal = Calendar.getInstance(Locale.US);
            solunar.populate(loc, cal);
            String event = solunar.getEventNotification(solunar.parseTime(cal.getTime())).trim();
            if (!event.isEmpty()) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean silence = prefs.getBoolean("Silence", false);
                if (!silence) {
                    MediaPlayer player = MediaPlayer.create(context, findSound(context, event));
                    player.start();
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                        NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.cc_notification)
                        .setTicker(event)
                        .setContentTitle("Solunar Event")
                        .setChannelId(NOTIFICATION_CHANNEL_ID)
                        .setContentText(event.startsWith("Minor") ? "Minor disappointment" : "Major disappointment")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(event))
                        .setAutoCancel(true);

                NotificationManager notificationmanager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                assert notificationmanager != null;
                notificationmanager.createNotificationChannel(notificationChannel);
                // Build Notification with Notification Manager
                notificationmanager.notify(1, builder.build());
            }
        }
    }

    Uri findSound(Context context, String event) {
        if (event.contains("Minor") && event.contains("is starting"))
            return (Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.cc_minor_start));
        if (event.contains("Minor") && event.contains("has ended"))
            return (Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.cc_minor_end));
        if (event.contains("Major") && event.contains("is starting"))
            return (Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.cc_major_start));
        if (event.contains("Major") && event.contains("has ended"))
            return (Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.cc_major_end));
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.drop);
    }
}