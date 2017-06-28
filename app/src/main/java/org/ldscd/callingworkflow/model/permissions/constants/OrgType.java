package org.ldscd.callingworkflow.model.permissions.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ldscd.callingworkflow.model.permissions.constants.PositionType.*;

/**
 * OrgType and grouped PositionType values.
 */
public enum OrgType {
    STAKE_PRESIDENCY (-1,
        STAKE_PRESIDENT,
        STAKE_PRESIDENCY_FIRST_COUNSELOR,
        STAKE_PRESIDENCY_SECOND_COUNSELOR,
        STAKE_EXECUTIVE_SECRETARY,
        STAKE_CLERK
    ),
    HIGH_COUNCIL (-1,
        STAKE_HIGH_COUNSELOR
    ),
    BISHOPRIC (1179,
        BISHOP,
        BISHOPRIC_FIRST_COUNSELOR,
        BISHOPRIC_SECOND_COUNSELOR,
        WARD_EXECUTIVE_SECRETARY,
        WARD_CLERK
    ),
    BRANCH_PRESIDENCY (-1,
        BRANCH_PRESIDENT,
        BRANCH_FIRST_COUNSELOR,
        BRANCH_SECOND_COUNSELOR,
        BRANCH_EXECUTIVE_SECRETARY,
        BRANCH_CLERK
    ),
    HIGH_PRIESTS (69,
        HIGH_PRIEST_GROUP_LEADER,
        HIGH_PRIEST_FIRST_ASSISTANT,
        HIGH_PRIEST_SECOND_ASSISTANT,
        HIGH_PRIEST_SECRETARY
    ),
    ELDERS (70,
        ELDERS_QUORUM_PRESIDENT,
        ELDERS_QUORUM_FIRST_COUNSELOR,
        ELDERS_QUORUM_SECOND_COUNSELOR,
        ELDERS_QUORUM_SECRETARY
    ),
    YOUNG_MEN (73,
        YOUNG_MENS_PRESIDENT,
        YOUNG_MENS_FIRST_COUNSELOR,
        YOUNG_MENS_SECOND_COUNSELOR,
        YOUNG_MENS_SECRETARY
    ),
    RELIEF_SOCIETY (74,
        RELIEF_SOCIETY_PRESIDENT,
        RELIEF_SOCIETY_FIRST_COUNSELOR,
        RELIEF_SOCIETY_SECOND_COUNSELOR,
        RELIEF_SOCIETY_SECRETARY
    ),
    YOUNG_WOMEN (76,
        YOUNG_WOMENS_PRESIDENT,
        YOUNG_WOMENS_FIRST_COUNSELOR,
        YOUNG_WOMENS_SECOND_COUNSELOR,
        YOUNG_WOMENS_SECRETARY
    ),
    SUNDAY_SCHOOL (75,
        SUNDAY_SCHOOL_PRESIDENT,
        SUNDAY_SCHOOL_FIRST_COUNSELOR,
        SUNDAY_SCHOOL_SECOND_COUNSELOR,
        SUNDAY_SCHOOL_SECRETARY
    ),
    PRIMARY (77,
        PRIMARY_PRESIDENT,
        PRIMARY_FIRST_COUNSELOR,
        PRIMARY_SECOND_COUNSELOR,
        PRIMARY_SECRETARY
    );

    /* Fields */
    private List<PositionType> positionTypes;
    private Integer orgId;
    /* Constructor */
    private OrgType(Integer orgId, PositionType... orgTypes) {
        this.positionTypes = new ArrayList<>(Arrays.asList(orgTypes));
        this.orgId = orgId;
    }
    /* Properties */
    public List<PositionType> getPositionTypes() {
        return positionTypes;
    }
    /* Methods */
}