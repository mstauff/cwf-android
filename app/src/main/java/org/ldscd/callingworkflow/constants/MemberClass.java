package org.ldscd.callingworkflow.constants;

/**
 * Represents the class a member is assigned to attend.
 */
public enum MemberClass {
    RELIEF_SOCIETY("Relief Society"),
    LAUREL("Laurel"),
    MIA_MAID("Mia Maid"),
    BEEHIVE("Beehive");

    private String printName;
    MemberClass(String printName) {
        this.printName = printName;
    }

    @Override
    public String toString() {
        return printName;
    }
}