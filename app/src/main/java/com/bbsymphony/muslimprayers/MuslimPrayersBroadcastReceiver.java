package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.bbsymphony.muslimprayers.alert.NotificationMuslimPrayers;
import com.bbsymphony.muslimprayers.alert.NotificationService;
import com.bbsymphony.muslimprayers.alert.SalatAlarmService;
import com.bbsymphony.muslimprayers.setting.SettingsActivity;

public class MuslimPrayersBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG    = "BroadcastReceiver";

    private static final int ALERT_FAJR = 1000;
    private static final int ALERT_DUHR = 2000;
    private static final int ALERT_ASR = 3000;
    private static final int ALERT_MAGHRIB = 4000;
    private static final int ALERT_ISHAA = 5000;

    private static final int ALERT_WUDU_FAJR = 1100;
    private static final int ALERT_WUDU_DUHR = 2100;
    private static final int ALERT_WUDU_ASR = 3100;
    private static final int ALERT_WUDU_MAGHRIB = 4100;
    private static final int ALERT_WUDU_ISHAA = 5100;

    private static final int ALERT_NOTIFICATION_FAJR = 1200;
    private static final int ALERT_NOTIFICATION_DUHR = 2200;
    private static final int ALERT_NOTIFICATION_ASR = 3200;
    private static final int ALERT_NOTIFICATION_MAGHRIB = 4200;
    private static final int ALERT_NOTIFICATION_ISHAA = 5200;

    private DateFormat df = new SimpleDateFormat("HH:mm");
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
    private boolean locationON;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.w(LOG, "[ONRECEIVE] BroadcastReceiver");

        //RemoteViews remoteViews;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.muslim_prayers);

        Log.w(LOG,"[ON RECEIVE] Action: " + intent.getAction());
        this.prefs = context.getApplicationContext().getSharedPreferences(ConfigurationClass.SHARED_PREF, context.MODE_PRIVATE);

        if (MuslimPrayers.SETTING_CLICK.equals(intent.getAction())) {
            try {
                Log.d(LOG, "[ON RECEIVE] Click-on Prev button ");
                Log.d(LOG, "[SETTING]");

                // Activating capabilities to read/write Settings
                StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                        .permitDiskWrites().build());

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
        this.setDefaultLocation(remoteViews, context, prefs.getBoolean("daylight_saving_id", true));
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

    public void setMuslimPrayersTime (RemoteViews views, Context context, Intent intent,
                                      double lattitude, double longitude, double timezone){
        // Villeneuve d'Ascq
        //double lattitude = 50.6329700;
        //double longitude = 3.0585800;
        //double timezone = 2;

        // Sioux Falls
        //this.lattitude = 43.55;
        //this.longitude = -96.7;
        //this.timezone = -5;

        Log.d(LOG,"Longitude: " + this.longitude + " Lattitude: " + this.lattitude + " TimeZone: " + this.timezone);

        if (this.locationON) {

            this.prefs = context.getSharedPreferences(ConfigurationClass.SHARED_PREF, Context.MODE_PRIVATE);

            Log.d(LOG, "Convention Calculation convention_id: " + prefs.getString("convention_id", null));
            Log.d(LOG, "Convention Calculation juristic_id: " + prefs.getString("juristic_id", "999"));
            prayers.setTimeFormat(ConfigurationClass.TIME24);

            //prayers.setCalcMethod(prayers.Jafari);
            if (!prefs.getBoolean("customAngle_id", false)) {
                Log.d(LOG, " No Custom calculation");
                prayers.setCalcMethod(Integer.parseInt(prefs.getString("convention_id", Integer.toString(ConfigurationClass.METHOD_CUSTOM))));
            }
            else {
                prayers.setCalcMethod(ConfigurationClass.METHOD_CUSTOM);
                Log.d(LOG, " Custom calculation: fajr: " + prefs.getString("fajr_angle_id", "15"));
                prayers.setCustomParams(new double []{
                        Double.parseDouble(prefs.getString("fajr_angle_id", "15")),
                        1,0,0,
                        Double.parseDouble(prefs.getString("isha_angle_id", "15"))});
            }

            prayers.setAsrJuristic(Integer.parseInt(prefs.getString("juristic_id", Integer.toString(ConfigurationClass.JURISTIC_SHAFII))));
            prayers.setAdjustHighLats(Integer.parseInt(prefs.getString("higher_lattitude_id", Integer.toString(ConfigurationClass.ADJ_METHOD_ANGLEBASED))));
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
                Log.d(LOG, "Canceling alerts...");
                AlarmManager alarmManager = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);
                PendingIntent intentFajr = PendingIntent.getBroadcast(context,
                        ALERT_FAJR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentFajr);
                PendingIntent intentDuhr = PendingIntent.getBroadcast(context,
                        ALERT_DUHR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentDuhr);
                PendingIntent intentAsr = PendingIntent.getBroadcast(context,
                        ALERT_ASR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentAsr);
                PendingIntent intentMaghrib = PendingIntent.getBroadcast(context,
                        ALERT_MAGHRIB, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentMaghrib);
                PendingIntent intentIshaa = PendingIntent.getBroadcast(context,
                        ALERT_ISHAA, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentIshaa);

                //Cancelling Wudu alerts
                PendingIntent intentFajrWudu = PendingIntent.getBroadcast(context,
                        ALERT_WUDU_FAJR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentFajrWudu);
                PendingIntent intentDuhrWudu = PendingIntent.getBroadcast(context,
                        ALERT_WUDU_DUHR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentDuhrWudu);
                PendingIntent intentAsrWudu = PendingIntent.getBroadcast(context,
                        ALERT_WUDU_ASR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentAsr);
                PendingIntent intentMaghribWudu = PendingIntent.getBroadcast(context,
                        ALERT_WUDU_MAGHRIB, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentMaghribWudu);
                PendingIntent intentIshaaWudu = PendingIntent.getBroadcast(context,
                        ALERT_WUDU_ISHAA, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentIshaaWudu);

                //Cancelling Notification alerts
                PendingIntent intentFajrNotification = PendingIntent.getBroadcast(context,
                        ALERT_NOTIFICATION_FAJR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentFajrNotification);
                PendingIntent intentDuhrNotification = PendingIntent.getBroadcast(context,
                        ALERT_NOTIFICATION_DUHR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentDuhrNotification);
                PendingIntent intentAsrNotification = PendingIntent.getBroadcast(context,
                        ALERT_NOTIFICATION_ASR, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentAsrNotification);
                PendingIntent intentMaghribNotification = PendingIntent.getBroadcast(context,
                        ALERT_NOTIFICATION_MAGHRIB, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentMaghribNotification);
                PendingIntent intentIshaaNotification = PendingIntent.getBroadcast(context,
                        ALERT_NOTIFICATION_ISHAA, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(intentIshaaNotification);

            } catch (Exception e) {
                Log.d(LOG, "Exception unset alarm - " + e.toString());
            }
            Intent intentAlarm = new Intent(context, SalatAlarmService.class);
            Intent intentNotification = new Intent(context, NotificationService.class);
            Date now = new Date();
            Boolean isNotificationSalatON = prefs.getBoolean("notifications_salat_id", true);
            Boolean isNotificationWuduON = ! prefs.getString("notifications_abulition_id", "999").equals("0");
            Boolean isNotificationON = prefs.getBoolean("notifications_new_event_id", true);

            Log.d(LOG, "Notification is: " + isNotificationWuduON);

            ArrayList<String> timeSalat = prayers.getTimeSalat();
            ArrayList<String> timeWudu = prayers.getTimeSalat();

            if (isNotificationSalatON) {
                Log.d(LOG, "Salam is ON");
                intentAlarm.setAction(MuslimPrayers.SALAT_TIME);
                intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (isNotificationWuduON) {
                Log.d(LOG, "WUDU is ON");
                intentAlarm.setAction(MuslimPrayers.ABULITION_TIME);
                intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int delay = Integer.parseInt(prefs.getString("notifications_abulition_id", "999"));
                for (int i = 0; i < timeWudu.size(); i++) {
                    try {
                        Calendar calWudu = Calendar.getInstance();
                        calWudu.setTime(df.parse(timeSalat.get(i)));
                        calWudu.add(Calendar.MINUTE, delay * -1);
                        timeWudu.set(i, df.format(calWudu.getTime()));
                        Log.d(LOG, "Wudu calculation: " + timeWudu.get(i) + " calWudu: " + calWudu + " delay:" + delay);
                        Log.d(LOG, "Cal.getTime(): " + calWudu.getTime());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_FAJR, timeWudu.get(0));
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_SUNRISE, timeWudu.get(1));
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_DUHR, timeWudu.get(2));
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_ASR, timeWudu.get(3));
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_MAGHRIB, timeWudu.get(4));
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_SUNSET, timeWudu.get(5));
                intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_ISHAA, timeWudu.get(5));
            }

            /** ************************************************************************
             *                              Fajr Management
             * ************************************************************************
             */
            Log.d(LOG,"Fajr Time:" + getTimeDelayed(prayers.getFajrTime(), Integer.parseInt(prefs.getString("fajr_adj_id","0")))
                    + " vs. Now: " + now + " results:" + stringToDate(prayers.getFajrTime(), prefs.getString("fajr_adj_id","0")).compareTo(now));
            if (stringToDate(prayers.getFajrTime(), prefs.getString("fajr_adj_id","0")).compareTo(now) > 0) {
                if (isNotificationSalatON) {
                    Log.d(LOG, "Enabling alerts...");
                    intentAlarm.putExtra(ConfigurationClass.EXTRA_FAJR, prayers.getFajrTime());
                    PendingIntent pendingIntentFajr = PendingIntent.getService(context, ALERT_FAJR,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, pendingIntentFajr, prayers.getFajrTime());
                }
                if (isNotificationWuduON) {
                    PendingIntent wpendingIntentFajr = PendingIntent.getService(context, ALERT_WUDU_FAJR,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, wpendingIntentFajr, prayers.getFajrTime());
                }
                if (isNotificationON) {
                    Log.d(LOG, " Fajr notification: " + getTimeDelayed(prayers.getFajrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                    intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_FAJR, getTimeDelayed(prayers.getFajrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                    PendingIntent pendingIntentFajr = PendingIntent.getService(context, ALERT_NOTIFICATION_FAJR,
                            intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
                    setNotification(context, pendingIntentFajr, getTimeDelayed(prayers.getFajrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                }
            }


            /** ************************************************************************
             *                              Duhr Management
             * ************************************************************************
             */
            if (stringToDate(prayers.getDuhrTime()).compareTo(now) > 0) {
                if (isNotificationSalatON) {
                    intentAlarm.putExtra(ConfigurationClass.EXTRA_DUHR, prayers.getDuhrTime());
                    PendingIntent pendingIntentDuhr = PendingIntent.getService(context, ALERT_DUHR,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, pendingIntentDuhr, prayers.getDuhrTime());
                }
                if (isNotificationWuduON) {
                    PendingIntent wpendingIntentDuhr = PendingIntent.getService(context, ALERT_WUDU_DUHR,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, wpendingIntentDuhr, prayers.getDuhrTime());
                }
                if (isNotificationON) {
                    intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_DUHR, getTimeDelayed(prayers.getDuhrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                    PendingIntent pendingIntentDuhr = PendingIntent.getService(context, ALERT_NOTIFICATION_DUHR,
                            intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
                    setNotification(context, pendingIntentDuhr, getTimeDelayed(prayers.getDuhrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                }
            }


            /** ************************************************************************
             *                              Asr Management
             * ************************************************************************
             */
            if (stringToDate(prayers.getAsrTime()).compareTo(now) > 0) {
                if (isNotificationSalatON) {
                    intentAlarm.putExtra(ConfigurationClass.EXTRA_ASR, prayers.getAsrTime());
                    PendingIntent pendingIntentAsr = PendingIntent.getService(context, ALERT_ASR,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, pendingIntentAsr, prayers.getAsrTime());
                }
                if (isNotificationWuduON) {
                    PendingIntent wpendingIntentAsr = PendingIntent.getService(context, ALERT_WUDU_ASR,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, wpendingIntentAsr, prayers.getAsrTime());
                }
                if (isNotificationON) {
                    intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_ASR, getTimeDelayed(prayers.getAsrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                    PendingIntent pendingIntentAsr = PendingIntent.getService(context, ALERT_NOTIFICATION_ASR,
                            intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
                    setNotification(context, pendingIntentAsr, getTimeDelayed(prayers.getAsrTime(), ConfigurationClass.DELAY_NOTIFICATION));
                }
            }

            /** ************************************************************************
             *                              Maghrib Management
             * ************************************************************************
             */
            if (stringToDate(prayers.getMaghribTime()).compareTo(now) > 0) {
                if (isNotificationSalatON) {
                    intentAlarm.putExtra(ConfigurationClass.EXTRA_MAGHRIB, prayers.getMaghribTime());
                    PendingIntent pendingIntentMaghrib = PendingIntent.getService(context, ALERT_MAGHRIB,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, pendingIntentMaghrib, prayers.getMaghribTime());
                }
                if (isNotificationWuduON) {
                    PendingIntent wpendingIntentMaghrib = PendingIntent.getService(context, ALERT_WUDU_MAGHRIB,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, wpendingIntentMaghrib, getTimeDelayed(prayers.getMaghribTime(), ConfigurationClass.DELAY_NOTIFICATION));
                }
                if (isNotificationON) {
                    intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_MAGHRIB, getTimeDelayed(prayers.getMaghribTime(), ConfigurationClass.DELAY_NOTIFICATION));
                    PendingIntent pendingIntentMaghrib = PendingIntent.getService(context, ALERT_NOTIFICATION_MAGHRIB,
                            intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
                    setNotification(context, pendingIntentMaghrib, getTimeDelayed(prayers.getMaghribTime(), ConfigurationClass.DELAY_NOTIFICATION));
                }
            }

            /** ************************************************************************
             *                              Ishaa Management
             * ************************************************************************
             */
            if (stringToDate(prayers.getIshaTime()).compareTo(now) > 0) {
                if (isNotificationSalatON) {
                    intentAlarm.putExtra(ConfigurationClass.EXTRA_ISHAA, prayers.getIshaTime());
                    PendingIntent pendingIntentIsha = PendingIntent.getService(context, ALERT_ISHAA,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, pendingIntentIsha, prayers.getIshaTime());
                }
                if (isNotificationWuduON) {
                    PendingIntent wpendingIntentIsha = PendingIntent.getService(context, ALERT_WUDU_ISHAA,
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                    setAlarmSalat(context, wpendingIntentIsha, prayers.getIshaTime());
                }
                if (isNotificationON) {
                    intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_ISHAA, getTimeDelayed(prayers.getIshaTime(), ConfigurationClass.DELAY_NOTIFICATION));
                    PendingIntent pendingIntentIshaa = PendingIntent.getService(context, ALERT_NOTIFICATION_ISHAA,
                            intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
                    setNotification(context, pendingIntentIshaa, getTimeDelayed(prayers.getIshaTime(), ConfigurationClass.DELAY_NOTIFICATION));
                }
            }

        }
        else { // Longitude and lattitude not found
            views.setTextViewText(R.id.fajr_time, "--:--");
            views.setTextViewText(R.id.duhr_time, "--:--");
            views.setTextViewText(R.id.asr_time, "--:--");
            views.setTextViewText(R.id.maghrib_time, "--:--");
            views.setTextViewText(R.id.isha_time, "--:--");
            views.setTextViewText(R.id.sunrise_id, "--:--");
            views.setTextViewText(R.id.sunset_id, "--:--");
        }

    }

    public Date getDateDisplayed() {
        return dateDisplayed;
    }

    public void setDateDisplayedViews(Date dateDisplayed) {
        this.dateDisplayed = dateDisplayed;
    }

    public void setDefaultLocation (Context contezt){
    }

    public void setDefaultLocation(RemoteViews remoteViews, Context context, boolean dayLightSaving) {
        this.locationON = false;
        myLocation = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Criteria c=new Criteria();
        provider = myLocation.getBestProvider(c, false);
        //now you have best provider
        //get location
        try {
            address = myLocation.getLastKnownLocation(provider);
            if (address != null) {

                this.locationON = true;
                //get latitude and longitude of the location
                this.longitude = address.getLongitude();
                this.lattitude = address.getLatitude();
                this.timezone = (TimeZone.getDefault().getRawOffset() / 1000.0) / 3600;

                this.prefs = context.getApplicationContext().getSharedPreferences(ConfigurationClass.SHARED_PREF, context.MODE_PRIVATE);
                if (dayLightSaving) {
                    this.timezone = this.timezone + (TimeZone.getDefault().getDSTSavings()/1000)/3600;
                }

                //display on text view
                Log.d(LOG, "[Location] Longitude: " + longitude + " lattitude:" + lattitude + " timezone:" + timezone + " Daylight: " + (TimeZone.getDefault().getDSTSavings() / 1000) / 3600);

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
                this.locationON = false;
                Log.d(LOG, "[Location] Not available - Can't getLastKnownLocation");
            }
        } catch (Exception e) {
            Log.d(LOG, "[Location] Not available - Exception " + e);
            remoteViews.setTextViewText(R.id.city_id, "Not available");
        }
    }

    private Calendar stringToCalendar (String time) {
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
        return calendar;
    }

    private Date stringToDate (String time) {
        return stringToCalendar(time).getTime();
    }


    private Date stringToDate (String time, String adjustment) {
        Calendar calendar = stringToCalendar(time);
        try {
            calendar.add(Calendar.MINUTE, Integer.parseInt(adjustment));
        } catch (Exception e) { Log.d(LOG, e.toString());}
        return calendar.getTime();
    }

    public void setAlarmSalat(Context context, PendingIntent intent, String time) {
        Calendar calendar = stringToCalendar(time);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
    }


    public void setNotification (Context context, PendingIntent intent, String time) {
        Calendar calendar = stringToCalendar(time);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
    }

    public String getTimeDelayed (String prayerTime, int additional) {
        Calendar calendar = stringToCalendar(prayerTime);
        calendar.add(Calendar.MINUTE, additional);

        return df.format(calendar.getTime());
    }

}

