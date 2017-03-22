package org.ldscd.callingworkflow.model;

/**
 * Represents a calling position.  i.e. CTR B teacher.
 */
public class Position {
    /* Fields */
    private String name;
    private int positionTypeId;
    private Boolean hidden;
    private Boolean multiplesAllowed;

    /* Constructors */
    public Position() {}
    public Position(String name, int positionTypeId, Boolean hidden, Boolean multiplesAllowed) {
        this.name = name;
        this.positionTypeId = positionTypeId;
        this.hidden = hidden;
        this.multiplesAllowed = multiplesAllowed;
    }

    /* Properties */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPositionTypeId() { return positionTypeId; }
    public void setPositionTypeId(int positionTypeId) { this.positionTypeId = positionTypeId; }

    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getMultiplesAllowed() {
        return multiplesAllowed;
    }
    public void setMultiplesAllowed(Boolean multiplesAllowed) {
        this.multiplesAllowed = multiplesAllowed;
    }

    public String toString() { return name; }

    public boolean equals(Position position) {
        return this.getPositionTypeId() == position.getPositionTypeId();
    }
}