package com.bbsymphony.muslimprayers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in
 */
public class MuslimPrayers extends AppWidgetProvider {

    private static final String SYNC_CLICKED    = "automaticWidgetSyncButtonClick";
    public static final String APPS_UPDATE    = "com.bbsymphony.muslimprayers.APPS_UPDATE";
    private static final String LOG    = "MuslimPrayers";
    public static final String SETTING_CLICK = "setting_button_click";
    public static final String PREFERENCE_UPDATE = "preference_update";
    public static final String SALAT_TIME = "salat_time";
    public static final String ABULITION_TIME = "abulition_time";

    private MuslimPrayersBroadcastReceiver mReceiver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        Log.w(LOG, "[ON UPDATE] STEP 0 - method called");
        Log.d(LOG, "[SETTING] STEP 1 - Set Security");
        this.mReceiver = new MuslimPrayersBroadcastReceiver();
        Intent intent = new Intent(context, MuslimPrayersBroadcastReceiver.class);


            IntentFilter filter = new IntentFilter();
            filter.addAction("com.bbsymphony.dailysalat.APPS_UPDATE");
            context.getApplicationContext().registerReceiver(mReceiver, filter);

            Log.w(LOG, "[ON UPDATE] STEP 3 - DONE sending broadcast");

            Log.w(LOG, "[ON UPDATE] STEP 4 - Click on Background");
            //Intent clickPrevButton = new Intent();
            Intent clickBackground = new Intent(context, MuslimPrayersBroadcastReceiver.class);
            // Button click managenent
            clickBackground.setAction(SETTING_CLICK);
            clickBackground.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingBckIntent = PendingIntent.getBroadcast(context, 0, clickBackground, 0);

            Intent clickDate = new Intent(context, MuslimPrayersBroadcastReceiver.class);
            // Button click managenent
            clickDate.setAction(SETTING_CLICK);
            clickDate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingDateIntent = PendingIntent.getBroadcast(context, 0, clickDate, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.muslim_prayers);

        views.setOnClickPendingIntent(R.id.image_background_id, pendingBckIntent);
        views.setOnClickPendingIntent(R.id.date_id, pendingDateIntent);


        Log.w(LOG, "[ON UPDATE] STEP 2 - sending broadcast");
        context.sendBroadcast(intent);

        Log.w(LOG, "[ON UPDATE] STEP 5 - SetOnClick done");

        Log.w(LOG, "[ON UPDATE] STEP 6 - UpdatingAppWidget");

        appWidgetManager.updateAppWidget(appWidgetIds, views);

        }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        try {
            context.getApplicationContext().unregisterReceiver(this.mReceiver);
            final int N = appWidgetIds.length;
            //for (int i = 0; i < N; i++) {
            //MuslimPrayersConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
            //}
        } catch (Exception e) { Log.d(LOG, e.toString());}
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = MuslimPrayersConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.muslim_prayers);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        //appWidgetManager.updateAppWidget(appWidgetId, views);
    }



}

