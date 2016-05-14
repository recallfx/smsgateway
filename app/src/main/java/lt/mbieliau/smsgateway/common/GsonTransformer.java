/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.Transformer;
import com.google.gson.Gson;

public class GsonTransformer implements Transformer {
    public static final String TAG = GsonTransformer.class.getSimpleName();
    public <T> T transform(String url, Class<T> type, String encoding, byte[] data, AjaxStatus status) {
        Gson gson = new Gson();
        String string = new String(data);
        T result = null;

        Log.d(TAG, String.format("%s: %s", url, string));

        try {
            result = gson.fromJson(string, type);
        }
        catch (Exception ex){
            Log.e(Constants.TAG, ex.getMessage());
        }

        return result;
    }
}