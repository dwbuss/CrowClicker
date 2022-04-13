package com.example.clicker;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.clicker.objectbo.MyObjectBox;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import io.flic.flic2libandroid.Flic2Manager;
import io.objectbox.BoxStore;


public class ObjectBoxApp extends Application {

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        boxStore = MyObjectBox.builder().androidContext(ObjectBoxApp.this).build();
        // To prevent the application process from being killed while the app is running in the background, start a Foreground Service
        ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), Flic2Service.class));

        // Initialize the Flic2 manager to run on the same thread as the current thread (the main thread)
        Flic2Manager manager = Flic2Manager.initAndGetInstance(getApplicationContext(), new Handler());
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}