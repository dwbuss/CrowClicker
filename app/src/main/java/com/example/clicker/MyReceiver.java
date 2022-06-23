package com.example.clicker;

import static com.example.clicker.Constants.NOTIFICATION_CHANNEL_ID;
import static com.example.clicker.Constants.NOTIFICATION_CHANNEL_NAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MyReceiver extends BroadcastReceiver {
    private final Location loc;

    public MyReceiver(Location loc) {
        this.loc = loc;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
            Solunar solunar = new Solunar();
            Calendar cal = GregorianCalendar.getInstance();
            solunar.populate(loc, cal);
            String event = solunar.getEventNotification(solunar.parseTime(cal.getTime())).trim();
            if (!event.isEmpty()) {
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
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
                assert notificationmanager != null;
                notificationmanager.createNotificationChannel(notificationChannel);
                // Build Notification with Notification Manager
                notificationmanager.notify(1, builder.build());
            }
        }
    }
}