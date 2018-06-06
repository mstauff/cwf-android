package org.ldscd.callingworkflow.model.serialize;

import java.util.List;
import java.util.Map;

public class LocalCache {

    /* Fields */
    private Long memberAssignmentExpirationDate;
    private Map<Long, List<Long>> memberAssignmentMap;

    /* Constructor(s) */
    public LocalCache() {}

    /* Properties */
    public Long getMemberAssignmentExpirationDate() {
        return memberAssignmentExpirationDate;
    }
    public void setMemberAssignmentExpirationDate(Long memberAssignmentExpirationDate) {
        this.memberAssignmentExpirationDate = memberAssignmentExpirationDate;
    }

    public Map<Long, List<Long>> getMemberAssignmentMap() {
        return memberAssignmentMap;
    }
    public void setMemberAssignmentMap(Map<Long, List<Long>> memberAssignmentMap) {
        this.memberAssignmentMap = memberAssignmentMap;
    }
}