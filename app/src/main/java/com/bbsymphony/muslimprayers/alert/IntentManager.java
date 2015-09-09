package com.bbsymphony.muslimprayers.alert;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import com.bbsymphony.muslimprayers.ConfigurationClass;
import com.bbsymphony.muslimprayers.MuslimPrayers;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ahmed on 9/8/2015.
 */
public final class IntentManager {

    private static HashSet<Intent> wuduSet = new HashSet<Intent>();  // One item only
    private static HashSet<Intent> salatSet = new HashSet<Intent>(); // One item only
    private static HashSet<Intent> notificationSet = new HashSet<Intent>(); // One item only

    private static HashMap<Integer, AlarmManager> wuduAlarmMap = new HashMap<Integer,AlarmManager>(); // One item by salat (meaning 5)
    private static HashMap<Integer,AlarmManager> salatAlarmMap = new HashMap<Integer,AlarmManager>(); // One item by salat
    private static HashMap<Integer,PendingIntent> wuduPendingIntentMap = new HashMap<Integer,PendingIntent>(); // One item by salat
    private static HashMap<Integer,PendingIntent> salatPendingIntentMap = new HashMap<Integer,PendingIntent>(); // One item by salat

    private static Integer[] salatIdentificator = new Integer[] {ConfigurationClass.ALERT_FAJR,
            ConfigurationClass.ALERT_DUHR,ConfigurationClass.ALERT_ASR,
            ConfigurationClass.ALERT_MAGHRIB, ConfigurationClass.ALERT_ISHAA};

    private static Integer[] wuduIdentificator = new Integer[] {ConfigurationClass.ALERT_WUDU_FAJR,
            ConfigurationClass.ALERT_WUDU_DUHR,ConfigurationClass.ALERT_WUDU_ASR,
            ConfigurationClass.ALERT_WUDU_MAGHRIB, ConfigurationClass.ALERT_WUDU_ISHAA};

    private static Integer[] notificationIdentificator = new Integer[] {ConfigurationClass.ALERT_NOTIFICATION_FAJR,
            ConfigurationClass.ALERT_NOTIFICATION_DUHR,ConfigurationClass.ALERT_NOTIFICATION_ASR,
            ConfigurationClass.ALERT_NOTIFICATION_MAGHRIB, ConfigurationClass.ALERT_NOTIFICATION_ISHAA};

    private static HashMap<Integer,AlarmManager> notificationAlarmMap = new HashMap<Integer,AlarmManager>(); // One item by salat
    private static HashMap<Integer,PendingIntent> notificationPendingIntentMap = new HashMap<Integer,PendingIntent>(); // One item by salat

    private static int iteratorWudu = 0;
    private static int iteratorSalat = 0;
    private static int iteratorNotification = 0;

    private static String LOG ="INTENT MGR";


    public static void cancelWuduIntent (Context context) {
        Intent intentAlarm = new Intent(context, SalatAlarmService.class);

        for (int i =0; i < wuduIdentificator.length; i++) {
            Log.d(LOG, "Cancelling alert wudu: " + wuduIdentificator[i]);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), wuduIdentificator[i] ,intentAlarm,PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.getApplicationContext().ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }

        wuduPendingIntentMap.clear();
        wuduAlarmMap.clear();
        iteratorWudu = 0;
    }

    public static void cancelSalatIntent (Context context) {
        Intent intentAlarm = new Intent(context, SalatAlarmService.class);

        for (int i =0; i < salatIdentificator.length; i++) {
            Log.d(LOG, "Cancelling alert salat: " + salatIdentificator[i]);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), salatIdentificator[i] ,intentAlarm,PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.getApplicationContext().ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
        salatPendingIntentMap.clear();
        salatAlarmMap.clear();
        iteratorSalat = 0;
    }

    public static void cancelNotificationIntent (Context context) {
        Intent intentAlarm = new Intent(context, NotificationService.class);

        for (int i =0; i < notificationIdentificator.length; i++) {
            Log.d(LOG, "Cancelling alert notification: " + notificationIdentificator[i]);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notificationIdentificator[i], intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.getApplicationContext().ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
        notificationPendingIntentMap.clear();
        notificationAlarmMap.clear();
        iteratorNotification = 0;
    }


    public static void manageWuduIntent (Context context, List<String> timeWudu, boolean isNotificationWuduON) {
        Intent intentAlarm = new Intent(context, SalatAlarmService.class);
        cancelWuduIntent(context);

        if (isNotificationWuduON) {
            Log.d(LOG, "WUDU is ON");
            wuduSet.clear();
            intentAlarm.setAction(MuslimPrayers.ABULITION_TIME);
            intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_FAJR, timeWudu.get(0));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_SUNRISE, timeWudu.get(1));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_DUHR, timeWudu.get(2));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_ASR, timeWudu.get(3));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_SUNSET, timeWudu.get(4));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_MAGHRIB, timeWudu.get(5));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_WUDU_ISHAA, timeWudu.get(6));

            PendingIntent wpendingIntentFajr = PendingIntent.getService(context, ConfigurationClass.ALERT_WUDU_FAJR,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmWudu(context, wpendingIntentFajr, timeWudu.get(0));

            PendingIntent wpendingIntentDuhr = PendingIntent.getService(context, ConfigurationClass.ALERT_WUDU_DUHR,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmWudu(context, wpendingIntentDuhr, timeWudu.get(2));

            PendingIntent wpendingIntentAsr = PendingIntent.getService(context, ConfigurationClass.ALERT_WUDU_ASR,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmWudu(context, wpendingIntentAsr, timeWudu.get(3));

            PendingIntent wpendingIntentMaghrib = PendingIntent.getService(context, ConfigurationClass.ALERT_WUDU_MAGHRIB,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmWudu(context, wpendingIntentMaghrib, timeWudu.get(4));

            PendingIntent wpendingIntentIsha = PendingIntent.getService(context, ConfigurationClass.ALERT_WUDU_ISHAA,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmWudu(context, wpendingIntentIsha, timeWudu.get(6));

            wuduSet.add(intentAlarm);
        }
    }

    public static void manageSalatIntent (Context context, List<String> timeSalat, boolean isNotificationSalatON) {
        Intent intentAlarm = new Intent(context, SalatAlarmService.class);
        cancelSalatIntent(context);

        if (isNotificationSalatON) {
            Log.d(LOG, "Salat is ON");
            salatSet.clear();
            intentAlarm.setAction(MuslimPrayers.SALAT_TIME);
            intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentAlarm.putExtra(ConfigurationClass.EXTRA_FAJR, timeSalat.get(0));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_DUHR, timeSalat.get(2));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_ASR, timeSalat.get(3));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_MAGHRIB, timeSalat.get(5));
            intentAlarm.putExtra(ConfigurationClass.EXTRA_ISHAA, timeSalat.get(6));

            PendingIntent wpendingIntentFajr = PendingIntent.getService(context, ConfigurationClass.ALERT_FAJR,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, wpendingIntentFajr, timeSalat.get(0));

            PendingIntent wpendingIntentDuhr = PendingIntent.getService(context, ConfigurationClass.ALERT_DUHR,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, wpendingIntentDuhr, timeSalat.get(2));

            PendingIntent wpendingIntentAsr = PendingIntent.getService(context, ConfigurationClass.ALERT_ASR,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, wpendingIntentAsr, timeSalat.get(3));

            PendingIntent wpendingIntentMaghrib = PendingIntent.getService(context, ConfigurationClass.ALERT_MAGHRIB,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, wpendingIntentMaghrib, timeSalat.get(4));

            PendingIntent wpendingIntentIsha = PendingIntent.getService(context, ConfigurationClass.ALERT_ISHAA,
                    intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            setAlarmSalat(context, wpendingIntentIsha, timeSalat.get(6));

            salatSet.add(intentAlarm);
        }
    }


    public static void manageNotificationIntent (Context context, List<String> timeSalat, boolean isNotificationON, int delay) {
        Intent intentNotification = new Intent(context, NotificationService.class);
        cancelNotificationIntent(context);

        if (isNotificationON) {
            Log.d(LOG, "Salat is ON");
            notificationSet.clear();
            intentNotification.setAction(MuslimPrayers.NOTIFICATION_TIME);
            intentNotification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_FAJR, getTimeDelayed(timeSalat.get(0), delay));
            intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_DUHR, getTimeDelayed(timeSalat.get(2), delay));
            intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_ASR, getTimeDelayed(timeSalat.get(3), delay));
            intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_MAGHRIB, getTimeDelayed(timeSalat.get(4), delay));
            intentNotification.putExtra(ConfigurationClass.EXTRA_NOTIFICATION_ISHAA, getTimeDelayed(timeSalat.get(6), delay));

            PendingIntent pendingIntentFajrNotification = PendingIntent.getService(context, ConfigurationClass.ALERT_NOTIFICATION_FAJR,
                    intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
            setNotification(context, pendingIntentFajrNotification, getTimeDelayed(timeSalat.get(0), delay));

            PendingIntent pendingIntentDuhrNotification = PendingIntent.getService(context, ConfigurationClass.ALERT_NOTIFICATION_DUHR,
                    intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
            setNotification(context, pendingIntentDuhrNotification, getTimeDelayed(timeSalat.get(2),delay));

            PendingIntent pendingIntentAsrNotification = PendingIntent.getService(context, ConfigurationClass.ALERT_NOTIFICATION_ASR,
                    intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
            setNotification(context, pendingIntentAsrNotification, getTimeDelayed(timeSalat.get(3),delay));

            PendingIntent pendingIntentMaghribNotification = PendingIntent.getService(context, ConfigurationClass.ALERT_NOTIFICATION_MAGHRIB,
                    intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
            setNotification(context, pendingIntentMaghribNotification, getTimeDelayed(timeSalat.get(5),delay));

            PendingIntent pendingIntentIshaaNotification = PendingIntent.getService(context, ConfigurationClass.ALERT_NOTIFICATION_ISHAA,
                    intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
            setNotification(context, pendingIntentIshaaNotification, getTimeDelayed(timeSalat.get(6),delay));

            notificationSet.add(intentNotification);
        }

    }

    public static void setAlarmWudu(Context context, PendingIntent intent, String time) {
        Calendar calendar = ConfigurationClass.stringToCalendar(time);
        Date now = new Date();
        if (ConfigurationClass.stringToDate(time).compareTo(now) > 0) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
            Log.d(LOG, "Alarm Wudu set for:" + time + " calendar " + calendar.getTime());

            wuduPendingIntentMap.put(iteratorWudu, intent);
            wuduAlarmMap.put(iteratorWudu, am);
            iteratorWudu = iteratorWudu + 1;
        }

    }

    public static void setAlarmSalat(Context context, PendingIntent intent, String time) {
        Calendar calendar = ConfigurationClass.stringToCalendar(time);

        Date now = new Date();
        if (ConfigurationClass.stringToDate(time).compareTo(now) > 0) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
            Log.d(LOG, "Alarm Salat set for:" + time + " calendar " + calendar.getTime());

            salatPendingIntentMap.put(iteratorSalat, intent);
            salatAlarmMap.put(iteratorSalat, am);
            iteratorSalat = iteratorSalat + 1;
        }
    }


    public static void setNotification (Context context, PendingIntent intent, String time) {
        Calendar calendar = ConfigurationClass.stringToCalendar(time);

        Date now = new Date();
        if (ConfigurationClass.stringToDate(time).compareTo(now) > 0) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
            Log.d(LOG, "Alarm notification set for:" + time + " calendar " + calendar.getTime());

            notificationPendingIntentMap.put(iteratorNotification, intent);
            notificationAlarmMap.put(iteratorNotification, am);
            iteratorNotification = iteratorNotification + 1;
        }
    }

    public static String getTimeDelayed (String prayerTime, int additional) {
        Calendar calendar = ConfigurationClass.stringToCalendar(prayerTime);
        calendar.add(Calendar.MINUTE, additional * -1);

        return ConfigurationClass.getFormat().format(calendar.getTime());
    }

}
