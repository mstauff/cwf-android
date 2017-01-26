package org.ldscd.callingworkflow.constants;

import java.util.HashMap;
import java.util.Map;

public enum UnitLevelOrgType {
    Bishopric(1179),
    BranchPresidency(-1),
    HighPriests(69),
    Elders(70),
    ReliefSociety(74),
    YoungMen(73),
    YoungWomen(76),
    SundaySchool(75),
    Primary(77),
    WardMissionaries(1310),
    Other(1185);

    private final int id;
    private UnitLevelOrgType(int id) {
        this.id = id;
    }
    int getValue() { return id; }
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
