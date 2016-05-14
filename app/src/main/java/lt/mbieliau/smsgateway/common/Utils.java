/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.content.Context;

import lt.mbieliau.smsgateway.R;

public class Utils {
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }

    public static String getStatusTest(Context context, int status){
        switch (status){
            case -2:
                return context.getString(R.string.sms_status_malformed);
            case -1:
                return context.getString(R.string.sms_status_blocked);
            case 1:
                return context.getString(R.string.sms_status_complete);
        }

        // default 0
        return context.getString(R.string.sms_status_pending);
    }

}
