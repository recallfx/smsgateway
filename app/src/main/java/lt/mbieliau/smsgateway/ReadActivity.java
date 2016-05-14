/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import lt.mbieliau.smsgateway.common.SmsDb;
import lt.mbieliau.smsgateway.common.Utils;
import lt.mbieliau.smsgateway.common.data.Booking;

public class ReadActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        TextView address = (TextView) findViewById(R.id.read_address);
        TextView status = (TextView) findViewById(R.id.read_status);
        TextView body = (TextView) findViewById(R.id.read_body);
        TextView dateView = (TextView) findViewById(R.id.read_date);
        TextView passengers = (TextView) findViewById(R.id.read_passengers);
        TextView origin = (TextView) findViewById(R.id.read_origin);
        TextView destination = (TextView) findViewById(R.id.read_destination);
        TextView comment = (TextView) findViewById(R.id.read_comment);

        String strId = getIntent().getStringExtra("id");
        long id = Long.parseLong(strId);

        SmsDb smsDb = SmsDb.getInstance(this);

        Booking booking = smsDb.getBooking(id);

        if (booking != null){
            address.setText(booking.getAddress());
            status.setText(Utils.getStatusTest(getApplicationContext(), booking.getStatus()));
            body.setText(booking.getBody());

            long date = booking.getDate();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);

            String format = "yyyy-MM-dd HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String dateString = sdf.format(cal.getTime());
            dateView.setText(dateString);

            passengers.setText(Integer.toString(booking.getPassengers()));
            origin.setText(booking.getOrigin());
            destination.setText(booking.getDestination());
            comment.setText(booking.getComment());
        }
        else {
            Toast.makeText(this, "Booking not found!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
