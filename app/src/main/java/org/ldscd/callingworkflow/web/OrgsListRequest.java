package org.ldscd.callingworkflow.web;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrgsListRequest extends Request<List<Org>> {
    private static final String TAG = "OrgsListRequest";

    private static final String orgIdFieldName = "subOrgId";
    private static final String positionCwfIdFieldName = "cwfId";
    private static final String defaultNameFieldName = "defaultOrgName";
    private static final String customNameFieldName = "customOrgName";
    private static final String orgTypeIdFieldName = "firstOrgTypeId";
    private static final String displayOrderFieldName = "displayOrder";
    private static final String childrenArrayName = "children";
    private static final String callingArrayName = "callings";
    private static final String existingStatusFieldName = "existingStatus";
    private static final String activeDateFieldName = "activeDate";
    private static final String hiddenFieldName = "hidden";
    private static final String proposedStatusFieldName = "proposedStatus";
    private static final String proposedIndIdFieldName = "proposedIndId";
    private static final String notesFieldName = "notes";
    private static final String editableByOrgFieldName = "editableByOrg";
    private static final String currentMemberIdFieldName = "memberId";
    private static final String positionIdFieldName = "positionId";
    private static final String positionTypeIdFieldName = "positionTypeId";
    private static final String positionFieldName = "position";
    private static final String multiplesAllowedFieldName = "allowMultiple";

    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
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

    public List<Org> extractOrgs(JSONArray orgsJson) throws JSONException {
        List<Org> orgs = new ArrayList<>();
        for(int i=0; i < orgsJson.length(); i++) {
            try {
                JSONObject orgJson = orgsJson.getJSONObject(i);
                Org org = extractOrg(orgJson);
                orgs.add(org);
            } catch(JSONException e) {
                e.printStackTrace();
            }
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
        int typeId = orgJson.has(orgTypeIdFieldName) ? orgJson.getInt(orgTypeIdFieldName) : orgJson.getInt("orgTypeId");
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

    private Calling extractCalling(JSONObject json, Long parentId) throws JSONException {
        long currentMemberId = 0;
        if(!json.isNull(currentMemberIdFieldName)) {
            currentMemberId = json.getLong(currentMemberIdFieldName);
        }
        String cwfId = null;
        if(!json.isNull(positionCwfIdFieldName)) {
            cwfId = json.getString(positionCwfIdFieldName);
        }
        Long id = null;
        if(!json.isNull(positionIdFieldName)) {
            id = json.getLong(positionIdFieldName);
        }
        Position position = new Position();
        long positionTypeId = 0;
        if(!json.isNull(positionTypeIdFieldName)) {
            position.setPositionTypeId(json.getInt(positionTypeIdFieldName));
        }
        position.setName(json.getString(positionFieldName));
        if(position.getName().equals("null")) {
            position.setName(null);
        }
        if(!json.isNull(multiplesAllowedFieldName)) {
            position.setAllowMultiple(json.getBoolean(multiplesAllowedFieldName));
        }
        String existingStatus = json.has(existingStatusFieldName) ? json.getString(existingStatusFieldName) : null;
        if(existingStatus != null && existingStatus.equals("null")) {
            existingStatus = null;
        }
        DateTime activeDate = null;
        if(json.has(activeDateFieldName) && !json.getString(activeDateFieldName).equals("null")) {
            activeDate = fmt.parseDateTime(json.getString(activeDateFieldName));
        }
        if(!json.isNull(hiddenFieldName)) {
            position.setHidden(json.getBoolean(hiddenFieldName));
        }
        String proposedStatus = json.has(proposedStatusFieldName) ? json.getString(proposedStatusFieldName) : null;
        if(proposedStatus != null && proposedStatus.equals("null")) {
            proposedStatus = null;
        }
        long proposedIndId = 0;
        if(!json.isNull(proposedIndIdFieldName)) {
            proposedIndId = json.getLong(proposedIndIdFieldName);
        }
        String notes = json.has(notesFieldName) ? json.getString(notesFieldName) : null;
        if(notes != null && notes.equals("null")) {
            notes = null;
        }
        boolean editableByOrg = false;
        if(!json.isNull(editableByOrgFieldName)) {
            editableByOrg = json.getBoolean(editableByOrgFieldName);
        }

        return new Calling(id, cwfId, currentMemberId, proposedIndId, activeDate, position, existingStatus, proposedStatus, notes, editableByOrg, parentId);
    }
}
