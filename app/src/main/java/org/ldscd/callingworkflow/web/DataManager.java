package org.ldscd.callingworkflow.web;

import android.app.Activity;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.PositionMetaData;

import java.util.List;

public interface DataManager {
    /* Calling data. */
    Calling getCalling(String id);
    Org getOrg(long id);
    List<Org> getOrgs();
    void loadOrgs(Response.Listener<Boolean> listener, ProgressBar progressBar, Activity activity);
    void loadOrg(Response.Listener<Org> listener, Org org);
    List<PositionMetaData> getAllPositionMetadata();
    PositionMetaData getPositionMetadata(int positionTypeId);
    /* Member data. */
    String getMemberName(Long id);
    Member getMember(Long id);
    void getWardList(Response.Listener<List<Member>> listener);
    void loadMembers(Response.Listener<Boolean> listener, ProgressBar progressBar);
    /* Google data. */
    void addCalling(Response.Listener<Boolean> listener, Calling calling, Org org);
    void updateCalling(Response.Listener<Boolean> listener, Calling calling, Org org);
    void deleteCalling(Response.Listener<Boolean> listener, Calling calling, Org org);
    List<Calling> getUnfinalizedCallings();
}