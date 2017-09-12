package org.ldscd.callingworkflow.model.permissions.constants;

import org.ldscd.callingworkflow.constants.UnitLevelOrgType;

import java.util.HashMap;
import java.util.Map;

/**
 * PositionType with id.
 */
public enum PositionType {
    STAKE_PRESIDENT(1, UnitLevelOrgType.STAKE_PRESIDENCY),
    STAKE_PRESIDENCY_FIRST_COUNSELOR(2, UnitLevelOrgType.STAKE_PRESIDENCY),
    STAKE_PRESIDENCY_SECOND_COUNSELOR(3, UnitLevelOrgType.STAKE_PRESIDENCY),
    STAKE_HIGH_COUNSELOR(94, UnitLevelOrgType.STAKE_PRESIDENCY),
    STAKE_EXECUTIVE_SECRETARY(51, UnitLevelOrgType.STAKE_PRESIDENCY),
    STAKE_CLERK(52, UnitLevelOrgType.STAKE_PRESIDENCY),

    //TODO: NEED TO GET POSITIONTYPEID'S
    /*DISTRICT_PRESIDENT,
    DISTRICT_PRESIDENCY_FIRST_COUNSELOR,
    DISTRICT_PRESIDENCY_SECOND_COUNSELOR,
    DISTRICT_CLERK,
    DISTRICT_EXECUTIVE_SECRETARY,*/

    BISHOP(4, UnitLevelOrgType.BISHOPRIC),
    BISHOPRIC_FIRST_COUNSELOR(54, UnitLevelOrgType.BISHOPRIC),
    BISHOPRIC_SECOND_COUNSELOR(55, UnitLevelOrgType.BISHOPRIC),
    WARD_EXECUTIVE_SECRETARY(56, UnitLevelOrgType.BISHOPRIC),
    WARD_CLERK(57, UnitLevelOrgType.BISHOPRIC),
    //    WardAsstClerk(58

    BRANCH_PRESIDENT(12, UnitLevelOrgType.BRANCH_PRESIDENCY),
    BRANCH_FIRST_COUNSELOR(59, UnitLevelOrgType.BRANCH_PRESIDENCY),
    BRANCH_SECOND_COUNSELOR(60, UnitLevelOrgType.BRANCH_PRESIDENCY),
    BRANCH_CLERK(789, UnitLevelOrgType.BRANCH_PRESIDENCY),
    BRANCH_EXECUTIVE_SECRETARY(1278, UnitLevelOrgType.BRANCH_PRESIDENCY),
    // BranchAsstClerk(790

    HIGH_PRIEST_GROUP_LEADER(133, UnitLevelOrgType.HIGH_PRIESTS),
    HIGH_PRIEST_FIRST_ASSISTANT(134, UnitLevelOrgType.HIGH_PRIESTS),
    HIGH_PRIEST_SECOND_ASSISTANT(135, UnitLevelOrgType.HIGH_PRIESTS),
    HIGH_PRIEST_SECRETARY(136, UnitLevelOrgType.HIGH_PRIESTS),
    //    HPAsstSecretary(???

    ELDERS_QUORUM_PRESIDENT(138, UnitLevelOrgType.ELDERS),
    ELDERS_QUORUM_FIRST_COUNSELOR(139, UnitLevelOrgType.ELDERS),
    ELDERS_QUORUM_SECOND_COUNSELOR(140, UnitLevelOrgType.ELDERS),
    ELDERS_QUORUM_SECRETARY(141, UnitLevelOrgType.ELDERS),
    // EQAsstSecretary(???

    RELIEF_SOCIETY_PRESIDENT(143, UnitLevelOrgType.RELIEF_SOCIETY),
    RELIEF_SOCIETY_FIRST_COUNSELOR(144, UnitLevelOrgType.RELIEF_SOCIETY),
    RELIEF_SOCIETY_SECOND_COUNSELOR(145, UnitLevelOrgType.RELIEF_SOCIETY),
    RELIEF_SOCIETY_SECRETARY(146, UnitLevelOrgType.RELIEF_SOCIETY),
    // RSAsstSec(???

    YOUNG_MENS_PRESIDENT(158, UnitLevelOrgType.YOUNG_MEN),
    YOUNG_MENS_FIRST_COUNSELOR(159, UnitLevelOrgType.YOUNG_MEN),
    YOUNG_MENS_SECOND_COUNSELOR(160, UnitLevelOrgType.YOUNG_MEN),
    YOUNG_MENS_SECRETARY(161, UnitLevelOrgType.YOUNG_MEN),

    YOUNG_WOMENS_PRESIDENT(183, UnitLevelOrgType.YOUNG_WOMEN),
    YOUNG_WOMENS_FIRST_COUNSELOR(184, UnitLevelOrgType.YOUNG_WOMEN),
    YOUNG_WOMENS_SECOND_COUNSELOR(185, UnitLevelOrgType.YOUNG_WOMEN),
    YOUNG_WOMENS_SECRETARY(186, UnitLevelOrgType.YOUNG_WOMEN),

    SUNDAY_SCHOOL_PRESIDENT(204, UnitLevelOrgType.SUNDAY_SCHOOL),
    SUNDAY_SCHOOL_FIRST_COUNSELOR(205, UnitLevelOrgType.SUNDAY_SCHOOL),
    SUNDAY_SCHOOL_SECOND_COUNSELOR(206, UnitLevelOrgType.SUNDAY_SCHOOL),
    SUNDAY_SCHOOL_SECRETARY(207, UnitLevelOrgType.SUNDAY_SCHOOL),

    PRIMARY_PRESIDENT(210, UnitLevelOrgType.PRIMARY),
    PRIMARY_FIRST_COUNSELOR(211, UnitLevelOrgType.PRIMARY),
    PRIMARY_SECOND_COUNSELOR(212, UnitLevelOrgType.PRIMARY),
    PRIMARY_SECRETARY(213, UnitLevelOrgType.PRIMARY);

    /* Fields */
    private int positionId;
    private UnitLevelOrgType orgType;
    private static final Map<Integer, PositionType> positionTypeLookup = new HashMap<Integer, PositionType>();
    private static final Map<Integer, UnitLevelOrgType> orgTypeLookup = new HashMap<Integer, UnitLevelOrgType>();

    /* Constructor */
    PositionType(int positionId, UnitLevelOrgType orgType) {
        this.positionId = positionId;
        this.orgType = orgType;
    }

    /* Properties */
    static {
        for (PositionType positionType : PositionType.values()) {
            positionTypeLookup.put(positionType.positionId, positionType);
            orgTypeLookup.put(positionType.positionId, positionType.orgType);
        }
    }

    public static PositionType get(Integer positionId) {
        return positionTypeLookup.get(positionId);
    }

    public static UnitLevelOrgType getOrgType(Integer positionId) { return orgTypeLookup.get(positionId); }
}