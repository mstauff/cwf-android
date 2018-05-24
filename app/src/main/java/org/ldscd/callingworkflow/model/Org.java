package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.constants.ConflictCause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents an auxiliary.  i.e. primary, R.S. or E.Q.
 */
public class Org {

    /* Fields */
    private long unitNumber;
    private long subOrgId;
    private String defaultOrgName;
    private int orgTypeId;
    private int displayOrder;
    private List<Org> children;
    private List<Calling> callings;
    private transient Map<Integer, Position> positions;
    private transient ConflictCause conflictCause;
    private transient boolean hasUnsavedChanges = false;
    private transient boolean canView = false;
    private transient List<Long> assignedMembers;

    /* Constructors */
    public Org() {}

    public Org(long unitNumber, long subOrgId, String defaultOrgName, int orgTypeId, int displayOrder, List<Org> children, List<Calling> callings) {
        this.unitNumber = unitNumber;
        this.subOrgId = subOrgId;
        this.defaultOrgName = defaultOrgName;
        this.orgTypeId = orgTypeId;
        this.displayOrder = displayOrder;
        setChildren(children);
        setCallings(callings);
    }

    /* Properties */
    public long getUnitNumber() {
        return unitNumber;
    }
    public void setUnitNumber(long unitNumber) {
        this.unitNumber = unitNumber;
    }
    
    public long getId() { return subOrgId; }
    public void setId(long id) { this.subOrgId = subOrgId; }

    public String getDefaultOrgName() { return defaultOrgName; }
    public void setDefaultOrgName(String defaultOrgName) { this.defaultOrgName = defaultOrgName; }

    public int getOrgTypeId() { return orgTypeId; }
    public void setOrgTypeId(int orgTypeId) { this.orgTypeId = orgTypeId; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<Org> getChildren() { return children; }
    public void setChildren(List<Org> children) {
        this.children = children == null ? new ArrayList<Org>() : children;
    }

    public List<Calling> getCallings() { return callings; }
    public void setCallings(List<Calling> callings) {
        this.callings = callings == null ? new ArrayList<Calling>() : callings;
        this.positions = new HashMap<>();
        for(Calling calling : this.callings) {
            this.positions.put(calling.getPosition().getPositionTypeId(), calling.getPosition());
        }
    }

    public ConflictCause getConflictCause() { return conflictCause; }
    public void setConflictCause(ConflictCause conflictCause) { this.conflictCause = conflictCause; }

    public boolean hasUnsavedChanges() { return hasUnsavedChanges; }
    public void setHasUnsavedChanges(boolean hasUnsavedChanges) { this.hasUnsavedChanges = hasUnsavedChanges; }

    public boolean getCanView() {
        return canView;
    }
    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public void setAssignedMembers(List<Long> assignedMembers) {
        this.assignedMembers = assignedMembers;
    }
    public List<Long> getAssignedMembers() {
        return this.assignedMembers;
    }

    public HashSet<Position> getPositions() {
        return new HashSet<>(this.positions.values());
    }

    public List<String> allOrgCallingIds() {
        List<Calling> allCallings = allOrgCallings();
        List<String> callingIds = new ArrayList<>(allCallings.size());
        for(Calling calling : allCallings) {
            callingIds.add(calling.getCallingId());
        }
        return callingIds;
    }

    public List<Calling> allOrgCallings() {
        List<Calling> newCallings = new ArrayList<>(this.callings);
        for (Org org : this.children) {
            newCallings.addAll(org.allOrgCallings());
        }
        return newCallings;
    }

    public Calling getCallingById(String param) {
        for(Calling calling : allOrgCallings()) {
            if(calling.getCallingId().equals(param)) {
                return calling;
            }
        }
        return null;
    }

    public boolean removeCalling(Calling param) {
        if(this.getId() == param.getParentOrg()) {
            List<Calling> callings = this.getCallings();
            for (Iterator<Calling> callingIterator = callings.iterator(); callingIterator.hasNext(); ) {
                Calling calling = callingIterator.next();
                if (calling.getCallingId().equals(param.getCallingId())) {
                    callings.remove(calling);
                    return true;
                }
            }
        }
        return false;
    }

    public List<Calling> getCallingByPositionTypeId(int positionTypeId) {
        List<Calling> callings = new ArrayList<>();
        for(Calling calling : allOrgCallings()) {
            if(calling.getPosition().getPositionTypeId() == positionTypeId) {
                callings.add(calling);
            }
        }
        return callings;
    }

    public List<Position> potentialNewPositions() {
        Set<Integer> existingPositionTypeIds = new HashSet<>();
        for(Calling calling : callings) {
            existingPositionTypeIds.add(calling.getPosition().getPositionTypeId());
        }
        List<Position> potentialPositions = new ArrayList<>();
        for(Position position : this.positions.values()) {
            if(position.getAllowMultiple() || !existingPositionTypeIds.contains(position.getPositionTypeId())) {
                potentialPositions.add(position);
            }
        }
        return potentialPositions;
    }

    /* Methods */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Org org = (Org) o;

        return subOrgId == org.subOrgId;
    }

    @Override
    public int hashCode() {
        return (int) (subOrgId ^ (subOrgId >>> 32));
    }
}