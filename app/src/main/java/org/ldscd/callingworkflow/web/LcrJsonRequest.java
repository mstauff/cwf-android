package org.ldscd.callingworkflow.web;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class LcrJsonRequest extends JsonObjectRequest {
    private static String TAG = "LcrJsonRequest";
    private Map<String, String> headers;

    public LcrJsonRequest(int method, String url, Map<String, String> headers, JSONObject jsonRequest, Listener<JSONObject> listener, final Listener<WebException> errorListener) {
        super(method, url, jsonRequest, listener, new ErrorListener() {
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
        this.headers = headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
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
                Response<JSONObject> response = super.parseNetworkResponse(volleyError.networkResponse);
                if(response.result != null && response.result.has("errors")) {
                    try {
                        JSONObject errors = response.result.getJSONObject("errors");
                        if(errors.has("memberId")) {
                            JSONArray messages = errors.getJSONArray("memberId");
                            if(messages != null && messages.length() > 0) {
                                serverErrorMsg = messages.getString(0);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.getStackTrace().toString());
                    }
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
