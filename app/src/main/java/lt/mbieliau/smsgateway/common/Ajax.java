/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

import android.text.TextUtils;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

public class Ajax {
    public static final String TAG = Ajax.class.getSimpleName();
    private static GsonTransformer transformer = null;
    public static <T> AjaxCallback<T> sendJSON(AQuery aq, String url, Object obj, Class<T> type, AjaxCallback<T> cb, String asyncCallback) {
        String error_message = "";
        if (transformer == null){
            transformer = new GsonTransformer();
        }

        cb.url(url);
        cb.type(type);

        cb.transformer(transformer);
        cb.header("Content-Type", "application/json");

        Settings settings = Settings.getInstance(aq.getContext().getApplicationContext());
        cb.header("X-Auth", settings.authValue);

        StringEntity entity = null;

        try {
            Gson gson = new Gson();
            entity = new StringEntity(gson.toJson(obj), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(Constants.TAG, e.getMessage());
            error_message = e.getMessage();
        }

        cb.param(AQuery.POST_ENTITY, entity);

        if (!TextUtils.isEmpty(asyncCallback)){
            cb.weakHandler(aq.getContext(), asyncCallback);
        }

        if (error_message.isEmpty()){
            aq.ajax(cb);
        }
        else {
            AjaxStatus status = new AjaxStatus();
            status.message(error_message);

            if (!TextUtils.isEmpty(asyncCallback)){
                cb.callback(url, null, status);
            }
        }

        return cb;
    }
}
