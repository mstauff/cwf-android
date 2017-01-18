package org.ldscd.callingworkflow.web;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrgsListRequest extends Request<List<Org>> {
    private static final String TAG = "OrgsListRequest";

    private static final String orgIdFieldName = "subOrgId";
    private static final String defaultNameFieldName = "defaultOrgName";
    private static final String customNameFieldName = "customOrgName";
    private static final String orgTypeIdFieldName = "firstOrgTypeId";
    private static final String displayOrderFieldName = "displayOrder";
    private static final String childrenArrayName = "children";
    private static final String callingArrayName = "callings";

    private static final String currentIndIdFieldName = "memberId";
    private static final String positionIdFieldName = "positionId";
    private static final String callingNameFieldName = "position";

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
            orgsList = extractOrgs(orgs);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(orgsList, HttpHeaderParser.parseCacheHeaders(response));
    }

    private List<Org> extractOrgs(JSONArray orgsJson) throws JSONException {
        List<Org> orgs = new ArrayList<>();
        for(int i=0; i < orgsJson.length(); i++) {
            JSONObject orgJson = orgsJson.getJSONObject(i);
            Org org = extractOrg(orgJson);
            orgs.add(org);
        }
        return orgs;
    }

    private Org extractOrg(JSONObject orgJson) throws JSONException {
        long id = orgJson.getLong(orgIdFieldName);
        String name;
        if(orgJson.isNull(customNameFieldName)) {
            name = orgJson.getString(defaultNameFieldName);
        } else {
            name = orgJson.getString(customNameFieldName);
        }
        int typeId = orgJson.getInt(orgTypeIdFieldName);
        int order = orgJson.getInt(displayOrderFieldName);

        JSONArray childOrgsJson = orgJson.getJSONArray(childrenArrayName);
        List<Org> childOrgs = extractOrgs(childOrgsJson);

        JSONArray callingsJson = orgJson.getJSONArray(callingArrayName);
        List<Calling> callings = new ArrayList<>();
        for(int i=0; i < callingsJson.length(); i++) {
            JSONObject callingJson = callingsJson.getJSONObject(i);
            Calling calling = extractCalling(callingJson, id);
            callings.add(calling);
        }

        return new Org(id, name, typeId, order, childOrgs, callings);
    }

    private Calling extractCalling(JSONObject json, long parentId) throws JSONException {
        long currentId = 0;
        if(!json.isNull(currentIndIdFieldName)) {
            currentId = json.getLong(currentIndIdFieldName);
        }
        long positionId = 0;
        if(!json.isNull(positionIdFieldName)) {
            positionId = json.getLong(positionIdFieldName);
        }
        String callingName = json.getString(callingNameFieldName);

        return new Calling(currentId, 0, positionId, null, callingName, null, parentId, null, true);
    }
}
