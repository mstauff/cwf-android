package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.model.permissions.UnitRole;

import java.util.List;

/**
 * Represents an LDS User and their positions they are currently called to serve in.
 */
public class LdsUser {
    /* Fields */
    long individualId;
    List<Position> positions;
    List<UnitRole> unitRoles;

    /* Constructor(s) */
    public LdsUser(long individualId, List<Position> positions) {
        this.individualId = individualId;
        this.positions = positions;
    }

    /* Properties */
    public long getIndividualId() {
        return individualId;
    }

    public void setIndividualId(long individualId) {
        this.individualId = individualId;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public List<UnitRole> getUnitRoles() {
        return unitRoles;
    }

    public void setUnitRoles(List<UnitRole> unitRoles) {
        this.unitRoles = unitRoles;
    }

    public Long getUnitNumber() {
        return this.getPositions() != null && this.getPositions().size() > 0 ? this.getPositions().get(0).getUnitNumber() : 0L;
    }
}