package org.ldscd.callingworkflow.web;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.ProgressBar;

import com.android.volley.Response;

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
    /* User data */
    void getUserInfo(String userName, String password, boolean hasChanges, Response.Listener<LdsUser> userListener);
    void getSharedPreferences(Response.Listener<SharedPreferences> listener);
    /* Calling data. */
    void getCallingStatus(Response.Listener<List<CallingStatus>> listener);
    Calling getCalling(String id);
    Org getOrg(long id);
    List<Org> getOrgs();
    void refreshLCROrgs(Response.Listener<Boolean> listener);
    void loadOrgs(Response.Listener<Boolean> listener, ProgressBar progressBar, Activity activity);
    void refreshOrg(Response.Listener<Org> listener, Long orgId);
    List<PositionMetaData> getAllPositionMetadata();
    PositionMetaData getPositionMetadata(int positionTypeId);
    void releaseLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.ErrorListener errorListener) throws JSONException;
    void updateLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.ErrorListener errorListener) throws JSONException;
    void deleteLDSCalling(Calling calling, Response.Listener<Boolean> callback, Response.ErrorListener errorListener) throws JSONException;
    boolean canDeleteCalling(Calling calling, Org org);
    /* Member data. */
    String getMemberName(Long id);
    Member getMember(Long id);
    void getWardList(Response.Listener<List<Member>> listener);
    void loadMembers(Response.Listener<Boolean> listener, ProgressBar progressBar);
    /* Google data. */
    void addCalling(Response.Listener<Boolean> listener, Calling calling);
    void updateCalling(Response.Listener<Boolean> listener, Calling calling);
    void deleteCalling(Calling calling, Response.Listener<Boolean> listener, Response.ErrorListener errorListener);
    List<Calling> getUnfinalizedCallings();
    /* Unit Settings */
    void getUnitSettings(Response.Listener<UnitSettings> listener);
    void saveUnitSettings(Response.Listener<Boolean> listener, UnitSettings unitSettings);
}