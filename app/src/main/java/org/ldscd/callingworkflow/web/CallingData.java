package org.ldscd.callingworkflow.web;


import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.MemberClass;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.*;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
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
    private PermissionManager permissionManager;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<String, Calling> callingsById;
    private Map<Long, Org> baseOrgByOrgId;
    private List<PositionMetaData> allPositionMetadata;
    private Map<Integer, PositionMetaData> positionMetaDataByPositionTypeId;

    public CallingData(IWebResources webResources, GoogleDataService googleDataService, MemberData memberData, PermissionManager permissionManager) {
        this.webResources = webResources;
        this.googleDataService = googleDataService;
        this.memberData = memberData;
        this.permissionManager = permissionManager;
    }

    /* Org Data */

    public void loadOrgs(final Response.Listener<Boolean> orgsCallback, final ProgressBar pb, Activity activity, final LdsUser currentUser) {
        googleDataService.init(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    pb.setProgress(pb.getProgress() + 15);
                    webResources.getOrgs(new Response.Listener<List<Org>>() {
                        @Override
                        public void onResponse(List<Org> lcrOrgs) {
                            orgs = lcrOrgs;
                            orgsById = new HashMap<Long, Org>();
                            callingsById = new HashMap<String, Calling>();
                            baseOrgByOrgId = new HashMap<Long, Org>();
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

    /*public void loadOrg(final Response.Listener<Boolean> listener, final Org org) {
        googleDataService.getOrgData(new Response.Listener<Org>() {
            @Override
            public void onResponse(Org googleOrg) {
                if(googleOrg != null) {
                    extractOrg(googleOrg, googleOrg.getId());
                }
                listener.onResponse(true);
            }
        }, null, org);
    }*/

    public void refreshOrgFromGoogleDrive(final Response.Listener<Org> listener, Long orgId) {
        final Org org = getOrg(orgId);
        if(org != null) {
            googleDataService.getOrgData(new Response.Listener<Org>() {
                @Override
                public void onResponse(Org googleOrg) {
                    //changes to current and potential assignments will leave incorrect data in the member objects, we must remove them now
                    //while references still exist in the current org and correct data is added back in after the merge completes
                    memberData.removeMemberCallings(org.getCallings());
                    mergeOrgs(org, googleOrg);
                    extractOrg(org, org.getId());
                    listener.onResponse(org);
                }
            }, null, org);
        }
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

    public void extractOrg(Org org, long baseOrgId) {
        orgsById.put(org.getId(), org);
        baseOrgByOrgId.put(org.getId(), orgsById.get(baseOrgId));
        if(org.getCallings() != null) {
            for (Calling calling : org.getCallings()) {
                callingsById.put(calling.getCallingId(), calling);
            }
        }
        if(org.getChildren() != null) {
            for (Org subOrg : org.getChildren()) {
                extractOrg(subOrg, baseOrgId);
            }
        }
    }

    /* Import and Merge Data */

    private void importAndMergeGoogleData(final Response.Listener<Boolean> mergeFinishedCallback) {
        googleDataService.getOrgs(new Response.Listener<List<Org>>() {
            @Override
            public void onResponse(List<Org> cwfOrgs) {
                List<Org> cwfOrgsToAdd = new ArrayList<Org>();
                for (Org cwfOrg : cwfOrgs) {
                    boolean matchFound = false;
                    for (Org org : orgs) {
                        if (cwfOrg.getId() == org.getId()) {
                            mergeOrgs(org, cwfOrg);
                            matchFound = true;
                            break;
                        }
                    }
                    if(!matchFound) {
                        cwfOrg.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                        cwfOrgsToAdd.add(cwfOrg);
                    }
                }
                orgs.addAll(cwfOrgsToAdd);

                mergeFinishedCallback.onResponse(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mergeFinishedCallback.onResponse(false);
            }
        });
    }

    //This merges the orgs and callings from the cwfOrg into the lcrOrg so when completed the lcrOrg will have both
    public void mergeOrgs(Org lcrOrg, Org cwfOrg) {
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
        if(cwfOrg.getCallings() != null) {
            for (Calling cwfCalling : cwfOrg.getCallings()) {
                boolean matchFound = false;
                //Check for a match by Id, if so import cwf data
                for (Calling lcrCalling : lcrCallings) {

                    if (cwfCalling.isEqualsTo(lcrCalling)) {
                        // todo - this needs to be persisted to google drive
                        //if the proposed member has become the current we can discard the old proposed info
                        if(!(cwfCalling.getProposedIndId() > 0 && cwfCalling.getProposedIndId().equals(lcrCalling.getMemberId()))) {
                            lcrCalling.importCWFData(cwfCalling);
                        } else {
                            lcrCalling.setCwfId(cwfCalling.getCwfId());
                        }
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    //see if there's a matching current id for the proposed id
                    boolean potentialMatchesActual = false;
                    for (Calling lcrCalling : lcrCallings) {
                        if (lcrCalling.getPosition().equals(cwfCalling.getPosition()) && lcrCalling.getMemberId().equals(cwfCalling.getProposedIndId())) {
                            potentialMatchesActual = true;
                        }
                    }

                    //set the flag if there's a match, else if lcr based id is present then it previously existed so mark it as deleted
                    if (potentialMatchesActual) {
                        cwfCalling.setConflictCause(ConflictCause.EQUIVALENT_POTENTIAL_AND_ACTUAL);
                    } else if(cwfCalling.getId() != null && cwfCalling.getId() > 0) {
                        cwfCalling.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                    }
                    lcrCallings.add(cwfCalling);
                }
            }
        }
        //merge complete, this should now include callings from both sources
        memberData.setMemberCallings(lcrCallings);
    }

    /* Calling Data */

    public List<Calling> getUnfinalizedCallings() {
        List<Calling> result = new ArrayList<>();
        for(Org org : getOrgs()) {
            for(Calling calling: org.allOrgCallings()) {
                if ((calling.getProposedIndId() != null && calling.getProposedIndId() > 0)
                        || calling.getProposedStatus() != CallingStatus.NONE || calling.getNotes() != null) {
                    result.add(calling);
                }
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

    public void releaseLDSCalling(Calling calling, int orgTypeId, Response.Listener callback) throws JSONException {
        webResources.releaseCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(), orgTypeId, callback);
    }

    public void updateLDSCalling(Calling calling, int orgTypeId, Response.Listener callback) throws JSONException {
        webResources.updateCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(), orgTypeId, callback);
    }

    public void deleteLDSCalling(Calling calling, int orgTypeId, Response.Listener callback) throws JSONException {
        webResources.deleteCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(), orgTypeId, callback);
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