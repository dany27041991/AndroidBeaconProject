package com.softhings.localizer.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreference {

    public static final String PREFS_NAME = "PREFS";
    public static final String PREFS_FULL_NAME = "FULL_PREFS";
    public static final String PREFS_KEY = "json";
    public static final String PREFS_KEY0 = "version";
    public static final String PREFS_KEY1 = "userID";
    public static final String PREFS_KEY2 = "UUID";
    public static final String PREFS_KEY3 = "scanPeriod";
    public static final String PREFS_KEY4 = "interScanPeriod";
    public static final String PREFS_KEY5 = "ray";
    public static final String PREFS_KEY6 = "homeBeacons";
    public static final String PREFS_KEY7 = "POIBeacons";
    public static final String PREFS_KEY8 = "cityZones";

    public SharedPreference() {
        super();
    }

    public void saveString(Context context, String text)
    {
        SharedPreferences settings;
        Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_FULL_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2
        editor.putString(PREFS_KEY, text);
        editor.commit();
    }

    public void save(Context context, String text0, String text1, String text2, String text3, String text4
            , String text5, String text6, String text7, String text8) {
        SharedPreferences settings;
        Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putString(PREFS_KEY0, text0);
        editor.putString(PREFS_KEY1, text1);
        editor.putString(PREFS_KEY2, text2);
        editor.putString(PREFS_KEY3, text3);
        editor.putString(PREFS_KEY4, text4);
        editor.putString(PREFS_KEY5, text5);
        editor.putString(PREFS_KEY6, text6);
        editor.putString(PREFS_KEY7, text7);
        editor.putString(PREFS_KEY8, text8);
        editor.commit(); //4
    }

    public String getJsonString(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_FULL_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY, null);
        return text;
    }

    //This method is not used but useful if you want to display shared preferences on screen
    public String getValueVersion(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY0, null);
        return text;
    }
    public String getValueUserID(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY1, null);
        return text;
    }
    public String getValueUUID(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY2, null);
        return text;
    }
    public String getValueScanPeriod(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY3, null);
        return text;
    }
    public String getValueInterScanPeriod(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY4, null);
        return text;
    }
    public String getValueRay(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY5, null);
        return text;
    }
    public String getValueHomeBeacons(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY6, null);
        return text;
    }
    public String getValuePOIBeacons(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY7, null);
        return text;
    }
    public String getValueCityZones(Context context) {
        SharedPreferences settings;
        String text;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        //
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(PREFS_KEY8, null);
        return text;
    }
}