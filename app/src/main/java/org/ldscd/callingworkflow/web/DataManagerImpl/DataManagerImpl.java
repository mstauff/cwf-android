package org.ldscd.callingworkflow.web.DataManagerImpl;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.MemberData;

import java.util.List;

import javax.inject.Inject;

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
    public void saveFile(Response.Listener<Boolean> listener, Org org) {
        googleDataService.saveFile(listener, org);
    }
    /* Calling and Google Data */
    public void saveCalling(final Response.Listener<Boolean> listener, final Calling calling) {
        callingData.addNewCalling(calling);
        Org baseOrg = callingData.getBaseOrg(calling.getParentOrg());
        saveFile(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean successGoogle) {
                if(!successGoogle) {
                    Log.e(TAG, "Failed to save new calling to Google");
                    listener.onResponse(false);
                } else {
                    listener.onResponse(true);
                }
            }
        }, baseOrg);
    }
}