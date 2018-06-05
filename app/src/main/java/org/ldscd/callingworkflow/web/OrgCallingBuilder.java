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

public class OrgCallingBuilder {
    /* Fields */
    private static final String unitNumberFieldName = "unitNumber";
    private static final String unitNoFieldName = "unitNo";
    private static final String orgIdFieldName = "subOrgId";
    private static final String orgTypeIdFieldName = "orgTypeId";
    private static final String positionCwfIdFieldName = "cwfId";
    private static final String cwfOnlyFieldName = "cwfOnly";
    private static final String defaultOrgNameFieldName = "defaultOrgName";
    private static final String defaultNameFieldName = "name";
    private static final String customOrgNameFieldName = "customOrgName";
    private static final String firstOrgTypeIdFieldName = "firstOrgTypeId";
    private static final String firstTypeIdFieldName = "firstTypeId";
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
    private static final String stringNull = "null";
    private static final String membersString = "members";

    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
    /* Constructor */
    public OrgCallingBuilder() {}

    /* Methods */
    public List<Org> extractOrgs(JSONArray orgsJson, boolean includeMemberAssignments) {
        List<Org> orgs = new ArrayList<>();
        for(int i=0; i < orgsJson.length(); i++) {
            try {
                JSONObject orgJson = orgsJson.getJSONObject(i);
                Org org = extractOrg(orgJson, includeMemberAssignments);
                if(org != null && org.getOrgTypeId() > 0) {
                    orgs.add(org);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        return orgs;
    }

    public Org extractOrg(JSONObject orgJson, boolean includeMemberAssignments) throws JSONException {
        long unitNumber = 0;
        try {
            unitNumber = orgJson.getLong(unitNumberFieldName);
        } catch(Exception e) {
            Log.e(String.valueOf(orgJson.getLong(orgIdFieldName)), e.getMessage());
        }
        if(unitNumber > 0) {
            long id = orgJson.getLong(orgIdFieldName);
            String name = null;
            if (orgJson.has(defaultOrgNameFieldName)) {
                name = orgJson.getString(defaultOrgNameFieldName);
            } else if(orgJson.has(customOrgNameFieldName)) {
                name = orgJson.getString(customOrgNameFieldName);
            } else if(orgJson.has(defaultNameFieldName)) {
                name = orgJson.getString(defaultNameFieldName);
            }
            int typeId = 0;
            if(orgJson.has(firstOrgTypeIdFieldName)) {
                typeId = orgJson.getInt(firstOrgTypeIdFieldName);
            } else if(orgJson.has(firstTypeIdFieldName)) {
                typeId = orgJson.getInt(firstTypeIdFieldName);
            } else if(orgJson.has(orgTypeIdFieldName)) {
                typeId = orgJson.getInt(orgTypeIdFieldName);
            }
            int order = 0;
            if(orgJson.has(displayOrderFieldName)) {
                order = orgJson.getInt(displayOrderFieldName);
            }

            JSONArray childOrgsJson = new JSONArray();
            if(orgJson.has(childrenArrayName)) {
                childOrgsJson = orgJson.getJSONArray(childrenArrayName);
            }
            List<Long> members = new ArrayList();
            if(includeMemberAssignments && orgJson.has(membersString)) {

            }
            List<Org> childOrgs = extractOrgs(childOrgsJson, false);

            JSONArray callingsJson = new JSONArray();
            if(orgJson.has(callingArrayName)) {
                callingsJson = orgJson.getJSONArray(callingArrayName);
            }
            List<Calling> callings = new ArrayList<>();
            for (int i = 0; i < callingsJson.length(); i++) {
                JSONObject callingJson = callingsJson.getJSONObject(i);
                Calling calling = extractCalling(callingJson, id);
                callings.add(calling);
            }

            Org org = new Org(unitNumber, id, name, typeId, order, childOrgs, callings);
            if(members.size() > 0) {
                org.setAssignedMembers(members);
            }
            return org;
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
        if(existingStatus != null && existingStatus.equals(stringNull)) {
            existingStatus = null;
        }
        DateTime activeDate = null;
        if(json.has(activeDateFieldName) && !json.get(activeDateFieldName).equals(stringNull) && !json.get(activeDateFieldName).equals(null) && !json.get(activeDateFieldName).equals("")) {
            activeDate = fmt.parseDateTime(json.getString(activeDateFieldName));
        }
        CallingStatus proposedStatus = CallingStatus.NONE;
        String proposedStatusString = json.has(proposedStatusFieldName) ? json.getString(proposedStatusFieldName) : null;
        if(proposedStatusString != null && !proposedStatusString.equals(stringNull)) {
            proposedStatus = CallingStatus.get(proposedStatusString);
        }
        long proposedIndId = 0;
        if(!json.isNull(proposedIndIdFieldName)) {
            proposedIndId = json.getLong(proposedIndIdFieldName);
        }
        String notes = json.has(notesFieldName) ? json.getString(notesFieldName) : null;
        if(notes != null && notes.equals(stringNull)) {
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
            if(position.getName() != null && position.getName().equals(stringNull)) {
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
            if(json.has(unitNoFieldName)) {
                position.setUnitNumber(json.getLong(unitNoFieldName));
            }
        }
        return position;
    }
}