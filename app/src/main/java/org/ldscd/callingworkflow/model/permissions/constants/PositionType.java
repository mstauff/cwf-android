package org.ldscd.callingworkflow.model.permissions.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * PositionType with id.
 */
public enum PositionType {
    STAKE_PRESIDENT(1 , OrgType.STAKE_PRESIDENCY),
    STAKE_PRESIDENCY_FIRST_COUNSELOR(2, OrgType.STAKE_PRESIDENCY),
    STAKE_PRESIDENCY_SECOND_COUNSELOR(3, OrgType.STAKE_PRESIDENCY),
    STAKE_HIGH_COUNSELOR(94, OrgType.STAKE_PRESIDENCY),
    STAKE_EXECUTIVE_SECRETARY(51, OrgType.STAKE_PRESIDENCY),
    STAKE_CLERK(52, OrgType.STAKE_PRESIDENCY),

    //TODO: NEED TO GET POSITIONTYPEID'S
    /*DISTRICT_PRESIDENT,
    DISTRICT_PRESIDENCY_FIRST_COUNSELOR,
    DISTRICT_PRESIDENCY_SECOND_COUNSELOR,
    DISTRICT_CLERK,
    DISTRICT_EXECUTIVE_SECRETARY,*/

    BISHOP(4, OrgType.BISHOPRIC),
    BISHOPRIC_FIRST_COUNSELOR(54, OrgType.BISHOPRIC),
    BISHOPRIC_SECOND_COUNSELOR(55, OrgType.BISHOPRIC),
    WARD_EXECUTIVE_SECRETARY(56, OrgType.BISHOPRIC),
    WARD_CLERK(57, OrgType.BISHOPRIC),
    //    WardAsstClerk(58

    BRANCH_PRESIDENT(12, OrgType.BRANCH_PRESIDENCY),
    BRANCH_FIRST_COUNSELOR(59, OrgType.BRANCH_PRESIDENCY),
    BRANCH_SECOND_COUNSELOR(60, OrgType.BRANCH_PRESIDENCY),
    BRANCH_CLERK(789, OrgType.BRANCH_PRESIDENCY),
    BRANCH_EXECUTIVE_SECRETARY(1278, OrgType.BRANCH_PRESIDENCY),
    // BranchAsstClerk(790

    HIGH_PRIEST_GROUP_LEADER(133, OrgType.HIGH_PRIESTS),
    HIGH_PRIEST_FIRST_ASSISTANT(134, OrgType.HIGH_PRIESTS),
    HIGH_PRIEST_SECOND_ASSISTANT(135, OrgType.HIGH_PRIESTS),
    HIGH_PRIEST_SECRETARY(136, OrgType.HIGH_PRIESTS),
    //    HPAsstSecretary(???

    ELDERS_QUORUM_PRESIDENT(138, OrgType.ELDERS),
    ELDERS_QUORUM_FIRST_COUNSELOR(139, OrgType.ELDERS),
    ELDERS_QUORUM_SECOND_COUNSELOR(140, OrgType.ELDERS),
    ELDERS_QUORUM_SECRETARY(141, OrgType.ELDERS),
    // EQAsstSecretary(???

    RELIEF_SOCIETY_PRESIDENT(143, OrgType.RELIEF_SOCIETY),
    RELIEF_SOCIETY_FIRST_COUNSELOR(144, OrgType.RELIEF_SOCIETY),
    RELIEF_SOCIETY_SECOND_COUNSELOR(145, OrgType.RELIEF_SOCIETY),
    RELIEF_SOCIETY_SECRETARY(146, OrgType.RELIEF_SOCIETY),
    // RSAsstSec(???

    YOUNG_MENS_PRESIDENT(158, OrgType.YOUNG_MEN),
    YOUNG_MENS_FIRST_COUNSELOR(159, OrgType.YOUNG_MEN),
    YOUNG_MENS_SECOND_COUNSELOR(160, OrgType.YOUNG_MEN),
    YOUNG_MENS_SECRETARY(161, OrgType.YOUNG_MEN),

    YOUNG_WOMENS_PRESIDENT(183, OrgType.YOUNG_WOMEN),
    YOUNG_WOMENS_FIRST_COUNSELOR(184, OrgType.YOUNG_WOMEN),
    YOUNG_WOMENS_SECOND_COUNSELOR(185, OrgType.YOUNG_WOMEN),
    YOUNG_WOMENS_SECRETARY(186, OrgType.YOUNG_WOMEN),

    SUNDAY_SCHOOL_PRESIDENT(204, OrgType.SUNDAY_SCHOOL),
    SUNDAY_SCHOOL_FIRST_COUNSELOR(205, OrgType.SUNDAY_SCHOOL),
    SUNDAY_SCHOOL_SECOND_COUNSELOR(206, OrgType.SUNDAY_SCHOOL),
    SUNDAY_SCHOOL_SECRETARY(207, OrgType.SUNDAY_SCHOOL),

    PRIMARY_PRESIDENT(210, OrgType.PRIMARY),
    PRIMARY_FIRST_COUNSELOR(211, OrgType.PRIMARY),
    PRIMARY_SECOND_COUNSELOR(212, OrgType.PRIMARY),
    PRIMARY_SECRETARY(213, OrgType.PRIMARY);

    /* Fields */
    private int positionId;
    private OrgType orgType;
    private static final Map<Integer, PositionType> positionTypeLookup = new HashMap<Integer, PositionType>();
    private static final Map<Integer, OrgType> orgTypeLookup = new HashMap<Integer, OrgType>();

    /* Constructor */
    PositionType(int positionId, OrgType orgType) {
        this.positionId = positionId;
        this.orgType = orgType;
    }

    /* Properties */
    static {
        for (PositionType positionType : PositionType.values()) {
            positionTypeLookup.put(positionType.positionId, positionType);
        }
    }

    static {
        for (PositionType positionType : PositionType.values()) {
            orgTypeLookup.put(positionType.positionId, positionType.orgType);
        }
    }

    public static PositionType get(Integer positionId) {
        return positionTypeLookup.get(positionId);
    }

    public static OrgType getOrgType(Integer positionId) { return orgTypeLookup.get(positionId); }
}