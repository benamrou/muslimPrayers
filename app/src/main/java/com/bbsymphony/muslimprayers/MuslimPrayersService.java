package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.bbsymphony.muslimprayers.setting.KeyObserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MuslimPrayersService
        extends IntentService {

    private Context context;
    private DateFormat dateDisplayedFormat =new SimpleDateFormat("EEE, d MMM yyyy");
    private DateFormat df = new SimpleDateFormat("hh:mm");
    private Date dateDisplayed =new Date();
    private KeyObserver keyObs;

    private static final String LOG    = "iprayService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MuslimPrayersService(String name) {
        super(name);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(LOG, "[ONSTART] Service MuslimPrayers kicked off...");
        super.onStart(intent, startId);

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        updateAllAppWidgets(manager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG, "[ONSTARTCOMMAND] Service MuslimPrayers kicked off...");
        super.onStartCommand(intent, flags, startId);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        updateAllAppWidgets(manager);
        return START_NOT_STICKY;
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
    public void onConfigurationChanged(Configuration newConfig)
    {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        // Push update to home screen
        updateAllAppWidgets(manager);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.w(LOG, "[ONHandle] Date today: " + dateDisplayedFormat.format(dateDisplayed));

        AppWidgetManager appWidgetManager =
                AppWidgetManager.getInstance(this);

        this.keyObs = new KeyObserver(this,new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, keyObs);

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

        getApplicationContext().sendBroadcast(intent);
    }

    /**
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
        appWidgetManager.updateAppWidget(new ComponentName(this, MuslimPrayers.class),
                new RemoteViews(getApplicationContext().getPackageName(),R.layout.muslim_prayers));
    }


    @Override
    public void onDestroy(){
        getApplicationContext().getContentResolver().unregisterContentObserver(keyObs);
    }


    private void animateWidget(Intent intent) {
        Log.d(LOG, "Animate method");
        /*
        String time = df.format(new Date());
        Date now = new Date();
        Bundle prayerTimes = intent.getExtras();


        Log.d(LOG, "prayersTimes: " + prayerTimes);
        if (prayerTimes == null ) {
            return;
        }

        RemoteViews remoteView=new RemoteViews(getApplicationContext().getPackageName(),R.layout.muslim_prayers);
        //remoteView.setInt(R.id.fajr_id, "setBackgroundColor", R.color.blue);


        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.muslim_prayers, null);
        TextView fajr_id = (TextView) view.findViewById(R.id.fajr_id);
        TextView fajr_time = (TextView) view.findViewById(R.id.fajr_time);

        fajr_id.setHighlightColor(Color.BLACK);
        fajr_time.setAlpha(1);

        //setAlphaForView(fajr_id,0);

        //fajr_id.setTextColor(Color.argb(128, 100, 20, 30)); //50% transparent
        /*if (ConfigurationClass.stringToDate(prayerTimes.getString(ConfigurationClass.EXTRA_FAJR, "99:99")).compareTo(now) > 0) {
            // Colorful
            Log.d(LOG, "Fajr is NOT passed");
            fajr_id.setBackgroundColor(Color.parseColor("#ffff7a13"));
            fajr_time.setBackgroundColor(Color.parseColor("#ffff7a13"));
            // Step 1: Blinking the salat if it's within coming 10 minutes
            if (ConfigurationClass.stringToDate(ConfigurationClass.getTimeDelayed(prayerTimes.getString(ConfigurationClass.EXTRA_FAJR, "99:99"), -10)).
                    compareTo(now) > 0) {
                Log.d(LOG, "Activating Animation...");
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(50); //You can manage the time of the blink with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                fajr_id.startAnimation(anim);
                fajr_time.startAnimation(anim);
            } else {
                fajr_id.clearAnimation();
                fajr_time.clearAnimation();
            }
        } else {
            // Step 2: Gray the paased salat + 5 minutes
            Log.d(LOG, "Fajr is passed - Drawable");
            fajr_id.setBackgroundResource(R.color.red);
            //R.drawable.disabled);
            //fajr_id.setBackgroundColor(Color.BLUE);
            fajr_time.setBackground(getResources().getDrawable(R.drawable.disabled));
            Log.d(LOG, "Color used is disabled_salat");
        }*/
    }


    private void setAlphaForView(View v, float alpha) {
        Log.d(LOG, "setting Alpha for: " + v + " alpha:" + alpha );
        AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
        animation.setDuration(0);
        animation.setFillAfter(true);
        v.startAnimation(animation);
    }
}

