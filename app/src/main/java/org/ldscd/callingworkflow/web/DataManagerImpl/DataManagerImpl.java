package org.ldscd.callingworkflow.web.DataManagerImpl;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.MemberData;

import java.util.List;

public class DataManagerImpl implements DataManager {
    private static final String TAG = "DataManager";
    /* Properties */
    private CallingData callingData;
    private MemberData memberData;
    private GoogleDataService googleDataService;
    /* Constructor */
    public DataManagerImpl(CallingData callingData, MemberData memberData, GoogleDataService googleDataService) {
        this.callingData = callingData;
        this.memberData = memberData;
        this.googleDataService = googleDataService;
    }
    /* Methods */
    /* calling data. */
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
        callingData.loadOrgs(listener, progressBar, activity);
    }
    public void loadOrg(Response.Listener<Boolean> listener, Org org) {
        callingData.loadOrg(listener, org);
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
        memberData.loadMembers(listener, progressBar);
    }

    /* Google data. */
    @Override
    public void addCalling(Response.Listener<Boolean> listener, Calling calling, Org org) {
        callingData.addNewCalling(calling);
        Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
        saveCalling(listener, baseOrg, calling, Operation.CREATE);
    }
    @Override
    public void updateCalling(Response.Listener<Boolean> listener, Calling calling, Org org) {
        saveCalling(listener, org, calling, Operation.UPDATE);
    }
    @Override
    public void deleteCalling(Response.Listener<Boolean> listener, Calling calling, Org org) {
        saveCalling(listener, org, calling, Operation.DELETE);

    }
    /* Calling and Google Data */
    private void saveCalling(final Response.Listener<Boolean> listener, final Org org,  final Calling calling, Operation operation) {
        googleDataService.getOrgData(new Response.Listener<Org>() {
            @Override
            public void onResponse(Org response) {
                response.updateWithCallingChange(calling, Operation.UPDATE);
                googleDataService.saveFile(listener, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Failed to save new calling to Google");
                listener.onResponse(false);
            }
        }, org);
    }

    @Override
    public List<Calling> getUnfinalizedCallings() {
        return callingData.getUnfinalizedCallings();
    }

}