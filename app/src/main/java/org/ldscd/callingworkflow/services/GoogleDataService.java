package org.ldscd.callingworkflow.services;

import android.app.Activity;
import com.android.volley.Response;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.UnitSettings;

import java.util.List;

public interface GoogleDataService {
    void init(Response.Listener<Boolean> listener, Activity activity);
    void getOrgData(Response.Listener<Org> listener, Response.ErrorListener errorListener, Org org);
    void getOrgs(Response.Listener<List<Org>> listener, Response.ErrorListener errorListener);
    void saveOrgFile(final Response.Listener<Boolean> listener, final Org org);
    void deleteFile(final Response.Listener<Boolean> listener, final Org org);
    boolean isAuthenticated();
    void syncDriveIds(Response.Listener<Boolean> listener, List<Org> orgList);
    void stopConnection();
    void restartConnection(Response.Listener<Boolean> listener, Activity activity);
    void getUnitSettings(Response.Listener<UnitSettings> listener, Long unitNumber);
    void saveUnitSettings(Response.Listener<Boolean> listener, UnitSettings jsonObject);
}