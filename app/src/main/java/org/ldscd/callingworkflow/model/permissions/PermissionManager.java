package org.ldscd.callingworkflow.model.permissions;

import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.model.permissions.constants.OrgType;
import org.ldscd.callingworkflow.model.permissions.constants.PositionType;
import org.ldscd.callingworkflow.model.permissions.constants.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class PermissionManager {

    /* Fields */
    List<OrgType> orgTypeExceptions = Arrays.asList(OrgType.HIGH_PRIESTS,
            OrgType.ELDERS, OrgType.RELIEF_SOCIETY, OrgType.YOUNG_MEN,
            OrgType.YOUNG_WOMEN, OrgType.SUNDAY_SCHOOL, OrgType.PRIMARY);

    /* Constructor */

    public List<UnitRole> createUnitRoles(List<Position> positions,  Long unitNum) {
        List<UnitRole> unitRoles = new ArrayList<>();
        List<Position> unitPositions = new ArrayList<>();
        boolean isUnitAdmin = false;
        for(Position position : positions) {
            if(position.getUnitNumber() == unitNum) {
                PositionType positionType = PositionType.get(position.getPositionTypeId());
                Role role = Role.getRole(positionType);
                if(positionType != null) {
                    Integer orgRightsException = null; //orgTypeExceptions.contains(positionType) ? positionType : null;
                    UnitRole unitRole = new UnitRole(role, unitNum, null, positionType.getOrgType(position.getPositionTypeId()), position, orgRightsException);
                    if(unitRole.getRole().equals(Role.UNIT_ADMIN)) {
                        isUnitAdmin = true;
                    }
                    unitRoles.add(unitRole);
                }
            }
            if(isUnitAdmin) {
                break;
            }
        }


        return unitRoles;
    }

    /*func authorizedUnits( forUser user: LdsUser ) -> [Int64] {
        let allUnitNums = user.positions.filter() { PositionType(rawValue: $0.positionTypeId) != nil }.flatMap() {$0.unitNum}
        return Array( Set(allUnitNums) )
    }*/
  /*  func createUserRoles( forPositions positions: [Position], inUnit unitNum: Int64 ) -> [UnitRole] {
        var unitRoles : [UnitRole] = []
        // filter out any positions that are in a different unit
        let unitPositions = positions.filter() {
            $0.unitNum == unitNum
        }

        // get all the roles for the positions
        for currPosition in unitPositions  {

            if let positionType = PositionType(rawValue: currPosition.positionTypeId), let role = Role.getRole(forPositionType: positionType) {
                let orgType = positionType.orgType
                let orgRightsException : Int? = orgType == nil ? nil : PermissionManager.unitLevelOrgExceptions[orgType!]

                let unitRole = UnitRole(role: role, unitNum: currPosition.unitNum!, orgId: nil, orgType: orgType, activePosition: currPosition, orgRightsException:  orgRightsException)

                // if they have a unit admin position, then that overrides anything else, set it and we break out
                if unitRole.role.type == .UnitAdmin {
                    unitRoles = [unitRole]
                    break;
                } else {
                    unitRoles.append(unitRole)
                }
            }
        }
        return unitRoles
    }

    *//** returns a list of all the units that has a supported calling in. This is for startup purposes in trying to determine whether there's a single unit, or multiple units that would require prompting a user to choose *//*
    func authorizedUnits( forUser user: LdsUser ) -> [Int64] {
        let allUnitNums = user.positions.filter() { PositionType(rawValue: $0.positionTypeId) != nil }.flatMap() {$0.unitNum}
        return Array( Set(allUnitNums) )
    }

    *//** returns true if the role has the permission for the domain object, false otherwise. *//*
    public func hasPermission( unitRole: UnitRole, domain: Domain, permission: Permission ) -> Bool {
        // have to compare to true, because it could return nil as well as t/f
        return unitRole.role.permissions[domain]?.contains( permission ) == true
    }

    *//** Checks to see if one of the roles in the list has the specified permission *//*
    public func hasPermission( unitRoles: [UnitRole], domain: Domain, permission: Permission ) -> Bool {
        var hasPerm = false
        for role in unitRoles {
            hasPerm = self.hasPermission(unitRole: role, domain: domain, permission: permission)
            if hasPerm {
                break
            }
        }
        return hasPerm
    }

    *//** Determines if a user has a specified permission to perform an action on a set of data. An OrgAdmin may have permission to update a calling, but only on data within their org. *//*
    public func isAuthorized( unitRole: UnitRole, domain: Domain, permission: Permission, targetData: Authorizable ) -> Bool {
        return hasPermission(unitRole: unitRole, domain: domain, permission: permission) && permissionResolvers[unitRole.role.type]!.isAuthorized(role: unitRole, domain: domain, permission: permission, targetData: targetData)

    }

    *//** Determines if a user has a in their list of roles, permission to perform an action on a set of data *//*
    public func isAuthorized( unitRoles: [UnitRole], domain: Domain, permission: Permission, targetData: Authorizable ) -> Bool {
        var isAuthorized = false
        for role in unitRoles {
            isAuthorized = self.isAuthorized(unitRole: role, domain: domain, permission: permission, targetData: targetData)
            if isAuthorized {
                break
            }
        }
        return isAuthorized
    }
     */
}