package com.bbsymphony.muslimprayers;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class SalatAlarmService extends IntentService {

    private static String LOG = "SalatAlarmService";
    private boolean firstTime = true;

    public SalatAlarmService() {
        super(LOG);
    }

    public SalatAlarmService(String service) {
        super(service);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(LOG, "[ONSTART] Service kicked off...");
        super.onStart(intent, startId);
        playSound(this.getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG, "[ONSTARTCOMMAND] Service kicked off...");
        this.firstTime = false;
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void playSound(Context context) {
        try {
            MediaPlayer mp = MediaPlayer.create(context, R.raw.adhan_makkah);
            mp.prepare();
            mp.start();
            mp.release();
        } catch (Exception e) {
            Log.d(LOG, e.toString());
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }


}
