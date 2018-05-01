package org.ldscd.callingworkflow.web;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class LcrJsonRequest extends JsonObjectRequest {
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
        if(volleyError.networkResponse.statusCode == 403) {
            return new WebException(ExceptionType.LDS_AUTH_REQUIRED, volleyError.networkResponse);
        } else {
            return new WebException(ExceptionType.UNKNOWN_EXCEPTION, volleyError.networkResponse);
        }
    }
}