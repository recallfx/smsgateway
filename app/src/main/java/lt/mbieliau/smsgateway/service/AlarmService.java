/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import lt.mbieliau.smsgateway.R;
import lt.mbieliau.smsgateway.common.Ajax;
import lt.mbieliau.smsgateway.common.AjaxError;
import lt.mbieliau.smsgateway.common.Settings;
import lt.mbieliau.smsgateway.common.SmsDb;
import lt.mbieliau.smsgateway.common.data.Sms;
import lt.mbieliau.smsgateway.common.object.SmsObject;
import lt.mbieliau.smsgateway.common.result.SmsResult;
import lt.mbieliau.smsgateway.receiver.AlarmReceiver;

public class AlarmService extends Service {
    public static final String TAG = AlarmService.class.getSimpleName();

    private static final int NOTIFICATION = R.string.app_name;
    protected AlarmReceiver alarmReceiver;
    private Sms currentSms;

    // Binding
    private AlarmServiceBinder binder = new AlarmServiceBinder();
    public static class AlarmServiceBinder extends Binder {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Wakelock
    private static PowerManager.WakeLock lockStatic;
    public synchronized static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }
        return lockStatic;
    }

    public synchronized static void acquireLock(Context context){
        PowerManager.WakeLock lock = getLock(context);
        if (!lock.isHeld()) {
            lock.acquire();
            Log.v(TAG, "Alarm service Wakelock acquired");
        }
    }

    public synchronized static void releaseLock(Context context){
        PowerManager.WakeLock lock = getLock(context);
        if (lock.isHeld()) {
            lock.release();
            Log.v(TAG, "Alarm service Wakelock released");
        }
    }

    // Handler
    private Handler handler;
    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.v(TAG, "onCreate");

        handler = new Handler();

        alarmReceiver = new AlarmReceiver();
        alarmReceiver.SetAlarm(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "AlarmService:onStartCommand");

        // start foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            sendNotification();
        }

        fetchMessages();

        return START_STICKY;
    }

    private void fetchMessages() {
        Settings settings = Settings.getInstance(this);
        String smsUrl = settings.getSmsUrl();

        AQuery aq = new AQuery(this);

        Ajax.sendJSON(aq, smsUrl, new SmsObject(), SmsResult.class, new AjaxCallback<SmsResult>() {
            @Override
            public void callback(String url, SmsResult object, AjaxStatus status) {
                AjaxError error = AjaxError.getError(url, object, status);

                if (error == null) {
                    Log.i(TAG, String.format("Success: %s", url));

                    SmsDb smsDb = SmsDb.getInstance(getApplicationContext());

                    for (Sms sms : object.data){
                        long id = smsDb.addSms(sms);

                        if (id < 0){
                            Log.d(TAG, "Failed to add SMS to the database.");
                        }
                    }

                    sendMessages();
                } else {
                    Log.d(TAG, error.getMessage());
                }
            }
        }, null);
    }

    private void sendMessages(){
        SmsDb smsDb = SmsDb.getInstance(getApplicationContext());

        currentSms = smsDb.getPendingSms();
        if (currentSms != null){
            sendSMS(currentSms.getAddress(), currentSms.getBody());
        }
        else {
            Cursor cursor = smsDb.getSmsCursor();
            if (cursor != null){
                // iterate
                while (cursor.moveToNext()) {
                    Sms sms = new Sms(cursor);

                    Log.i(TAG, sms.toString());
                    sendSmsResult(sms);
                }

                if (!cursor.isClosed()){
                    cursor.close();
                }
            }

            // release lock after complete ans top itself
            releaseLock(getApplicationContext());
            stopSelf();
        }
    }

    /**
     * Synchronous sms result send operation.
     * @param sms Sms to send to the server.
     * */
    private void sendSmsResult(final Sms sms){
        Settings settings = Settings.getInstance(this);
        String sms_url = settings.getSmsUrl();

        AQuery aq = new AQuery(this);
        AjaxCallback ac = new AjaxCallback<SmsResult>();
        ac.expire(10000); // 10s expiration

        // send
        Ajax.sendJSON(aq, sms_url, sms, SmsResult.class, ac, null);

        // parse result
        SmsResult result = (SmsResult)ac.getResult();
        AjaxError error = AjaxError.getError(sms_url, result, ac.getStatus());

        if (error == null) {
            Log.i(TAG, String.format("Success: %s", sms_url));

            // TODO: update db record with some kind of status. And in case it was not able to send it to the server, redeliver nex time
        } else {
            Log.d(TAG, error.getMessage());
        }
    }

    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                SmsDb smsDb = SmsDb.getInstance(getApplicationContext());

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        smsDb.updateSmsStatus(currentSms.getId(), Sms.STATUS_MALFORMED);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        smsDb.updateSmsStatus(currentSms.getId(), Sms.STATUS_MALFORMED);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                SmsDb smsDb = SmsDb.getInstance(getApplicationContext());

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        currentSms.setStatus(Sms.STATUS_DELIVERED);
                        smsDb.updateSmsStatus(currentSms.getId(), Sms.STATUS_DELIVERED);

                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }

                AlarmService.this.sendMessages();
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void sendNotification(){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Alarm Service");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        startForeground(NOTIFICATION, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseLock(getApplicationContext());
    }
}
