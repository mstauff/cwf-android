package org.ldscd.callingworkflow.model;

/**
 * Represents an object to offer a simple name for position.
 */
public class PositionMetaData {
    /* Fields */
    int positionTypeId;
    String shortName;
    PositionRequirements requirements;

    /* Constructor */
    public PositionMetaData(int positionTypeId, String shortName, PositionRequirements requirements) {
        this.positionTypeId = positionTypeId;
        this.shortName = shortName;
        this.requirements = requirements;
    }

    /* Properties */
    public int getPositionTypeId() {
        return positionTypeId;
    }
    public void setPositionTypeId(int positionTypeId) {
        this.positionTypeId = positionTypeId;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public PositionRequirements getRequirements() {
        return requirements;
    }
    public void setRequirements(PositionRequirements requirements) {
        this.requirements = requirements;
    }

    @Override
    public boolean equals(Object obj) {
        PositionMetaData positionMetaData = (PositionMetaData) obj;
        return this.positionTypeId == positionMetaData.positionTypeId;
    }
}