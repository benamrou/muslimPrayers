package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.bbsymphony.muslimprayers.R;

public class MuslimPrayersBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG    = "BroadcastReceiver";
    private static final String SHARED_PREF    = "com.bbsymphony.muslimprayers_preferences";

    private static final int ALERT_FAJR = 1000;
    private static final int ALERT_DUHR = 2000;
    private static final int ALERT_ASR = 3000;
    private static final int ALERT_MAGHRIB = 4000;
    private static final int ALERT_ISHAA = 5000;

    private static final int ALERT_WUDU_FAJR = 1100;
    private static final int ALERT_WUDU_DUHR = 2200;
    private static final int ALERT_WUDU_ASR = 3300;
    private static final int ALERT_WUDU_MAGHRIB = 4400;
    private static final int ALERT_WUDU_ISHAA = 5500;

    private DateFormat df = new SimpleDateFormat("hh:mm");
    private DateFormat dateDisplayedFormat = new SimpleDateFormat("EEE, d MMM yyyy");
    private Date dateDisplayed = new Date ();
    private PrayTime prayers = new PrayTime();
    private String provider;
    private SharedPreferences prefs;

    // Location Management
    private LocationManager myLocation;
    private Location address;
    private double longitude;
    private double lattitude;
    private double timezone;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.w(LOG, "[ONRECEIVE] BroadcastReceiver");

        //RemoteViews remoteViews;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.muslim_prayers);

        Log.w(LOG,"[ON RECEIVE] Action: " + intent.getAction());
        if (MuslimPrayers.SETTING_CLICK.equals(intent.getAction())) {
            try {
                Log.d(LOG, "[ON RECEIVE] Click-on Prev button ");
                Log.d(LOG, "[SETTING]");

                // Activating capabilities to read/write Settings
                StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                        .permitDiskWrites().build());

                this.prefs = context.getApplicationContext().getSharedPreferences(this.SHARED_PREF, context.MODE_PRIVATE);
                Intent setting = new Intent(context, SettingsActivity.class);
                setting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(setting);
                }
                // Option access
                catch (Exception e) {
                    Log.w(LOG,"[ON Receive error]" + e.toString());
                }
            }
        if (MuslimPrayers.PREFERENCE_UPDATE.equals(intent.getAction())) {
            Log.d(LOG, "[ON RECEIVE] Preference update ");

        }

        // Update text, images, whatever - here
        this.setDefaultLocation(remoteViews, context);
        this.setMuslimPrayersTime(remoteViews, context, intent, lattitude, longitude, timezone);
        remoteViews.setTextViewText(R.id.date_id, this.dateDisplayedFormat.format(this.dateDisplayed));
        Log.w(LOG,"[ONRECEIVE] Date today: " + this.dateDisplayedFormat.format(this.dateDisplayed));

        // Trigger widget layout update
        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, MuslimPrayers.class), remoteViews);

        ComponentName thisWidget = new ComponentName(context, MuslimPrayers.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Update the widgets via the service
        Intent startServiceIntent = new Intent(context, MuslimPrayersService.class);
        startServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(startServiceIntent);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

    }

    /* Request updates at startup */
    protected void onResume() {
        //locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    protected void onPause() {
        //locationManager.removeUpdates(this);
    }

    //@Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    //@Override
    public void onProviderEnabled(String provider) {

    }

    //@Override
    public void onProviderDisabled(String provider) {

    }

    public void setMuslimPrayersTime (RemoteViews views, Context context, Intent intent, double lattitude, double longitude, double timezone){
        //double latitude = 50.6329700;
        //double longitude = 3.0585800;
        //double timezone = 2;

        SharedPreferences prefs =  context.getSharedPreferences(this.SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Log.d(LOG, "Convention Calculation convention_id: " + prefs.getString("convention_id",null));
        Log.d(LOG, "Convention Calculation juristic_id: " + prefs.getString("juristic_id", "999"));
        prayers.setTimeFormat(PrayTime.TIME24);

        //prayers.setCalcMethod(prayers.Jafari);
        prayers.setCalcMethod(Integer.parseInt(prefs.getString("convention_id", Integer.toString(PrayTime.METHOD_CUSTOM))));
        prayers.setAsrJuristic(Integer.parseInt(prefs.getString("juristic_id", Integer.toString(PrayTime.JURISTIC_SHAFII))));
        prayers.setAdjustHighLats(Integer.parseInt(prefs.getString("higher_lattitude_id", Integer.toString(PrayTime.ADJ_METHOD_ANGLEBASED))));
        int[] offsets = {
                Integer.parseInt(prefs.getString("fajr_adj_id", "0")),
                0, // Sunrise
                Integer.parseInt(prefs.getString("duhr_adj_id", "0")),
                Integer.parseInt(prefs.getString("asr_adj_id", "0")),
                0, //Sunset
                Integer.parseInt(prefs.getString("maghreb_adj_id", "0")),
                Integer.parseInt(prefs.getString("ishaa_adj_id", "0"))};
        ////0, 0, 0, 0, 0, 0, 0};  {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);
        prayers.setTimeZone(this.timezone);

        //Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateDisplayed);

        ArrayList<String> prayerTimes = prayers.getPrayerTimes(cal,
                this.lattitude, this.longitude, this.timezone);
        ArrayList<String> prayerNames = prayers.getTimeNames();

        // Update the display
        views.setTextViewText(R.id.fajr_time, prayers.getFajrTime());
        views.setTextViewText(R.id.duhr_time, prayers.getDuhrTime());
        views.setTextViewText(R.id.asr_time, prayers.getAsrTime());
        views.setTextViewText(R.id.maghrib_time, prayers.getMaghribTime());
        views.setTextViewText(R.id.isha_time, prayers.getIshaTime());
        views.setTextViewText(R.id.sunrise_id, "Sunrise: " + prayers.getSunriseTime());
        views.setTextViewText(R.id.sunset_id, prayers.getSunsetTime() + " : Sunset");

        // Update the AlarmManager for Salat

        try {
           // Cancel alert set
            Log.d(LOG,"Canceling alerts...");
            AlarmManager alarmManager = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            PendingIntent intentFajr = PendingIntent.getBroadcast(context,
                    ALERT_FAJR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(intentFajr);
            Log.e(LOG,"REMOVE ALERT - without GeoLoc fajr");
            PendingIntent intentDuhr = PendingIntent.getBroadcast(context,
                    ALERT_DUHR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(intentDuhr);
            Log.e(LOG,"REMOVE ALERT - without GeoLoc duhr");
            PendingIntent intentAsr = PendingIntent.getBroadcast(context,
                    ALERT_ASR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(intentAsr);
            Log.e(LOG,"REMOVE ALERT - without GeoLoc asr");
            PendingIntent intentMaghrib = PendingIntent.getBroadcast(context,
                    ALERT_MAGHRIB, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(intentMaghrib);
            Log.e(LOG, "REMOVE ALERT - without GeoLoc maghrib");
            PendingIntent intentIshaa = PendingIntent.getBroadcast(context,
                    ALERT_ISHAA, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(intentIshaa);
            Log.e(LOG,"REMOVE ALERT - without GeoLoc ishaa");
        } catch (Exception e) {
            Log.d(LOG, "Exception unset alarm - " + e.toString());
        }
        if (prefs.getBoolean("notifications_salat_id",true) && !MuslimPrayers.SETTING_CLICK.equals(intent.getAction())) {
            Log.d(LOG, "Enabling alerts...");
            Intent intentSalat = new Intent(context, SalatAlarmService.class);
            intentSalat.setAction(MuslimPrayers.SALAT_TIME);
            intentSalat.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntentFajr = PendingIntent.getService(context,ALERT_FAJR,
                    intentSalat,PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, pendingIntentFajr, prayers.getFajrTime());

            //PendingIntent pendingIntentDuhr = PendingIntent.getActivity(context,
            //        ALERT_DUHR, intentSalat, 0);
            PendingIntent pendingIntentDuhr = PendingIntent.getService(context,ALERT_DUHR,
                    intentSalat,PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, pendingIntentDuhr, prayers.getDuhrTime());


            PendingIntent pendingIntentAsr = PendingIntent.getService(context, ALERT_ASR,
                    intentSalat, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, pendingIntentAsr, prayers.getAsrTime());

            PendingIntent pendingIntentMaghrib = PendingIntent.getService(context, ALERT_MAGHRIB,
                    intentSalat, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, pendingIntentMaghrib, prayers.getMaghribTime());

            PendingIntent pendingIntentIsha = PendingIntent.getService(context,ALERT_ISHAA,
                    intentSalat,PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, pendingIntentIsha, prayers.getIshaTime());

            //context.startService(intentSalat);
        }
    }

    public Date getDateDisplayed() {
        return dateDisplayed;
    }

    public void setDateDisplayed(Date dateDisplayed) {
        this.dateDisplayed = dateDisplayed;
    }

    public void setDefaultLocation (Context contezt){
    }

    public void setDefaultLocation(RemoteViews remoteViews, Context context) {
        myLocation = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Criteria c=new Criteria();
        provider = myLocation.getBestProvider(c, false);
        //now you have best provider
        //get location
        try {
            address = myLocation.getLastKnownLocation(provider);
            if (address != null) {
                //get latitude and longitude of the location
                this.longitude = address.getLongitude();
                this.lattitude = address.getLatitude();
                this.timezone = TimeZone.getDefault().getRawOffset();
                //display on text view
                Log.d(LOG, "[Location] Longitude: " + longitude + " lattitude:" + lattitude + " timezone:" + timezone);

                Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

                try {
                    List<Address> addresses = geocoder.getFromLocation(lattitude, longitude, 1);

                    Log.d(LOG, "[Address] address: " + addresses);
                    if(addresses != null) {
                        Address returnedAddress = addresses.get(0);
                        StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
                        for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                        }

                        remoteViews.setTextViewText(R.id.city_id, returnedAddress.getLocality() +", " +returnedAddress.getCountryCode());
                        Log.d(LOG, "[Address] " + returnedAddress.getLocality());
                    }
                    else{
                        Log.d(LOG, "[Address] address: " + addresses);
                        //remoteViews.setTextViewText(R.id.city_id, "Not available");
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //ERROR - remoteViews.setTextViewText(R.id.city_id, "Not available");
                    Log.d(LOG, "[Location] Not available - Exception Geocoder " + e);
                }
            } else {
                //ERROR - remoteViews.setTextViewText(R.id.city_id, "Not available");
                Log.d(LOG, "[Location] Not available - Can't getLastKnownLocation");
            }
        } catch (Exception e) {
            Log.d(LOG, "[Location] Not available - Exception " + e);
            remoteViews.setTextViewText(R.id.city_id, "Not available");
        }
    }

    public void setAlarmSalat(Context context, PendingIntent intent, String time) {
        Date date = null;   // given date
        try {
            date = df.parse(time);
        } catch (ParseException e) {
            Log.d(LOG, e.toString());
        }

        Calendar calendar = Calendar.getInstance(); // creates a new calendar instance
        String[] tokens = time.split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[0])); // gets hour in 24h format
        calendar.set(Calendar.MINUTE, Integer.parseInt(tokens[1]));        // gets month number, NOTE this is zero based!
        calendar.set(Calendar.SECOND, 0);        // sets second number, NOTE this is zero based!

        Log.d(LOG, "Alert set at: " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE));
        Log.d(LOG, "Calendar cal: " + calendar.toString());
        Log.d(LOG, "Cal: " + calendar.getTime());

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
    }

}

