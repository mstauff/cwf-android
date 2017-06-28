package org.ldscd.callingworkflow.model;

/**
 * Represents a calling position.  i.e. CTR B teacher.
 */
public class Position {
    /* Fields */
    private String name;
    private int positionTypeId;
    private Boolean hidden;
    private Boolean allowMultiple;
    private long positionDisplayOrder;
    private long unitNumber;

    /* Constructors */
    public Position() {}
    public Position(String name, int positionTypeId, Boolean hidden, Boolean allowMultiple,
                    Long positionDisplayOrder, Long unitNumber) {
        this.name = name;
        this.positionTypeId = positionTypeId;
        this.hidden = hidden;
        this.allowMultiple = allowMultiple;
        this.positionDisplayOrder = positionDisplayOrder;
        this.unitNumber = unitNumber;
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

    public Boolean getAllowMultiple() {
        return allowMultiple;
    }
    public void setAllowMultiple(Boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public long getPositionDisplayOrder() { return positionDisplayOrder; }
    public void setPositionDisplayOrder(long positionDisplayOrder) { this.positionDisplayOrder = positionDisplayOrder; }

    public long getUnitNumber() {
        return unitNumber;
    }
    public void setUnitNumber(long unitNumber) {
        this.unitNumber = unitNumber;
    }

    public boolean equals(Position position) {
        return this.getPositionTypeId() == position.getPositionTypeId();
    }

    @Override
    public String toString() {
        return this.name;
    }
}