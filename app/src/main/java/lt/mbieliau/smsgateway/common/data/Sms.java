/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common.data;

import android.database.Cursor;
import android.telephony.SmsMessage;

import java.io.Serializable;

public class Sms implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int STATUS_MALFORMED = -2; // malformed
    public static final int STATUS_BLOCKED = -1; // blocked
    public static final int STATUS_PENDING = 0; // just got it
    public static final int STATUS_DELIVERED = 1; // sent to server/client

    protected long id;
    protected String address;
    protected String body;
    protected long date;
    protected int status;
    protected boolean unread;

    public Sms(){
    }

    public Sms(Cursor cursor){
        this.id = cursor.getLong(0);
        this.address = cursor.getString(1);
        this.body = cursor.getString(2);
        this.date = cursor.getLong(3);
        this.status = cursor.getInt(4);
        this.unread = cursor.getInt(5) != 0;
    }

    public Sms(SmsMessage smsMessage){
        init(smsMessage.getOriginatingAddress(), smsMessage.getMessageBody(), smsMessage.getTimestampMillis());
    }

    public Sms(String address, String body, long date){
        init(address, body, date);
    }

    public void init(String address, String body, long date){
        this.id = 0;
        this.address = address;
        this.body = body;
        this.date = date;
        status = 0;
        unread = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean getUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return address + " " + body;
    }
}