/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import lt.mbieliau.smsgateway.common.SmsDb;
import lt.mbieliau.smsgateway.common.data.Booking;
import lt.mbieliau.smsgateway.common.data.Sms;
import lt.mbieliau.smsgateway.service.SmsService;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static boolean CANCEL_INTENT_ON_MESSAGE_EXCEPTION = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "SmsReceiver:onReceive");

        // Acquire SmsService lock
        final PowerManager.WakeLock lock = SmsService.getLock(context);
        if (!lock.isHeld()) {
            SmsService.acquireLock(context);
        }

        // get the SMS message passed in
        SmsMessage[] smsMessages = getSmsMessages(intent);
        boolean updated = false;

        if (smsMessages != null){
            StringBuilder sb = new StringBuilder();
            SmsDb smsDb = SmsDb.getInstance(context);
            Booking booking;

            // process received messages
            for (SmsMessage smsMessage : smsMessages) {
                if (!TextUtils.isEmpty(smsMessage.getMessageBody())) {
                    booking = new Booking(smsMessage);

                    if (!smsDb.isSmsBlocked(booking)){
                        if (!smsDb.isAbusing(booking)){
                            if (booking.isValid()){
                                smsDb.addBooking(booking);

                                updated = true;

                                // Abort broadcast, because we don't need this message to appear in SMS inbox;
                                abortBroadcast();
                            }
                        }
                        else {
                            blockBooking(smsDb, booking, "abuse");
                        }
                    }
                    else {
                        blockBooking(smsDb, booking, "rule");
                    }

                    // StringBuilder
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append("SMS[");
                    sb.append(booking.toString());
                    sb.append("]");
                }
            }

            if (updated){
                Intent serverIntent = new Intent(context, SmsService.class);
                context.startService(serverIntent);
            }

            // display the new SMS message
            Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save blocked booking to the DB and set comment about the cause of this block;
     * This also aborts pending broadcast;
     *
     * @param smsDb SmsDb instance;
     * @param booking Booking to be blocked;
     * @param cause Cause of this block;
     * */
    private void blockBooking(SmsDb smsDb, Booking booking, String cause){
        Log.i(TAG, "Message blocked: " + cause);
        booking.setStatus(Sms.STATUS_BLOCKED);
        String comment = booking.getComment();
        booking.setComment("[" + cause + " block]" + comment);
        smsDb.addBooking(booking);

        // Abort broadcast, because we don't need this message to appear in SMS inbox;
        abortBroadcast();
    }

    /**
     * Get SMS messages from intent bundle
     * @param intent Received intent
     * */
    private SmsMessage[] getSmsMessages(Intent intent){
        SmsMessage[] smsMessages = null;

        if (intent != null){
            Bundle bundle = intent.getExtras();

            if (bundle != null){
                Object[] pdus = null;

                try {
                    // retrieve the SMS message received
                    pdus = (Object[]) bundle.get("pdus");
                } catch (Exception ex){
                    Log.e(TAG, "Error getting received messages", ex);
                }

                if (pdus != null){
                    smsMessages = new SmsMessage[pdus.length];

                    for (int i = 0; i < smsMessages.length; i++){
                        try {
                            smsMessages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        } catch (Exception ex){
                            Log.e(TAG, "Error creating message from Pdu", ex);

                            if (CANCEL_INTENT_ON_MESSAGE_EXCEPTION){
                                smsMessages = null;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return smsMessages;
    }
}