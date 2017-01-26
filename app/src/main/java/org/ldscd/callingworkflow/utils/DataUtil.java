package org.ldscd.callingworkflow.utils;

import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Org;

public class DataUtil {
    static String fileDelimiter = "-";

    public static String getFileName(Org org) {
        return UnitLevelOrgType.get(org.getOrgTypeId()) + fileDelimiter + org.getId();
    }
}
