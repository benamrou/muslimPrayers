package com.bbsymphony.muslimprayers;

/**
 * Created by Ahmed on 6/25/2015.
 */

//--------------------- Copyright Block ----------------------
/*

PrayTime.java: Prayer Times Calculator (ver 1.0)
Copyright (C) 2007-2010 PrayTimes.org

Java Code By: Hussain Ali Khan
Original JS Code By: Hamid Zarrabi-Zadeh

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or
	without modification, in any website or application
	provided that credit is given to the original work
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY.

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

*/


import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class PrayTime {

    private static String LOG = "PrayTime";
    // ---------------------- Global Variables --------------------
    private int calcMethod; // caculation method
    private int asrJuristic; // Juristic method for Asr
    private int dhuhrMinutes; // minutes after mid-day for Dhuhr
    private int adjustHighLats; // adjusting method for higher latitudes
    private int timeFormat; // time format
    private double lat; // latitude
    private double lng; // longitude
    private double timeZone; // time-zone
    private double JDate; // Julian date
    // ------------------------------------------------------------

    // Time Names
    private ArrayList<String> timeNames;
    private ArrayList<String> timeSalat;

    private String InvalidTime; // The string used for invalid times
    // --------------------- Technical Settings --------------------
    private int numIterations; // number of iterations needed to compute times
    // ------------------- Calc Method Parameters --------------------
    private HashMap<Integer, double[]> methodParams;

    /*
     * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
     *
     * fa : fajr angle
     * ms : maghrib selector (0 = angle; 1 = minutes after sunset)
     * mv : maghrib parameter value (in angle or minutes)
     * is : isha selector (0 = angle; 1 = minutes after maghrib)
     * iv : isha parameter value (in angle or minutes)
     */
    private double[] prayerTimesCurrent;
    private int[] offsets;

    public PrayTime() {
        // Initialize vars

        this.setCalcMethod(0);
        this.setAsrJuristic(0);
        this.setDhuhrMinutes(0);
        this.setAdjustHighLats(1);
        this.setTimeFormat(0);

        // Time Names
        timeNames = new ArrayList<String>();
        timeNames.add("Fajr");
        timeNames.add("Sunrise");
        timeNames.add("Dhuhr");
        timeNames.add("Asr");
        timeNames.add("Sunset");
        timeNames.add("Maghrib");
        timeNames.add("Isha");

        InvalidTime = "-----"; // The string used for invalid times

        // --------------------- Technical Settings --------------------

        this.setNumIterations(1); // number of iterations needed to compute
        // times

        // ------------------- Calc Method Parameters --------------------

        // Tuning offsets {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha}
        offsets = new int[7];
        offsets[0] = 0;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        offsets[5] = 0;
        offsets[6] = 0;

        /*
         *
         * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
         * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
         * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter
         * value (in angle or minutes)
         */
        methodParams = new HashMap<Integer, double[]>();

        // Jafari
        double[] Jvalues = {16,0,4,0,14};
        methodParams.put(ConfigurationClass.METHOD_JAFARI, Jvalues);

        // Karachi
        double[] Kvalues = {18,1,0,0,18};
        methodParams.put(ConfigurationClass.METHOD_KARACHI, Kvalues);

        // ISNA
        double[] Ivalues = {15,1,0,0,15};
        methodParams.put(ConfigurationClass.METHOD_ISNA, Ivalues);

        // MWL
        double[] MWvalues = {18,1,0,0,17};
        methodParams.put(ConfigurationClass.METHOD_MWL, MWvalues);

        // Makkah
        double[] MKvalues = {18.5,1,0,1,90};
        methodParams.put(ConfigurationClass.METHOD_MAKKAH, MKvalues);

        // Egypt
        double[] Evalues = {19.5,1,0,0,17.5};
        methodParams.put(ConfigurationClass.METHOD_EGYPT, Evalues);

        // Tehran
        double[] Tvalues = {17.7,0,4.5,0,14};
        methodParams.put(ConfigurationClass.METHOD_TEHERAN, Tvalues);

        // Custom
        double[] Cvalues = {12,1,0,0,12};
        methodParams.put(ConfigurationClass.METHOD_CUSTOM, Cvalues);

    }

    // ---------------------- Trigonometric Functions -----------------------
    // range reduce angle in degrees.
    private double fixangle(double a) {
        a = a - (360 * (Math.floor(a / 360.0)));
        a = a < 0 ? (a + 360) : a;
        return a;
    }

    // range reduce hours to 0..23
    private double fixhour(double a) {
        a = a - 24.0 * Math.floor(a / 24.0);
        a = a < 0 ? (a + 24) : a;
        return a;
    }

    // radian to degree
    private double radiansToDegrees(double alpha) {
        return ((alpha * 180.0) / Math.PI);
    }

    // deree to radian
    private double DegreesToRadians(double alpha) {
        return ((alpha * Math.PI) / 180.0);
    }

    // degree sin
    private double dsin(double d) {
        return (Math.sin(DegreesToRadians(d)));
    }

    // degree cos
    private double dcos(double d) {
        return (Math.cos(DegreesToRadians(d)));
    }

    // degree tan
    private double dtan(double d) {
        return (Math.tan(DegreesToRadians(d)));
    }

    // degree arcsin
    private double darcsin(double x) {
        double val = Math.asin(x);
        return radiansToDegrees(val);
    }

    // degree arccos
    private double darccos(double x) {
        double val = Math.acos(x);
        return radiansToDegrees(val);
    }

    // degree arctan
    private double darctan(double x) {
        double val = Math.atan(x);
        return radiansToDegrees(val);
    }

    // degree arctan2
    private double darctan2(double y, double x) {
        double val = Math.atan2(y, x);
        return radiansToDegrees(val);
    }

    // degree arccot
    private double darccot(double x) {
        double val = Math.atan2(1.0, x);
        return radiansToDegrees(val);
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private double getTimeZone1() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = (timez.getRawOffset() / 1000.0) / 3600;
        return hoursDiff;
    }

    // compute base time-zone of the system
    private double getBaseTimeZone() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = (timez.getRawOffset() / 1000.0) / 3600;
        return hoursDiff;

    }

    // detect daylight saving in a given date
    private double detectDaylightSaving() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = timez.getDSTSavings();
        return hoursDiff;
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private double julianDate(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);
        double B = 2 - A + Math.floor(A / 4.0);
        double JD = Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
        return JD;
    }

    // convert a calendar date to julian date (second method)
    private double calcJD(int year, int month, int day) {
        double J1970 = 2440588.0;
        Date date = new Date(year, month - 1, day);

        double ms = date.getTime(); // # of milliseconds since midnight Jan 1,
        // 1970
        double days = Math.floor(ms / (1000.0 * 60.0 * 60.0 * 24.0));
        return J1970 + days - 0.5;
    }

    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    private double[] sunPosition(double jd) {

        double D = jd - 2451545;
        double g = fixangle(357.529 + 0.98560028 * D);
        double q = fixangle(280.459 + 0.98564736 * D);
        double L = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)));

        // double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
        // (2*g)];
        double e = 23.439 - (0.00000036 * D);
        double d = darcsin(dsin(e) * dsin(L));
        double RA = (darctan2((dcos(e) * dsin(L)), (dcos(L))))/ 15.0;
        RA = fixhour(RA);
        double EqT = q/15.0 - RA;
        double[] sPosition = new double[2];
        sPosition[0] = d;
        sPosition[1] = EqT;

        return sPosition;
    }

    // compute equation of time
    private double equationOfTime(double jd) {
        double eq = sunPosition(jd)[1];
        return eq;
    }

    // compute declination angle of sun
    private double sunDeclination(double jd) {
        double d = sunPosition(jd)[0];
        return d;
    }

    // compute mid-day (Dhuhr, Zawal) time
    private double computeMidDay(double t) {
        double T = equationOfTime(this.getJDate() + t);
        double Z = fixhour(12 - T);
        return Z;
    }

    // compute time for a given angle G
    private double computeTime(double G, double t) {

        double D = sunDeclination(this.getJDate() + t);
        double Z = computeMidDay(t);
        double Beg = -dsin(G) - dsin(D) * dsin(this.getLat());
        double Mid = dcos(D) * dcos(this.getLat());
        double V = darccos(Beg/Mid)/15.0;

        return Z + (G > 90 ? -V : V);
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private double computeAsr(double step, double t) {
        double D = sunDeclination(this.getJDate() + t);
        double G = -darccot(step + dtan(Math.abs(this.getLat() - D)));
        return computeTime(G, t);
    }

    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private double timeDiff(double time1, double time2) {
        return fixhour(time2 - time1);
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    private ArrayList<String> getDatePrayerTimes(int year, int month, int day,
                                                 double latitude, double longitude, double tZone) {
        this.setLat(latitude);
        this.setLng(longitude);
        this.setTimeZone(tZone);
        this.setJDate(julianDate(year, month, day));
        double lonDiff = longitude / (15.0 * 24.0);
        this.setJDate(this.getJDate() - lonDiff);
        timeSalat = computeDayTimes();
        return timeSalat;
    }

    public void calculatePrayerTimes(Calendar date, double latitude,
                                     double longitude, double tZone) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);
        this.getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone);
    }

    // return prayer times for a given date
    public ArrayList<String> getPrayerTimes(Calendar date, double latitude,
                                            double longitude, double tZone) {

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);

        return getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone);
    }

    // set custom values for calculation parameters
    public void setCustomParams(double[] params) {

        for (int i = 0; i < 5; i++) {
            if (params[i] == -1) {
                params[i] = methodParams.get(this.getCalcMethod())[i];
                methodParams.put(ConfigurationClass.METHOD_CUSTOM, params);
            } else {
                methodParams.get(ConfigurationClass.METHOD_CUSTOM)[i] = params[i];
            }
        }
        this.setCalcMethod(ConfigurationClass.METHOD_CUSTOM);
    }

    // set the angle for calculating Fajr
    public void setFajrAngle(double angle) {
        double[] params = {angle, -1, -1, -1, -1};
        setCustomParams(params);
    }

    // set the angle for calculating Maghrib
    public void setMaghribAngle(double angle) {
        double[] params = {-1, 0, angle, -1, -1};
        setCustomParams(params);

    }

    // set the angle for calculating Isha
    public void setIshaAngle(double angle) {
        double[] params = {-1, -1, -1, 0, angle};
        setCustomParams(params);

    }

    // set the minutes after Sunset for calculating Maghrib
    public void setMaghribMinutes(double minutes) {
        double[] params = {-1, 1, minutes, -1, -1};
        setCustomParams(params);

    }

    // set the minutes after Maghrib for calculating Isha
    public void setIshaMinutes(double minutes) {
        double[] params = {-1, -1, -1, 1, minutes};
        setCustomParams(params);

    }

    // convert double hours to 24h format
    public String floatToTime24(double time) {

        String result;

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + 0.5 / 60.0); // add 0.5 minutes to round
        int hours = (int)Math.floor(time);
        double minutes = Math.floor((time - hours) * 60.0);

        if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
            result = "0" + hours + ":0" + Math.round(minutes);
        } else if ((hours >= 0 && hours <= 9)) {
            result = "0" + hours + ":" + Math.round(minutes);
        } else if ((minutes >= 0 && minutes <= 9)) {
            result = hours + ":0" + Math.round(minutes);
        } else {
            result = hours + ":" + Math.round(minutes);
        }
        return result;
    }

    // convert double hours to 12h format
    public String floatToTime12(double time, boolean noSuffix) {

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + 0.5 / 60); // add 0.5 minutes to round
        int hours = (int)Math.floor(time);
        double minutes = Math.floor((time - hours) * 60);
        String suffix, result;
        if (hours >= 12) {
            suffix = "pm";
        } else {
            suffix = "am";
        }
        hours = ((((hours+ 12) -1) % (12))+ 1);
        /*hours = (hours + 12) - 1;
        int hrs = (int) hours % 12;
        hrs += 1;*/
        if (noSuffix == false) {
            if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
                result = "0" + hours + ":0" + Math.round(minutes) + " "
                        + suffix;
            } else if ((hours >= 0 && hours <= 9)) {
                result = "0" + hours + ":" + Math.round(minutes) + " " + suffix;
            } else if ((minutes >= 0 && minutes <= 9)) {
                result = hours + ":0" + Math.round(minutes) + " " + suffix;
            } else {
                result = hours + ":" + Math.round(minutes) + " " + suffix;
            }

        } else {
            if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
                result = "0" + hours + ":0" + Math.round(minutes);
            } else if ((hours >= 0 && hours <= 9)) {
                result = "0" + hours + ":" + Math.round(minutes);
            } else if ((minutes >= 0 && minutes <= 9)) {
                result = hours + ":0" + Math.round(minutes);
            } else {
                result = hours + ":" + Math.round(minutes);
            }
        }
        return result;

    }

    // convert double hours to 12h format with no suffix
    public String floatToTime12NS(double time) {
        return floatToTime12(time, true);
    }

    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    private double[] computeTimes(double[] times) {

        double[] t = dayPortion(times);

        double Fajr = this.computeTime(
                180 - methodParams.get(this.getCalcMethod())[0], t[0]);

        double Sunrise = this.computeTime(180 - 0.833, t[1]);

        double Dhuhr = this.computeMidDay(t[2]);
        double Asr = this.computeAsr(1 + this.getAsrJuristic(), t[3]);
        double Sunset = this.computeTime(0.833, t[4]);

        double Maghrib = this.computeTime(
                methodParams.get(this.getCalcMethod())[2], t[5]);
        double Isha = this.computeTime(
                methodParams.get(this.getCalcMethod())[4], t[6]);

        double[] CTimes = {Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha};

        return CTimes;

    }

    // compute prayer times at given julian date
    private ArrayList<String> computeDayTimes() {
        double[] times = {5, 6, 12, 13, 18, 18, 18}; // default times

        for (int i = 1; i <= this.getNumIterations(); i++) {
            times = computeTimes(times);
        }

        times = adjustTimes(times);
        times = tuneTimes(times);

        return adjustTimesFormat(times);
    }

    // adjust times in a prayer time array
    private double[] adjustTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += this.getTimeZone() - this.getLng() / 15;

        }

        times[2] += this.getDhuhrMinutes() / 60; // Dhuhr
        if (methodParams.get(this.getCalcMethod())[1] == 1) // Maghrib
        {
            times[5] = times[4] + methodParams.get(this.getCalcMethod())[2]/ 60;
        }
        if (methodParams.get(this.getCalcMethod())[3] == 1) // Isha
        {
            times[6] = times[5] + methodParams.get(this.getCalcMethod())[4]/ 60;
        }

        if (this.getAdjustHighLats() != ConfigurationClass.ADJ_METHOD_NONE) {
            times = adjustHighLatTimes(times);
        }

        return times;
    }

    // convert times array to given time format
    private ArrayList<String> adjustTimesFormat(double[] times) {

        ArrayList<String> result = new ArrayList<String>();

        if (this.getTimeFormat() == ConfigurationClass.FLOATING) {
            for (double time : times) {
                result.add(String.valueOf(time));
            }
            return result;
        }

        for (int i = 0; i < 7; i++) {
            if (this.getTimeFormat() == ConfigurationClass.TIME12) {
                result.add(floatToTime12(times[i], false));
            } else if (this.getTimeFormat() == ConfigurationClass.TIME12NS) {
                result.add(floatToTime12(times[i], true));
            } else {
                result.add(floatToTime24(times[i]));
            }
        }

        return result;
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private double[] adjustHighLatTimes(double[] times) {
        double nightTime = timeDiff(times[4], times[1]); // sunset to sunrise

        // Adjust Fajr
        double FajrDiff = nightPortion(methodParams.get(this.getCalcMethod())[0]) * nightTime;

        if (Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
            times[0] = times[1] - FajrDiff;
        }

        // Adjust Isha
        double IshaAngle = (methodParams.get(this.getCalcMethod())[3] == 0) ? methodParams.get(this.getCalcMethod())[4] : 18;
        double IshaDiff = this.nightPortion(IshaAngle) * nightTime;
        if (Double.isNaN(times[6]) || this.timeDiff(times[4], times[6]) > IshaDiff) {
            times[6] = times[4] + IshaDiff;
        }

        // Adjust Maghrib
        double MaghribAngle = (methodParams.get(this.getCalcMethod())[1] == 0) ? methodParams.get(this.getCalcMethod())[2] : 4;
        double MaghribDiff = nightPortion(MaghribAngle) * nightTime;
        if (Double.isNaN(times[5]) || this.timeDiff(times[4], times[5]) > MaghribDiff) {
            times[5] = times[4] + MaghribDiff;
        }

        return times;
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle) {
        double calc = 0;

        if (adjustHighLats == ConfigurationClass.ADJ_METHOD_ANGLEBASED)
            calc = (angle)/60.0;
        else if (adjustHighLats == ConfigurationClass.ADJ_METHOD_MIDNIGHT)
            calc = 0.5;
        else if (adjustHighLats == ConfigurationClass.ADJ_METHOD_ONESEVENTH)
            calc = 0.14286;

        return calc;
    }

    // convert hours to day portions
    private double[] dayPortion(double[] times) {
        for (int i = 0; i < 7; i++) {
            times[i] /= 24;
        }
        return times;
    }

    // Tune timings for adjustments
    // Set time offsets
    public void tune(int[] offsetTimes) {

        for (int i = 0; i < offsetTimes.length; i++) { // offsetTimes length
            // should be 7 in order
            // of Fajr, Sunrise,
            // Dhuhr, Asr, Sunset,
            // Maghrib, Isha
            this.offsets[i] = offsetTimes[i];
        }
    }

    private double[] tuneTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] + this.offsets[i] / 60.0;
        }

        return times;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        //double latitude = -37.823689;
        //double longitude = 145.121597;
        //double timezone = 10;
        double latitude = 50.6329700;
        double longitude = 3.0585800;
        double timezone = 1;

        // Test Prayer times here
        PrayTime prayers = new PrayTime();

        prayers.setTimeFormat(ConfigurationClass.TIME12);
        prayers.setCalcMethod(ConfigurationClass.METHOD_JAFARI);
        prayers.setAsrJuristic(ConfigurationClass.JURISTIC_SHAFII);
        prayers.setAdjustHighLats(ConfigurationClass.ADJ_METHOD_ANGLEBASED);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        ArrayList<String> prayerTimes = prayers.getPrayerTimes(cal,
                latitude, longitude, timezone);
        ArrayList<String> prayerNames = prayers.getTimeNames();

        for (int i = 0; i < prayerTimes.size(); i++) {
            System.out.println(prayerNames.get(i) + " - " + prayerTimes.get(i));
        }

    }

    public String getFajrTime() {
        return timeSalat.get(0);
    }

    public String getSunriseTime() {
        return timeSalat.get(1);
    }

    public String getDuhrTime() {
        return timeSalat.get(2);
    }

    public String getAsrTime() {
        return timeSalat.get(3);
    }

    public String getSunsetTime() {
        return timeSalat.get(4);
    }

    public String getMaghribTime() {
        return timeSalat.get(5);
    }

    public String getIshaTime() {
        return timeSalat.get(6);
    }

    public ArrayList<String> getTimeSalat() {
        return timeSalat;
    }

    public int getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(int calcMethod) {
        this.calcMethod = calcMethod;
    }

    public int getAsrJuristic() {
        return asrJuristic;
    }

    public void setAsrJuristic(int asrJuristic) {
        this.asrJuristic = asrJuristic;
    }

    public int getDhuhrMinutes() {
        return dhuhrMinutes;
    }

    public void setDhuhrMinutes(int dhuhrMinutes) {
        this.dhuhrMinutes = dhuhrMinutes;
    }

    public int getAdjustHighLats() {
        return adjustHighLats;
    }

    public void setAdjustHighLats(int adjustHighLats) {
        this.adjustHighLats = adjustHighLats;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(int timeFormat) {
        this.timeFormat = timeFormat;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    public double getJDate() {
        return JDate;
    }

    public void setJDate(double jDate) {
        JDate = jDate;
    }


    private int getNumIterations() {
        return numIterations;
    }

    private void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public ArrayList<String> getTimeNames() {
        return timeNames;
    }
}
