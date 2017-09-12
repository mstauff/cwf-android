package org.ldscd.callingworkflow.model.permissions;

import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.model.permissions.constants.PositionType;
import org.ldscd.callingworkflow.model.permissions.constants.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The entry point methods for permission authorization management.
 */
public class PermissionManager {

    /* Fields */
    private static UnitPermissionResolver unitPermissionResolver = new UnitPermissionResolver();
    private static OrgPermissionResolver orgPermResolver = new OrgPermissionResolver();
    private static Map<Role, PermissionResolver> permissionResolvers = new HashMap<>();

    /* Constructor */
    public PermissionManager() {}

    static {
        permissionResolvers.put(Role.UNIT_ADMIN, unitPermissionResolver);
        permissionResolvers.put(Role.STAKE_ASSISTANT, unitPermissionResolver);
        permissionResolvers.put(Role.ORG_ADMIN, orgPermResolver);
    }

    /* Methods */
    public List<UnitRole> createUnitRoles(List<Position> positions,  Long unitNum) {
        List<UnitRole> unitRoles = new ArrayList<>();
        boolean isUnitAdmin = false;
        if(positions != null) {
            for (Position position : positions) {
                if (position.getUnitNumber() == unitNum) {
                    PositionType positionType = PositionType.get(position.getPositionTypeId());
                    Role role = Role.getRoleByPositionType(positionType);
                    if (role != null) {
                        UnitLevelOrgType orgType = PositionType.getOrgType(position.getPositionTypeId());
                        UnitRole unitRole = new UnitRole(role, unitNum, null, orgType, position);
                        if (unitRole.getRole().equals(Role.UNIT_ADMIN)) {
                            isUnitAdmin = true;
                            unitRoles = new ArrayList<UnitRole>();
                        }
                        unitRoles.add(unitRole);
                    }
                }
                if (isUnitAdmin) {
                    break;
                }
            }
        }

        return unitRoles;
    }

    public List<Permission> getPermissions(List<UnitRole> unitRoles) {
        List<Permission> permissions = new ArrayList<>();
        for(UnitRole unitRole : unitRoles) {
            if(unitRole.getRole() != null &&
               unitRole.getRole().getPermissions() != null &&
               unitRole.getRole().getPermissions().size() > 0) {
                permissions.addAll(unitRole.getRole().getPermissions());
            }
        }
        return permissions;
    }
    /* returns a list of all the units that has a supported calling in. This is for startup
        purposes in trying to determine whether there's a single unit, or multiple units that
        would require prompting a user to choose
     */
    public Set<Long> authorizedUnit(LdsUser user) {
        Set<Long> allUnitNums = new HashSet<>();
        for(Position position : user.getPositions()) {
           if(position.getPositionTypeId() > 0) {
               allUnitNums.add(position.getUnitNumber());
           }
        }
        return allUnitNums;
    }

    /* Returns true if the role has the permission for the domain object, false otherwise. */
    public boolean hasPermission(UnitRole unitRole, Permission permission) {
        return unitRole.getRole().getPermissions().contains(permission);

    }

    /* Checks to see if one of the roles in the list has the specified permission */
    public boolean hasPermission(List<UnitRole> unitRoles, Permission permission) {
        boolean hasPermission = false;
        for(UnitRole unitRole : unitRoles) {
            if(hasPermission(unitRole, permission)) {
                hasPermission = true;
                break;
            }
        }
        return hasPermission;
    }

    /* Determines if a user has a specified permission to perform an action on a set of data.
        An OrgAdmin may have permission to update a calling, but only on data within their org.
     */
    public boolean isAuthorized(UnitRole unitRole, Permission permission, Authorizable authorizable) {
        return hasPermission(unitRole, permission) &&
        permissionResolvers.get(unitRole.getRole()).isAuthorized(unitRole, authorizable);
    }

    /* Determines if a user has a in their list of roles, permission to perform an action on a set of data */
    public boolean isAuthorized(List<UnitRole> unitRoles, Permission permission, Authorizable authorizable) {
        boolean isAuthorized = false;
        for(UnitRole unitRole : unitRoles) {
            if(isAuthorized(unitRole, permission, authorizable)) {
                isAuthorized = true;
                break;
            }
        }

        return isAuthorized;
    }

    public List<Org> getAuthorizableOrgs(List<Org> lcrOrgs, LdsUser currentUser) {
        List<Org> orgs = new ArrayList<>();
        if(lcrOrgs != null && lcrOrgs.size() > 0) {
            for(Org org : lcrOrgs) {
                if(isAuthorized(currentUser.getUnitRoles(),
                                Permission.ORG_INFO_READ,
                                new AuthorizableOrg(currentUser.getUnitNumber(),
                                                    UnitLevelOrgType.get(org.getOrgTypeId())))) {
                    orgs.add(org);
                }
            }
        }
        return orgs;
    }
}