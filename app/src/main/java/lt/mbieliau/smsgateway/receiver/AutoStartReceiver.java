/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class AutoStartReceiver extends BroadcastReceiver
{
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent != null){
            String action = intent.getAction();

            if (!TextUtils.isEmpty(action)){
                if (action.equals(BOOT_COMPLETED)){
                    AlarmReceiver.SetAlarm(context);
                }
            }
        }
    }
}