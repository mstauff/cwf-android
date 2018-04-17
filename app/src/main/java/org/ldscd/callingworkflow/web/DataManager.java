package org.ldscd.callingworkflow.web;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.model.UnitSettings;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;

import java.util.List;

public interface DataManager {
    /* Methods */
    PermissionManager getPermissionManager();
    /* Google Drive */
    boolean isGoogleDriveAuthenticated(Context context);
    /* User data */
    LdsUser getCurrentUser();
    void getUserInfo(String userName, String password, boolean hasChanges, Response.Listener<LdsUser> userListener, Response.Listener<WebResourcesException> errorCallback);
    void getSharedPreferences(Response.Listener<SharedPreferences> listener);
    /* Calling data. */
    void getCallingStatus(Response.Listener<List<CallingStatus>> listener);
    Calling getCalling(String id);
    Org getOrg(long id);
    List<Org> getOrgs();
    boolean removeSubOrg(Org org, long subOrgId);
    Task<Boolean> updateOrg(Org org);
    void refreshGoogleDriveOrgs(List<Long> orgIds, Response.Listener<Boolean> listener, Response.Listener<WebResourcesException> errorCallback);
    void refreshLCROrgs(Response.Listener<Boolean> listener, Response.Listener<WebResourcesException> errorCallback);
    void loadOrgs(Response.Listener<Boolean> listener, Response.Listener<WebResourcesException> errorCallback, ProgressBar progressBar, Activity activity);
    void loadPositionMetadata();
    void clearLocalOrgData();
    void refreshOrg(Response.Listener<Org> listener, Long orgId);
    List<PositionMetaData> getAllPositionMetadata();
    PositionMetaData getPositionMetadata(int positionTypeId);
    void releaseLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.Listener<WebResourcesException> errorListener);
    void updateLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.Listener<WebResourcesException> errorListener);
    void deleteLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.Listener<WebResourcesException> errorListener);
    boolean canDeleteCalling(Calling calling, Org org);
    /* Member data. */
    String getMemberName(Long id);
    Member getMember(Long id);
    void getWardList(Response.Listener<List<Member>> listener);
    void loadMembers(Response.Listener<Boolean> listener, Response.Listener<WebResourcesException> errorCallback, ProgressBar progressBar);
    /* Google data. */
    void addCalling(Response.Listener<Boolean> listener, Calling calling);
    void updateCalling(Response.Listener<Boolean> listener, Calling calling);
    void deleteCalling(Calling calling, Response.Listener<Boolean> listener, Response.Listener<WebResourcesException> errorListener);
    List<Calling> getUnfinalizedCallings();
    Task<Boolean> deleteOrg(Org org);
    /* Unit Settings */
    void getUnitSettings(Response.Listener<UnitSettings> listener, boolean getCachedItems);
    void saveUnitSettings(Response.Listener<Boolean> listener, UnitSettings unitSettings);
}