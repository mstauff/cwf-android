package org.ldscd.callingworkflow.utils;

import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Org;

public class DataUtil {
    static String fileDelimiter = "-";
    static String fileExtension = ".json";

    public static String getFileName(Org org) {
        return UnitLevelOrgType.get(org.getOrgTypeId()).name() + fileDelimiter + org.getId() + fileExtension;
    }
}
