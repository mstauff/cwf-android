package org.ldscd.callingworkflow.constants;

import java.util.HashMap;
import java.util.Map;

/* Specific to file naming only and has a string value for the name. */
public enum UnitLevelOrgType {
    STAKE_PRESIDENCY(-1, -1, "STAKE_PRESIDENCY"),
    HIGH_COUNCIL (-1, -1, "HIGH_COUNCIL"),
    BISHOPRIC(1179, 1179, "BISHOPRIC"),
    BRANCH_PRESIDENCY(-1, -1, "BRANCH_PRES"),
    HIGH_PRIESTS(69, 1299, "HP"),
    ELDERS(70, 1295, "EQ"),
    RELIEF_SOCIETY(74, 1279, "RS"),
    YOUNG_MEN(73, 1311, "YM"),
    YOUNG_WOMEN(76, 1312, "YW"),
    SUNDAY_SCHOOL(75, 1308, "SS"),
    PRIMARY(77, 1303, "PRIMARY"),
    WARD_MISSIONARIES(1310, -1, "WARD_MISSIONARY"),
    OTHERS(1185, -1, "OTHER");

    private final int id;
    private Integer exceptionId;
    private final String name;

    UnitLevelOrgType(int id, int exceptionId, String name) {
        this.id = id;
        this.exceptionId = exceptionId;
        this.name = name;
    }

    public int getOrgTypeId() { return id; }
    public String getOrgName() { return name; }

    private static final Map<Integer, UnitLevelOrgType> lookup = new HashMap<Integer, UnitLevelOrgType>();

    static {
        for (UnitLevelOrgType orgType : UnitLevelOrgType.values()) {
            lookup.put(orgType.getOrgTypeId(), orgType);
        }
    }

    public static UnitLevelOrgType get(int id) {
        return lookup.get(id);
    }

    public static int getOrgTypeIdByName(String name) {
        int orgTypeId = 0;
        for(UnitLevelOrgType orgType : UnitLevelOrgType.values()) {
            if(orgType.name.equals(name)) {
                orgTypeId = orgType.id;
                break;
            }
        }
        return orgTypeId;
    }

    public Integer getExceptionId() {
        return this.exceptionId;
    }
}
