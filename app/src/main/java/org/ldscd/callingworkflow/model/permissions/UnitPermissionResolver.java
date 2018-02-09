package org.ldscd.callingworkflow.model.permissions;

/**
 * Represents a Unit object and resolves the right and permissions for this Unit.
 */
public class UnitPermissionResolver implements PermissionResolver {
    public boolean isAuthorized(UnitRole unitRole, Authorizable authorizable) {
        boolean hasPermission;
        /* This check is for bishopric members viewing their own Bishopric */
        if(authorizable instanceof AuthorizableOrg) {
            AuthorizableOrg authorizableOrg = (AuthorizableOrg) authorizable;
            if(authorizableOrg.getOrgTypeId() == unitRole.getOrgRightsException()) {
                hasPermission = false;
            } else {
                hasPermission = true;
            }
        } else {
            /* All other unit admins will fall into this category */
            hasPermission = unitRole.getUnitNum().equals(authorizable.getUnitNum());
        }

        return hasPermission;
    }
}