package org.ldscd.callingworkflow.web.DataManagerImpl;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.model.UnitSettings;
import org.ldscd.callingworkflow.model.permissions.AuthorizableOrg;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.MemberData;

import java.util.List;

public class DataManagerImpl implements DataManager {
    private static final String TAG = "DataManager";
    /* Properties */
    private CallingData callingData;
    private MemberData memberData;
    private GoogleDataService googleDataService;
    private IWebResources webResources;
    private PermissionManager permissionManager;
    private static final boolean cacheData = true;
    private LdsUser currentUser;

    /* Constructor */
    public DataManagerImpl(CallingData callingData, MemberData memberData, GoogleDataService googleDataService, IWebResources webResources, PermissionManager permissionManager) {
        this.callingData = callingData;
        this.memberData = memberData;
        this.googleDataService = googleDataService;
        this.webResources = webResources;
        this.permissionManager = permissionManager;
    }
    /* Methods */
    /* User data */
    @Override
    public void getUserInfo(final Response.Listener<LdsUser> userListener) {
        if(currentUser == null) {
            webResources.getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    currentUser = ldsUser;
                    if(currentUser.getUnitRoles() == null || currentUser.getUnitRoles().size() == 0) {
                        currentUser.setUnitRoles(permissionManager.createUnitRoles(currentUser.getPositions(),
                                currentUser.getUnitNumber()));
                    }
                    userListener.onResponse(currentUser);
                }
            });
        } else {
            userListener.onResponse(currentUser);
        }
    }

    /* calling data. */
    @Override
    public void getCallingStatus(Response.Listener<List<CallingStatus>> listener) {
        callingData.getCallingStatus(listener, getUnitNumber());
    }
    public Calling getCalling(String id) {
        return callingData.getCalling(id);
    }
    public Org getOrg(long id) {
        return callingData.getOrg(id);
    }
    public List<Org> getOrgs() {
        return callingData.getOrgs();
    }
    public void loadOrgs(Response.Listener<Boolean> listener, ProgressBar progressBar, Activity activity) {
        callingData.loadOrgs(listener, progressBar, activity, currentUser);
        callingData.loadPositionMetadata();
    }
    public void refreshOrg(Response.Listener<Org> listener, Long orgId) {
        callingData.refreshOrgFromGoogleDrive(listener, orgId);
    }
    public List<PositionMetaData> getAllPositionMetadata() {
        return callingData.getAllPositionMetadata();
    }
    public PositionMetaData getPositionMetadata(int positionTypeId) {
        return callingData.getPositionMetadata(positionTypeId);
    }
    public void releaseLDSCalling(Calling calling, Response.Listener callback) throws JSONException {

    }
    public void updateLDSCalling(Calling calling, Response.Listener callback) throws JSONException {
        callingData.updateLDSCalling(calling, getOrg(calling.getParentOrg()).getOrgTypeId(), callback);
    }
    public void deleteLDSCalling(Calling calling, Response.Listener callback) throws JSONException {
        callingData.deleteLDSCalling(calling, getOrg(calling.getParentOrg()).getOrgTypeId(), callback);
    }

    /* Member data. */
    public String getMemberName(Long id) {
        if(id != null) {
            return memberData.getMemberName(id);
        } else {
            return "";
        }
    }
    public void getWardList(Response.Listener<List<Member>> listener) {
        memberData.getMembers(listener);
    }

    public Member getMember(Long id) {
        return memberData.getMember(id);
    }
    public void loadMembers(Response.Listener<Boolean> listener, ProgressBar progressBar) {
        if(permissionManager.hasPermission(currentUser.getUnitRoles(), Permission.ORG_INFO_READ)) {
            memberData.loadMembers(listener, progressBar);
        }
    }

    /* Google data. */
    @Override
    public void addCalling(Response.Listener<Boolean> listener, Calling calling) {
        Org org = getOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_CREATE,
                new AuthorizableOrg(org.getUnitNumber(), UnitLevelOrgType.get(org.getOrgTypeId()), org.getOrgTypeId())))
        {
            callingData.addNewCalling(calling);
            Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
            saveCalling(listener, baseOrg, calling, Operation.CREATE);
        }
    }
    @Override
    public void updateCalling(Response.Listener<Boolean> listener, Calling calling) {
        Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_UPDATE,
                new AuthorizableOrg(baseOrg.getUnitNumber(), UnitLevelOrgType.get(baseOrg.getOrgTypeId()), baseOrg.getOrgTypeId()))) {
            saveCalling(listener, baseOrg, calling, Operation.UPDATE);
        }
    }
    @Override
    public void deleteCalling(final Response.Listener<Boolean> listener, final Calling calling, final Org org) {
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_DELETE,
                new AuthorizableOrg(org.getUnitNumber(), UnitLevelOrgType.get(org.getOrgTypeId()), org.getOrgTypeId()))) {
            if(canDeleteCalling(calling, org)) {
                googleDataService.getOrgData(new Response.Listener<Org>() {
                    @Override
                    public void onResponse(final Org newOrg) {
                        //Here we're pulling a fresh copy of the base org to keep from overwriting any remote changes within the base org
                        //we have to manually remove the calling from both the fresh copy and locally since the merge process doesn't always remove callings,
                        //but we still need to merge to include any remote changes
                        memberData.removeMemberCallings(org.getCallings()); //this must be done before changes are made, they are repopulated while merging orgs
                        Org parentOrg = findSubOrg(newOrg, calling.getParentOrg());
                        Calling callingToRemove = parentOrg.getCallingById(calling.getCallingId());
                        parentOrg.getCallings().remove(callingToRemove);
                        Org localParentOrg = callingData.getOrg(calling.getParentOrg());
                        Calling localCallingToRemove = localParentOrg.getCallingById(calling.getCallingId());
                        localParentOrg.getCallings().remove(localCallingToRemove);
                        callingData.mergeOrgs(org, newOrg);
                        callingData.extractOrg(org, org.getId());
                        googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                            @Override
                            public void onResponse(Boolean success) {
                                if(!success) {
                                    updateCalling(newOrg, calling, Operation.CREATE);
                                    callingData.mergeOrgs(org, newOrg);
                                    callingData.extractOrg(org, org.getId());
                                }
                                listener.onResponse(success);
                            }
                        }, newOrg);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                        listener.onResponse(false);
                    }
                }, org);
            }
        }
    }
    /* Calling and Google Data */
    private void saveCalling(final Response.Listener<Boolean> listener, final Org org, final Calling calling, final Operation operation) {
        googleDataService.getOrgData(new Response.Listener<Org>() {
            @Override
            public void onResponse(final Org newOrg) {
                //Here we're pulling a fresh copy of the base org to keep from overwriting any remote changes within the base org
                //We'll make the changes to the calling in the fresh copy, merge them into our local data and save them back to google drive
                memberData.removeMemberCallings(org.getCallings()); //this must be done before changes are made, they are repopulated while merging orgs
                final Calling backupCalling = operation.equals(Operation.CREATE) ? null : new Calling(newOrg.getCallingById(calling.getCallingId()));
                updateCalling(newOrg, calling, operation);
                callingData.mergeOrgs(org, newOrg);
                callingData.extractOrg(org, org.getId());

                //Save to google drive and revert local changes if the save fails
                googleDataService.saveOrgFile(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean success) {
                        memberData.removeMemberCallings(org.getCallings());
                        if(!success) {
                            if (operation.equals(Operation.UPDATE)) {
                                updateCalling(newOrg, backupCalling, operation);
                            } else if (operation.equals(Operation.CREATE)) {
                                Org parentOrg = findSubOrg(newOrg, calling.getParentOrg());
                                Calling callingToRemove = parentOrg.getCallingById(calling.getCallingId());
                                parentOrg.getCallings().remove(callingToRemove);
                                Org localParentOrg = callingData.getOrg(calling.getParentOrg());
                                callingToRemove = localParentOrg.getCallingById(calling.getCallingId());
                                localParentOrg.getCallings().remove(callingToRemove);
                            }
                            callingData.mergeOrgs(org, newOrg);
                            callingData.extractOrg(org, org.getId());
                        }
                        listener.onResponse(success);
                    }
                }, newOrg);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                listener.onResponse(false);
            }
        }, org);
    }

    private void updateCalling(Org baseOrg, Calling updatedCalling, Operation operation) {
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
    private Org findSubOrg(Org org, long subOrgId) {
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

    @Override
    public List<Calling> getUnfinalizedCallings() {
        return callingData.getUnfinalizedCallings();
    }

    @Override
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

    /* Unit Settings */
    @Override
    public void getUnitSettings(Response.Listener<UnitSettings> listener) {
        googleDataService.getUnitSettings(listener, getUnitNumber());
    }
    @Override
    public void saveUnitSettings(Response.Listener<Boolean> listener, UnitSettings unitSettings) {
        googleDataService.saveUnitSettings(listener, unitSettings);
    }
    private Long getUnitNumber() {
        return currentUser != null ? currentUser.getUnitNumber() : 0L;
    }
}