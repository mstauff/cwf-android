package org.ldscd.callingworkflow.web.DataManagerImpl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ClassAssignment;
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
import org.ldscd.callingworkflow.services.FileService;
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
    private FileService fileService;
    private LdsUser currentUser;

    /* Constructor */
    public DataManagerImpl(CallingData callingData, MemberData memberData, GoogleDriveService googleDataService, IWebResources webResources, PermissionManager permissionManager, FileService fileService) {
        this.callingData = callingData;
        this.memberData = memberData;
        this.googleDataService = googleDataService;
        this.webResources = webResources;
        this.permissionManager = permissionManager;
        this.fileService = fileService;
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
    public void getUserInfo(String userName, String password, final Response.Listener<LdsUser> userListener, Response.Listener<WebException> errorCallback) {

        if (currentUser == null) {
                webResources.setCredentials(userName, password);
            webResources.getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    currentUser = ldsUser;
                    if (currentUser != null &&
                        (currentUser.getUnitRoles() == null ||
                         (currentUser.getUnitRoles() == null || currentUser.getUnitRoles().size() == 0)))
                    {
                        currentUser.setUnitRoles(permissionManager.createUnitRoles(currentUser.getPositions(), currentUser.getUnitNumber()));
                    }
                    userListener.onResponse(currentUser);
                }
            }, errorCallback);
        } else {
            userListener.onResponse(currentUser);
        }
    }
    @Override
    public void signOut(final Response.Listener<Boolean> authCallback, final Response.Listener<WebException> errorCallback) {
        webResources.signOut(authCallback, errorCallback);
        currentUser = null;
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
        Task<Boolean> task;
        if (isParentOrg) {
            /* Remove it from database/google drive */
            task = googleDataService.deleteOrg(org);
        } else {
            callingData.removeSubOrg(baseOrg, org.getId());
            task = googleDataService.saveOrgFile(baseOrg);
        }
        task.addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    if (isParentOrg) {
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
        if (currentUser != null && !orgIds.isEmpty()) {
            callingData.refreshGoogleDriveOrgs(listener, errorCallback, currentUser, orgIds);
        } else {
            listener.onResponse(false);
        }
    }
    @Override
    public void refreshLCROrgs(Response.Listener<Boolean> listener, Response.Listener<WebException> errorCallback) {
        if (currentUser != null) {
            callingData.refreshLCROrgs(listener, errorCallback, currentUser);
        } else {
            listener.onResponse(false);
        }
    }
    @Override
    public void loadOrgs(Response.Listener<Boolean> listener, Response.Listener<WebException> errorCallback, ProgressBar progressBar, Activity activity) {
        if (currentUser != null && permissionManager.hasPermission(currentUser.getUnitRoles(), Permission.ORG_INFO_READ)) {
            callingData.loadOrgs(listener, errorCallback, progressBar, activity, currentUser);
        } else {
            errorCallback.onResponse(new WebException(currentUser == null ? ExceptionType.LDS_AUTH_REQUIRED : ExceptionType.NO_PERMISSIONS));
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

    public void releaseLDSCalling(final Calling calling, final Response.Listener<Boolean> callback, final Response.Listener<WebException> errorListener) {
        callingData.releaseLDSCalling(calling, currentUser)
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean response) {
                    callback.onResponse(response);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    errorListener.onResponse(ensureWebException(e));
                }
            });
    }
    public void updateLDSCalling(final Calling calling, final Response.Listener<Boolean> callback, final Response.Listener<WebException> errorListener) {
        callingData.updateLDSCalling(calling, currentUser)
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean response) {
                    callback.onResponse(response);
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
    public void deleteLDSCalling(final Calling calling, final Response.Listener<Boolean> callback, final Response.Listener<WebException> errorListener) {
        callingData.deleteLDSCalling(calling, currentUser, callback, errorListener);
    }

    /* Member data. */
    @Override
    public String getMemberName(Long id) {
        return id != null ? memberData.getMemberName(id) : "";
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
        if (permissionManager.hasPermission(currentUser.getUnitRoles(), Permission.ORG_INFO_READ)) {
            memberData.loadMembers(listener, errorCallback, progressBar);
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_PERMISSIONS));
        }
    }
    @Override
    public  boolean isCachedClassMemberAssignmentsExpired() {
        return fileService.isCachedMemberAssignmentsExpired();
    }
    @Override
    public Task<Boolean> loadClassMemberAssignments() {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        final Map<Long, List<Long>> memberAssignments = new HashMap<>();
        List<Org> orgs = getOrgs();
        if (orgs == null || orgs.size() > 0) {
            getClassAssignments(orgs, false)
                .addOnSuccessListener(new OnSuccessListener<List<Org>>() {
                    @Override
                    public void onSuccess(List<Org> orgs) {
                        for(Org org : orgs) {
                            List<Long> assignedMember = org.getAssignedMembers();
                            if(assignedMember != null) {
                                memberAssignments.put(org.getId(), assignedMember);
                                for (Long memberId : assignedMember) {
                                    Member member = memberData.getMember(memberId);
                                    if (member != null) {
                                        member.setClassAssignment(org.getId());
                                    }
                                }
                            }
                        }
                        fileService.saveMemberAssignmentsToCache(memberAssignments);
                        taskCompletionSource.setResult(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        taskCompletionSource.setException(e);
                    }
                });
        } else {
            taskCompletionSource.setResult(true);
        }
        return taskCompletionSource.getTask();
    }

    private Task<List<Org>> getClassAssignments(List<Org> orgs, boolean getAllOrgs) {
        final TaskCompletionSource<List<Org>> taskClassMemberAssignment = new TaskCompletionSource<>();
        final List<Task<List<Org>>> tasks = new ArrayList<>();
        for (Org org : orgs) {
            if (getAllOrgs) {
                tasks.add(webResources.getOrgWithMembers(org.getId()));
            } else {
                for (ClassAssignment classAssignment : ClassAssignment.values()) {
                    if (org.getConflictCause() == null && org.getOrgTypeId() == UnitLevelOrgType.getOrgTypeIdByName(classAssignment.name())) {
                        tasks.add(webResources.getOrgWithMembers(org.getId()));
                        break;
                    }
                }
            }
        }

        Tasks.whenAll(tasks)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    List<Org> items = new ArrayList<>();
                    for (Task<List<Org>> item : tasks) {
                        items.addAll(item.getResult());
                    }
                    taskClassMemberAssignment.setResult(items);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskClassMemberAssignment.setException(e);
                }
            });
        return taskClassMemberAssignment.getTask();
    }

    /* Google data. */
    @Override
    public void addCalling(Response.Listener<Boolean> listener, Response.Listener<WebException> errorListener, Calling calling) {
        Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
        if (permissionManager.isAuthorized(currentUser.getUnitRoles(),
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
        if (permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_UPDATE,
                new AuthorizableOrg(baseOrg.getUnitNumber(), UnitLevelOrgType.get(baseOrg.getOrgTypeId()), baseOrg.getOrgTypeId()))) {
            callingData.updateCalling(listener, errorListener, calling, baseOrg, currentUser);
        }
    }
    @Override
    public void deleteCalling(final Calling calling, final Response.Listener<Boolean> listener, final Response.Listener<WebException> errorListener) {
        final Org org = callingData.getBaseOrg(calling.getParentOrg());
        if (permissionManager.isAuthorized(currentUser.getUnitRoles(),
                Permission.POTENTIAL_CALLING_DELETE,
                new AuthorizableOrg(org.getUnitNumber(), UnitLevelOrgType.get(org.getOrgTypeId()), org.getOrgTypeId()))) {
            if (canDeleteCalling(calling, org)) {
                /* Get the latest file from google drive. */
                googleDataService.getOrgData(org)
                    .addOnSuccessListener(new OnSuccessListener<Org>() {
                        @Override
                        public void onSuccess(final Org newOrg) {
                            if (newOrg != null) {
                                /* Get's the subOrg within the main org(ie. instructors subOrg in the EQ Org) */
                                Org newParentOrg = callingData.findSubOrg(newOrg, calling.getParentOrg());
                                /* Removes the calling from within the new org */
                                newParentOrg.removeCalling(calling);
                                /* Save the changes back to google drive */
                                googleDataService.saveOrgFile(newOrg)
                                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean response) {
                                            if (response) {
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
                    if (task.isSuccessful()) {
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