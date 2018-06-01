package org.ldscd.callingworkflow.web;

import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationRequest extends Request<Boolean> {
    private final Response.Listener<Boolean> listener;
    private Map<String, String> params;


    public AuthenticationRequest(String userName, String password, String url, Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener) {
        super(Method.POST, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //all exceptions should be of type WebException, we'll cast it here so we don't have to everywhere else
                errorListener.onResponse((WebException) error);
            }
        });
        this.params = new HashMap<>(2);
        this.params.put("username", userName);
        this.params.put("password", password);
        this.listener = listener;
        this.setShouldCache(false);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    protected void deliverResponse(Boolean response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<Boolean> parseNetworkResponse(NetworkResponse response) {
        if(WebException.isWebsiteDownResponse(getUrl())) {
            return Response.error(new WebException(ExceptionType.SERVER_UNAVAILABLE, response));
        }
        if(response.statusCode == 200) {
            return Response.success(true, HttpHeaderParser.parseCacheHeaders(response));
        } else {
            return Response.error(new WebException(ExceptionType.UNAUTHORIZED, response));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
            if(volleyError.networkResponse.statusCode == 403) {
                return new WebException(ExceptionType.LDS_AUTH_REQUIRED, volleyError.networkResponse);
            } else if(volleyError.networkResponse.statusCode == 302  &&
                      volleyError.networkResponse.headers.get("Location").contains("authfailed")) {
                return new WebException(ExceptionType.UNAUTHORIZED, volleyError.networkResponse);
            }
        }
        return new WebException(ExceptionType.UNKNOWN_EXCEPTION, volleyError.networkResponse);
    }
}
