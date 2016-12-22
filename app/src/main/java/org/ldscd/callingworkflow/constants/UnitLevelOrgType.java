package org.ldscd.callingworkflow.constants;

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
    public int getValue() { return id; }
}
