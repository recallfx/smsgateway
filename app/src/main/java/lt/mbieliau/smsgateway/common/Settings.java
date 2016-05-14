/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Settings singleton class
 */
public class Settings {
    public static final String TAG = Settings.class.getSimpleName();
    private static Settings instance;
    private SharedPreferences sharedPref;

    // cached values
    public String authValue;
    public String bookingUrl;
    public String smsUrl;

    /**
     * Singleton initialization method.
     * @param context Application context.
     * */
    public static Settings getInstance(Context context){
        if (!isInitialised()){
            instance = new Settings(context);
        }

        return instance;
    }

    public static boolean isInitialised(){
        return instance != null;
    }

    /**
     * Private constructor
     * */
    private Settings(Context context){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        preloadSettings();
    }

    /**
     * Preload settings
     * */
    public void preloadSettings(){
        bookingUrl = getBookingUrl();
        smsUrl = getSmsUrl();
        authValue = getAuthValue();
    }

    // urls
    public String getBookingUrl() {
        String root = sharedPref.getString(Constants.Keys.Settings.Urls.ROOT, Constants.Defaults.Urls.root);
        return root.concat(Constants.Defaults.Urls.booking);
    }

    public String getSmsUrl() {
        String root = sharedPref.getString(Constants.Keys.Settings.Urls.ROOT, Constants.Defaults.Urls.root);
        return root.concat(Constants.Defaults.Urls.sms);
    }

    public String getCompCode(){
        return sharedPref.getString(Constants.Keys.Settings.CODE, Constants.Defaults.code);
    }

    public String getCompToken(){
        return sharedPref.getString(Constants.Keys.Settings.TOKEN, Constants.Defaults.token);
    }

    public String getAuthValue() {
        return getCompCode() + ":" + getCompToken();
    }
}