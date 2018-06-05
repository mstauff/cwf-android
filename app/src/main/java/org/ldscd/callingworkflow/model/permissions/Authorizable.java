package org.ldscd.callingworkflow.model.permissions;

/**
 * Base class for all security level objects.
 */
public class Authorizable {
    long unitNum;

    public Authorizable(long unitNum) {
        this.unitNum = unitNum;
    }

    public long getUnitNum() {
        return unitNum;
    }

    public void setUnitNum(long unitNum) {
        this.unitNum = unitNum;
    }
}