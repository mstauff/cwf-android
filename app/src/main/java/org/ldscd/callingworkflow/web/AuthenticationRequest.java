package org.ldscd.callingworkflow.web;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationRequest extends Request<String> {
    private static final String cookieKey = "ObSSOCookie";
    private final Response.Listener<String> listener;
    private Map<String, String> params;


    public AuthenticationRequest(String userName, String password, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.params = new HashMap<>(2);
        this.params.put("username", userName);
        this.params.put("password", password);
        this.listener = listener;
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String sessionCookie = null;
        if(response.headers.containsKey("Set-Cookie")) {
            String[] cookies = response.headers.get("Set-Cookie").split(";");
            for(String cookie: cookies) {
                if(cookie.startsWith(cookieKey)) {
                    sessionCookie = cookie;
                    break;
                }
            }
        }
        if(sessionCookie != null) {
            return Response.success(sessionCookie, HttpHeaderParser.parseCacheHeaders(response));
        } else {
            return Response.error(new VolleyError("Error while Authenticating"));
        }
    }
}
