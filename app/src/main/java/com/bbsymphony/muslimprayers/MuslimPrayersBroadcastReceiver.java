package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.bbsymphony.muslimprayers.setting.SettingsActivity;

public class MuslimPrayersBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG    = "BroadcastReceiver";

    private static final int REFRESH_WIDGET = 7000;


    private DateFormat df = new SimpleDateFormat("HH:mm");
    private DateFormat dateDisplayedFormat = new SimpleDateFormat("EEE, d MMM yyyy");
    private Date dateDisplayed = new Date ();
    private SharedPreferences prefs;


    // Location Management
    private LocationManager myLocation;

    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.w(LOG, "[ONRECEIVE] BroadcastReceiver");


        // Stop Playing song
        com.bbsymphony.muslimprayers.alert.PlaySound.stop();

        //RemoteViews remoteViews;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.muslim_prayers);

        Log.w(LOG,"[ON RECEIVE] Action: " + intent.getAction());
        this.prefs = context.getApplicationContext().getSharedPreferences(ConfigurationClass.SHARED_PREF, Context.MODE_PRIVATE);

        if (MuslimPrayers.SETTING_CLICK.equals(intent.getAction())) {
            try {
                Log.d(LOG, "[ON RECEIVE] Click-on Prev button ");
                Log.d(LOG, "[SETTING]");

                Intent setting = new Intent(context, SettingsActivity.class);
                setting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(setting);
                }
                // Option access
                catch (Exception e) {
                    Log.w(LOG,"[ON Receive error]" + e.toString());
                    return;
                }
            }

        dateDisplayed = new Date();
        remoteViews.setTextViewText(R.id.date_id, dateDisplayedFormat.format(dateDisplayed));
        // Get Longitude Latitude and timezone
        ArrayList<Double> detailAddress = ConfigurationClass.setDefaultLocation(remoteViews, context, prefs.getBoolean("daylight_saving_id", true));

        ArrayList<String> prayersIntent = ConfigurationClass.setMuslimPrayersTime(remoteViews, context, intent,
                detailAddress.get(1), detailAddress.get(0), detailAddress.get(2),
                detailAddress.get(3)==0?false: true, dateDisplayed);


        // Trigger widget layout update
        //AppWidgetManager.getInstance(context).updateAppWidget(
        //        new ComponentName(context, MuslimPrayers.class), remoteViews);

        ComponentName thisWidget = new ComponentName(context, MuslimPrayers.class);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Update the widgets via the service
        Intent startServiceIntent = new Intent(context, MuslimPrayersService.class);

        PendingIntent pendingIntentMuslimPrayer = PendingIntent.getService(context, REFRESH_WIDGET,
                startServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        startServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Every 5 minutes
        ConfigurationClass.setRepetitiveService(context, pendingIntentMuslimPrayer, 50 * 1000);

        context.startService(startServiceIntent);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        update(context, appWidgetManager, appWidgetIds, null);
    }

    //This is where we do the actual updating
    public void update(Context context, AppWidgetManager manager, int[] ids, Object data) {
        Log.d(LOG, "[UPDATE] Change on something updating widget");
        //data will contain some predetermined data, but it may be null
        //for (int widgetId : ids) {
            onUpdate(context, manager, ids);
        //}
    }


}

