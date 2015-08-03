package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MuslimPrayersService
        extends IntentService {

    private Context context;
    private DateFormat dateDisplayedFormat =new SimpleDateFormat("EEE, d MMM yyyy");
    private DateFormat df = new SimpleDateFormat("hh:mm");
    private Date dateDisplayed =new Date();

    private static final String LOG    = "iprayService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MuslimPrayersService(String name) {
        super(name);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public MuslimPrayersService() {
        super("MuslimPrayersService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null; // throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.w(LOG, "[ONHandle] Date today: " + dateDisplayedFormat.format(dateDisplayed));

        Log.d(LOG, "[SETTING] Set Security");

        AppWidgetManager appWidgetManager =
                AppWidgetManager.getInstance(this);

        int incomingAppWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (incomingAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            updateOneAppWidget(appWidgetManager,
                    incomingAppWidgetId);
        }
        else
        {
            updateAllAppWidgets(appWidgetManager);
        }
    }

    /**
     * For each Daily Salat app widget on the user's home
     * screen, updates its display with a new date, and
     * registers click handling for its buttons.
     */
    private void updateAllAppWidgets(AppWidgetManager
                                             appWidgetManager)
    {
        ComponentName appWidgetProvider = new ComponentName(this,
                com.bbsymphony.muslimprayers.MuslimPrayers.class);
        int[] appWidgetIds =
                appWidgetManager.getAppWidgetIds(appWidgetProvider);
        int N = appWidgetIds.length;
        for (int i = 0; i < N; i++)
        {
            int appWidgetId = appWidgetIds[i];
            updateOneAppWidget(appWidgetManager, appWidgetId);
        }
    }

    /**
     * For the Daily Salat app widget with the provided ID,
     * updates its display with a new passcode, and registers
     * click handling for its buttons.
     */
    private void updateOneAppWidget(AppWidgetManager
                                            appWidgetManager, int appWidgetId)
    {
        // To be implemented if needed
    }


}

