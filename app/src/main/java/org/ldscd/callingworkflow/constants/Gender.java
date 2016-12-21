package org.ldscd.callingworkflow.constants;

public enum Gender {
    /** Male. */
    M("Male"),
    /** Female. */
    F("Female"),
    /** Gender is not known, or not specified. */
    UNKNOWN("Unknown");

    private final String name;

    private Gender(String name) {
        this.name = name;
    }

    /**
     * @return The string representation of this element in the enumeration.
     */
    public String getName() {
        return this.name;
    }
}