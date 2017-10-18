package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.constants.CallingStatus;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an object that is persisted and hydrated from external json file.
 * It contains various items to allow the user to customize the application, somewhat.
 */
public class UnitSettings implements Serializable {
    /* Field(s) */
    private Long unitNumber;
    private List<CallingStatus> disabledStatuses;

    /* Constructor(s) */
    public UnitSettings() {}
    public UnitSettings(Long unitNumber, List<CallingStatus> disabledStatuses) {
        this.unitNumber = unitNumber;
        this.disabledStatuses = disabledStatuses;
    }

    /* Properties */
    public Long getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(Long unitNumber) {
        this.unitNumber = unitNumber;
    }

    public List<CallingStatus> getDisabledStatuses() {
        return disabledStatuses;
    }

    public void setDisabledStatuses(List<CallingStatus> disabledStatuses) {
        this.disabledStatuses = disabledStatuses;
    }
}