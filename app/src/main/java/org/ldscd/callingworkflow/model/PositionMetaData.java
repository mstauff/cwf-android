package org.ldscd.callingworkflow.model;

/**
 * Represents an object to offer a simple name for position.
 */
public class PositionMetaData {
    /* Fields */
    int positionTypeId;
    String shortName;
    String mediumName;
    PositionRequirements requirements;

    /* Constructor */
    public PositionMetaData(int positionTypeId, String shortName, String mediumName, PositionRequirements requirements) {
        this.positionTypeId = positionTypeId;
        this.shortName = shortName;
        this.mediumName = mediumName;
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
        return shortName != null ? shortName : mediumName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getMediumName() {
        return mediumName;
    }
    public void setMediumName(String mediumName) {
        this.mediumName = mediumName;
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