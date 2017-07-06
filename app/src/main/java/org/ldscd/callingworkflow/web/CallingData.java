package org.ldscd.callingworkflow.web;


import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.MemberClass;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.model.*;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallingData {
    private static final String TAG = "CallingData";

    private static final String AGE = "age";
    private static final String GENDER = "gender";
    private static final String PRIESTHOOD = "priesthood";
    private static final String MEMBER_CLASS = "memberClasses";
    private static final String POSITION_TYPE_ID = "positionTypeId";
    private static final String SHORT_NAME = "shortName";
    private static final String REQUIREMENTS = "requirements";

    private IWebResources webResources;
    private GoogleDataService googleDataService;
    private MemberData memberData;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<String, Calling> callingsById;
    private Map<Long, Org> baseOrgByOrgId;
    private List<Calling> allCallings;
    private List<PositionMetaData> allPositionMetadata;
    private Map<Integer, PositionMetaData> positionMetaDataByPositionTypeId;

    public CallingData(IWebResources webResources, GoogleDataService googleDataService, MemberData memberData) {
        this.webResources = webResources;
        this.googleDataService = googleDataService;
        this.memberData = memberData;
    }

    /* Org Data */

    public void loadOrgs(final Response.Listener<Boolean> orgsCallback, final ProgressBar pb, Activity activity) {
        googleDataService.init(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    pb.setProgress(pb.getProgress() + 15);
                    webResources.getOrgs(new Response.Listener<List<Org>>() {
                        @Override
                        public void onResponse(List<Org> response) {
                            orgs = response;
                            orgsById = new HashMap<Long, Org>();
                            callingsById = new HashMap<String, Calling>();
                            baseOrgByOrgId = new HashMap<Long, Org>();
                            allCallings = new ArrayList<Calling>();
                            pb.setProgress(pb.getProgress() + 20);
                            googleDataService.syncDriveIds(new Response.Listener<Boolean>() {
                                @Override
                                public void onResponse(Boolean driveSyncSucceeded) {
                                    if(driveSyncSucceeded) {
                                        pb.setProgress(pb.getProgress() + 15);
                                        importAndMergeGoogleData(new Response.Listener<Boolean>(){
                                            @Override
                                            public void onResponse(Boolean mergeSucceeded) {
                                                if(mergeSucceeded){
                                                    for (Org org : orgs) {
                                                        extractOrg(org, org.getId());
                                                    }
                                                    pb.setProgress(pb.getProgress() + 10);
                                                    orgsCallback.onResponse(true);
                                                } else {
                                                    Log.e(TAG, "failed to merge cwf file data");
                                                    orgsCallback.onResponse(false);
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, "failed to sync drive Ids");
                                        orgsCallback.onResponse(false);
                                    }
                                }
                            }, orgs);
                        }
                    });
                } else {
                    orgsCallback.onResponse(false);
                }
            }
        }, activity);
    }

    public void loadOrg(final Response.Listener<Boolean> listener, final Org org) {
        googleDataService.getOrgData(new Response.Listener<Org>() {
            @Override
            public void onResponse(Org googleOrg) {
                if(googleOrg != null) {
                    webResources.getOrgs(new Response.Listener<List<Org>>() {
                        @Override
                        public void onResponse(List<Org> lcrOrgs) {
                            if(lcrOrgs != null) {
                                for(Org lcrOrg : lcrOrgs) {
                                    if(lcrOrg.equals(org)) {
                                        mergeOrgs(lcrOrg, org);
                                        extractOrg(lcrOrg, org.getId());
                                        break;
                                    }
                                }
                            }
                        }
                    });
                }
                listener.onResponse(true);
            }
        }, null, org);
    }

    public List<Org> getOrgs() {
        return orgs;
    }

    public Org getOrg(long orgId) {
        return orgsById.get(orgId);
    }

    public Org getBaseOrg(long orgId) {
        return baseOrgByOrgId.get(orgId);
    }

    private void extractOrg(Org org, long baseOrgId) {
        orgsById.put(org.getId(), org);
        baseOrgByOrgId.put(org.getId(), orgsById.get(baseOrgId));
        for(Calling calling: org.getCallings()) {
            allCallings.add(calling);
            callingsById.put(calling.getCallingId(), calling);
        }
        for(Org subOrg: org.getChildren()) {
            extractOrg(subOrg, baseOrgId);
        }
    }

    /* Import and Merge Data */

    private void importAndMergeGoogleData(final Response.Listener<Boolean> mergeFinishedCallback) {
        final int[] orgsFinishedImporting = {0};

        for(int i=0; i < orgs.size(); i++) {
            final Org org = orgs.get(i);
            googleDataService.getOrgData(new Response.Listener<Org>() {
                @Override
                public void onResponse(Org cwfOrg) {
                    mergeOrgs(org, cwfOrg);
                    // todo - this should be enhanced to make use of promises, potential threading issues
                    // if multiple threads handle responses the ++ may not be correct
                    orgsFinishedImporting[0]++;
                    if (orgsFinishedImporting[0] == orgs.size()) {
                        mergeFinishedCallback.onResponse(true);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mergeFinishedCallback.onResponse(false);
                }
            }, org);
        }
    }

    private void mergeOrgs(Org lcrOrg, Org cwfOrg) {
        if(cwfOrg != null && cwfOrg.getChildren() != null) {
            for (Org cwfSubOrg : cwfOrg.getChildren()) {
                boolean matchFound = false;
                for (Org lcrSubOrg : lcrOrg.getChildren()) {
                    if (cwfSubOrg.equals(lcrSubOrg)) {
                        //org exists in both lists, merge children recursively
                        mergeOrgs(lcrSubOrg, cwfSubOrg);
                        if (lcrSubOrg.hasUnsavedChanges()) {
                            lcrOrg.setHasUnsavedChanges(true);
                        }
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    //org exists in cwf but not lcr, flag and add to list
                    cwfSubOrg.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                    lcrOrg.getChildren().add(cwfSubOrg);
                }
            }
        }
        mergeCallings(lcrOrg, cwfOrg);
    }

    private void mergeCallings(Org lcrOrg, Org cwfOrg) {
        List<Calling> lcrCallings = lcrOrg.getCallings();
        memberData.setMemberCallings(lcrCallings);
        if(cwfOrg != null && cwfOrg.getCallings() != null) {
            for (Calling cwfCalling : cwfOrg.getCallings()) {
                boolean matchFound = false;

                //Check for a match by Id, if so import cwf data
                for (Calling lcrCalling : lcrCallings) {

                    if (cwfCalling.equals(lcrCalling)) {
                        // todo - test, but probably should not merge in every case. If the potential
                        // became actual then no need to merge
                        // todo - also this needs to be persisted to google drive
                        lcrCalling.importCWFData(cwfCalling);
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    //Ids don't match so check for position matches
                    List<Calling> positionMatches = new ArrayList<>();
                    for (Calling lcrCalling : lcrCallings) {
                        if (lcrCalling.getPosition().equals(cwfCalling.getPosition())) {
                            positionMatches.add(lcrCalling);
                        }
                    }
                    //no matches, we'll mark it as deleted and add it to the list
                    if (positionMatches.size() == 0) {
                        //if there's no id than it didn't exist in lcr to begin with and shouldn't be marked
                        if (cwfCalling.getId() != null) {
                            cwfCalling.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                        }
                        lcrCallings.add(cwfCalling);
                    }
                    //exactly one match so we'll check and copy proposed data if it doesn't match the new current id
                    else if (positionMatches.size() == 1) {
                        Calling lcrCalling = positionMatches.get(0);
                        if (cwfCalling.getProposedIndId() != null && !cwfCalling.getProposedIndId().equals(lcrCalling.getMemberId())) {
                            lcrCalling.importCWFData(cwfCalling);
                        }
                    }
                    //multiple matches by position type, if the potential id matches any current ids we'll mark it as changed and add it otherwise we'll mark it as deleted and add it
                    else if (positionMatches.size() > 1) {
                        if (cwfCalling.getId() != null) {
                            cwfCalling.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                        }
                        for (Calling lcrCalling : positionMatches) {
                            if (cwfCalling.getProposedIndId() != null && cwfCalling.getProposedIndId().equals(lcrCalling.getMemberId())) {
                                cwfCalling.setConflictCause(ConflictCause.EQUIVALENT_POTENTIAL_AND_ACTUAL);
                                break;
                            }
                        }
                        lcrCallings.add(cwfCalling);
                    }
                    // todo - what about EQ Instructor that allows multiples - same positionTypeId but both have a diff
                    // cwfId - they don't ever get matched up and we end up with duplicates
                }
            }
        }
    }

    /* Calling Data */

    public List<Calling> getUnfinalizedCallings() {
        List<Calling> result = new ArrayList<>();
        for(Calling calling: allCallings) {
            if((calling.getProposedIndId() != null && calling.getProposedIndId() > 0) || calling.getProposedStatus() != null) {
                result.add(calling);
            }
        }
        return result;
    }

    public Calling getCalling(String callingId) {
        return callingsById.get(callingId);
    }

    public void addNewCalling(Calling calling) {
        if(calling != null && calling.getParentOrg() != null && calling.getParentOrg() > 0) {
            Org parentOrg = getOrg(calling.getParentOrg());
            parentOrg.getCallings().add(calling);
        }
    }

    /* Position Meta Data */

    public void loadPositionMetadata() {
        webResources.getPositionMetaData(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if(allPositionMetadata == null) {
                        allPositionMetadata = new ArrayList<PositionMetaData>();
                    }
                    if(positionMetaDataByPositionTypeId == null) {
                        positionMetaDataByPositionTypeId = new HashMap<Integer, PositionMetaData>();
                    }
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);

                        JSONObject jsonRequirements = json.has(REQUIREMENTS) ? json.getJSONObject(REQUIREMENTS) : null;
                        PositionRequirements requirement = null;
                        if(jsonRequirements != null) {
                            List<Priesthood> priesthoodList = jsonRequirements.has(PRIESTHOOD)
                                    ? JsonUtil.toEnumListFromJSONArray(Priesthood.class, new JSONArray(jsonRequirements.optString(PRIESTHOOD)))
                                    : JsonUtil.toEnumListFromJSONArray(Priesthood.class, null);

                            List<MemberClass> memberCallingList = jsonRequirements.has(MEMBER_CLASS)
                                    ? JsonUtil.toEnumListFromJSONArray(MemberClass.class, jsonRequirements.getJSONArray(MEMBER_CLASS))
                                    : JsonUtil.toEnumListFromJSONArray(MemberClass.class, null);

                            requirement = new PositionRequirements(
                                    jsonRequirements.has(GENDER) ? Gender.valueOf(jsonRequirements.optString(GENDER)) : null,
                                    jsonRequirements.has(AGE) ? json.optInt(AGE) : 0,
                                    priesthoodList,
                                    memberCallingList);
                        }
                        PositionMetaData positionMetaData = new PositionMetaData(
                                json.has(POSITION_TYPE_ID) ? json.getInt(POSITION_TYPE_ID) : 0,
                                json.has(SHORT_NAME) ? json.getString(SHORT_NAME) : null,
                                requirement);

                        allPositionMetadata.add(positionMetaData);
                        positionMetaDataByPositionTypeId.put(positionMetaData.getPositionTypeId(), positionMetaData);
                    }
                } catch (JSONException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public List<PositionMetaData> getAllPositionMetadata() {
        return this.allPositionMetadata;
    }

    public PositionMetaData getPositionMetadata(int positionTypeId) {
        return positionMetaDataByPositionTypeId.get(positionTypeId);
    }
}