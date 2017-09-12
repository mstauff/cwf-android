package org.ldscd.callingworkflow.model.permissions;


import org.ldscd.callingworkflow.constants.UnitLevelOrgType;

/**
 * Security object to validate the Org objects.
 */
public class AuthorizableOrg extends Authorizable {
    /* This is the org type of the root org. So it may be that the org the user is trying to
        edit is the EQ Instructors, but this org type would be that of the root org (EQ)
     */
    UnitLevelOrgType orgType;
    /* This is the orgType of the actual org that is being verified. We don't have an enum
        for every possible sub org type (i.e. EQ Instructors), we only make an enum for the main
        root level org types (HP, EQ, RS, etc.). So that's why we use the enum for the
        unitLevelOrgType, but just the orgTypeId for the actual org. This is primarily used to
        verify if it's a sub-org that may be an exception to the users privileges. Most orgs
        presidencies have rights to view their org, but they have an exception for the presidency
        org (so if a bishop is making changes to the primary presidency, the presidency members
        won't see it through the app when they shouldn't be privy to that information). So this
        id is necessary to know if an org that someone has rights to via their rights to the unit
        level org may not have rights to the presidency sub-org.
     */
    int orgTypeId;

    /* Constructor(s) */
    public AuthorizableOrg(long unitNum, UnitLevelOrgType unitLevelOrgType) {
        super(unitNum);
        this.orgType = unitLevelOrgType;
        this.orgTypeId = unitLevelOrgType.getOrgTypeId();
    }

    public AuthorizableOrg(long unitNum, UnitLevelOrgType unitLevelOrgType, int orgTypeId) {
        super(unitNum);
        this.orgType = unitLevelOrgType;
        this.orgTypeId = orgTypeId;
    }

    /* Properties */
    public UnitLevelOrgType getOrgType() {
        return orgType;
    }

    public void setUnitLevelOrgType(UnitLevelOrgType unitLevelOrgType) {
        this.orgType = orgType;
    }

    public int getOrgTypeId() {
        return orgTypeId;
    }

    public void setOrgTypeId(int orgTypeId) {
        this.orgTypeId = orgTypeId;
    }
}