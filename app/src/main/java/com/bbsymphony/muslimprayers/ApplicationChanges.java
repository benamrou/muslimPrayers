package com.bbsymphony.muslimprayers;

import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by Ahmed on 10/21/2015.
 */
public class ApplicationChanges extends Application {

    private static final String LOG    = "ApplicationChange";

    @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            Log.d(LOG, "[onConfigurationChanged]");

            // create intent to update all instances of the widget
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MuslimPrayers.class);

            // retrieve all appWidgetIds for the widget & put it into the Intent
            AppWidgetManager appWidgetMgr = AppWidgetManager.getInstance(this);

            //setLayoutIntent(getApplicationContext(),appWidgetMgr,startId);
            ComponentName cm = new ComponentName(this, MuslimPrayers.class);
            int[] appWidgetIds = appWidgetMgr.getAppWidgetIds(cm);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            // update the widget
            sendBroadcast(intent);
        }


    public void setLayoutIntent (Context context, AppWidgetManager appWidgetManager, int appWidgetIds) {

        Intent intent = new Intent(context, MuslimPrayersBroadcastReceiver.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.bbsymphony.dailysalat.APPS_UPDATE");
        //context.getApplicationContext().registerReceiver(mReceiver, filter);

        Log.w(LOG, "[ON UPDATE] STEP 3 - DONE sending broadcast");

        Log.w(LOG, "[ON UPDATE] STEP 4 - Click on Background");
        //Intent clickPrevButton = new Intent();
        Intent clickBackground = new Intent(context, MuslimPrayersBroadcastReceiver.class);
        // Button click managenent
        clickBackground.setAction(MuslimPrayers.SETTING_CLICK);
        clickBackground.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingBckIntent = PendingIntent.getBroadcast(context, 0, clickBackground, 0);

        Intent clickDate = new Intent(context, MuslimPrayersBroadcastReceiver.class);
        // Button click managenent
        clickDate.setAction(MuslimPrayers.SETTING_CLICK);
        clickDate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingDateIntent = PendingIntent.getBroadcast(context, 0, clickDate, 0);

        Intent clickCity = new Intent(context, MuslimPrayersBroadcastReceiver.class);
        // Button click managenent
        clickDate.setAction(MuslimPrayers.SETTING_CLICK);
        clickDate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pendingCityIntent = PendingIntent.getBroadcast(context, 0, clickCity, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.muslim_prayers);

        views.setOnClickPendingIntent(R.id.image_background_id, pendingBckIntent);
        views.setOnClickPendingIntent(R.id.date_id, pendingDateIntent);
        views.setOnClickPendingIntent(R.id.city_id, pendingCityIntent);

        // register on Key listener

        Log.w(LOG, "[ON UPDATE] STEP 2 - sending broadcast");
        context.sendBroadcast(intent);

        Log.w(LOG, "[ON UPDATE] STEP 5 - SetOnClick done");

        Log.w(LOG, "[ON UPDATE] STEP 6 - UpdatingAppWidget");

        appWidgetManager.updateAppWidget(appWidgetIds, views);

    }
}
