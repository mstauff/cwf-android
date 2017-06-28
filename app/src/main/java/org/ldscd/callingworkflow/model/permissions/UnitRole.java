package org.ldscd.callingworkflow.model.permissions;

import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.model.permissions.constants.OrgType;
import org.ldscd.callingworkflow.model.permissions.constants.Role;

/**
 *
 */
public class UnitRole {
    /* Fields */
    Role role;
    Long unitNum;
    Long orgId;
    OrgType orgType;
    Position activePosition;
    Integer orgRightsException;

    /* Constructor */
    public UnitRole(Role role, Long unitNum, Long orgId, OrgType orgType, Position activePosition, Integer orgRightsException) {
        this.role = role;
        this.unitNum = unitNum;
        this.orgId = orgId;
        this.orgType = orgType;
        this.activePosition = activePosition;
        this.orgRightsException = orgRightsException;
    }

    /* Properties */
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getUnitNum() {
        return unitNum;
    }

    public void setUnitNum(Long unitNum) {
        this.unitNum = unitNum;
    }

    public Long getOrgId() {
        return this.orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public OrgType getOrgType() {
        return orgType;
    }

    public void setOrgType(OrgType orgType) {
        this.orgType = orgType;
    }

    public Position getActivePosition() {
        return activePosition;
    }

    public void setActivePosition(Position activePosition) {
        this.activePosition = activePosition;
    }

    public Integer getOrgRightsException() {
        return orgRightsException;
    }

    public void setOrgRightsException(Integer orgRightsException) {
        this.orgRightsException = orgRightsException;
    }
}
/*
 let role : Role
    let unitNum : Int64
    // The ID of the org that the role is associated with. It's optional as for a unit admin it would not be set, only for an org admin. Currently it's not used, we base everything off of the position's org type, but if we want to limit an EQ Pres in a unit with multiple EQ's we would need to make use of this
    let orgId : Int64?

    // The orgType that an org admin is associated with (so EQ for EQ Pres)
    let orgType : UnitLevelOrgType?

    // The position that the user holds that grants this role. It's not currently being used.
    let activePosition : Position

    // An OrgAdmin will have rights to all sub-orgs in their org, except for the presidency org. This field stores the type id of the presidency org so we can handle it specially. The reason it's an ID rather than an enum like orgType is we only have enums for the main unit level orgs (RS, EQ, HP, etc.). There are too many suborgs, and the potential for new ones to be added or changed so we don't maintain an enum of all those, we just use the ID's
    let orgRightsException : Int?
 */