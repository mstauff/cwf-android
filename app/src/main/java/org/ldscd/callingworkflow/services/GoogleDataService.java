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
    void deleteFile(final Response.Listener<Boolean> listener, final List<Org> orgs);
    boolean isAuthenticated();
    void syncDriveIds(List<Org> orgList, Response.Listener<Boolean> listener);
    void stopConnection();
    void restartConnection(Response.Listener<Boolean> listener, Activity activity);
    void getUnitSettings(Long unitNumber, Response.Listener<UnitSettings> listener);
    void saveUnitSettings(UnitSettings unitSettings, Response.Listener<Boolean> listener);
}