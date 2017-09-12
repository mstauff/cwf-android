package org.ldscd.callingworkflow.model.permissions;

/**
 * Represents and Org and offer resolution on permissions and rights.
 */
public class OrgPermissionResolver implements PermissionResolver {
    public boolean isAuthorized(UnitRole unitRole, Authorizable authorizable) {
        AuthorizableOrg authorizableOrg = null;
        if(authorizable instanceof AuthorizableOrg) {
            authorizableOrg = (AuthorizableOrg)authorizable;
        } else if(authorizable instanceof AuthorizableCalling) {
            authorizableOrg = ((AuthorizableCalling) authorizable).getAuthorizableOrg();
        }

        boolean result = false;

        if(authorizableOrg != null && authorizableOrg.getOrgTypeId() != unitRole.getOrgRightsException()) {
            result = authorizableOrg.getUnitNum() == unitRole.getUnitNum() && authorizableOrg.getOrgType() == unitRole.getOrgType();
        }
        return result;
    }
}