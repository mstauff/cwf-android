package org.ldscd.callingworkflow.web;


import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.MemberClass;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.model.PositionRequirements;
import org.ldscd.callingworkflow.model.UnitSettings;
import org.ldscd.callingworkflow.model.permissions.AuthorizableOrg;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CallingData {
    private static final String TAG = "CallingData";

    private static final String AGE = "age";
    private static final String GENDER = "gender";
    private static final String PRIESTHOOD = "priesthood";
    private static final String MEMBER_CLASS = "memberClasses";
    private static final String POSITION_TYPE_ID = "positionTypeId";
    private static final String SHORT_NAME = "shortName";
    private static final String MEDIUM_NAME = "mediumName";
    private static final String REQUIREMENTS = "requirements";

    private IWebResources webResources;
    private GoogleDataService googleDataService;
    private MemberData memberData;
    private PermissionManager permissionManager;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<String, Calling> callingsById;
    private Map<Long, Org> baseOrgByOrgId;
    private Map<Long, Org> LcrBaseOrgByOrgId;
    private Map<Long, Org> lcrOrgsById;
    private List<PositionMetaData> allPositionMetadata;
    private Map<Integer, PositionMetaData> positionMetaDataByPositionTypeId;
    private Set<Org> orgsToSave;
    private Set<Org> baseOrgsToRemove;
    private LdsUser currentUser;

    public CallingData(IWebResources webResources, GoogleDataService googleDataService, MemberData memberData, PermissionManager permissionManager) {
        this.webResources = webResources;
        this.googleDataService = googleDataService;
        this.memberData = memberData;
        this.permissionManager = permissionManager;
        orgsToSave = new HashSet<>();
        baseOrgsToRemove = new HashSet<>();
    }

    /* Org Data */

    public void loadOrgs(final Response.Listener<Boolean> orgsCallback, final ProgressBar pb, Activity activity, final LdsUser currentUser) {
        googleDataService.init(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    pb.setProgress(pb.getProgress() + 15);
                    webResources.getOrgs(true, new Response.Listener<List<Org>>() {
                        @Override
                        public void onResponse(List<Org> lcrOrgs) {
                            orgs = lcrOrgs;
                            setLCRBaseOrgs();
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

                                                    Set<Org> baseOrgsToSave = new HashSet<>();
                                                    for(Org org: orgsToSave) {
                                                        baseOrgsToSave.add(getBaseOrg(org.getId()));
                                                    }
                                                    orgsToSave.clear();
                                                    for(Org org: baseOrgsToSave) {
                                                        googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                                                            @Override
                                                            public void onResponse(Boolean success) {
                                                                if(!success) {
                                                                    Log.e(TAG, "Failed to save merge changes to Google Drive");
                                                                }
                                                            }
                                                        }, org);
                                                    }
                                                    // TODO: need to remove this code or implement this functionality.
                                                    /*for(Org org: baseOrgsToRemove) {
                                                        *//*googleDataService.deleteFile(new Response.Listener<Boolean>() {
                                                            @Override
                                                            public void onResponse(Boolean success) {
                                                                if(!success) {
                                                                    Log.e(TAG, "Failed to remove org file from Google Drive");
                                                                }
                                                            }
                                                        }, org);*//*
                                                    }*/
                                                    orgsCallback.onResponse(true);
                                                } else {
                                                    Log.e(TAG, "failed to merge cwf file data");
                                                    orgsCallback.onResponse(false);
                                                }
                                            }
                                        }, currentUser);
                                    } else {
                                        Log.e(TAG, "failed to sync drive Ids");
                                        orgsCallback.onResponse(false);
                                    }
                                }
                            }, getAuthorizableOrgs(orgs, currentUser));
                        }
                    });
                } else {
                    orgsCallback.onResponse(false);
                }
            }
        }, activity);
    }

    public void clearLocalOrgData() {
        memberData.removeAllMemberCallings();
        orgs = new ArrayList<>();
        orgsById = new HashMap<>();
        callingsById = new HashMap<>();
        baseOrgByOrgId = new HashMap<>();
    }
    public void refreshLCROrgs(final Response.Listener<Boolean> listener, final LdsUser currentUser) {
        webResources.getOrgs(true, new Response.Listener<List<Org>>() {
            @Override
            public void onResponse(List<Org> lcrOrgs) {
                if(lcrOrgs != null) {
                    /* Reset all cached items. */
                    orgs = lcrOrgs;
                    orgsById = new HashMap<Long, Org>();
                    callingsById = new HashMap<String, Calling>();
                    baseOrgByOrgId = new HashMap<Long, Org>();
                    /* Resync's with google drive */
                    googleDataService.syncDriveIds(new Response.Listener<Boolean>() {
                        @Override
                        public void onResponse(Boolean driveSyncSucceeded) {
                            if (driveSyncSucceeded) {
                                /* Get the files from google drive and merge data */
                                importAndMergeGoogleData(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        if(response) {
                                            /* Add items to cached elements */
                                            for (Org org : orgs) {
                                                extractOrg(org, org.getId());
                                            }
                                            Set<Org> baseOrgsToSave = new HashSet<>();
                                            for (Org org : orgsToSave) {
                                                baseOrgsToSave.add(getBaseOrg(org.getId()));
                                            }
                                            orgsToSave.clear();
                                            for (Org org : baseOrgsToSave) {
                                                googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                                                    @Override
                                                    public void onResponse(Boolean success) {
                                                        if (!success) {
                                                            Log.e(TAG, "Failed to save merge changes to Google Drive");
                                                        }
                                                    }
                                                }, org);
                                            }
                                            listener.onResponse(true);
                                        } else {
                                            listener.onResponse(false);
                                        }
                                    }
                                }, currentUser);
                            } else {
                                listener.onResponse(false);
                            }
                        }
                    }, getAuthorizableOrgs(lcrOrgs, currentUser));
                }
            }
        });
    }

    public void refreshOrgFromGoogleDrive(final Response.Listener<Org> listener, Long orgId, final LdsUser currentUser) {
        final Org org = getOrg(orgId);
        /* Check org viewing permissions first */
        isAuthorizableOrg(org, currentUser, org.getId());
        if(org != null && org.getCanView()) {
            googleDataService.getOrgData(new Response.Listener<Org>() {
                @Override
                public void onResponse(Org googleOrg) {
                    //changes to current and potential assignments will leave incorrect data in the member objects, we must remove them now
                    //while references still exist in the current org and correct data is added back in after the merge completes
                    memberData.removeMemberCallings(org.getCallings());
                    mergeOrgs(org, googleOrg, currentUser);
                    extractOrg(org, org.getId());
                    listener.onResponse(org);
                }
            }, null, org);
        } else {
            listener.onResponse(org);
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

    private void importAndMergeGoogleData(final Response.Listener<Boolean> mergeFinishedCallback, final LdsUser currentUser) {
        googleDataService.getOrgs(new Response.Listener<List<Org>>() {
            @Override
            public void onResponse(List<Org> cwfOrgs) {
                List<Org> cwfOrgsToAdd = new ArrayList<Org>();
                /* Cycle through all google drive orgs */
                for (Org cwfOrg : cwfOrgs) {
                    boolean matchFound = false;
                    /* Cycle through all lcr orgs only until or if a match is found */
                    for (Org org : orgs) {
                        if (cwfOrg.getId() == org.getId()) {
                            memberData.removeMemberCallings(cwfOrg.allOrgCallings());
                            mergeOrgs(org, cwfOrg, currentUser);
                            matchFound = true;
                            break;
                        }
                    }
                    /* If the org is gone then the user will need to address this cwf org */
                    if(!matchFound) {
                        if(hasProposedData(cwfOrg)) {
                            cwfOrg.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                            cwfOrgsToAdd.add(cwfOrg);
                        } else {
                            baseOrgsToRemove.add(cwfOrg);
                        }
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
    public void mergeOrgs(Org lcrOrg, Org cwfOrg, LdsUser currentUser) {
        if(cwfOrg != null && cwfOrg.getChildren() != null) {
            for (Org cwfSubOrg : cwfOrg.getChildren()) {
                boolean matchFound = false;
                for (Org lcrSubOrg : lcrOrg.getChildren()) {
                    if (cwfSubOrg.equals(lcrSubOrg)) {
                        //org exists in both lists, merge children recursively
                        mergeOrgs(lcrSubOrg, cwfSubOrg, currentUser);
                        if (lcrSubOrg.hasUnsavedChanges()) {
                            lcrOrg.setHasUnsavedChanges(true);
                        }
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    //org exists in cwf but not lcr, if it has proposed data flag and add to list
                    if(hasProposedData(cwfSubOrg)) {
                        cwfSubOrg.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                        lcrOrg.getChildren().add(cwfSubOrg);
                    } else { //if it doesn't have proposed data don't add it and add org to the save list to remove missing subOrg from google drive
                        orgsToSave.add(lcrOrg);
                    }
                }
            }
        }
        if(lcrOrg.getChildren().size() > cwfOrg.getChildren().size()) {
            orgsToSave.add(lcrOrg);
        }
        /* Check viewable rights.  If current user can view the merge calling data. */
        isAuthorizableOrg(cwfOrg, currentUser,  cwfOrg.getId());
        if(cwfOrg.getCanView()) {
            mergeCallings(lcrOrg, cwfOrg);
        }
    }

    //This merges callings from both orgs into the lcrOrg
    private void mergeCallings(Org lcrOrg, Org cwfOrg) {
        List<Calling> lcrCallings = lcrOrg.getCallings();
        int cwfMatchableCallingCount = 0;
        if(cwfOrg.getCallings() != null) {
            //initialize a map by position type to list the empty callings which we have no way of matching
            //we can deal with them as a group once the list is complete
            Map<Integer, List<Calling>> cwfVacantCallingsByType = new HashMap<>();
            for (Calling cwfCalling : cwfOrg.getCallings()) {
                if(!cwfVacantCallingsByType.containsKey(cwfCalling.getPosition().getPositionTypeId())) {
                    cwfVacantCallingsByType.put(cwfCalling.getPosition().getPositionTypeId(), new ArrayList<Calling>());
                }

                //if it's cwfOnly then we only try to merge by cwfId in case it has been added on previously
                if(cwfCalling.isCwfOnly()) {
                    boolean matchFound = false;
                    for(Calling lcrCalling: lcrCallings) {
                        if(cwfCalling.getCwfId().equals(lcrCalling.getCwfId())) {
                            lcrCalling.importCWFData(cwfCalling);
                            matchFound = true;
                            break;
                        }
                    }
                    if(!matchFound) {
                        lcrCallings.add(cwfCalling);
                    }
                }
                //Check if calling is one of the empty unmatchable callings, callings where multiples aren't allowed they can be matched on type without the list so we don't include them
                else if((cwfCalling.getId() == null || cwfCalling.getId() < 1) && cwfCalling.getPosition().getAllowMultiple()) {
                    cwfVacantCallingsByType.get(cwfCalling.getPosition().getPositionTypeId()).add(cwfCalling);
                }
                //special cases have been handled, now try to merge and set conflict flags if a match wasn't found
                else {
                    cwfMatchableCallingCount++;
                    boolean matchFound = false;
                    //Check for a match by Id, if so import cwf data
                    for (Calling lcrCalling : lcrCallings) {

                        if (cwfCalling.isEqualsTo(lcrCalling)) {
                            //if the proposed member has become the current we can discard the old proposed info
                            if (!(cwfCalling.getProposedIndId() > 0 && cwfCalling.getProposedIndId().equals(lcrCalling.getMemberId()))) {
                                lcrCalling.importCWFData(cwfCalling);
                            }
                            matchFound = true;
                            break;
                        }
                    }

                    if (!matchFound) {
                        //see if there's proposed data and if there's a matching current id for the proposed id
                        boolean hasPotentialInfo = hasProposedData(cwfCalling);
                        boolean potentialMatchesActual = false;
                        if(hasPotentialInfo) {
                            for (Calling lcrCalling : lcrCallings) {
                                if (lcrCalling.getPosition().equals(cwfCalling.getPosition())) {
                                    if (lcrCalling.getMemberId() != null && cwfCalling.getProposedIndId() != null && lcrCalling.getMemberId().equals(cwfCalling.getProposedIndId())) {
                                        potentialMatchesActual = true;
                                        break;
                                    }
                                }
                            }
                        }

                        //set the flag if there's a matching potentialId, else if there's potential data we'll save it with the deleted flag
                        //if neither of those is true then discard and set the org to be saved without it
                        if (potentialMatchesActual) {
                            cwfCalling.setConflictCause(ConflictCause.EQUIVALENT_POTENTIAL_AND_ACTUAL);
                            lcrCallings.add(cwfCalling);
                        } else if(hasPotentialInfo) {
                            cwfCalling.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                            lcrCallings.add(cwfCalling);
                        } else {
                            orgsToSave.add(lcrOrg);
                        }
                    }
                }
            }

            //all empty calling lists for cwf should now be complete, we'll build lcr lists to match up with them
            Map<Integer, List<Calling>> lcrVacantCallingsByType = new HashMap<>();
            for(Calling lcrCalling: lcrCallings) {
                if(!lcrCalling.isCwfOnly() && (lcrCalling.getId() == null || lcrCalling.getId() < 1) && lcrCalling.getPosition().getAllowMultiple()) {
                    if(!lcrVacantCallingsByType.containsKey(lcrCalling.getPosition().getPositionTypeId())) {
                        lcrVacantCallingsByType.put(lcrCalling.getPosition().getPositionTypeId(), new ArrayList<Calling>());
                    }
                    lcrVacantCallingsByType.get(lcrCalling.getPosition().getPositionTypeId()).add(lcrCalling);
                }
            }
            //Since there's no real difference between the empty callings, we'll just merge them together in order and any extras we'll flag and add to the end
            for(Integer vacantPositionTypeId: cwfVacantCallingsByType.keySet()) {
                List<Calling> cwfVacantCallings = cwfVacantCallingsByType.get(vacantPositionTypeId);
                List<Calling> lcrVacantCallings = lcrVacantCallingsByType.containsKey(vacantPositionTypeId) ? lcrVacantCallingsByType.get(vacantPositionTypeId) : new ArrayList<Calling>();

                //if there are new empty callings from lcr set we'll save them to google
                if(lcrVacantCallings.size() > cwfVacantCallings.size()) {
                    orgsToSave.add(lcrOrg);
                }

                //Trim extra entries off if possible
                if(cwfVacantCallings.size() > lcrVacantCallings.size()) {
                    int numExtraCallings = cwfVacantCallings.size() - lcrVacantCallings.size();
                    for(int i = cwfVacantCallings.size()-1; i >= 0; i--) {
                        if(numExtraCallings > 0 && !hasProposedData(cwfVacantCallings.get(i))) {
                            cwfVacantCallings.remove(i);
                            orgsToSave.add(lcrOrg);
                            numExtraCallings--;
                        }
                    }
                }

                for(int i=0; i < cwfVacantCallings.size(); i++) {

                    if(i < lcrVacantCallings.size()) {
                        lcrVacantCallings.get(i).importCWFData(cwfVacantCallings.get(i));
                    } else {
                        Calling calling = cwfVacantCallings.get(i);
                        calling.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                        lcrCallings.add(calling);
                    }
                }
            }
        }

        //check if there are more calling from lcr that need saved to google
        int lcrMatchableCallingCount = 0;
        for(Calling lcrCalling: lcrCallings) {
            if (!lcrCalling.getPosition().getAllowMultiple() || (lcrCalling.getId() != null && lcrCalling.getId() > 0)) {
                lcrMatchableCallingCount++;
            }
        }
        if(lcrMatchableCallingCount > cwfMatchableCallingCount) {
            orgsToSave.add(lcrOrg);
        }

        //merge complete, this should now include callings from both sources
        memberData.setMemberCallings(lcrCallings);
    }

    private boolean hasProposedData(Org org) {
        boolean result = false;
        for(Calling calling: org.getCallings()) {
            if(hasProposedData(calling)) {
                result = true;
                break;
            }
        }
        if(!result) {
            for(Org subOrg: org.getChildren()) {
                if(hasProposedData(subOrg)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean hasProposedData(Calling calling) {
        return ((calling.getProposedIndId() != null && calling.getProposedIndId() > 0) ||
                (calling.getProposedStatus() != null && !calling.getProposedStatus().equals(CallingStatus.NONE)) ||
                (calling.getNotes() != null && calling.getNotes() != ""));
    }

    /* Permission on orgs */
    private void isAuthorizableOrg(Org org, LdsUser currentUser, long baseOrgId) {
        boolean hasPermission = false;
        org.setCanView(false);
        UnitLevelOrgType unitLevelOrgType = null;
        Org baseOrg = lcrOrgsById.get(baseOrgId);
        if(baseOrg != null) {
            unitLevelOrgType =  UnitLevelOrgType.get(baseOrg.getOrgTypeId());
        }
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.ORG_INFO_READ,
                new AuthorizableOrg(
                        currentUser.getUnitNumber(),
                        unitLevelOrgType,
                        org.getOrgTypeId()))) {
            hasPermission = true;
        }
        org.setCanView(hasPermission);

        for(Org child : org.getChildren()) {
            isAuthorizableOrg(child, currentUser, baseOrgId);
        }
    }

    private List<Org> getAuthorizableOrgs(List<Org> lcrOrgs, LdsUser currentUser) {
        List<Org> orgs = new ArrayList<>();
        if(lcrOrgs != null && lcrOrgs.size() > 0) {
            for(Org org : lcrOrgs) {
                isAuthorizableOrg(org, currentUser, org.getId());
                if(org.getCanView()) {
                    orgs.add(org);
                }
            }
        }
        return orgs;
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

    public void releaseLDSCalling(final Calling calling, final Response.Listener<Boolean> callback, final Response.ErrorListener errorListener) throws JSONException {
        webResources.releaseCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(),  getOrg(calling.getParentOrg()).getOrgTypeId(),  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if(jsonObject.has("errors") && jsonObject.getJSONObject("errors").length() > 0) {
                            errorListener.onErrorResponse(hydrateErrorListener(jsonObject, Operation.RELEASE));
                        } else {
                            calling.setId(null);
                            calling.setNotes("");
                            calling.setExistingStatus(null);
                            calling.setProposedIndId(null);
                            calling.setProposedStatus(CallingStatus.NONE);
                            calling.setActiveDate(null);
                            calling.setActiveDateTime(null);
                            calling.setCwfId(UUID.randomUUID().toString());
                            calling.setMemberId(null);
                            calling.setConflictCause(null);
                            googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                                @Override
                                public void onResponse(Boolean response) {
                                    if(response) {
                                        callback.onResponse(true);
                                    } else {
                                        callback.onResponse(false);
                                    }
                                }
                            }, getBaseOrg(calling.getParentOrg()));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onResponse(false);
                }
            }
        });
    }

    public void updateLDSCalling(final Calling calling, final Response.Listener<Boolean> callback, final Response.ErrorListener errorListener) throws JSONException {
        webResources.updateCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(), getOrg(calling.getParentOrg()).getOrgTypeId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if(jsonObject.has("errors") && jsonObject.getJSONObject("errors").length() > 0) {
                            errorListener.onErrorResponse(hydrateErrorListener(jsonObject, Operation.UPDATE));
                        } else {
                            try {
                                if(jsonObject.has("positionId")) {
                                    calling.setId(jsonObject.getLong("positionId"));
                                }
                                calling.setNotes("");
                                calling.setExistingStatus(null);
                                calling.setProposedIndId(null);
                                calling.setProposedStatus(CallingStatus.NONE);
                                calling.setActiveDate(DateTime.now());
                                calling.setActiveDateTime(DateTime.now());
                                calling.setCwfId("");
                                calling.setMemberId(jsonObject.getLong("memberId"));
                                calling.setConflictCause(null);
                                if(calling.isCwfOnly()) {
                                    calling.setCwfOnly(false);
                                }
                                googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        if(response) {
                                            callingsById.put(calling.getCallingId(), calling);
                                            callback.onResponse(true);
                                        } else {
                                            callback.onResponse(false);
                                        }
                                    }
                                }, getBaseOrg(calling.getParentOrg()));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /* Removes the calling from LCR then from google drive */
    public void deleteLDSCalling(final Calling calling, final Response.Listener callback, final Response.ErrorListener errorListener) throws JSONException {
        final Org originalOrg = getOrg(calling.getParentOrg());
        webResources.deleteCalling(calling, originalOrg.getUnitNumber(), originalOrg.getOrgTypeId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if(jsonObject != null && jsonObject.has("errors") && jsonObject.getJSONObject("errors").length() > 0) {
                        errorListener.onErrorResponse(hydrateErrorListener(jsonObject, Operation.DELETE));
                    } else {
                        /* Get Latest org changes from google drive. */
                        googleDataService.getOrgData(new Response.Listener<Org>() {
                            @Override
                            public void onResponse(final Org latestGoogleDriveOrg) {
                                /* Create a calling before the changes are made for backup purposes. */
                                final Calling backupCalling = new Calling(calling);
                                /* Removes the calling from within the new org. */
                                Org parentOrg = findSubOrg(latestGoogleDriveOrg, calling.getParentOrg());
                                parentOrg.removeCalling(calling);
                                /* Save the changes back to google drive. */
                                googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean success) {
                                        if(success) {
                                            /* Remove the callings that belong to the Org from the members with a proposed calling. */
                                            memberData.removeMemberCallings(latestGoogleDriveOrg.getCallings());
                                            /* Extract recent subOrgs and callings to cached items. */
                                            extractOrg(latestGoogleDriveOrg, latestGoogleDriveOrg.getId());
                                            /* Re-add Member Assignments */
                                            memberData.setMemberCallings(latestGoogleDriveOrg.getCallings());
                                            if(backupCalling.getProposedIndId() != null && backupCalling.getProposedIndId() > 0) {
                                                memberData.getMember(backupCalling.getProposedIndId()).removeProposedCalling(calling);
                                            }
                                        }
                                        callback.onResponse(true);
                                    }
                                }, latestGoogleDriveOrg);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.getMessage());
                                callback.onResponse(false);
                            }
                        }, getBaseOrg(originalOrg.getId()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private VolleyError hydrateErrorListener(JSONObject json, Operation action) {
        VolleyError error = null;
        try {
            switch(action) {
                case CREATE:
                    break;
                case READ:
                    break;
                case UPDATE:
                    error = new VolleyError(json.getJSONObject("errors").getString("memberId").replace("[", "").replace("]", ""));
                    break;
                case DELETE:
                    break;
                case RELEASE:
                    break;
                case DELETE_LCR:
                    break;
            }
        } catch (JSONException e) {
            error = new VolleyError("An internal error occurred");
        }
        return error;
    }

    public void updateCalling(Org baseOrg, Calling updatedCalling, Operation operation) {
        if(operation.equals(Operation.UPDATE)) {
            Calling original = baseOrg.getCallingById(updatedCalling.getCallingId());
            original.setNotes(updatedCalling.getNotes());
            original.setProposedIndId(updatedCalling.getProposedIndId());
            if(!updatedCalling.getProposedStatus().equals(CallingStatus.UNKNOWN)) {
                original.setProposedStatus(updatedCalling.getProposedStatus());
            }
        } else if(operation.equals(Operation.CREATE)) {
            Org org = findSubOrg(baseOrg, updatedCalling.getParentOrg());
            org.getCallings().add(updatedCalling);
        }
    }

    public boolean canDeleteCalling(Calling calling, Org org) {
        boolean canDelete = false;
        if(calling != null) {
            if(!calling.getPosition().getAllowMultiple()) {
                return false;
            } else {
                if(org.getCallingByPositionTypeId(calling.getPosition().getPositionTypeId()).size() > 1) {
                    canDelete = true;
                }
            }
        }
        return canDelete;
    }

    public Org findSubOrg(Org org, long subOrgId) {
        Org result = null;
        if(org.getId() == subOrgId) {
            result = org;
        } else {
            for(Org subOrg: org.getChildren()) {
                result = findSubOrg(subOrg, subOrgId);
                if(result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /* Use only for initial load */
    private void setLCRBaseOrgs() {
        LcrBaseOrgByOrgId = new HashMap<>(orgs.size());
        for(Org org : orgs) {
            LcrBaseOrgByOrgId.put(org.getId(), org);
            setLcrOrgsById(org);
        }
    }

    private void setLcrOrgsById(Org org) {
        if(lcrOrgsById == null) {
            lcrOrgsById = new HashMap<>();
        }
        lcrOrgsById.put(org.getId(), org);
        for(Org child : org.getChildren()) {
            setLcrOrgsById(child);
        }
    }

    /* Only to be used on initial load */
    private Org getLcrBaseOrg(long id) {
        return LcrBaseOrgByOrgId.get(id);
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

                        JSONObject jsonRequirements = json.has(REQUIREMENTS) && !json.isNull(REQUIREMENTS) ? json.getJSONObject(REQUIREMENTS) : null;
                        PositionRequirements requirement = null;
                        if(jsonRequirements != null) {
                            List<Priesthood> priesthoodList = jsonRequirements.has(PRIESTHOOD)
                                    ? JsonUtil.toEnumListFromJSONArray(Priesthood.class, new JSONArray(jsonRequirements.optString(PRIESTHOOD)))
                                    : JsonUtil.toEnumListFromJSONArray(Priesthood.class, null);

                            List<MemberClass> memberCallingList = jsonRequirements.has(MEMBER_CLASS)
                                    ? JsonUtil.toEnumListFromJSONArray(MemberClass.class, new JSONArray(jsonRequirements.optString(MEMBER_CLASS)))
                                    : JsonUtil.toEnumListFromJSONArray(MemberClass.class, null);

                            requirement = new PositionRequirements(
                                    jsonRequirements.has(GENDER) ? Gender.valueOf(jsonRequirements.optString(GENDER)) : null,
                                    jsonRequirements.has(AGE) ? json.optInt(AGE) : 0,
                                    priesthoodList,
                                    memberCallingList);
                        }
                        PositionMetaData positionMetaData = new PositionMetaData(
                                json.has(POSITION_TYPE_ID) ? json.getInt(POSITION_TYPE_ID) : 0,
                                json.has(SHORT_NAME) && !json.isNull(SHORT_NAME) ? json.getString(SHORT_NAME) : null,
                                json.has(MEDIUM_NAME) && !json.isNull(MEDIUM_NAME) ? json.getString(MEDIUM_NAME) : null,
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

    /* Returns a filtered list of calling status' */
    public void getCallingStatus(final Response.Listener<List<CallingStatus>> listener, Long unitNumber) {
        googleDataService.getUnitSettings(new Response.Listener<UnitSettings>() {
            @Override
            public void onResponse(UnitSettings unitSettings) {
                List<CallingStatus> statuses = new ArrayList<CallingStatus>();
                if(unitSettings.getDisabledStatuses().size()+1 == CallingStatus.values().length) {
                    statuses.clear();
                    statuses.addAll(Arrays.asList(CallingStatus.values()));
                } else {
                    for (CallingStatus status : CallingStatus.values()) {
                        if (!unitSettings.getDisabledStatuses().contains(status)) {
                            statuses.add((status));
                        }
                    }
                }
                listener.onResponse(statuses);
            }
        }, unitNumber);
    }
}