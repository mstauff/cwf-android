package org.ldscd.callingworkflow.model.permissions;

/**
 * Interface for object permission resolver.  Objects implementation could include Unit, Org etc.
 */
public interface PermissionResolver {
    boolean isAuthorized(UnitRole unitRole, Authorizable authorizable);
}