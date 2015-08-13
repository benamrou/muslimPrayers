package com.bbsymphony.muslimprayers.alert;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import com.bbsymphony.muslimprayers.ConfigurationClass;
import com.bbsymphony.muslimprayers.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by Ahmed on 8/4/2015.
 */
public class NotificationService extends IntentService {

    private static String LOG = "NotificationService";
    private DateFormat df = new SimpleDateFormat("HH:mm");

    // Event Notification management
    private int notificationID = 0;


    public NotificationService() {
        super(LOG);
    }

    public NotificationService (String service) {
        super(service);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(LOG, "[ONSTART] Service Notification kicked off...");
        super.onStart(intent, startId);
        SharedPreferences prefs =  this.getSharedPreferences(ConfigurationClass.SHARED_PREF,
                Context.MODE_PRIVATE);
        Bundle notificationTimes = intent.getExtras();
        String time = df.format(new Date());
        String prayer = new String("");

        Log.d(LOG, "[ONSTART] Service Notification kicked off...");
        if (notificationTimes.getString(ConfigurationClass.EXTRA_NOTIFICATION_FAJR,"99:99").equals(time)) {
            prayer = getResources().getString(R.string.fajrPrayer_id);
        }
        if (notificationTimes.getString(ConfigurationClass.EXTRA_NOTIFICATION_DUHR,"99:99").equals(time)) {
            prayer = getResources().getString(R.string.duhrPrayer_id);
        }
        if (notificationTimes.getString(ConfigurationClass.EXTRA_NOTIFICATION_ASR,"99:99").equals(time)) {
            prayer = getResources().getString(R.string.asrPrayer_id);
        }
        if (notificationTimes.getString(ConfigurationClass.EXTRA_NOTIFICATION_MAGHRIB,"99:99").equals(time)) {
            prayer = getResources().getString(R.string.maghribPrayer_id);
        }
        if (notificationTimes.getString(ConfigurationClass.EXTRA_NOTIFICATION_ISHAA,"99:99").equals(time)) {
            prayer = getResources().getString(R.string.ishaPrayer_id);
        }

        if (prefs.getBoolean("notifications_new_event_id", true)) {
            NotificationMuslimPrayers notification = new NotificationMuslimPrayers();

            notificationID = notificationID + 1;
            notification.notify(getApplicationContext(), prayer + " prayer notification",
                "Salat " + prayer + " is in " + getResources().getString(R.string.x_minutes_notification) + " minutes",
                    R.drawable.ic_launcher,
                    0, notificationID);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG, "[ONSTARTCOMMAND] Service kicked off...");
        super.onStartCommand(intent, flags, startId);
        onStart(intent, startId);
        return START_NOT_STICKY;
    }


    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onDestroy ()  {
        super.onDestroy();
    }

}

