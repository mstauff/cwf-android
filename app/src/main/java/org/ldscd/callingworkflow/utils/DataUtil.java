package org.ldscd.callingworkflow.utils;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Org;

public class DataUtil {
    static String fileDelimiter = "-";
    static String fileExtension = ".json";

    public static String getFileName(Org org) {
        return UnitLevelOrgType.get(org.getOrgTypeId()).getOrgName() + fileDelimiter + org.getId() + fileExtension;
    }

    public static int getMonthsSinceActiveDate(String activeDate) {
        int result = 0;
        DateTime activeDateTime = getDateTime(activeDate);
        if(activeDateTime != null) {
            result = Months.monthsBetween(activeDateTime, new DateTime()).getMonths();
        }
        return result;
    }

    public static DateTime getDateTime(String activeDate) {
        DateTime result = null;
        if(activeDate != null && activeDate.length() == 8) {
            int year = Integer.parseInt(activeDate.substring(0, 4));
            int month = Integer.parseInt(activeDate.substring(4, 6));
            int day = Integer.parseInt(activeDate.substring(6));
            result = new DateTime(year, month, day, 0, 0);
        }
        return result;
    }
}
