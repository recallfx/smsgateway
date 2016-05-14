/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import lt.mbieliau.smsgateway.R;

public class CustomCursorAdapter extends CursorAdapter {
    private final LayoutInflater mInflater;

    public CustomCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.sms_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String address = cursor.getString(cursor.getColumnIndex("address"));
        String formattedNumber = PhoneNumberUtils.formatNumber(address);
        ((TextView) view.findViewById(R.id.sms_address)).setText(formattedNumber);

        String body = cursor.getString(cursor.getColumnIndex("body"));
        ((TextView) view.findViewById(R.id.sms_body)).setText(body);

        TextView statusTextView = (TextView) view.findViewById(R.id.sms_status);
        statusTextView.setText(Utils.getStatusTest(context, cursor.getInt(cursor.getColumnIndex("status"))));

        long date = cursor.getLong(cursor.getColumnIndex("date"));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        String format = "yyyy-MM-dd HH:mm";
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateString = sdf.format(cal.getTime());

        ((TextView) view.findViewById(R.id.sms_date)).setText(dateString);
    }
}