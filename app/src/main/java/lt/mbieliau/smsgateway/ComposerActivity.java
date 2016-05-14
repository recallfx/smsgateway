/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ComposerActivity extends ActionBarActivity {

    private static final String ACTION_SENT = "com.appsrox.smsxp.SENT";
    private static final int DIALOG_SENDTO = 1;

    private EditText et1;
    private ImageButton ib3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);
        setTitle("New Message");

        et1 = (EditText) findViewById(R.id.sms_text);
        et1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(et1.getText().toString()))
                    ib3.setEnabled(false);
                else
                    ib3.setEnabled(true);
            }
        });

        ib3 = (ImageButton) findViewById(R.id.sms_send);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sms_send:
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.putExtra("sms_body", et1.getText().toString());
                    sendIntent.setType("vnd.android-dir/mms-sms");
                    startActivity(sendIntent);

                } catch (ActivityNotFoundException e) {
                    showDialog(DIALOG_SENDTO);
                }
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SENDTO:
                final EditText et = new EditText(this);
                et.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                return new AlertDialog.Builder(this)
                        .setTitle("To")
                        .setView(et)
                        .setCancelable(true)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String to = et.getText().toString().trim();
                                sendSMS(to);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .create();
        }
        return super.onCreateDialog(id);
    }

    private BroadcastReceiver sent = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(ComposerActivity.this, "Sent successfully",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                case SmsManager.RESULT_ERROR_NULL_PDU:
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(ComposerActivity.this, "Error sending SMS",
                            Toast.LENGTH_LONG).show();
                    break;
            }

            unregisterReceiver(this);
        }
    };

    private void sendSMS(String to) {
        registerReceiver(sent, new IntentFilter(ACTION_SENT));

        SmsManager manager = SmsManager.getDefault() ;
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SENT), 0);
        manager.sendTextMessage(to, null, et1.getText().toString(), sentIntent, null);
    }
}