package com.bbsymphony.muslimprayers.setting;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;


import com.bbsymphony.muslimprayers.ConfigurationClass;
import com.bbsymphony.muslimprayers.MuslimPrayers;
import com.bbsymphony.muslimprayers.MuslimPrayersBroadcastReceiver;
import com.bbsymphony.muslimprayers.R;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity
        extends PreferenceActivity
        implements LocationListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    public static String LOG = "SettingsActivity";

    // Preference change
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    // Location Management
    private LocationManager myLocation;
    private String provider;
    private Location address;



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        Log.d(LOG, "[SETTING] Activity");
        super.onPostCreate(savedInstanceState);
        Log.d(LOG, "[SETTING] ON CREATE - Preference name:" + this.getPreferenceManager().getSharedPreferencesName());
        setupSimplePreferencesScreen();

        // Preference Test Adhan button management
        Preference buttonAdhan = (Preference) getPreferenceManager().findPreference("notifications_test_id");
        if (buttonAdhan != null) {
            Log.d(LOG,"[ONCREATE] STEP 1 - Button setUp");
            buttonAdhan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private String LOG = "OnPreferenceClickListener";
                private MediaPlayer mp;
                @Override
                public boolean onPreferenceClick(Preference prefs) {
                    //finish();
                    Log.d(LOG, "Click on Media Player Salat Test");
                    try {
                        if (mp != null) {
                            Log.d(LOG, "Killing actaul media player...");
                            mp.stop();
                            mp.release();
                            mp = null;
                        }
                        else {
                            Log.d(LOG, "Playing media player wudu_djouher072015...");
                            mp = MediaPlayer.create(prefs.getContext(), R.raw.adhan_makkah);
                            mp.start();
                        }
                    } catch (Exception e) {
                        Log.d(LOG, e.toString());
                    }
                    return true;
                }
            });
        }

        // Preference Test Wudu button management
        Preference buttonTestWudu = (Preference) getPreferenceManager().findPreference("notifications_test_wudu_id");
        if (buttonTestWudu != null) {
            Log.d(LOG,"[ONCREATE] STEP 1 - Button setUp");
            buttonTestWudu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private String LOG = "OnPreferenceClickListenerWudu";
                private MediaPlayer mp;

                @Override
                public boolean onPreferenceClick(Preference prefs) {
                    //finish();
                    Log.d(LOG, "Click on Test Wudu");
                    try {
                        if (mp != null) {
                            Log.d(LOG, "Killing actaul media player...");
                            mp.stop();
                            mp.release();
                            mp = null;
                        }
                        else {
                            Log.d(LOG, "Playing media player wudu_djouher072015...");
                            mp = MediaPlayer.create(prefs.getContext(), R.raw.wudu_djouher072015);
                            mp.start();
                        }
                    } catch (Exception e) {
                        Log.d(LOG, e.toString());
                    }
                    return true;
                }
            });
        }

        // Preference Recommendation button management
        Preference recommendationBtn = (Preference) getPreferenceManager().findPreference("recommendation_id");
        if (recommendationBtn != null) {

            Log.d(LOG, "[ONCREATE] STEP 1 - recommendationBtn setUp");
            recommendationBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private String LOG = "OnPreferenceClickListener";

                @Override
                public boolean onPreferenceClick(Preference prefs) {
                    //finish()
                    return true;
                }
            });
        }
    }


    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }


        Log.d(LOG, "[SETTING] Set Security");

        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                .permitDiskWrites()
                .build());
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.

        addPreferencesFromResource(R.xml.pref_general);

        // Add 'prayer times' preferences, and a corresponding header.
        PreferenceCategory fakeHeaderPrayerTime = new PreferenceCategory(this);
        fakeHeaderPrayerTime.setTitle(R.string.pref_header_prayers_time);
        getPreferenceScreen().addPreference(fakeHeaderPrayerTime);
        addPreferencesFromResource(R.xml.pref_prayers_times);

        // Add 'adjustment' preferences, and a corresponding header.
        PreferenceCategory fakeHeaderAdjustment = new PreferenceCategory(this);
        fakeHeaderAdjustment.setTitle(R.string.pref_header_prayers_time_adjustment);
        getPreferenceScreen().addPreference(fakeHeaderAdjustment);
        addPreferencesFromResource(R.xml.pref_prayers_times_adjustments);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeaderNotification = new PreferenceCategory(this);
        fakeHeaderNotification.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeaderNotification);
        addPreferencesFromResource(R.xml.pref_notification);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        //bindPreferenceSummaryToValue(findPreference("location_id"));
        bindPreferenceSummaryToValue(findPreference("customAngle_id"));
        bindPreferenceSummaryToValue(findPreference("convention_id"));
        bindPreferenceSummaryToValue(findPreference("fajr_angle_id"));
        bindPreferenceSummaryToValue(findPreference("isha_angle_id"));
        bindPreferenceSummaryToValue(findPreference("juristic_id"));
        bindPreferenceSummaryToValue(findPreference("higher_lattitude_id"));
        bindPreferenceSummaryToValue(findPreference("fajr_adj_id"));
        bindPreferenceSummaryToValue(findPreference("duhr_adj_id"));
        bindPreferenceSummaryToValue(findPreference("asr_adj_id"));
        bindPreferenceSummaryToValue(findPreference("maghreb_adj_id"));
        bindPreferenceSummaryToValue(findPreference("ishaa_adj_id"));
        bindPreferenceSummaryToValue(findPreference("notifications_abulition_id"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK) >= android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        Log.d(LOG, "Building headers...");
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            String LOG = "PreferenceChangeListener";
            Log.d(LOG, "[RECOMMENDATION] STP 0 - preference key:" + stringValue);

            if (preference.getKey().equals("customAngle_id")) {

                Log.d(LOG, "Custom Angles click: " + value);
                //Boolean booleanValue = new Boolean((Boolean) value);

                SharedPreferences sp =  preference.getSharedPreferences(); //.getSharedPreferences(ConfigurationClass.SHARED_PREF, Context.MODE_PRIVATE);
                PreferenceManager pm = preference.getPreferenceManager();
                ListPreference conventionList = (ListPreference) pm.findPreference("convention_id");

                if (value.toString().equals("true")) {
                    Log.d(LOG, "Value is true: " + value);
                    conventionList.setEnabled(false);
                } else {
                    Log.d(LOG, "Value is false: " +  value);
                    conventionList.setEnabled(true); }
                stringValue = "";
            }

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                String angles = "";
                Log.d(LOG,"Change on Convention ...");
                if (preference.getKey().equals("convention_id")) {
                    switch (Integer.parseInt(stringValue)) {
                        case ConfigurationClass.METHOD_EGYPT:
                            angles = angles + "(19.5°/17.5°)";
                            break;
                        case ConfigurationClass.METHOD_ISNA:
                            angles =  angles + "(15°/15°)";
                            break;
                        case ConfigurationClass.METHOD_KARACHI:
                            angles =  angles + "(18°/18°)";
                            break;
                        case ConfigurationClass.METHOD_MAKKAH:
                            angles =  angles + "(18.5°/90 min after Maghrib, 120 min during Ramadhan)";
                            break;
                        case ConfigurationClass.METHOD_MWL:
                            angles =  angles + "(18°/17°)";
                            break;
                        case ConfigurationClass.METHOD_JAFARI:
                            angles =  angles + "(16°/14°)";
                            break;
                        case ConfigurationClass.METHOD_TEHERAN:
                            angles =  angles + "(17.7°/14°)";
                            break;
                        case ConfigurationClass.METHOD_CUSTOM:
                            // implement fajr/isha selection
                            Log.d(LOG, "Custom angle selection...");

                            break;
                        default:
                            angles = "";
                            break;
                    }
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index] + "\n" + angles
                                    : null);

                } else if (preference.getKey().equals("recommendation_id")) {
                    Log.d(LOG, "Reselect default value - recommedation_id");
                    ((ListPreference) preference).setValue(ConfigurationClass.CANCEL_RECOMMENDATION);
                }
                else {
                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);
                }
            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if (preference.getKey().equals("fajr_adj_id") ||
                        preference.getKey().equals("duhr_adj_id") ||
                        preference.getKey().equals("asr_adj_id") ||
                        preference.getKey().equals("maghreb_adj_id") ||
                        preference.getKey().equals("ishaa_adj_id") ||
                        preference.getKey().equals("notifications_abulition_id")) {
                    if (stringValue.equals("0") || stringValue.equals("1") || stringValue.equals("-1")) {
                        preference.setSummary(stringValue + " minute");
                    }
                    else
                        preference.setSummary(stringValue + " minutes");
                }
                else
                    preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference.getKey().equals("customAngle_id")) {
            String value = String.valueOf(PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getBoolean("customAngle_id", true));
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,value);
        }
        else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG,"Location changed...");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG,"Provider is enabling...");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG,"Provider is disabling...");
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private String LOG = "General";
        @Override
        public void onCreate(Bundle savedInstanceState){

        Log.d(LOG, "[SETTING_GENERAL] Set Security");
        super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("recommendation_id"));
            //bindPreferenceSummaryToValue(findPreference("example_list"));
        }

    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_title_abuliton"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }

    /**
     * This fragment shows prayers times preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrayerTimesPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(LOG, "Calling on create fragment");
            addPreferencesFromResource(R.xml.pref_prayers_times);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_list_convention_titles"));
        }


        /*@Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            Log.d(LOG, "Change on custom angle " + prefs.getBoolean(key, false));
            if (key.equals("customAngle_id")) {

                Log.d(LOG, "Custom Angles click: " + prefs.getBoolean(key, false));

                Preference conventionCK = findPreference("convention_id");
                //SharedPreferences sp =  getApplicationContext().getSharedPreferences(ConfigurationClass.SHARED_PREF, Context.MODE_PRIVATE);
                //CheckBoxPreference conventionCK = (CheckBoxPreference) sp.findPreference(sp.getString(R.string.convention_id));
                if (prefs.getBoolean(key,false)) {
                    Log.d(LOG, "Value is true: " + prefs.getBoolean(key,false));
                    conventionCK.setEnabled(false);
                } else {
                    Log.d(LOG, "Value is false: " + prefs.getBoolean(key,false));
                    conventionCK.setEnabled(true); }
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }*/
    }


    /**
     * This fragment shows prayers times preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrayerTimesAdjustmentPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_prayers_times_adjustments);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_fajr_adjustment"));
            bindPreferenceSummaryToValue(findPreference("pref_duhr_adjustment"));
            bindPreferenceSummaryToValue(findPreference("pref_asr_adjustment"));
            bindPreferenceSummaryToValue(findPreference("pref_maghreb_adjustment"));
            bindPreferenceSummaryToValue(findPreference("pref_ishaa_adjustment"));

        }
    }
    @Override
    public void onBackPressed() {

        Log.d(LOG, "onBackPressed() ");
        // this is the intent broadcast/returned to the widget
        Intent updateIntent = new Intent(this, MuslimPrayersBroadcastReceiver.class);
        updateIntent.setAction(MuslimPrayers.PREFERENCE_UPDATE);
        sendBroadcast(updateIntent);
        super.onBackPressed();
    }


    @Override
    protected void onResume() {
        Log.d(LOG, "[ON RESUME]");
        super.onResume();
        // Set up a listener whenever a key changes

        // Use instance field for listener // It will not be gc'd as long as this instance is kept referenced
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            private String subject;
            private String body;
            private String to;

            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                Log.d(LOG, "[ON RESUME] key:" + key);

                if (key.equals("recommendation_id")) {
                    if (prefs.getString(key,null).equals(ConfigurationClass.SHARED_RECOMMENDATION)) {
                        subject = "[MuslimPrayers] Sharing ideas...";
                        body = "Here some suggestions \n\n\n\nRegards,\nAdvicer";
                        to="muslimprayers@bbsymphony.com";
                    } else if (prefs.getString(key,null).equals(ConfigurationClass.HELP_RECOMMENDATION)) {
                        subject = "[MuslimPrayers] Help request...";
                        body = "Here looking for some help/guidance on \n\n\n\nRegards,\nHelp needed";
                        to="muslimprayers@bbsymphony.com";
                    } else if (prefs.getString(key,null).equals(ConfigurationClass.BUG_RECOMMENDATION)) {
                        subject = "[MuslimPrayers] Bug report...";
                        body = "Found a bug in the apps, \n\n\n\nRegards,\nHelp needed";
                        to="muslimprayers@bbsymphony.com";
                    }
                    if (!prefs.getString(key,null).equals(ConfigurationClass.CANCEL_RECOMMENDATION)) {
                        Log.d(LOG, "[RECOMMENDATION] prefernce key:" + key);
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        //i.setType("text/plain"); //use this line for testing in the emulator
                        emailIntent.setType("message/rfc822"); // use from live device
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{this.to});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, this.subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, this.body);
                        startActivity(Intent.createChooser(emailIntent, "Select email application."));

                        ListPreference lp = (ListPreference)findPreference("recommendation_id");
                        Log.d(LOG, "Setting value...");
                        lp.setValue(ConfigurationClass.CANCEL_RECOMMENDATION); // Cancel
                    }
                }

            }
        };
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        Log.d(LOG, "[Onchange] Listener registered");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

}


