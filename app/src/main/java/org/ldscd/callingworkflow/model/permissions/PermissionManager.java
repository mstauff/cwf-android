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

    public List<UnitRole> createUnitRole(List<Position> positions,  Long unitNum) {
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
    /*
    func createUserRoles( forPositions positions: [Position], inUnit unitNum: Int64 ) -> [UnitRole] {
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
     */
}