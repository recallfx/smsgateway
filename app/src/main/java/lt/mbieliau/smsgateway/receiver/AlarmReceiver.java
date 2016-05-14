/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import lt.mbieliau.smsgateway.service.AlarmService;
import lt.mbieliau.smsgateway.service.SmsService;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.v(SmsService.TAG, "AlarmReceiver:onReceive");
        // Acquire AlarmService lock

        final PowerManager.WakeLock lock = AlarmService.getLock(context);
        if (!lock.isHeld()) {
            AlarmService.acquireLock(context);

            // DEBUGGING: wait if previous action is taking time
            //Intent serviceIntent = new Intent(context, AlarmService.class);
            //context.startService(serviceIntent);
        }
    }

    public static void SetAlarm(Context context)
    {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        long interval = 30000;
        // Milli seconds * Second * Minute
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 500, interval, pi);
    }

    public static void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}