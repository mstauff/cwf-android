package org.ldscd.callingworkflow.model.permissions.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.ldscd.callingworkflow.model.permissions.constants.PositionType.*;

/**
 * Role representation
 */
public enum Role {
    /* Enums */
    UNIT_ADMIN (new Permission[] {Permission.ORG_INFO_READ, Permission.POTENTIAL_CALLING_CREATE,
                                  Permission.POTENTIAL_CALLING_DELETE, Permission.POTENTIAL_CALLING_UPDATE,
                                  Permission.ACTIVE_CALLING_UPDATE, Permission.ACTIVE_CALLING_RELEASE,
                                  Permission.ACTIVE_CALLING_DELETE, Permission.PRIESTHOOD_OFFICE_READ,
                                  Permission.ACTIVE_CALLING_CREATE, Permission.UNIT_GOOGLE_ACCOUNT_CREATE,
                                  Permission.UNIT_GOOGLE_ACCOUNT_UPDATE},
        /* Stake */
            STAKE_PRESIDENT,
            STAKE_PRESIDENCY_FIRST_COUNSELOR,
            STAKE_PRESIDENCY_SECOND_COUNSELOR,
            STAKE_EXECUTIVE_SECRETARY,
            STAKE_CLERK,
        /* Bishopric */
            BISHOP,
            BISHOPRIC_FIRST_COUNSELOR,
            BISHOPRIC_SECOND_COUNSELOR,
            WARD_EXECUTIVE_SECRETARY,
            WARD_CLERK,
        /* Branch Presidency */
            BRANCH_PRESIDENT,
            BRANCH_FIRST_COUNSELOR,
            BRANCH_SECOND_COUNSELOR,
            BRANCH_EXECUTIVE_SECRETARY,
            BRANCH_CLERK
    ),
    PRIESTHOOD_ORG_ADMIN (new Permission[] {Permission.ORG_INFO_READ, Permission.POTENTIAL_CALLING_CREATE,
                                            Permission.POTENTIAL_CALLING_DELETE, Permission.POTENTIAL_CALLING_UPDATE,
                                            Permission.ACTIVE_CALLING_CREATE, Permission.ACTIVE_CALLING_UPDATE,
                                            Permission.ACTIVE_CALLING_RELEASE, Permission.ACTIVE_CALLING_DELETE,
                                            Permission.PRIESTHOOD_OFFICE_READ },
        /* High Priest */
            HIGH_PRIEST_GROUP_LEADER,
            HIGH_PRIEST_FIRST_ASSISTANT,
            HIGH_PRIEST_SECOND_ASSISTANT,
            HIGH_PRIEST_SECRETARY,
        /* Elders Quorum */
            ELDERS_QUORUM_PRESIDENT,
            ELDERS_QUORUM_FIRST_COUNSELOR,
            ELDERS_QUORUM_SECOND_COUNSELOR,
            ELDERS_QUORUM_SECRETARY,
        /* Young Mens */
            YOUNG_MENS_PRESIDENT,
            YOUNG_MENS_FIRST_COUNSELOR,
            YOUNG_MENS_SECOND_COUNSELOR,
            YOUNG_MENS_SECRETARY
    ),

    ORG_ADMIN (new Permission[] { Permission.ORG_INFO_READ, Permission.POTENTIAL_CALLING_CREATE,
                                  Permission.POTENTIAL_CALLING_DELETE, Permission.POTENTIAL_CALLING_UPDATE,
                                  Permission.ACTIVE_CALLING_UPDATE, Permission.ACTIVE_CALLING_RELEASE,
                                  Permission.ACTIVE_CALLING_CREATE, Permission.ACTIVE_CALLING_DELETE },
        /* Relief Society */
            RELIEF_SOCIETY_PRESIDENT,
            RELIEF_SOCIETY_FIRST_COUNSELOR,
            RELIEF_SOCIETY_SECOND_COUNSELOR,
            RELIEF_SOCIETY_SECRETARY,
        /* Young Womens */
            YOUNG_WOMENS_PRESIDENT,
            YOUNG_WOMENS_FIRST_COUNSELOR,
            YOUNG_WOMENS_SECOND_COUNSELOR,
            YOUNG_WOMENS_SECRETARY,
        /* Sunday School */
            SUNDAY_SCHOOL_PRESIDENT,
            SUNDAY_SCHOOL_FIRST_COUNSELOR,
            SUNDAY_SCHOOL_SECOND_COUNSELOR,
            SUNDAY_SCHOOL_SECRETARY,
        /* Primary */
            PRIMARY_PRESIDENT,
            PRIMARY_FIRST_COUNSELOR,
            PRIMARY_SECOND_COUNSELOR,
            PRIMARY_SECRETARY
    ),

    STAKE_ASSISTANT (new Permission[] { Permission.ORG_INFO_READ, Permission.POTENTIAL_CALLING_CREATE,
                                        Permission.POTENTIAL_CALLING_DELETE, Permission.POTENTIAL_CALLING_UPDATE },
            STAKE_HIGH_COUNSELOR
    );

    /* Fields */
    private List<Permission> permissions;
    private List<PositionType> positionTypes;

    /* Constructor */
    Role(Permission[] permissions, PositionType... positionTypes) {
        this.permissions = Collections.unmodifiableList(Arrays.asList(permissions));
        this.positionTypes = Collections.unmodifiableList(Arrays.asList(positionTypes));
    }

    /* Methods */
    public List<Permission> getPermissions() {
        return this.permissions;
    }

    public static Role getRoleByPositionType(PositionType position) {
        for(Role role : Role.values()) {
            if (role.positionTypes.contains(position)) {
                return role;
            }
        }
        return null;
    }
};