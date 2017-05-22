package org.ldscd.callingworkflow.web;


import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallingData {
    private static final String TAG = "CallingData";

    private IWebResources webResources;
    private GoogleDataService googleDataService;
    private MemberData memberData;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<Long, List<Calling>> callingsByOrg;
    private Map<String, Calling> callingsById;
    private Map<Long, Org> baseOrgByOrgId;

    public CallingData(IWebResources webResources, GoogleDataService googleDataService, MemberData memberData) {
        this.webResources = webResources;
        this.googleDataService = googleDataService;
        this.memberData = memberData;
    }

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
                            callingsByOrg = new HashMap<Long, List<Calling>>();
                            callingsById = new HashMap<String, Calling>();
                            baseOrgByOrgId = new HashMap<Long, Org>();
                            for (Org org : orgs) {
                                callingsByOrg.put(org.getId(), new ArrayList<Calling>());
                                extractOrg(org, org.getId());
                            }
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

    private void extractOrg(Org org, long baseOrgId) {
        orgsById.put(org.getId(), org);
        baseOrgByOrgId.put(org.getId(), orgsById.get(baseOrgId));
        List<Calling> callingsList = callingsByOrg.get(baseOrgId);
        for(Calling calling: org.getCallings()) {
            callingsById.put(calling.getCallingId(), calling);
            callingsList.add(calling);
        }
        for(Org subOrg: org.getChildren()) {
            extractOrg(subOrg, baseOrgId);
        }
    }

    private void importAndMergeGoogleData(final Response.Listener<Boolean> mergeFinishedCallback) {
        final int[] orgsFinishedImporting = {0};

        for(int i=0; i < orgs.size(); i++) {
            final Org org = orgs.get(i);
            googleDataService.getOrgData(new Response.Listener<Org>() {
                @Override
                public void onResponse(Org cwfOrg) {
                    mergeOrgs(org, cwfOrg);
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
                }
            }
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

    //returns all callings in an org, including callings from subOrgs
    //this only works for base organizations
    public List<Calling> getCallings(long orgId) {
        return callingsByOrg.get(orgId);
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
}