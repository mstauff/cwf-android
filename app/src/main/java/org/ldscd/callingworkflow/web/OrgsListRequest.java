package org.ldscd.callingworkflow.web;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import org.json.JSONArray;
import org.ldscd.callingworkflow.model.Org;

import java.util.List;
import java.util.Map;

public class OrgsListRequest extends Request<List<Org>> {
    private static final String TAG = "OrgsListRequest";

    private final Response.Listener<List<Org>> listener;
    private Map<String, String> headers;

    public OrgsListRequest(String url, Map<String, String> headers, Response.Listener<List<Org>> listener, final Response.Listener<WebResourcesException> errorListener) {
        super(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //all errors should be of type WebResourcesException at this point, so we'll cast it for use in the rest of the app
                errorListener.onResponse((WebResourcesException)error);
            }
        });
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(List<Org> response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<List<Org>> parseNetworkResponse(NetworkResponse response) {
        if(WebResourcesException.isSessionExpiredResponse(getUrl())) {
            return Response.error(new WebResourcesException(ExceptionType.SESSION_EXPIRED, response));
        } else if(WebResourcesException.isWebsiteDownResponse(getUrl())) {
            return Response.error(new WebResourcesException(ExceptionType.SERVER_UNAVAILABLE, response));
        } else {
            List<Org> orgsList;
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                JSONArray orgs = new JSONArray(jsonString);
                OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
                orgsList = orgCallingBuilder.extractOrgs(orgs);
            } catch (Exception e) {
                return Response.error(new WebResourcesException(ExceptionType.PARSING_ERROR, e));
            }
            return Response.success(orgsList, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse.statusCode == 403) {
            return new WebResourcesException(ExceptionType.LDS_AUTH_REQUIRED, volleyError.networkResponse);
        } else {
            return new WebResourcesException(ExceptionType.UNKNOWN_EXCEPTION, volleyError.networkResponse);
        }
    }
}
