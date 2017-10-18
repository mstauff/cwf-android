package org.ldscd.callingworkflow.web;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.ldscd.callingworkflow.model.Org;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class OrgsListRequest extends Request<List<Org>> {
    private static final String TAG = "OrgsListRequest";

    private final Response.Listener<List<Org>> listener;
    private Map<String, String> headers;

    public OrgsListRequest(String url, Map<String, String> headers, Response.Listener<List<Org>> listener, Response.ErrorListener errorListener) {
        super(Request.Method.GET, url, errorListener);
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
        List<Org> orgsList;
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONArray orgs = new JSONArray(jsonString);
            OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
            orgsList = orgCallingBuilder.extractOrgs(orgs);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(orgsList, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse != null && volleyError.networkResponse.data != null && volleyError.networkResponse.statusCode==400) {
            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
        }

        return volleyError;
    }
}
