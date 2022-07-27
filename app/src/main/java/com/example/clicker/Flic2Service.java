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
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class Flic2Service extends Service {

    private static final String TAG = "Flic2Service";

    @Override
    public void onCreate() {
        super.onCreate();

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(mChannel);

        Intent stopIntent = new Intent(this, Flic2Service.class);
        stopIntent.setAction(Constants.STOP_LISTENING);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent statsIntent = new Intent(this, StatisticsActivity.class);
        PendingIntent statsPendingIntent = PendingIntent.getService(this, 1, statsIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Crow Clicker")
                .setContentText("Listening for Clickers and Cluckers.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .addAction(R.mipmap.ic_launcher, "Statistics", statsPendingIntent)
                .addAction(R.mipmap.ic_launcher, "Stop", stopPendingIntent)
                .build();
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, String.format("Received %s message in Flic2Service", intent.getAction()));
        if (intent.getAction().equals(Constants.STOP_LISTENING)) {
            stopForeground(true);
            stopSelfResult(startId);
            int id = android.os.Process.myPid();
            android.os.Process.killProcess(id);
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }
}
