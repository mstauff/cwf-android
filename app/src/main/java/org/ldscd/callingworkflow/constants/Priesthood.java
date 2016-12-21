package org.ldscd.callingworkflow.constants;

public enum Priesthood {
    /* Deacon. */
    DEACON("Deacon"),
    /* Teacher. */
    TEACHER("Teacher"),
    /* Priest. */
    PRIEST("Priest"),
    /* Elder. */
    ELDER("Elder"),
    /* High Priest. */
    HIGH_PRIEST("High Priest"),
    /* Seventy. */
    SEVENTY("Seventy"),
    /* Unknown priesthood office. */
    UNKNOWN("Unknown");

    private final String priesthood;

    private Priesthood(String priesthood) {
        this.priesthood = priesthood;
    }

    /**
     * @return The string representation of this element in the enumeration.
     */
    public String getPriesthood() {
        return this.priesthood;
    }
}
