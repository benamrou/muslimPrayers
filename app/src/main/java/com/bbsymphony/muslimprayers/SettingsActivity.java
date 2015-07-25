package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 7/16/2015.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
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
    public static String SHARED_RECOMMENDATION ="0";
    public static String HELP_RECOMMENDATION ="1";
    public static String BUG_RECOMMENDATION ="2";
    public static String CANCEL_RECOMMENDATION ="3";


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
        Log.d(LOG,"[SETTING] ON CREATE - Preference name:" + this.getPreferenceManager().getSharedPreferencesName());
        setupSimplePreferencesScreen();
        Preference button = (Preference) getPreferenceManager().findPreference("notifications_test_id");
        if (button != null) {
            Log.d(LOG,"[ONCREATE] STEP 1 - Button setUp");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private String LOG = "OnPreferenceClickListener";

                @Override
                public boolean onPreferenceClick(Preference prefs) {
                    //finish();
                    Log.d(LOG, "Click on Test");
                    MediaPlayer mp = MediaPlayer.create(prefs.getContext(), R.raw.adhan_makkah);
                    mp.start();
                    return true;
                }
            });
        }

        Preference recommendationBtn = (Preference) getPreferenceManager().findPreference("recommendation_id");
        if (recommendationBtn != null) {
            Log.d(LOG, "[ONCREATE] STEP 1 - recommendationBtn setUp");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private String LOG = "OnPreferenceClickListener";

                @Override
                public boolean onPreferenceClick(Preference prefs) {
                    //finish();
                    Log.d(LOG, "Click on recommendationBtn");

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
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_prayers_time);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_prayers_times);

        // Add 'adjustment' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_prayers_time_adjustment);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_prayers_times_adjustments);

        // Add 'notifications' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        // fakeHeader = new PreferenceCategory(this);
        // fakeHeader.setTitle(R.string.pref_header_data_sync);
        // getPreferenceScreen().addPreference(fakeHeader);
        // addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        //bindPreferenceSummaryToValue(findPreference("location_id"));
        bindPreferenceSummaryToValue(findPreference("convention_id"));
        bindPreferenceSummaryToValue(findPreference("juristic_id"));
        bindPreferenceSummaryToValue(findPreference("higher_lattitude_id"));
        bindPreferenceSummaryToValue(findPreference("fajr_adj_id"));
        bindPreferenceSummaryToValue(findPreference("duhr_adj_id"));
        bindPreferenceSummaryToValue(findPreference("asr_adj_id"));
        bindPreferenceSummaryToValue(findPreference("maghreb_adj_id"));
        bindPreferenceSummaryToValue(findPreference("ishaa_adj_id"));
        bindPreferenceSummaryToValue(findPreference("notifications_abulition_id"));
        //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
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
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
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

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                String angles = "";
                if (preference.getKey().equals("convention_id")) {
                    switch (Integer.parseInt(stringValue)) {
                        case PrayTime.METHOD_EGYPT:
                            angles = angles + "(19.5°/17.5°)";
                            break;
                        case PrayTime.METHOD_ISNA:
                            angles =  angles + "(15°/15°)";
                            break;
                        case PrayTime.METHOD_KARACHI:
                            angles =  angles + "(18°/18°)";
                            break;
                        case PrayTime.METHOD_MAKKAH:
                            angles =  angles + "(18.5°/90 min after Maghrib, 120 min during Ramadhan)";
                            break;
                        case PrayTime.METHOD_MWL:
                            angles =  angles + "(18°/17°)";
                            break;
                        case PrayTime.METHOD_JAFARI:
                            angles =  angles + "(16°/14°)";
                            break;
                        case PrayTime.METHOD_TEHERAN:
                            angles =  angles + "(17.7°/14°)";
                            break;
                        case PrayTime.METHOD_CUSTOM:
                            // implement fajr/isha selection
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
                    ((ListPreference) preference).setValue(CANCEL_RECOMMENDATION);
                }
                else {
                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);
                }
            }
            /*
            else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }*/
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
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG,"[onSharedPreferenceChanged] key:" + key);
        //ListPreference lp = (ListPreference) findPreference(key);
        //Log.d(LOG,"[Onchange] key:" + key + " lp:" + lp.getValue());
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
        public void onCreate(Bundle savedInstanceState) {

            Log.d(LOG, "[SETTING_GENERAL] Set Security");
            StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                    .permitDiskWrites()
                    .build());
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
            //addPreferencesFromResource(R.xml.pref_prayers_times);

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


    /**
     * This fragment shows prayers times preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrayerTimesAdjustmentPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_prayers_times);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_list_convention_titles"));
        }
    }
    @Override
    public void onBackPressed() {

        Log.d(LOG, "onBackPressed() ");
        // this is the intent broadcast/returned to the widget
        Intent updateIntent = new Intent(this, MuslimPrayersBroadcastReceiver.class);
        updateIntent.setAction(MuslimPrayers.PREFERENCE_UPDATE);
        String defaultValue = getResources().getString(R.string.pref_list_convention_value_default);
        //ListPreference lp = (ListPreference) findPreference("convention_id");
        //(this.getPreferences((MODE_PRIVATE)).getInt(new String("" + R.string.pref_header_convention),-1)));
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

                if (key.equals("recommendaton_id")) {
                    if (prefs.getString(key,null).equals(SHARED_RECOMMENDATION)) {
                        subject = "[MuslimPrayers] Sharing ideas...";
                        body = "Here some suggestions \n\n\n\nRegards,\nAdvicer";
                        to="muslimprayers@bbsymphony.com";
                    } else if (prefs.getString(key,null).equals(HELP_RECOMMENDATION)) {
                        subject = "[MuslimPrayers] Help request...";
                        body = "Here looking for some help/guidance on \n\n\n\nRegards,\nHelp needed";
                        to="muslimprayers@bbsymphony.com";
                    } else if (prefs.getString(key,null).equals(BUG_RECOMMENDATION)) {
                        subject = "[MuslimPrayers] Bug report...";
                        body = "Found a bug in the apps, \n\n\n\nRegards,\nHelp needed";
                        to="muslimprayers@bbsymphony.com";
                    }
                    if (!prefs.getString(key,null).equals(CANCEL_RECOMMENDATION)) {
                        Log.d(LOG, "[RECOMMENDATION] prefernce key:" + key);
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        //i.setType("text/plain"); //use this line for testing in the emulator
                        emailIntent.setType("message/rfc822"); // use from live device
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {this.to});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, this.subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, this.body);
                        startActivity(Intent.createChooser(emailIntent, "Select email application."));

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


