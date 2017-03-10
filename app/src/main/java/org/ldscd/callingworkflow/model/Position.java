package org.ldscd.callingworkflow.model;

/**
 * Represents a calling position.  i.e. CTR B teacher.
 */
public class Position {
    /* Fields */
    long positionId;
    String name;
    int positionOrgTypeId;

    /* Constructors */
    public Position() {}
    public Position(long positionId, String name, int positionOrgTypeId) {
        this.positionId = positionId;
        this.name = name;
        this.positionOrgTypeId = positionOrgTypeId;
    }

    /* Properties */
    public long getPositionId() { return positionId; }
    public void setPositionId(long positionId) { this.positionId = positionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPositionOrgTypeId() { return positionOrgTypeId; }
    public void setPositionOrgTypeId(int positionOrgTypeId) { this.positionOrgTypeId = positionOrgTypeId; }

    public String toString() { return name; }
}