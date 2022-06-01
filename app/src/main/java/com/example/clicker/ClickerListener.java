package com.example.clicker;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;

public class ClickerListener extends Flic2ButtonListener {
    private final MainActivity activity;

    public ClickerListener(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onButtonSingleOrDoubleClick(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick) {
        super.onButtonSingleOrDoubleClick(button, wasQueued, lastQueued, timestamp, isSingleClick, isDoubleClick);
        if (isSingleClick) activity.addFollow(activity.getCurrentFocus());
        if (isDoubleClick) activity.addContact(activity.getCurrentFocus());
    }
}
