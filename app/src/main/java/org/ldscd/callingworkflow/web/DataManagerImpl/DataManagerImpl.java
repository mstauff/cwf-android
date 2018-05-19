package org.ldscd.callingworkflow.web.DataManagerImpl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ClassAssignment;
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
import org.ldscd.callingworkflow.web.ExceptionType;
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.MemberData;
import org.ldscd.callingworkflow.web.WebException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Override
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
    /* Google Drive */
    @Override
    public boolean isGoogleDriveAuthenticated(Context context) {
        return googleDataService.isAuthenticated(context);
    }
    /* User data */
    @Override
    public LdsUser getCurrentUser() { return currentUser; }
    @Override
    public void getUserInfo(String userName, String password, boolean hasChanges, final Response.Listener<LdsUser> userListener, Response.Listener<WebException> errorCallback) {

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
            }, errorCallback);
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
    public void getCallingStatus(Response.Listener<List<CallingStatus>> listener, Response.Listener<WebException> errorListener) {
        callingData.getCallingStatus(listener, errorListener, getUnitNumber());
    }
    @Override
    public Calling getCalling(String id) {
        return callingData.getCalling(id);
    }
    @Override
    public Org getOrg(long id) {
        return callingData.getOrg(id);
    }
    @Override
    public List<Org> getOrgs() {
        return callingData.getOrgs();
    }
    @Override
    public Org getBaseOrg(long orgId) {
        return callingData.getBaseOrg(orgId);
    }
    @Override
    public Task<Boolean> deleteOrg(final Org org) {
        final Org baseOrg = callingData.getBaseOrg(org.getId());
        final boolean isParentOrg = baseOrg != null && baseOrg.equals(org);
        Task<Boolean> task = null;
        if(isParentOrg) {
            /* Remove it from database/google drive */
            task = googleDataService.deleteOrg(org);
        } else {
            callingData.removeSubOrg(baseOrg, org.getId());
            task = googleDataService.saveOrgFile(baseOrg);
        }
        task.addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if(result) {
                    if(isParentOrg) {
                        /* If it's a base org just remove it from cache once it's been deleted in the db */
                        callingData.removeOrg(baseOrg);
                    } else {
                        /* If it's a suborg handle it accordingly. */
                        callingData.removeOrg(org);
                    }
                }
            }
        });

        return task;
    }
    @Override
    public void refreshGoogleDriveOrgs(List<Long> orgIds, Response.Listener<Boolean> listener, Response.Listener<WebException> errorCallback) {
        if(currentUser != null && !orgIds.isEmpty()) {
            callingData.refreshGoogleDriveOrgs(listener, errorCallback, currentUser, orgIds);
        } else {
            listener.onResponse(false);
        }
    }
    @Override
    public void refreshLCROrgs(Response.Listener<Boolean> listener, Response.Listener<WebException> errorCallback) {
        if(currentUser != null) {
            callingData.refreshLCROrgs(listener, errorCallback, currentUser);
        } else {
            listener.onResponse(false);
        }
    }
    @Override
    public void loadOrgs(Response.Listener<Boolean> listener, Response.Listener<WebException> errorCallback, ProgressBar progressBar, Activity activity) {
        if(currentUser != null) {
            callingData.loadOrgs(listener, errorCallback, progressBar, activity, currentUser);
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.LDS_AUTH_REQUIRED));
        }
    }
    @Override
    public void loadPositionMetadata() {
        callingData.loadPositionMetadata();
    }
    @Override
    public void clearLocalOrgData() {
        callingData.clearLocalOrgData();
    }
    @Override
    public void refreshOrg(Response.Listener<Org> listener, Response.Listener<WebException> errorListener, Long orgId) {
        callingData.refreshOrgFromGoogleDrive(listener, errorListener, orgId, currentUser);
    }
    @Override
    public List<PositionMetaData> getAllPositionMetadata() {
        return callingData.getAllPositionMetadata();
    }
    @Override
    public PositionMetaData getPositionMetadata(int positionTypeId) {
        return callingData.getPositionMetadata(positionTypeId);
    }
    @Override
    public void releaseLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.Listener<WebException> errorListener) {
        callingData.releaseLDSCalling(calling, currentUser, callback, errorListener);
    }
    @Override
    public void updateLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.Listener<WebException> errorListener) {
        callingData.updateLDSCalling(calling, currentUser, callback, errorListener);
    }
    @Override
    public void deleteLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.Listener<WebException> errorListener) {
        callingData.deleteLDSCalling(calling, currentUser, callback, errorListener);
    }

    /* Member data. */
    @Override
    public String getMemberName(Long id) {
        if(id != null) {
            return memberData.getMemberName(id);
        } else {
            return "";
        }
    }
    @Override
    public void getWardList(Response.Listener<List<Member>> listener) {
        memberData.getMembers(listener);
    }
    @Override
    public Member getMember(Long id) {
        return memberData.getMember(id);
    }
    @Override
    public void loadMembers(Response.Listener<Boolean> listener, Response.Listener<WebException> errorCallback, ProgressBar progressBar) {
        if(permissionManager.hasPermission(currentUser.getUnitRoles(), Permission.ORG_INFO_READ)) {
            memberData.loadMembers(listener, errorCallback, progressBar);
        }
    }
    @Override
    public void loadClassMemberAssignments() {
        final Map<Long, List<Long>> classMemberAssignment = new HashMap<>();
        List<Org> orgs = getOrgs();
        final List<Task<Map<Long, List<Long>>>> tasks = new ArrayList<>();
        for(ClassAssignment classAssignment : ClassAssignment.values()) {
            for(Org org : orgs) {
                if(org.getOrgTypeId() == UnitLevelOrgType.getOrgTypeIdByName(classAssignment.name())) {
                    tasks.add(webResources.getOrgMembers(org.getId()));
                    break;
                }
            }
        }

        Tasks.whenAll(tasks)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    for(Task<Map<Long, List<Long>>> item : tasks) {
                        classMemberAssignment.putAll(item.getResult());
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //TODO: need to respond to the calling method appropriately or show some sort of problem in the error handling services.
                }
            });
    }

    /* Google data. */
    @Override
    public void addCalling(Response.Listener<Boolean> listener, Response.Listener<WebException> errorListener, Calling calling) {
        Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_CREATE,
                new AuthorizableOrg(baseOrg.getUnitNumber(), UnitLevelOrgType.get(baseOrg.getOrgTypeId()), baseOrg.getOrgTypeId())))
        {
            callingData.addNewCalling(listener, errorListener, calling, baseOrg, currentUser);
        } else {
            listener.onResponse(false);
        }
    }
    @Override
    public void updateCalling(Response.Listener<Boolean> listener, Response.Listener<WebException> errorListener, Calling calling) {
        Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_UPDATE,
                new AuthorizableOrg(baseOrg.getUnitNumber(), UnitLevelOrgType.get(baseOrg.getOrgTypeId()), baseOrg.getOrgTypeId()))) {
            callingData.updateCalling(listener, errorListener, calling, baseOrg, currentUser);
        }
    }
    @Override
    public void deleteCalling(final Calling calling, final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener) {
        final Org org = callingData.getBaseOrg(calling.getParentOrg());
        if(permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_DELETE,
                new AuthorizableOrg(org.getUnitNumber(), UnitLevelOrgType.get(org.getOrgTypeId()), org.getOrgTypeId()))) {
            if(canDeleteCalling(calling, org)) {
                /* Get the latest file from google drive. */
                final Task<Org> getOrgDataTask = googleDataService.getOrgData(org);
                getOrgDataTask
                    .addOnSuccessListener(new OnSuccessListener<Org>() {
                        @Override
                        public void onSuccess(final Org newOrg) {
                            if(newOrg != null) {
                                /* Get's the subOrg within the main org(ie. instructors subOrg in the EQ Org) */
                                Org newParentOrg = callingData.findSubOrg(newOrg, calling.getParentOrg());
                                /* Removes the calling from within the new org */
                                newParentOrg.removeCalling(calling);
                                /* Save the changes back to google drive */
                                Task<Boolean> saveOrgFileTask = googleDataService.saveOrgFile(newOrg);
                                saveOrgFileTask
                                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean response) {
                                            if(response) {
                                                callingData.replaceOrg(newOrg, currentUser);
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
                            } else {
                                listener.onResponse(false);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errorListener.onResponse(ensureWebException(e));
                        }
                    });
            } else {
                listener.onResponse(false);
            }
        }
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
    public void getUnitSettings(final Response.Listener<UnitSettings> listener, final Response.Listener<WebException> errorListener, boolean getCachedItems) {
        googleDataService.getUnitSettings(getUnitNumber(), getCachedItems)
            .addOnSuccessListener(new OnSuccessListener<UnitSettings>() {
                @Override
                public void onSuccess(UnitSettings unitSettings) {
                    listener.onResponse(unitSettings);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    errorListener.onResponse(ensureWebException(e));
                }
            });
    }
    @Override
    public void saveUnitSettings(final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener, UnitSettings unitSettings) {
        googleDataService.saveUnitSettings(getUnitNumber(), unitSettings)
            .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if(task.isSuccessful()) {
                        listener.onResponse(task.getResult());
                    } else {
                        errorListener.onResponse(ensureWebException(task.getException()));
                    }
                }
            });
    }

    private Long getUnitNumber() {
        return currentUser != null ? currentUser.getUnitNumber() : 0L;
    }

    private WebException ensureWebException(Exception exception) {
        return exception instanceof WebException ? (WebException) exception : new WebException(ExceptionType.UNKOWN_GOOGLE_EXCEPTION, exception);
    }
}