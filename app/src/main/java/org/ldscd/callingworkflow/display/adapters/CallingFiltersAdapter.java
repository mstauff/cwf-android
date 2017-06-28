package org.ldscd.callingworkflow.display.adapters;

import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Org;

import java.util.List;

public interface CallingFiltersAdapter {
    void setStatusList(List<CallingStatus> statusList);
    List<CallingStatus> getStatusList();

    void setOrgList(List<Org> orgList);
    List<Org> getOrgList();

    void filterCallings();
}
