/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

import lt.mbieliau.smsgateway.common.data.Booking;
import lt.mbieliau.smsgateway.common.data.Rule;
import lt.mbieliau.smsgateway.common.data.Sms;

public class SmsDb {
    private static final String DATABASE_NAME = "sms.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SMS = "sms";
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_RULES = "rules";

    public static final String COLUMN_ID = "_id";           //0
    public static final String COLUMN_ADDRESS = "address";  //1
    public static final String COLUMN_BODY = "body";        //2
    public static final String COLUMN_DATE = "date";        //3
    public static final String COLUMN_STATUS = "status";    //4
    public static final String COLUMN_UNREAD = "unread";    //5
    public static final String COLUMN_PASSENGERS = "passengers";    //6
    public static final String COLUMN_ORIGIN = "origin";            //7
    public static final String COLUMN_DESTINATION = "destination";  //8
    public static final String COLUMN_COMMENT = "comment";  //9

    private static final int MAX_SMS_INTERVAL_COUNT = 20;
    private static final int SMS_INTERVAL = 1000 * 60 * 60 * 24;
    public static final String DB_UPDATED = "lt.mbieliau.smsgateway.DB_UPDATED";


    private static SmsDb instance;
    private static SQLiteDatabase db = null;
    private static SmsDbHelper helper;



    private SmsDb(Context context) {
        helper = new SmsDbHelper(context);
    }

    public static SmsDb getInstance(Context context) {

        if (instance == null) {
            instance = new SmsDb(context.getApplicationContext());

        }
        if (db == null) {
            db = helper.getWritableDatabase();
        }
        return instance;
    }

    public static void close(){
        if (db != null){
            db.close();
        }

        if (helper != null){
            helper.close();
        }

        helper = null;
        db = null;
        instance = null;
    }

    private static class SmsDbHelper extends SQLiteOpenHelper {

        /**
         * Constructor should be private to prevent direct instantiation.
         * make call to static factory method "getInstance()" instead.
         */
        protected SmsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Database creation sql statement
        private static final String DATABASE_SMS_CREATE = "CREATE TABLE "
                + TABLE_SMS + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, '" +
                COLUMN_ADDRESS + "' TEXT NOT NULL, '" +
                COLUMN_BODY + "' TEXT NOT NULL, '" +
                COLUMN_DATE + "' INTEGER NOT NULL, '" +
                COLUMN_STATUS + "' INTEGER NOT NULL, '" +
                COLUMN_UNREAD + "' INTEGER NOT NULL);";

        private static final String DATABASE_BOOKINGS_CREATE = "CREATE TABLE "
                + TABLE_BOOKINGS + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, '" +
                COLUMN_ADDRESS + "' TEXT NOT NULL, '" +
                COLUMN_BODY + "' TEXT NOT NULL, '" +
                COLUMN_DATE + "' INTEGER NOT NULL, '" +
                COLUMN_STATUS + "' INTEGER NOT NULL, '" +
                COLUMN_UNREAD + "' INTEGER NOT NULL, '" +
                COLUMN_PASSENGERS + "' INTEGER NOT NULL, '" +
                COLUMN_ORIGIN + "' TEXT NOT NULL, '" +
                COLUMN_DESTINATION + "' TEXT NOT NULL, '" +
                COLUMN_COMMENT + "' TEXT NOT NULL);";

        private static final String DATABASE_RULES_CREATE = "CREATE TABLE "
                + TABLE_RULES + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, '" +
                COLUMN_ADDRESS + "' TEXT NOT NULL, '" +
                COLUMN_DATE + "' INTEGER NOT NULL, '" +
                COLUMN_STATUS + "' INTEGER NOT NULL);";

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_SMS_CREATE);
            database.execSQL(DATABASE_BOOKINGS_CREATE);
            database.execSQL(DATABASE_RULES_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(SmsDbHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RULES);
            onCreate(db);
        }
    }

    public long addSms(Sms sms) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, sms.getId());
        values.put(COLUMN_ADDRESS, sms.getAddress());
        values.put(COLUMN_BODY, sms.getBody());
        values.put(COLUMN_DATE, sms.getDate());
        values.put(COLUMN_STATUS, sms.getStatus());
        values.put(COLUMN_UNREAD, sms.getUnread());

        // Inserting Row
        return db.insert(TABLE_SMS, null, values);
    }

    public long addBooking(Booking booking) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, booking.getAddress());
        values.put(COLUMN_BODY, booking.getBody());
        values.put(COLUMN_DATE, booking.getDate());
        values.put(COLUMN_STATUS, booking.getStatus());
        values.put(COLUMN_UNREAD, booking.getUnread() ? 1 : 0);
        values.put(COLUMN_PASSENGERS, booking.getPassengers());
        values.put(COLUMN_ORIGIN, booking.getOrigin());
        values.put(COLUMN_DESTINATION, booking.getDestination());
        values.put(COLUMN_COMMENT, booking.getComment());

        // Inserting Row
        return db.insert(TABLE_BOOKINGS, null, values);
    }

    public void updateSmsStatus(long id, int statusComplete) {
        updateStatus(TABLE_SMS, id, statusComplete);
    }


    public void updateBookingStatus(long id, int statusComplete) {
        updateStatus(TABLE_BOOKINGS, id, statusComplete);
    }

    private void updateStatus(String table, long id, int statusComplete) {
        String strSQL = "UPDATE " + table +
                " SET " + COLUMN_STATUS + "=" + Integer.toString(statusComplete) +
                " WHERE " + COLUMN_ID + "=" + Long.toString(id);
        db.execSQL(strSQL, null);
    }


    public long addRule(Rule rule) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, rule.getAddress());
        values.put(COLUMN_STATUS, rule.getStatus());

        // Inserting Row
        return db.insert(TABLE_RULES, null, values);
    }

    public Cursor getSmsCursor(){
        return getCursor(TABLE_SMS);
    }

    public Cursor getBookingCursor(){
        return getCursor(TABLE_BOOKINGS);
    }

    private Cursor getCursor(String table){
        String selectQuery = "SELECT * FROM " + table;

        return db.rawQuery(selectQuery, null);
    }

    public Cursor getBookingCursor(int status){
        return getCursor(TABLE_BOOKINGS, status);
    }

    private Cursor getCursor(String table, int status){
        String selectQuery = "SELECT * FROM " + table + " WHERE " + COLUMN_STATUS + "=?";

        return db.rawQuery(selectQuery, new String[]{Integer.toString(status)});
    }

    /**
     * Get sms from DB by ID
     * @param id SMS id;
     * */
    public Sms getSms(long id){
        Sms result = null;
        String selectQuery = "SELECT * FROM " + TABLE_SMS + " WHERE " + COLUMN_ID + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(id)});

        if (cursor != null){
            if (cursor.moveToFirst()){
                result = new Sms(cursor);
            }

            if (!cursor.isClosed()){
                cursor.close();
            }
        }

        return result;
    }

    /**
     * Get pending SMS
     * */
    public Sms getPendingSms(){
        Sms result = null;
        String selectQuery = "SELECT * FROM " + TABLE_SMS + " WHERE " + COLUMN_STATUS + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{ Integer.toString(Sms.STATUS_PENDING) });

        if (cursor != null){
            if (cursor.moveToFirst()){
                result = new Sms(cursor);
            }

            if (!cursor.isClosed()){
                cursor.close();
            }
        }

        return result;
    }

    /**
     * Get booking from DB by ID
     * @param id Booking id;
     * */
    public Booking getBooking(long id){
        Booking result = null;
        String selectQuery = "SELECT * FROM " + TABLE_BOOKINGS + " WHERE " + COLUMN_ID + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(id)});

        if (cursor != null){
            if (cursor.moveToFirst()){
                result = new Booking(cursor);
            }

            if (!cursor.isClosed()){
                cursor.close();
            }
        }

        return result;
    }


    /**
     * Check if this message should be blocked from further processing
     * @param booking Received booking
     * */
    public boolean isSmsBlocked(Booking booking) {
        boolean result = false;

        // check if address is in rules
        Cursor rulesCursor = db.query(TABLE_RULES, //table
                new String[]{COLUMN_ID}, // select
                COLUMN_ADDRESS + "=? AND " + COLUMN_STATUS + ">?", // where
                new String[] { booking.getAddress(), "0" }, // where ?
                null, null, null, null);
        if (rulesCursor != null ){
            if (rulesCursor.getCount() > 0){
                // block
                result = true;
            }

            if (!rulesCursor.isClosed()){
                rulesCursor.close();
            }
        }

        return result;
    }

    /**
     * Check if booking is address is abusing the system (SMS count < 20/24h)
     * @param booking Received booking
     * */
    public boolean isAbusing(Booking booking) {
        boolean result = false;
        long date = new Date().getTime();
        date = date - SMS_INTERVAL;

        Cursor bookingsCursor = db.query(TABLE_BOOKINGS,
                new String[]{COLUMN_ID}, // select
                COLUMN_ADDRESS + "=? AND " + COLUMN_DATE + ">?", // where
                new String[] { booking.getAddress(), Long.toString(date) }, // where ?
                null, null, null, null);

        if (bookingsCursor != null){
            if (bookingsCursor.getCount() > MAX_SMS_INTERVAL_COUNT){
                // block
                result = true;
            }

            if (!bookingsCursor.isClosed()){
                bookingsCursor.close();
            }
        }

        return result;
    }
}
