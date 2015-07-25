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

    /*
    @Override
    public void onStart(Intent intent, int startId) {
    try{

    System.out.println("[ONSTART] Date today: "+dateDisplayedFormat.format(dateDisplayed));

    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
            .getApplicationContext());

    int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

    ComponentName thisWidget = new ComponentName(getApplicationContext(),
            com.bbsymphony.ipray.ipray.class);


    for(int widgetId: allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    com.bbsymphony.ipray.R.layout.activity_daily_salat_color);

            //EnableDisableConnectivity edConn = new EnableDisableConnectivity(this.getApplicationContext());
                //edConn.enableDisableDataPacketConnection(!checkConnectivityState(this.getApplicationContext()));

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(),
                    com.bbsymphony.ipray.ipray.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(com.bbsymphony.ipray.R.id.prev_btn, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        }
        catch (Exception ex) {
            Log.w(LOG,"[EXCEPTION]: " + ex.toString());
        }
        stopSelf();
        super.onStart(intent, startId);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        try{
        Log.w(LOG, "[ONSTARTCOMMAND] Date today: " + dateDisplayedFormat.format(dateDisplayed));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                com.bbsymphony.ipray.ipray.class);

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    com.bbsymphony.ipray.R.layout.activity_daily_salat_color);

            //EnableDisableConnectivity edConn = new EnableDisableConnectivity(this.getApplicationContext());
            //edConn.enableDisableDataPacketConnection(!checkConnectivityState(this.getApplicationContext()));

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(),
                    com.bbsymphony.ipray.ipray.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(com.bbsymphony.ipray.R.id.prev_btn, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        }
        catch (Exception ex) {
            Log.w(LOG,"[EXCEPTION]: " + ex.toString());
        }
        stopSelf();

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    } */

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null; // throw new UnsupportedOperationException("Not yet implemented");
    }


    private void setiprayTime (RemoteViews views){
        // To update a label
        /*
        views.setTextViewText(com.bbsymphony.ipray.R.id.fajr_time, df.format(new Date()));
        views.setTextViewText(com.bbsymphony.ipray.R.id.duhr_time, df.format(new Date()));
        views.setTextViewText(com.bbsymphony.ipray.R.id.asr_time, df.format(new Date()));
        views.setTextViewText(com.bbsymphony.ipray.R.id.maghrib_time, df.format(new Date()));
        views.setTextViewText(com.bbsymphony.ipray.R.id.isha_time, df.format(new Date()));
    */
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
        //RemoteViews remoteViews = new RemoteViews(this
        //        .getApplicationContext().getPackageName(),
        //        com.bbsymphony.ipray.R.layout.activity_daily_salat_color);

        //EnableDisableConnectivity edConn = new EnableDisableConnectivity(this.getApplicationContext());
        //edConn.enableDisableDataPacketConnection(!checkConnectivityState(this.getApplicationContext()));

        // Register an onClickListener
        //Intent clickIntent = new Intent(this.getApplicationContext(),
        //        com.bbsymphony.ipray.ipray.class);

        //String packageName = "com.bbsymphony.ipray";
        //String className = "iprayBroadcastReceiver";
        //ComponentName component = new ComponentName(packageName, className);

        //clickIntent.setComponent(component);

        //PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, clickIntent,
        //        PendingIntent.FLAG_UPDATE_CURRENT);
        //remoteViews.setOnClickPendingIntent(com.bbsymphony.ipray.R.id.prev_btn, pendingIntent);
        //appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        //context.sendBroadcast(clickIntent);
    }


}

