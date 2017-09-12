package org.ldscd.callingworkflow.model.permissions;

import org.ldscd.callingworkflow.model.permissions.constants.Permission;

/**
 * Interface for object permission resolver.  Objects implementation could include Unit, Org etc.
 */
public interface PermissionResolver {
    boolean isAuthorized(UnitRole unitRole, Authorizable authorizable);
}