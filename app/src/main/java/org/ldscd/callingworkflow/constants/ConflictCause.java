package org.ldscd.callingworkflow.constants;

public enum ConflictCause {
    /* Enum values. */
    LDS_EQUIVALENT_DELETED("LdsEquivalentDeleted"),
    EQUIVALENT_POTENTIAL_AND_ACTUAL("EquivalentPotentialAndActual");

    /* Field for enum selected storage. */
    private final String cause;

    /**
     * Constructor for enum
     * @param cause
     */
    ConflictCause(String cause) {
        this.cause = cause;
    }
    /**
     * @return The string representation of this element in the enumeration.
     */
    public String getConflictCause() { return this.cause; }
}
