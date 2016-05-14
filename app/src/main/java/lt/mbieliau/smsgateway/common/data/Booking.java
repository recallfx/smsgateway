/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common.data;

import android.database.Cursor;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;

import lt.mbieliau.smsgateway.common.Utils;

public class Booking extends Sms implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = Booking.class.getSimpleName();

    protected int passengers;
    protected String origin;
    protected String destination;
    protected String comment;

    public Booking(Cursor cursor) {
        super(cursor);

        this.passengers = cursor.getInt(6);
        this.origin = cursor.getString(7);
        this.destination = cursor.getString(8);
        this.comment = cursor.getString(9);
    }

    public Booking(SmsMessage smsMessage){
        super(smsMessage);

        initBooking();
    }

    public Booking(String address, String body, long date){
        super(address, body, date);

        initBooking();
    }

    public void initBooking(){
        passengers = 0;
        origin = "";
        destination = "";
        comment = "";

        try {
            process();
        } catch (Exception ex){
            Log.d(TAG,"Error processing booking data", ex);
        }
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isValid(){
        return !TextUtils.isEmpty(origin);
    }

    /**
     * Process SMS booking body.
     * Parameters:
     *  0. "T" - Constant string (required)
     *  1. Number - Number of passengers (optional);
     *  2. Departure location - Text describing departure location. Can be street, place or gps (space separated) (required);
     *  3. Destination location - Same as departure (optional);
     *  4. Comment - Some additional information (optional)
     *  *All parameters must be separated by one of these: [;|/><=*&^%$#@!~?]
     *
     * Format by parameter count:
     * 2 - T{count};FROM
     * 3 - T{count};origin
     * 4 - T{count};origin;TO
     * 5 - T{count};origin;TO;COMMENT
     *
     * */
    private void process() {
        String string = body.toLowerCase();

        // we process only when first character is "t"
        if (string.startsWith("t")){
            String[] tokens = string.split(";|\\||/|>|<|=|\\*|&|\\^|%|\\$|#|@|!|~|\\?");

            // include only [2...4] tokens.length
            if (2 <= tokens.length && tokens.length <= 4){
                if (tokens[0].length() > 1){
                    String strCount = tokens[0].substring(1);
                    if (Utils.isInteger(strCount)){
                        passengers = Integer.parseInt(strCount, 10);
                    }
                    else {
                        status = Sms.STATUS_MALFORMED;
                    }
                }
                else {
                    passengers = 1;
                }

                origin = tokens[1];

                if (tokens.length > 2){
                    destination = tokens[2];
                }

                if (tokens.length > 3){
                    comment = tokens[3];
                }
            }
            else {
                status = Sms.STATUS_MALFORMED;
            }
        }
        else {
            status = Sms.STATUS_MALFORMED;
        }
    }
}