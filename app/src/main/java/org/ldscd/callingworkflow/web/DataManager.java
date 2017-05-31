package org.ldscd.callingworkflow.web;

import android.app.Activity;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;

import java.util.List;

public interface DataManager {
    /* Calling data. */
    public Calling getCalling(String id);
    public Org getOrg(long id);
    public List<Org> getOrgs();
    public void loadOrgs(Response.Listener<Boolean> listener, ProgressBar progressBar, Activity activity);
    /* Member data. */
    public String getMemberName(Long id);
    public Member getMember(Long id);
    public void getWardList(Response.Listener<List<Member>> listener);
    public void loadMembers(Response.Listener<Boolean> listener, ProgressBar progressBar);
    /* Google data. */
    public void saveFile(Response.Listener<Boolean> listener, Org org);
    public void saveCalling(Response.Listener<Boolean> listener, Calling calling);
    public List<Calling> getUnfinalizedCallings();
}
