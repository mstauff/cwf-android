package org.ldscd.callingworkflow.model.permissions;

/**
 * Represents a Unit object and resolves the right and permissions for this Unit.
 */
public class UnitPermissionResolver implements PermissionResolver {
    public boolean isAuthorized(UnitRole unitRole, Authorizable authorizable) {
        return unitRole.getUnitNum().equals(authorizable.getUnitNum());
    }
}