package org.ldscd.callingworkflow.model.permissions;

import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.model.permissions.constants.Role;

/**
 *
 */
public class UnitRole {
    /* Fields */
    private Role role;
    private Long unitNum;
    private Long orgId;
    private UnitLevelOrgType orgType;
    /* The position that gave them rights to be in this role.  It's not currently being used.  Could possibly go away. */
    private Position activePosition;
    private Integer orgRightsException;

    /* Constructor */
    public UnitRole(Role role, Long unitNum, Long orgId, UnitLevelOrgType orgType, Position activePosition) {
        this.role = role;
        this.unitNum = unitNum;
        this.orgId = orgId;
        this.orgType = orgType;
        this.activePosition = activePosition;
        if(orgType != null) {
            this.orgRightsException = orgType.getExceptionId();
        }
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

    public UnitLevelOrgType getOrgType() {
        return orgType;
    }

    public void setOrgType(UnitLevelOrgType orgType) {
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
}