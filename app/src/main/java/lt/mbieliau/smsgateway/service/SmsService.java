/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;

import lt.mbieliau.smsgateway.R;
import lt.mbieliau.smsgateway.common.Ajax;
import lt.mbieliau.smsgateway.common.AjaxError;
import lt.mbieliau.smsgateway.common.Settings;
import lt.mbieliau.smsgateway.common.SmsDb;
import lt.mbieliau.smsgateway.common.data.Booking;
import lt.mbieliau.smsgateway.common.data.Sms;
import lt.mbieliau.smsgateway.common.result.BookingResult;

public class SmsService extends Service {
    public static final String TAG = SmsService.class.getSimpleName();

    private static final int NOTIFICATION = R.string.app_name;

    // Binding
    private SmsServiceBinder binder = new SmsServiceBinder();
    public static class SmsServiceBinder extends Binder {
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
            Log.v(TAG, "Wakelock acquired");
        }
    }

    public synchronized static void releaseLock(Context context){
        PowerManager.WakeLock lock = getLock(context);
        if (lock.isHeld()) {
            lock.release();
            Log.v(TAG, "Wakelock released");
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.v(TAG, "SmsService:onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "SmsService:onStartCommand");

        // start foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            sendNotification();
        }

        SmsDb smsDb = SmsDb.getInstance(this);
        Cursor cursor = smsDb.getBookingCursor(Sms.STATUS_PENDING);
        if (cursor != null){
            // iterate
            // TODO: if something is wrong, it loops forever. Implement backoff.
            while (cursor.moveToNext()) {
                Booking booking = new Booking(cursor);

                Log.i(TAG, booking.toString());
                sendBooking(booking);
            }

            if (!cursor.isClosed()){
                cursor.close();
            }
        }

        // notify anyone listening
        Intent dbUpdatedIntent = new Intent(SmsDb.DB_UPDATED);
        sendBroadcast(dbUpdatedIntent);

        stopSelf();

        return START_STICKY;
    }

    /**
     * Synchronous booking send operation.
     * @param booking Booking to send to the server.
     * */
    private void sendBooking(final Booking booking){
        Settings settings = Settings.getInstance(this);
        String booking_url = settings.getBookingUrl();

        AQuery aq = new AQuery(this);
        AjaxCallback ac = new AjaxCallback<BookingResult>();
        ac.expire(10000); // 10s expiration

        // send
        Ajax.sendJSON(aq, booking_url, booking, BookingResult.class, ac, null);

        // parse result
        BookingResult result = (BookingResult)ac.getResult();
        AjaxError error = AjaxError.getError(booking_url, result, ac.getStatus());

        if (error == null) {
            Log.i(TAG, String.format("Success: %s", booking_url));

            SmsDb smsDb = SmsDb.getInstance(getApplicationContext());
            smsDb.updateBookingStatus(booking.getId(), Sms.STATUS_DELIVERED);
        } else {
            Log.d(TAG, error.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void sendNotification(){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("SMS Service");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        startForeground(NOTIFICATION, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseLock(getApplicationContext());
    }
}
