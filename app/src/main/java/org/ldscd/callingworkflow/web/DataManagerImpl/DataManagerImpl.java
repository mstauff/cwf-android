package org.ldscd.callingworkflow.web.DataManagerImpl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import org.ldscd.callingworkflow.services.GoogleDriveService;
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
    private GoogleDriveService googleDataService;
    private IWebResources webResources;
    private PermissionManager permissionManager;
    private LdsUser currentUser;

    /* Constructor */
    public DataManagerImpl(CallingData callingData, MemberData memberData, GoogleDriveService googleDataService, IWebResources webResources, PermissionManager permissionManager) {
        this.callingData = callingData;
        this.memberData = memberData;
        this.googleDataService = googleDataService;
        this.webResources = webResources;
        this.permissionManager = permissionManager;
    }
    /* Methods */
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
    /* Google Drive */
    public boolean isGoogleDriveAuthenticated(Context context) {
        return googleDataService.isAuthenticated(context);
    }
    public void signIn(Task<Boolean> signInTask, Activity activity) {
        //googleDataService.signIn(signInTask, activity);
    }
    /* User data */
    @Override
    public void getUserInfo(String userName, String password, boolean hasChanges, final Response.Listener<LdsUser> userListener) {

        if(currentUser == null || hasChanges) {
            if(hasChanges) {
                webResources.setCredentials(userName, password);
            }
            webResources.getUserInfo(hasChanges, new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    currentUser = ldsUser;
                    if(currentUser != null &&
                            (currentUser.getUnitRoles() == null ||
                                    (currentUser.getUnitRoles() == null || currentUser.getUnitRoles().size() == 0))) {
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
    @Override
    public void getSharedPreferences(Response.Listener<SharedPreferences> listener) {
        webResources.getSharedPreferences(listener);
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
    @Override
    public void refreshGoogleDriveOrgs(List<Long> orgIds, Response.Listener<Boolean> listener) {
        if(currentUser != null && !orgIds.isEmpty()) {
            callingData.refreshGoogleDriveOrgs(listener, currentUser, orgIds);
        } else {
            listener.onResponse(false);
        }
    }
    @Override
    public void refreshLCROrgs(Response.Listener<Boolean> listener) {
        if(currentUser != null) {
            callingData.refreshLCROrgs(listener, currentUser);
        } else {
            listener.onResponse(false);
        }
    }
    public void loadOrgs(Response.Listener<Boolean> listener, ProgressBar progressBar, Activity activity) {
        callingData.loadOrgs(listener, progressBar, activity, currentUser);
    }
    public void loadPositionMetadata() {
        callingData.loadPositionMetadata();
    }
    public void clearLocalOrgData() {
        callingData.clearLocalOrgData();
    }
    public void refreshOrg(Response.Listener<Org> listener, Long orgId) {
        callingData.refreshOrgFromGoogleDrive(listener, orgId, currentUser);
    }
    public List<PositionMetaData> getAllPositionMetadata() {
        return callingData.getAllPositionMetadata();
    }
    public PositionMetaData getPositionMetadata(int positionTypeId) {
        return callingData.getPositionMetadata(positionTypeId);
    }
    public void releaseLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.ErrorListener errorListener) throws JSONException {
        callingData.releaseLDSCalling(calling, callback, errorListener);
    }
    public void updateLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.ErrorListener errorListener) throws JSONException {
        callingData.updateLDSCalling(calling, callback, errorListener);
    }
    public void deleteLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.ErrorListener errorListener) throws JSONException {
        callingData.deleteLDSCalling(calling, callback, errorListener);
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
        Org org = callingData.getBaseOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_CREATE,
                new AuthorizableOrg(org.getUnitNumber(), UnitLevelOrgType.get(org.getOrgTypeId()), org.getOrgTypeId())))
        {
            callingData.addNewCalling(calling);
            saveCalling(listener, org, calling, Operation.CREATE);
        } else {
            listener.onResponse(false);
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
    public void deleteCalling(final Calling calling, final Response.Listener<Boolean> listener, Response.ErrorListener errorListener) {
        final Org org = callingData.getBaseOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_DELETE,
                new AuthorizableOrg(org.getUnitNumber(), UnitLevelOrgType.get(org.getOrgTypeId()), org.getOrgTypeId()))) {
            if(canDeleteCalling(calling, org)) {
                /* Get the latest file from google drive. */
                final Task<Org> getOrgDataTask = googleDataService.getOrgData(org);
                getOrgDataTask.continueWithTask(new Continuation<Org, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Org> task) throws Exception {
                        Org newOrg = task.getResult();
                        if(newOrg != null) {
                            final Calling originalCalling = new Calling(calling);
                            /* Remove the callings that belong to the Org from the members with a proposed calling. */
                            memberData.removeMemberCallings(org.getCallings());
                            /* Get's the subOrg within the main org(ie. instructors subOrg in the EQ Org) */
                            Org newParentOrg = callingData.findSubOrg(newOrg, calling.getParentOrg());
                            /* Removes the calling from within the new org */
                            newParentOrg.removeCalling(calling);
                            /* Get's the old parent org */
                            Org oldParentOrg = callingData.getOrg(calling.getParentOrg());
                            /* Removes the calling from the old parent org */
                            oldParentOrg.removeCalling(calling);
                            /* Merge the changes in the old and new orgs */
                            callingData.mergeOrgs(org, newOrg, currentUser);
                            /* Extract recent subOrgs and callings to cached items */
                            callingData.extractOrg(org, org.getId());
                            /* Save the changes back to google drive */
                            Task<Boolean> saveOrgFileTask = googleDataService.saveOrgFile(newOrg);
                            saveOrgFileTask
                                .continueWithTask(new Continuation<Boolean, Task<Boolean>>() {
                                    @Override
                                    public Task<Boolean> then(@NonNull Task<Boolean> task) throws Exception {
                                        return task;
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Boolean> task) {
                                        listener.onResponse(task.getResult());
                                        if(!task.getResult()) {
                                            Org newOrg = getOrgDataTask.getResult();
                                            callingData.updateCalling(newOrg, originalCalling, Operation.CREATE);
                                            callingData.mergeOrgs(org, newOrg, currentUser);
                                            callingData.extractOrg(org, org.getId());
                                        } else if(originalCalling.getProposedIndId() > 0) {
                                            getMember(originalCalling.getProposedIndId()).removeProposedCalling(calling);
                                        }
                                    }
                                });
                        } else {
                            listener.onResponse(false);
                        }
                        return null;
                    }
                });
            } else {
                listener.onResponse(false);
            }
        }
    }

    /* Calling and Google Data */
    private void saveCalling(final Response.Listener<Boolean> listener, final Org org, final Calling calling, final Operation operation) {
        Task<Org> getOrgDataTask = googleDataService.getOrgData(org);
        getOrgDataTask.continueWithTask(new Continuation<Org, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(@NonNull Task<Org> task) throws Exception {
                return handleSaveCalling(org, task.getResult(), calling, operation);
            }
        })
        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                listener.onResponse(true);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onResponse(false);
            }
        });
    }

    private Task<Boolean> handleSaveCalling(final Org org, final Org newOrg,  final Calling calling, final Operation operation) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        if(newOrg != null) {
            memberData.removeMemberCallings(org.getCallings()); //this must be done before changes are made, they are repopulated while merging orgs
            final Calling backupCalling = operation.equals(Operation.CREATE) ? null : new Calling(newOrg.getCallingById(calling.getCallingId()));
            callingData.updateCalling(newOrg, calling, operation);
            callingData.mergeOrgs(org, newOrg, currentUser);
            callingData.extractOrg(org, org.getId());
            final Task<Boolean> saveOrgFileTask = googleDataService.saveOrgFile(newOrg);
            saveOrgFileTask.continueWith(new Continuation<Boolean, Boolean>() {
                @Override
                public Boolean then(@NonNull Task<Boolean> task) throws Exception {
                    Boolean success = saveOrgFileTask.getResult();
                    memberData.removeMemberCallings(org.getCallings());
                    if(!success) {
                        if (operation.equals(Operation.UPDATE)) {
                            callingData.updateCalling(newOrg, backupCalling, operation);
                        } else if (operation.equals(Operation.CREATE)) {
                            Org parentOrg = callingData.findSubOrg(newOrg, calling.getParentOrg());
                            Calling callingToRemove = parentOrg.getCallingById(calling.getCallingId());
                            parentOrg.getCallings().remove(callingToRemove);
                            Org localParentOrg = callingData.getOrg(calling.getParentOrg());
                            callingToRemove = localParentOrg.getCallingById(calling.getCallingId());
                            localParentOrg.getCallings().remove(callingToRemove);
                        }
                        callingData.mergeOrgs(org, newOrg, currentUser);
                        callingData.extractOrg(org, org.getId());
                    }
                    return success;
                }
            }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    taskCompletionSource.setResult(result);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskCompletionSource.setResult(false);
                    taskCompletionSource.setException(e);
                }
            });
        }
        return taskCompletionSource.getTask();
    }

    @Override
    public List<Calling> getUnfinalizedCallings() {
        return callingData.getUnfinalizedCallings();
    }

    @Override
    public boolean canDeleteCalling(Calling calling, Org org) {
        return callingData.canDeleteCalling(calling, org);
    }

    /* Unit Settings */
    @Override
    public void getUnitSettings(final Response.Listener<UnitSettings> listener) {
        googleDataService.getUnitSettings(getUnitNumber())
            .addOnSuccessListener(new OnSuccessListener<UnitSettings>() {
                @Override
                public void onSuccess(UnitSettings unitSettings) {
                    listener.onResponse(unitSettings);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onResponse(null);
                }
            });
    }
    @Override
    public void saveUnitSettings(final Response.Listener<Boolean> listener, UnitSettings unitSettings) {
        googleDataService.saveUnitSettings(unitSettings)
            .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    listener.onResponse(task.getResult());
                }
            });
    }
    private Long getUnitNumber() {
        return currentUser != null ? currentUser.getUnitNumber() : 0L;
    }
}