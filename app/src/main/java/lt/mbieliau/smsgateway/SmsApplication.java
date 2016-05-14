/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.androidquery.util.AQUtility;

import lt.mbieliau.smsgateway.common.SmsDb;

public class SmsApplication extends Application {
    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        AQUtility.setExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                AsyncTask<Throwable, Void, Void> task = new AsyncTask<Throwable, Void, Void>() {
                    @Override
                    protected Void doInBackground(Throwable... ex) {
                        try {
                            Throwable throwable = ex != null ? ex[0] : null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                task.execute(ex);
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        SmsDb.close();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        SmsDb.close();
    }
}
