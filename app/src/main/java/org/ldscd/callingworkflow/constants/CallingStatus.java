package org.ldscd.callingworkflow.constants;

public enum CallingStatus {
    PROPOSED("Proposed"),
    SUBMITTED("Submitted"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ON_HOLD("OnHold"),
    APPT_SET("AppointmentSet"),
    EXTENDED("Extended"),
    ACCEPTED("Accepted"),
    DECLINED("Declined"),
    SUSTAINED("Sustained"),
    SET_APART("SetApart"),
    RECORDED("Recorded"),
    UNKNOWN("Unknown");

    private final String status;

    private CallingStatus(String status) {
        this.status = status;
    }

    /**
     * @return The string representation of this element in the enumeration.
     */
    public String getStatus() {
        return this.status;
    }
}