package org.ldscd.callingworkflow.constants;

import java.util.HashMap;
import java.util.Map;

public enum CallingStatus {
    NONE(0, "None"),
    PROPOSED(1 , "Proposed"),
    SUBMITTED(2, "Submitted"),
    APPROVED(3, "Approved"),
    NOT_APPROVED(4, "Not Approved"),
    ON_HOLD(5, "On Hold"),
    APPOINTMENT_SET(6, "Appointment Set"),
    EXTENDED(7, "Extended"),
    ACCEPTED(8, "Accepted"),
    REFUSED(9, "Refused"),
    READY_TO_SUSTAIN(10, "Ready to Sustain"),
    SUSTAINED(11, "Sustained"),
    SET_APART(12, "Set Apart"),
    RECORDED(13, "Recorded"),
    RESUBMIT(14, "Resubmit"),
    UNKNOWN(15, "Unknown");

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