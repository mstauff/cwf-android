package org.ldscd.callingworkflow.web;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import org.json.JSONArray;
import org.ldscd.callingworkflow.model.Org;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgsListRequest extends Request<List<Org>> {
    private static final String TAG = "OrgsListRequest";

    private final Response.Listener<List<Org>> listener;

    public OrgsListRequest(String url, Response.Listener<List<Org>> listener, final Response.Listener<WebException> errorListener) {
        super(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //all errors should be of type WebException at this point, so we'll cast it for use in the rest of the app
                errorListener.onResponse((WebException)error);
            }
        });
        this.listener = listener;
    }

    @Override
    protected void deliverResponse(List<Org> response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<List<Org>> parseNetworkResponse(NetworkResponse response) {
        if(WebException.isSessionExpiredResponse(getUrl())) {
            return Response.error(new WebException(ExceptionType.SESSION_EXPIRED, response));
        } else if(WebException.isWebsiteDownResponse(getUrl())) {
            return Response.error(new WebException(ExceptionType.SERVER_UNAVAILABLE, response));
        } else {
            List<Org> orgsList;
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                JSONArray orgs = new JSONArray(jsonString);
                OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
                orgsList = orgCallingBuilder.extractOrgs(orgs, false);
            } catch (Exception e) {
                return Response.error(new WebException(ExceptionType.PARSING_ERROR, e));
            }
            return Response.success(orgsList, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 403) {
            return new WebException(ExceptionType.LDS_AUTH_REQUIRED, volleyError.networkResponse);
        } else {
            return new WebException(ExceptionType.UNKNOWN_EXCEPTION, volleyError.networkResponse);
        }
    }
}
