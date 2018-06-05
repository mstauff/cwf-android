package org.ldscd.callingworkflow.web;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

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
import org.ldscd.callingworkflow.services.GoogleDriveService;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    public static final String ERRORS = "errors";
    public static final String POSITION_ID = "positionId";
    public static final String MEMBER_ID = "memberId";

    private IWebResources webResources;
    private GoogleDriveService googleDriveService;
    private MemberData memberData;
    private PermissionManager permissionManager;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<String, Calling> callingsById;
    private Map<Long, Org> baseOrgByOrgId;
    private Map<Long, Org> LcrBaseOrgByOrgId;
    private Map<Long, Org> lcrOrgsById;
    private Map<Long, ConflictCause> orgConflicts;
    private Map<String, ConflictCause> callingConflicts;
    private List<PositionMetaData> allPositionMetadata;
    private Map<Integer, PositionMetaData> positionMetaDataByPositionTypeId;
    private Set<Org> orgsToSave;
    private Set<Org> baseOrgsToRemove;

    public CallingData(IWebResources webResources, GoogleDriveService googleDriveService, MemberData memberData, PermissionManager permissionManager) {
        this.webResources = webResources;
        this.googleDriveService = googleDriveService;
        this.memberData = memberData;
        this.permissionManager = permissionManager;
        orgsToSave = new HashSet<>();
        baseOrgsToRemove = new HashSet<>();
    }

    /* Org Data */

    public void loadOrgs(final Response.Listener<Boolean> orgsCallback, final Response.Listener<WebException> errorCallback, final ProgressBar pb, Activity activity, final LdsUser currentUser) {
        googleDriveService.init(activity)
            .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if(task.getResult()) {
                        pb.setProgress(pb.getProgress() + 15);
                        webResources.getOrgs(true, new Response.Listener<List<Org>>() {
                            @Override
                            public void onResponse(List<Org> lcrOrgs) {
                                orgs = lcrOrgs;
                                setLCRBaseOrgs();
                                orgsById = new HashMap<>();
                                callingsById = new HashMap<String, Calling>();
                                baseOrgByOrgId = new HashMap<>();
                                orgConflicts = new HashMap<>();
                                callingConflicts = new HashMap<>();
                                pb.setProgress(pb.getProgress() + 20);
                                googleDriveService.syncDriveIds(getAuthorizableOrgs(orgs, currentUser))
                                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            if(result) {
                                                pb.setProgress(pb.getProgress() + 15);
                                                importAndMergeGoogleData(new Response.Listener<Boolean>() {
                                                    @Override
                                                    public void onResponse(Boolean mergeSucceeded) {
                                                        if (mergeSucceeded) {
                                                            for (Org org : orgs) {
                                                                extractOrg(org, org.getId());
                                                            }
                                                            pb.setProgress(pb.getProgress() + 10);

                                                            /* Set the orgs that might need to deleted. */
                                                            for (Org org : baseOrgsToRemove) {
                                                                if (org.getConflictCause() != null && org.getConflictCause().getConflictCause().length() > 0) {
                                                                    setOrgConflict(org, ConflictCause.LDS_EQUIVALENT_DELETED);
                                                                    orgConflicts.put(org.getId(), org.getConflictCause());
                                                                }
                                                            }
                                                            /* Items to save */
                                                            Set<Org> baseOrgsToSave = new HashSet<>();
                                                            for (Org org : orgsToSave) {
                                                                baseOrgsToSave.add(getBaseOrg(org.getId()));
                                                            }
                                                            orgsToSave.clear();
                                                            final List<Task<Boolean>> saveOrgFileTasks = new ArrayList<>(orgs.size());
                                                            for (Org org : baseOrgsToSave) {
                                                                saveOrgFileTasks.add(googleDriveService.saveOrgFile(org));
                                                            }
                                                            Tasks.whenAllComplete(saveOrgFileTasks)
                                                                    .addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                                                                        @Override
                                                                        public void onSuccess(List<Task<?>> tasks) {
                                                                            for (Task<Boolean> results : saveOrgFileTasks) {
                                                                                if (!results.getResult()) {
                                                                                    orgsCallback.onResponse(false);
                                                                                    break;
                                                                                }
                                                                            }
                                                                            orgsCallback.onResponse(true);
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    errorCallback.onResponse(ensureWebException(e));
                                                                }
                                                            });
                                                        } else {
                                                            Log.e(TAG, "failed to merge cwf file data");
                                                            orgsCallback.onResponse(false);
                                                        }
                                                    }
                                                }, new Response.Listener<WebException>() {
                                                    @Override
                                                    public void onResponse(WebException response) {
                                                        errorCallback.onResponse(response);
                                                    }
                                                }, currentUser);
                                            } else {
                                                Log.e(TAG, "failed to sync drive Ids");
                                                errorCallback.onResponse(new WebException(ExceptionType.UNKOWN_GOOGLE_EXCEPTION));
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "failed to sync drive Ids");
                                        errorCallback.onResponse(ensureWebException(e));
                                    }
                                });
                            }
                        }, errorCallback);
                    } else {
                        errorCallback.onResponse(ensureWebException(task.getException()));
                    }
                }
            });
    }

    /* If an org has a conflict set the children orgs to conflicted as well. */
    private void setOrgConflict(Org org, ConflictCause conflictCause) {
        org.setConflictCause(conflictCause);
        if(conflictCause.equals(ConflictCause.LDS_EQUIVALENT_DELETED)) {
            conflictCause = ConflictCause.LDS_EQUIVALENT_PARENT_DELETED;
        }
        if(org.getChildren() != null) {
            for(Org child : org.getChildren()) {
                setOrgConflict(child, conflictCause);
            }
        }
    }
    public void clearLocalOrgData() {
        memberData.removeAllMemberCallings();
        orgs = new ArrayList<>();
        orgsById = new HashMap<>();
        callingsById = new HashMap<>();
        baseOrgByOrgId = new HashMap<>();
    }

    public void removeOrg(Org org) {
        removeOrgFromCache(org);
        orgs.remove(org);
    }

    public void removeOrgFromCache(Org org) {
        for(Calling calling : org.getCallings()) {
            memberData.removeCallingFromMembers(calling);
            callingsById.remove(calling.getCallingId());
        }
        orgsById.remove(org.getId());
        baseOrgByOrgId.remove(org.getId());
        for(Org subOrg: org.getChildren()) {
            removeOrgFromCache(subOrg);
        }
    }

    public void refreshLCROrgs(final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorCallback, final LdsUser currentUser) {
        webResources.getOrgs(true, new Response.Listener<List<Org>>() {
            @Override
            public void onResponse(List<Org> lcrOrgs) {
                if(lcrOrgs != null) {
                    /* Reset all cached items. */
                    orgs = lcrOrgs;
                    orgsById = new HashMap<Long, Org>();
                    callingsById = new HashMap<String, Calling>();
                    baseOrgByOrgId = new HashMap<Long, Org>();
                    orgConflicts = new HashMap<>();
                    callingConflicts = new HashMap<>();
                    Task<Boolean> syncDriveIdsTask = googleDriveService.syncDriveIds(getAuthorizableOrgs(lcrOrgs, currentUser));
                    syncDriveIdsTask.addOnSuccessListener(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean syncDriveIdsResult) {
                            if (syncDriveIdsResult) {
                                importAndMergeGoogleData(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        if (response) {
                                            /* Add items to cached elements. */
                                            for (Org org : orgs) {
                                                extractOrg(org, org.getId());
                                            }
                                            Set<Org> baseOrgsToSave = new HashSet<>();
                                            for (Org org : orgsToSave) {
                                                baseOrgsToSave.add(getBaseOrg(org.getId()));
                                            }
                                            orgsToSave.clear();
                                            /* Save orgs if necessary. */
                                            if (baseOrgsToSave.size() > 0) {
                                                final List<Task<Boolean>> saveOrgFileTasks = new ArrayList<>(orgs.size());
                                                for (Org org : baseOrgsToSave) {
                                                    saveOrgFileTasks.add(googleDriveService.saveOrgFile(org));
                                                }
                                                Tasks.whenAllComplete(saveOrgFileTasks)
                                                        .addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                                                            @Override
                                                            public void onSuccess(List<Task<?>> tasks) {
                                                                Boolean good = true;
                                                                for (Task<Boolean> results : saveOrgFileTasks) {
                                                                    if (!results.getResult()) {
                                                                        good = false;
                                                                        break;
                                                                    }
                                                                }
                                                                listener.onResponse(good);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        errorCallback.onResponse(ensureWebException(e));
                                                    }
                                                });
                                            } else {
                                                listener.onResponse(true);
                                            }
                                        } else {
                                            listener.onResponse(false);
                                        }
                                    }
                                }, new Response.Listener<WebException>() {
                                    @Override
                                    public void onResponse(WebException response) {
                                        errorCallback.onResponse(response);
                                    }
                                }, currentUser);
                            } else {
                                listener.onResponse(false);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errorCallback.onResponse(ensureWebException(e));
                        }
                    });
                }
            }
        }, errorCallback);
    }

    /* Removes and replaces the data in google drive with the latest data from LCR.  All pending callings will be removed. */
    public void refreshGoogleDriveOrgs(final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener, final LdsUser currentUser, final List<Long> orgIds) {
       webResources.getOrgs(true, new Response.Listener<List<Org>>() {
           @Override
           public void onResponse(final List<Org> orgs) {
               if(orgs != null) {
                   List<Task<Boolean>> tasks = new ArrayList<>(orgIds.size());
                   for(Org org : orgs) {
                       if(orgIds.contains(org.getId())) {
                           tasks.add(googleDriveService.saveOrgFile(org));
                       }
                   }
                   Tasks.whenAll(tasks)
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               for(Org org : orgs) {
                                   if(orgIds.contains(org.getId())) {
                                       replaceOrg(org, currentUser);
                                   }
                               }
                               listener.onResponse(true);
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               errorListener.onResponse(ensureWebException(new WebException(ExceptionType.UNKOWN_GOOGLE_EXCEPTION)));
                           }
                       });
               } else {
                   errorListener.onResponse(ensureWebException(new WebException(ExceptionType.UNKNOWN_EXCEPTION)));
               }
           }
       }, errorListener);
    }

    /* Get's the latest data stored in google drive and refreshes the data on the device. */
    public void refreshOrgFromGoogleDrive(final Response.Listener<Org> listener, final Response.Listener<WebException> errorListener, Long orgId, final LdsUser currentUser) {
        final Org org = getOrg(orgId);
        /* Check org viewing permissions first */
        isAuthorizableOrg(org, currentUser, org.getId());
        if(org != null && org.getCanView()) {
            googleDriveService.getOrgData(org)
                .addOnSuccessListener(new OnSuccessListener<Org>() {
                    @Override
                    public void onSuccess(Org googleOrg) {
                        replaceOrg(googleOrg, currentUser);
                        listener.onResponse(googleOrg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorListener.onResponse(ensureWebException(e));
                    }
            });
        } else {
            listener.onResponse(org);
        }
    }

    public void replaceOrg(Org newOrg, LdsUser currentUser) {
        Org originalOrg = orgsById.get(newOrg.getId());
        Org baseOrg = baseOrgByOrgId.get(newOrg.getId());
        removeOrgFromCache(originalOrg);
        orgs.set(orgs.indexOf(originalOrg), newOrg);
        extractOrg(newOrg, baseOrg.getId());
        isAuthorizableOrg(newOrg, currentUser, baseOrg.getId());
        restoreOrgConflicts(newOrg);
    }

    private void restoreOrgConflicts(Org org) {
        if(orgConflicts.containsKey(org.getId())) {
            setOrgConflict(org, orgConflicts.get(org.getId()));
        }
        for(Calling calling: org.getCallings()) {
            if(callingConflicts.containsKey(calling.getCallingId())) {
                calling.setConflictCause(callingConflicts.get(calling.getCallingId()));
            }
        }
        for(Org subOrg: org.getChildren()) {
            restoreOrgConflicts(subOrg);
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
                memberData.addCallingToMembers(calling);
            }
        }
        if(org.getChildren() != null) {
            for (Org subOrg : org.getChildren()) {
                extractOrg(subOrg, baseOrgId);
            }
        }
    }

    /* Import and Merge Data */

    private void importAndMergeGoogleData(final Response.Listener<Boolean> mergeFinishedCallback, final Response.Listener<WebException> errorCallback, final LdsUser currentUser) {
        googleDriveService.getOrgs()
            .continueWithTask(new Continuation<List<Org>, Task<Boolean>>() {
                @Override
                public Task<Boolean> then(@NonNull Task<List<Org>> task) throws Exception {
                    List<Org> cwfOrgs = task.getResult();
                    if(cwfOrgs != null) {
                        List<Org> cwfOrgsToAdd = new ArrayList<Org>();
                         /* Cycle through all google drive orgs. */
                        for (Org cwfOrg : cwfOrgs) {
                            boolean matchFound = false;
                             /* Cycle through all lcr orgs only until or if a match is found. */
                            for (Org org : orgs) {
                                if (cwfOrg.getId() == org.getId()) {
                                    memberData.removeMemberCallings(cwfOrg.allOrgCallings());
                                    mergeOrgs(org, cwfOrg, currentUser);
                                    matchFound = true;
                                    break;
                                }
                            }
                            /* If the org is gone then the user will need to address this cwf org. */
                            if (!matchFound) {
                                if (hasProposedData(cwfOrg)) {
                                    setOrgConflict(cwfOrg, ConflictCause.LDS_EQUIVALENT_DELETED);
                                    orgConflicts.put(cwfOrg.getId(), cwfOrg.getConflictCause());
                                    cwfOrgsToAdd.add(cwfOrg);
                                } else {
                                    baseOrgsToRemove.add(cwfOrg);
                                }
                            }
                        }
                        orgs.addAll(cwfOrgsToAdd);
                    }
                    TaskCompletionSource mergeCompletionTask = new TaskCompletionSource();
                    mergeCompletionTask.setResult(true);
                    return mergeCompletionTask.getTask();
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    mergeFinishedCallback.onResponse(result);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    errorCallback.onResponse(ensureWebException(e));
                }
            });
    }

    /* This merges the orgs and callings from the cwfOrg into the lcrOrg so when completed the lcrOrg will have both. */
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
                        setOrgConflict(cwfSubOrg, ConflictCause.LDS_EQUIVALENT_DELETED);
                        orgConflicts.put(cwfSubOrg.getId(), cwfSubOrg.getConflictCause());
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

    /* This merges callings from both orgs into the lcrOrg. */
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

                //if it's cwfOnly then we add it to the list
                if(cwfCalling.isCwfOnly()) {
                    lcrCallings.add(cwfCalling);
                }
                //Check if calling is one of the empty unmatchable callings, callings where multiples aren't allowed they can be matched on type so they don't go in this list
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
                            callingConflicts.put(cwfCalling.getCallingId(), cwfCalling.getConflictCause());
                            lcrCallings.add(cwfCalling);
                        } else if(hasPotentialInfo) {
                            cwfCalling.setConflictCause(ConflictCause.LDS_EQUIVALENT_DELETED);
                            callingConflicts.put(cwfCalling.getCallingId(), cwfCalling.getConflictCause());
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
                mergeEmptyCallings(lcrVacantCallings, cwfVacantCallings, lcrOrg);
            }
        }

        //check if there are more callings from lcr that need saved to google
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
    }

    private void mergeEmptyCallings(List<Calling> lcrVacantCallings, List<Calling> cwfVacantCallings, Org lcrOrg) {
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
                callingConflicts.put(calling.getCallingId(), calling.getConflictCause());
                lcrOrg.getCallings().add(calling);
            }
        }
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
                if ((calling.getProposedIndId() != null && calling.getProposedIndId() > 0) ||
                    (calling.getProposedStatus() != null && calling.getProposedStatus() != CallingStatus.NONE) ||
                    (calling.getNotes() != null && calling.getNotes().length() != 0)) {
                    result.add(calling);
                }
            }
        }
        return result;
    }

    public Calling getCalling(String callingId) {
        return callingsById.get(callingId);
    }

    public void addNewCalling(final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener, final Calling calling, Org baseOrg, final LdsUser currentUser) {
        Task<Org> getOrgDataTask = googleDriveService.getOrgData(baseOrg);
        getOrgDataTask.addOnSuccessListener(new OnSuccessListener<Org>() {
            @Override
            public void onSuccess(final Org newOrg) {
                Org subOrg = findSubOrg(newOrg, calling.getParentOrg());
                subOrg.getCallings().add(calling);
                final Task<Boolean> saveOrgFileTask = googleDriveService.saveOrgFile(newOrg);
                saveOrgFileTask.addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean response) {
                        if(response) {
                            replaceOrg(newOrg, currentUser);
                        }
                        listener.onResponse(response);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorListener.onResponse(ensureWebException(e));
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                errorListener.onResponse(ensureWebException(e));
            }
        });
    }

    public void updateCalling(final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener, final Calling calling, Org baseOrg, final LdsUser currentUser) {
        Task<Org> getOrgDataTask = googleDriveService.getOrgData(baseOrg);
        getOrgDataTask.addOnSuccessListener(new OnSuccessListener<Org>() {
            @Override
            public void onSuccess(final Org newOrg) {
                Calling newOrgCalling = newOrg.getCallingById(calling.getCallingId());
                newOrgCalling.setNotes(calling.getNotes());
                newOrgCalling.setProposedIndId(calling.getProposedIndId());
                if(!calling.getProposedStatus().equals(CallingStatus.UNKNOWN)) {
                    newOrgCalling.setProposedStatus(calling.getProposedStatus());
                }
                final Task<Boolean> saveOrgFileTask = googleDriveService.saveOrgFile(newOrg);
                saveOrgFileTask.addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean response) {
                        if(response) {
                            replaceOrg(newOrg, currentUser);
                        }
                        listener.onResponse(response);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorListener.onResponse(ensureWebException(e));
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                errorListener.onResponse(ensureWebException(e));
            }
        });
    }

    public Task<Boolean> releaseLDSCalling(final Calling calling, final LdsUser currentUser) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        webResources.releaseCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(), getOrg(calling.getParentOrg()).getOrgTypeId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.has(ERRORS) && jsonObject.getJSONObject(ERRORS).length() > 0) {
                        taskCompletionSource.setException(hydrateErrorListener(jsonObject, Operation.RELEASE));
                    } else {
                        googleDriveService.getOrgData(getBaseOrg(calling.getParentOrg()))
                                .addOnSuccessListener(new OnSuccessListener<Org>() {
                                    @Override
                                    public void onSuccess(final Org latestGoogleDriveOrg) {
                                        Calling googleCalling = latestGoogleDriveOrg.getCallingById(calling.getCallingId());
                                        googleCalling.setActiveDate(null);
                                        googleCalling.setActiveDateTime(null);
                                        googleCalling.setMemberId(null);
                                        googleDriveService.saveOrgFile(latestGoogleDriveOrg)
                                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean result) {
                                                if(result) {
                                                    replaceOrg(latestGoogleDriveOrg, currentUser);
                                                }
                                                taskCompletionSource.setResult(result);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                taskCompletionSource.setException(ensureWebException(e));
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                taskCompletionSource.setException(ensureWebException(e));
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    taskCompletionSource.setException(new WebException(ExceptionType.PARSING_ERROR, e));
                }
            }
        }, new Response.Listener<WebException>() {
            @Override
            public void onResponse(WebException e) {
                taskCompletionSource.setException(ensureWebException(e));
            }
        });
        return taskCompletionSource.getTask();
    }

    public Task<Boolean> updateLDSCalling(final Calling calling, final LdsUser currentUser) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        webResources.updateCalling(calling, getOrg(calling.getParentOrg()).getUnitNumber(), getOrg(calling.getParentOrg()).getOrgTypeId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject jsonObject) {
                try {
                    if (jsonObject.has(ERRORS) && jsonObject.getJSONObject(ERRORS).length() > 0) {
                        taskCompletionSource.setException(hydrateErrorListener(jsonObject, Operation.UPDATE));
                    } else {
                        googleDriveService.getOrgData(getBaseOrg(calling.getParentOrg()))
                            .addOnSuccessListener(new OnSuccessListener<Org>() {
                            @Override
                            public void onSuccess(final Org latestGoogleDriveOrg) {
                                try {
                                    Calling googleCalling = latestGoogleDriveOrg.getCallingById(calling.getCallingId());
                                    if (jsonObject.has(POSITION_ID)) {
                                        googleCalling.setId(jsonObject.getLong(POSITION_ID));
                                    }
                                    googleCalling.setNotes("");
                                    googleCalling.setExistingStatus(null);
                                    googleCalling.setProposedIndId(null);
                                    googleCalling.setProposedStatus(CallingStatus.NONE);
                                    googleCalling.setActiveDate(DateTime.now());
                                    googleCalling.setActiveDateTime(DateTime.now());
                                    googleCalling.setCwfId("");
                                    googleCalling.setMemberId(jsonObject.getLong(MEMBER_ID));
                                    if (googleCalling.isCwfOnly()) {
                                        googleCalling.setCwfOnly(false);
                                    }
                                    googleDriveService.saveOrgFile(latestGoogleDriveOrg)
                                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            if (result) {
                                                replaceOrg(latestGoogleDriveOrg, currentUser);
                                                taskCompletionSource.setResult(result);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            taskCompletionSource.setException(ensureWebException(e));
                                        }
                                    });
                                } catch(JSONException e) {
                                    e.printStackTrace();
                                    taskCompletionSource.setException(new WebException(ExceptionType.PARSING_ERROR, e));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                taskCompletionSource.setException(ensureWebException(e));
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    taskCompletionSource.setException(new WebException(ExceptionType.PARSING_ERROR, e));
                }
            }
        }, new Response.Listener<WebException>() {
            @Override
            public void onResponse(WebException e) {
                taskCompletionSource.setException(e);
            }
        });
        return taskCompletionSource.getTask();
    }

    /* Removes the calling from LCR then from google drive */
    public void deleteLDSCalling(final Calling calling, final LdsUser currentUser, final Response.Listener listener, final Response.Listener<WebException> errorListener) {
        final Org originalOrg = getOrg(calling.getParentOrg());
        webResources.deleteCalling(calling, originalOrg.getUnitNumber(), originalOrg.getOrgTypeId(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if(jsonObject != null && jsonObject.has(ERRORS) && jsonObject.getJSONObject(ERRORS).length() > 0) {
                        errorListener.onResponse(hydrateErrorListener(jsonObject, Operation.DELETE));
                    } else {
                        /* Get Latest org changes from google drive. */
                        Task<Org> getOrgDataTask = googleDriveService.getOrgData(getBaseOrg(originalOrg.getId()));
                        getOrgDataTask.addOnSuccessListener(new OnSuccessListener<Org>() {
                            @Override
                            public void onSuccess(final Org latestGoogleDriveOrg) {
                                    /* Removes the calling from within the new org. */
                                    Org parentOrg = findSubOrg(latestGoogleDriveOrg, calling.getParentOrg());
                                    parentOrg.removeCalling(calling);
                                    Task<Boolean> saveOrgFileTask = googleDriveService.saveOrgFile(latestGoogleDriveOrg);
                                    saveOrgFileTask.addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Boolean> task) {
                                            if(task.getResult()) {
                                                replaceOrg(latestGoogleDriveOrg, currentUser);
                                            }
                                            listener.onResponse(task.getResult());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            errorListener.onResponse(ensureWebException(e));
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                errorListener.onResponse(ensureWebException(e));
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorListener.onResponse(new WebException(ExceptionType.PARSING_ERROR, e));
                }
            }
        }, errorListener);
    }

    public WebException hydrateErrorListener(JSONObject json, Operation action) {
        WebException error = new WebException(ExceptionType.UNKNOWN_EXCEPTION);
        try {
            switch(action) {
                case CREATE:
                    break;
                case READ:
                    break;
                case UPDATE:
                    VolleyError volleyError = null;
                    if(json.getJSONObject(ERRORS).has(MEMBER_ID)) {
                        volleyError = new VolleyError(json.getJSONObject(ERRORS).getString(MEMBER_ID).replace("[", "").replace("]", ""));
                    }
                    error.setException(new WebException(ExceptionType.UNKNOWN_EXCEPTION, volleyError));
                    break;
                case DELETE:
                    break;
                case RELEASE:
                    break;
                case DELETE_LCR:
                    break;
            }
        } catch (JSONException e) {
            error = new WebException(ExceptionType.PARSING_ERROR, e);
        }
        return error;
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

    public boolean removeSubOrg(Org org, long subOrgId) {
        boolean result = false;
        List<Org> childOrgs = org.getChildren();
        for(Iterator<Org> orgIterator = childOrgs.iterator(); orgIterator.hasNext();) {
            Org subOrg = orgIterator.next();
            if(subOrg.getId() == subOrgId) {
                childOrgs.remove(subOrg);
                return true;
            }
            result = removeSubOrg(subOrg, subOrgId);
            if(result) {
                break;
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
                        allPositionMetadata = new ArrayList<>();
                    }
                    if(positionMetaDataByPositionTypeId == null) {
                        positionMetaDataByPositionTypeId = new HashMap<>();
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
    public void getCallingStatus(final Response.Listener<List<CallingStatus>> listener, final Response.Listener<WebException> errorListener, Long unitNumber) {
        Task<UnitSettings> getUnitSettingsTask = googleDriveService.getUnitSettings(unitNumber, true);
        getUnitSettingsTask.addOnSuccessListener(new OnSuccessListener<UnitSettings>() {
            @Override
            public void onSuccess(UnitSettings unitSettings) {
                List<CallingStatus> statuses = new ArrayList<>();
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
        }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            errorListener.onResponse(ensureWebException(e));
            }
        });
    }

    private WebException ensureWebException(Exception exception) {
        return exception instanceof WebException ? (WebException) exception : new WebException(ExceptionType.UNKOWN_GOOGLE_EXCEPTION, exception);
    }
}