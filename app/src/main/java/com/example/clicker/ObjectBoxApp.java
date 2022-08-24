package com.example.clicker;

import static android.Manifest.permission.BLUETOOTH_CONNECT;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.clicker.objectbo.MyObjectBox;

import java.util.List;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2Manager;
import io.objectbox.BoxStore;
import io.objectbox.DebugFlags;


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
        boxStore = MyObjectBox.builder().androidContext(this).build();
        // To prevent the application process from being killed while the app is running in the background, start a Foreground Service
        intent = new Intent(getApplicationContext(), Flic2Service.class);
        intent.setAction(Constants.START_LISTENING);
        ContextCompat.startForegroundService(getApplicationContext(), intent);

        // Initialize the Flic2 manager to run on the same thread as the current thread (the main thread)
        Flic2Manager manager = Flic2Manager.initAndGetInstance(getApplicationContext(), new Handler());

        // Make sure we can connect to BlueTooth Buttons
        if (permissionsPresentToConnectButtons())
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

    private boolean permissionsPresentToConnectButtons() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT < 31 || getApplicationInfo().targetSdkVersion < 31) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Application startup and we do not have ACCESS_FINE_LOCATION permission.  Not connecting to buttons.");
                retVal = false;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Application startup and we do not have BLUETOOTH_SCAN or BLUETOOTH_CONNECT permissions.  Not connecting to buttons.");
                retVal = false;
            }
        }
        return retVal;
    }
    public BoxStore getBoxStore() {
        return boxStore;
    }
}