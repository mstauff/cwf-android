package org.ldscd.callingworkflow.web;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JSONArrayRequest  extends JsonArrayRequest {
    private static final String ERRORS = "errors";
    private static final String MEMBER_ID = "memberId";
    private static String TAG = "LcrJsonRequest";

    public JSONArrayRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, final Response.Listener<WebException> errorListener) {
        super(method, url, jsonRequest, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //We wrap all volley errors in a webResourcesException for use in the rest of the app
                if(error instanceof WebException) {
                    errorListener.onResponse((WebException) error);
                } else {
                    errorListener.onResponse(new WebException(ExceptionType.PARSING_ERROR, error));
                }
            }
        });
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        String responseBody;
        try {
            responseBody = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            responseBody = "";
        }
        if(WebException.isSessionExpiredResponse(responseBody)) {
            return Response.error(new WebException(ExceptionType.SESSION_EXPIRED, response));
        } else if(WebException.isWebsiteDownResponse(responseBody)) {
            return Response.error(new WebException(ExceptionType.UNKNOWN_EXCEPTION, response));
        } else {
            return super.parseNetworkResponse(response);
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        WebException result;
        if(volleyError.networkResponse != null) {
            if(volleyError.networkResponse.statusCode == 403) {
                result = new WebException(ExceptionType.LDS_AUTH_REQUIRED, volleyError.networkResponse);
            } else {
                String serverErrorMsg = null;
                Response<JSONArray> response = super.parseNetworkResponse(volleyError.networkResponse);
                try {
                    if(response.result != null && response.result.getJSONObject(0).has(ERRORS)) {
                        try {
                            JSONObject errors = response.result.getJSONObject(0).getJSONObject(ERRORS);
                            if(errors.has(MEMBER_ID)) {
                                JSONArray messages = errors.getJSONArray(MEMBER_ID);
                                if(messages != null && messages.length() > 0) {
                                    serverErrorMsg = messages.getString(0);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getStackTrace().toString());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(serverErrorMsg != null) {
                    result = new WebException(ExceptionType.LDS_SERVER_PROVIDED_MESSAGE, volleyError.networkResponse);
                    result.setException(new Exception(serverErrorMsg));
                } else {
                    result = new WebException(ExceptionType.UNKNOWN_EXCEPTION, volleyError.networkResponse);
                }
            }
        } else {
            result = new WebException(ExceptionType.UNKNOWN_EXCEPTION);
        }
        return result;
    }
}