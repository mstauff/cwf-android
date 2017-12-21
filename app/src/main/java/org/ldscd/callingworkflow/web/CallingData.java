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
import org.ldscd.callingworkflow.model.*;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private List<PositionMetaData> allPositionMetadata;
    private Map<Integer, PositionMetaData> positionMetaDataByPositionTypeId;
    private Set<Org> orgsToSave;
    private Set<Org> baseOrgsToRemove;

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
                                                    for(Org org: baseOrgsToRemove) {
                                                        /*googleDataService.deleteFile(new Response.Listener<Boolean>() {
                                                            @Override
                                                            public void onResponse(Boolean success) {
                                                                if(!success) {
                                                                    Log.e(TAG, "Failed to remove org file from Google Drive");
                                                                }
                                                            }
                                                        }, org);*/
                                                    }
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
        mergeCallings(lcrOrg, cwfOrg);
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
                                    if (lcrCalling.getMemberId().equals(cwfCalling.getProposedIndId())) {
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

                        JSONObject jsonRequirements = json.has(REQUIREMENTS) && !json.isNull(REQUIREMENTS) ? json.getJSONObject(REQUIREMENTS) : null;
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