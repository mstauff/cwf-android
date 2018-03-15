package org.ldscd.callingworkflow.services;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.tasks.Task;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.UnitSettings;
import java.util.List;

public interface GoogleDriveService {
    boolean isAuthenticated(Context context);
    Task<Boolean> init(Activity activity);
    Task<Org> getOrgData(Org org);
    Task<List<Org>> getOrgs();
    Task<Boolean> saveOrgFile(final Org org);
    Task<Boolean> deleteOrgs(final List<Org> orgs);
    Task<Boolean> syncDriveIds(List<Org> orgList);
    Task<UnitSettings> getUnitSettings(Long unitNumber);
    Task<Boolean> saveUnitSettings(UnitSettings unitSettings);
}