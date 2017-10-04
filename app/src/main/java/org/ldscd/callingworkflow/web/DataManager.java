package org.ldscd.callingworkflow.web;

import android.app.Activity;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.json.JSONException;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.PositionMetaData;

import java.util.List;

public interface DataManager {
    /* User Info */
    void getUserInfo(Response.Listener<LdsUser> listener);
    /* Calling data. */
    Calling getCalling(String id);
    Org getOrg(long id);
    List<Org> getOrgs();
    void loadOrgs(Response.Listener<Boolean> listener, ProgressBar progressBar, Activity activity);
    void refreshOrg(Response.Listener<Org> listener, Long orgId);
    List<PositionMetaData> getAllPositionMetadata();
    PositionMetaData getPositionMetadata(int positionTypeId);
    void releaseLDSCalling(Calling calling, Response.Listener callback) throws JSONException;
    void updateLDSCalling(Calling calling, Response.Listener callback) throws JSONException;
    void deleteLDSCalling(Calling calling, Response.Listener callback) throws JSONException;
    /* Member data. */
    String getMemberName(Long id);
    Member getMember(Long id);
    void getWardList(Response.Listener<List<Member>> listener);
    void loadMembers(Response.Listener<Boolean> listener, ProgressBar progressBar);
    /* Google data. */
    void addCalling(Response.Listener<Boolean> listener, Calling calling);
    void updateCalling(Response.Listener<Boolean> listener, Calling calling);
    void deleteCalling(Response.Listener<Boolean> listener, Calling calling, Org org);
    List<Calling> getUnfinalizedCallings();
}