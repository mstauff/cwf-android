package org.ldscd.callingworkflow.services;

import android.app.Activity;

import com.android.volley.Response;
import org.ldscd.callingworkflow.model.Org;

import java.util.List;

public interface GoogleDataService {
    void init(Response.Listener<Boolean> listener, Activity activity);

    public void getOrgData(Response.Listener<Org> listener, Response.ErrorListener errorListener, Org org);
    public void getOrgs(Response.Listener<List<Org>> listener, Response.ErrorListener errorListener);
    public void saveFile(final Response.Listener<Boolean> listener, final Org org);
    public void deleteFile(final Response.Listener<Boolean> listener, final Org org);
    public boolean isAuthenticated();
    public void syncDriveIds(Response.Listener<Boolean> listener, List<Org> orgList);

    void stopConnection();

    void restartConnection(Response.Listener<Boolean> listener, Activity activity);
}
