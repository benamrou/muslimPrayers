package com.bbsymphony.muslimprayers.setting;

import android.content.Context;
import android.database.ContentObserver;
import android.util.Log;

import android.os.Handler;

/**
 * Created by Ahmed on 8/17/2015.
 */
public class KeyObserver extends ContentObserver {

    private final static String LOG="KEY LISTENER";

    public KeyObserver(Context context, Handler handler) {
        super(handler);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(LOG, "ON CHANGE");
        com.bbsymphony.muslimprayers.alert.PlaySound.stop();
    }
}
