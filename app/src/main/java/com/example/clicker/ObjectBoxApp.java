package com.example.clicker;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.clicker.objectbo.MyObjectBox;

import java.util.List;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.objectbox.BoxStore;


public class ObjectBoxApp extends Application {

    private static final String TAG = "ObjectBoxApp";
    private BoxStore boxStore;
    private Intent intent;

    public Intent getIntent() {
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boxStore = MyObjectBox.builder().androidContext(ObjectBoxApp.this).build();
        // To prevent the application process from being killed while the app is running in the background, start a Foreground Service
        intent = new Intent(getApplicationContext(), Flic2Service.class);
        intent.setAction(Constants.START_LISTENING);
        ContextCompat.startForegroundService(getApplicationContext(), intent);

        // Initialize the Flic2 manager to run on the same thread as the current thread (the main thread)
        Flic2Manager manager = Flic2Manager.initAndGetInstance(getApplicationContext(), new Handler());
        registerClickerButtons(manager);
    }

    private void registerClickerButtons(Flic2Manager manager) {
        List<Flic2Button> buttons = manager.getButtons();
        Log.d(TAG, String.format("Found %d Clickers!", buttons.size()));
        for (Flic2Button button : buttons) {
            button.connect();
            button.addListener(new ClickerListener(getApplicationContext()));
        }
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}