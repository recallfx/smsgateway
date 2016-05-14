/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.text.TextUtils;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;

import lt.mbieliau.smsgateway.common.result.AbstractResult;

public class AjaxError {
    public static final String TAG = AjaxError.class.getSimpleName();
    public String error_message;
    public String url;
    public AjaxStatus status;

    public AjaxError (String url, String error_message, AjaxStatus status){
        this.url = url;
        this.error_message = error_message;
        this.status = status;
    }

    public String getDebugMessage(){
        if (!TextUtils.isEmpty(error_message)){
            return String.format("%s: %s", url, error_message);
        }

        return String.format("%s: %s", url, status.getMessage());
    }

    public String getMessage(){
        Log.d(TAG, getDebugMessage());

        if (!TextUtils.isEmpty(error_message)){
            return error_message;
        }

        return status.getMessage();
    }


    public static AjaxError getError(String url, AbstractResult object, AjaxStatus status) {
        AjaxError error = null;

        if (object != null){
            if (!object.success){
                error = new AjaxError(url, object.error_message, status);
            }
        }
        else {
            error = new AjaxError(url, "", status);
        }

        return error;
    }
}
