package org.ldscd.callingworkflow.web;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

public class OrgCallingBuilder {
    /* Fields */
    private static final String unitNumberFieldName = "unitNumber";
    private static final String orgIdFieldName = "subOrgId";
    private static final String positionCwfIdFieldName = "cwfId";
    private static final String cwfOnlyFieldName = "cwfOnly";
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
    private static final String currentMemberIdFieldName = "memberId";
    private static final String positionIdFieldName = "positionId";
    private static final String positionTypeIdFieldName = "positionTypeId";
    private static final String positionFieldName = "position";
    private static final String positionDisplayOrderFieldName = "positionDisplayOrder";
    private static final String multiplesAllowedFieldName = "allowMultiple";

    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
    /* Constructor */
    public OrgCallingBuilder() {}

    /* Methods */
    public List<Org> extractOrgs(JSONArray orgsJson) throws JSONException {
        List<Org> orgs = new ArrayList<>();
        for(int i=0; i < orgsJson.length(); i++) {
            try {
                JSONObject orgJson = orgsJson.getJSONObject(i);
                Org org = extractOrg(orgJson);
                if(org != null && org.getOrgTypeId() > 0) {
                    orgs.add(org);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        return orgs;
    }

    public Org extractOrg(JSONObject orgJson) throws JSONException {
        long unitNumber = 0;
        try {
            unitNumber = orgJson.getLong(unitNumberFieldName);
        } catch(Exception e) {
            Log.e(String.valueOf(orgJson.getLong(orgIdFieldName)), e.getMessage());
        }
        if(unitNumber > 0) {
            long id = orgJson.getLong(orgIdFieldName);
            String name;
            if (orgJson.isNull(customNameFieldName)) {
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
            for (int i = 0; i < callingsJson.length(); i++) {
                JSONObject callingJson = callingsJson.getJSONObject(i);
                Calling calling = extractCalling(callingJson, id);
                callings.add(calling);
            }

            return new Org(unitNumber, id, name, typeId, order, childOrgs, callings);
        }
        return null;
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
        boolean cwfOnly = false;
        if(!json.isNull(cwfOnlyFieldName)) {
            cwfOnly = json.getBoolean(cwfOnlyFieldName);
        }
        String existingStatus = json.has(existingStatusFieldName) ? json.getString(existingStatusFieldName) : null;
        if(existingStatus != null && existingStatus.equals("null")) {
            existingStatus = null;
        }
        DateTime activeDate = null;
        if(json.has(activeDateFieldName) && !json.get(activeDateFieldName).equals("null") && !json.get(activeDateFieldName).equals(null) && !json.get(activeDateFieldName).equals("")) {
            activeDate = fmt.parseDateTime(json.getString(activeDateFieldName));
        }
        CallingStatus proposedStatus = CallingStatus.NONE;
        String proposedStatusString = json.has(proposedStatusFieldName) ? json.getString(proposedStatusFieldName) : null;
        if(proposedStatusString != null && !proposedStatusString.equals("null")) {
            proposedStatus = CallingStatus.get(proposedStatusString);
        }
        long proposedIndId = 0;
        if(!json.isNull(proposedIndIdFieldName)) {
            proposedIndId = json.getLong(proposedIndIdFieldName);
        }
        String notes = json.has(notesFieldName) ? json.getString(notesFieldName) : null;
        if(notes != null && notes.equals("null")) {
            notes = null;
        }

        return new Calling(id, cwfId, cwfOnly, currentMemberId, proposedIndId, activeDate, extractPosition(json), existingStatus, proposedStatus, notes, parentId);
    }

    public Position extractPosition(JSONObject json) throws JSONException {
        Position position = null;
        if(json != null) {
            position = new Position();
            if (!json.isNull(positionTypeIdFieldName)) {
                position.setPositionTypeId(json.getInt(positionTypeIdFieldName));
            }
            if (json.has(positionFieldName) && !json.isNull(positionFieldName)) {
                position.setName(json.getString(positionFieldName));
            }
            if(position.getName() != null && position.getName().equals("null")) {
                position.setName(null);
            }
            if (!json.isNull(multiplesAllowedFieldName)) {
                position.setAllowMultiple(json.getBoolean(multiplesAllowedFieldName));
            } else {
                position.setAllowMultiple(true);
            }
            if (!json.isNull(hiddenFieldName)) {
                position.setHidden(json.getBoolean(hiddenFieldName));
            }
            if (json.has(positionDisplayOrderFieldName)) {
                position.setPositionDisplayOrder(json.optLong(positionDisplayOrderFieldName));
            }
            if(json.has("unitNo")) {
                position.setUnitNumber(json.getLong("unitNo"));
            }
        }
        return position;
    }
}