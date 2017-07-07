package org.ldscd.callingworkflow.constants;

import java.util.HashMap;
import java.util.Map;

public enum CallingStatus {
    PROPOSED(0 , "Proposed"),
    SUBMITTED(1, "Submitted"),
    APPROVED(2, "Approved"),
    NOT_APPROVED(3, "Not Approved"),
    ON_HOLD(4, "On Hold"),
    APPOINTMENT_SET(5, "Appointment Set"),
    EXTENDED(6, "Extended"),
    ACCEPTED(7, "Accepted"),
    REFUSED(8, "Refused"),
    SUSTAINED(9, "Sustained"),
    SET_APART(10, "Set Apart"),
    RECORDED(11, "Recorded"),
    UNKNOWN(12, "Unknown"),
    READY_TO_SUSTAIN(13, "Ready to Sustain"),
    RESUBMIT(14, "Resubmit");

    private final String printName;
    private final int position;
    private CallingStatus(int position, String printName) {
        this.position = position;
        this.printName = printName;
    }

    /**
     * @return The string representation of this element in the enumeration.
     */
    public int getStatus() {
        return this.position;
    }

    private static final Map<String, CallingStatus> lookup = new HashMap<String, CallingStatus>();
    private static final Map<String, CallingStatus> byValueLookup = new HashMap<>();
    static {
        for (CallingStatus status : CallingStatus.values()) {
            lookup.put(status.name(), status);
            byValueLookup.put(status.toString(), status);
        }
    }

    public static CallingStatus get(String status) {
        return lookup.get(status);
    }
    public static CallingStatus getByValue(String status) {
        return byValueLookup.get(status);
    }

    @Override public String toString() {
        return this.printName;
    }
}