package com.example.clicker;

import static com.example.clicker.Constants.NOTIFICATION_CHANNEL_ID;
import static com.example.clicker.Constants.NOTIFICATION_CHANNEL_NAME;
import static com.example.clicker.Constants.SERVICE_NOTIFICATION_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class Flic2Service extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(mChannel);

        Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Crow Clicker")
                .setContentText("Listening for Clickers and Cluckers.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }
}