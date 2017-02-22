package org.ldscd.callingworkflow.constants;

import java.util.HashMap;
import java.util.Map;

public enum UnitLevelOrgType {
    BISHOPRIC(1179, "BISHOPRIC"),
    BRANCH_PRESIDENCY(-1, "BRANCH_PRES"),
    HIGH_PRIESTS(69, "HP"),
    ELDERS(70, "EQ"),
    RELIEF_SOCIETY(74, "RS"),
    YOUNG_MEN(73, "YM"),
    YOUNG_WOMEN(76, "YW"),
    SUNDAY_SCHOOL(75, "SS"),
    PRIMARY(77, "PRIMARY"),
    WARD_MISSIONARIES(1310, "WARD_MISSIONARY"),
    OTHERS(1185, "OTHER");

    private final int id;
    private final String name;

    UnitLevelOrgType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    int getValue() { return id; }
    String getName() { return name; }

    private static final Map<Integer, UnitLevelOrgType> lookup = new HashMap<Integer, UnitLevelOrgType>();

    static {
        for (UnitLevelOrgType orgType : UnitLevelOrgType.values()) {
            lookup.put(orgType.getValue(), orgType);
        }
    }

    public static UnitLevelOrgType get(int id) {
        return lookup.get(id);
    }
}
