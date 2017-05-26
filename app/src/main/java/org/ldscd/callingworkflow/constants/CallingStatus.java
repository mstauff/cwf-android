package org.ldscd.callingworkflow.constants;

import java.util.HashMap;
import java.util.Map;

public enum CallingStatus {
    PROPOSED(0 , "Proposed"),
    SUBMITTED(1, "Submitted"),
    APPROVED(2, "Approved"),
    REJECTED(3, "Rejected"),
    ON_HOLD(4, "On Hold"),
    APPT_SET(5, "Appointment Set"),
    EXTENDED(6, "Extended"),
    ACCEPTED(7, "Accepted"),
    DECLINED(8, "Declined"),
    SUSTAINED(9, "Sustained"),
    SET_APART(10, "Set Apart"),
    RECORDED(11, "Recorded"),
    UNKNOWN(12, "Unknown");

    private final String status;
    private final int position;
    private CallingStatus(int position, String status) {
        this.position = position;
        this.status = status;
    }

    /**
     * @return The string representation of this element in the enumeration.
     */
    public String getStatus() {
        return this.status;
    }

    private static final Map<String, CallingStatus> lookup = new HashMap<String, CallingStatus>();

    static {
        for (CallingStatus status : CallingStatus.values()) {
            lookup.put(status.name(), status);
        }
    }

    public static CallingStatus get(String status) {
        return lookup.get(status);
    }

    @Override public String toString() {
        return this.status;
    }
}