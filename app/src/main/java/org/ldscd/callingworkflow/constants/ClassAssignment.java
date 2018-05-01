package org.ldscd.callingworkflow.constants;

/**
 * Enum used for the Class assignment retrieval from LCR.  Only these defined Organizations
 * Should we get the class assignments for.
 */
public enum ClassAssignment {
    /**
     * Enum values.
     */
    YM("Young Men"),
    YW("Young Women"),
    EQ("Elders Quorum"),
    RS("Relief Society");

    /**
     * Private assignment field used for constructor(s) setting and getter retrieval.
     */
    private String assignment;

    /**
     * Constructor to set the selected Class Assignment enum
     * @param assignment
     */
    ClassAssignment(String assignment) {
        this.assignment = assignment;
    }

    /**
     * Getter for enum Class Assignment
     * @return selected Enum string of class assignment;
     */
    public String getClassAssignment() {
        return this.assignment;
    }
}