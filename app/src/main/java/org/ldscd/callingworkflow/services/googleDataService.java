package org.ldscd.callingworkflow.services;

import com.android.volley.Response;
import org.ldscd.callingworkflow.model.Org;

import java.util.List;

public interface GoogleDataService {
    public void getOrgData(Response.Listener<Org> listener, Response.ErrorListener errorListener, Org org);
    public boolean saveFile(final Org org);
    public boolean deleteFile(final Org org);
    public boolean isAuthenticated();
    public void syncDriveIds(List<Org> orgList);
}