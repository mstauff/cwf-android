package org.ldscd.callingworkflow.constants;

public enum ExistingCallingStatus {
    UNKNOWN("Unknown"),
    ACTIVE("Active"),
    NOTIFIED_OF_RELEASE("NotifiedOfRelease"),
    RELEASED("Released");
    private final String status;

    private ExistingCallingStatus(String status) {
        this.status = status;
    }

    /**
     * @return The string representation of this element in the enumeration.
     */
    public String getStatus() {
        return this.status;
    }
}
