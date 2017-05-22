package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.constants.Operation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents an auxiliary.  i.e. primary, R.S. or E.Q.
 */
public class Org {
    /* Fields */
    long subOrgId;
    String defaultOrgName;
    int orgTypeId;
    int displayOrder;
    List<Org> children;
    List<Calling> callings;
    private transient HashSet<Position> positions;
    private transient List<Long> callingIds;
    private transient ConflictCause conflictCause;
    private transient boolean hasUnsavedChanges = false;

    /* Constructors */
    public Org() {}

    public Org(long subOrgId, String defaultOrgName, int orgTypeId, int displayOrder, List<Org> children, List<Calling> callings) {
        this.subOrgId = subOrgId;
        this.defaultOrgName = defaultOrgName;
        this.orgTypeId = orgTypeId;
        this.displayOrder = displayOrder;
        this.children = children;
        this.callings = callings;
        for(Calling calling : callings) {
            if(this.positions == null) {
                this.positions = new HashSet<>();
            }
            this.positions.add(calling.getPosition());
        }
    }

    /* Properties */
    public long getId() { return subOrgId; }
    public void setId(long id) { this.subOrgId = subOrgId; }

    public String getDefaultOrgName() { return defaultOrgName; }
    public void setDefaultOrgName(String defaultOrgName) { this.defaultOrgName = defaultOrgName; }

    public int getOrgTypeId() { return orgTypeId; }
    public void setOrgTypeId(int orgTypeId) { this.orgTypeId = orgTypeId; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<Org> getChildren() { return children; }
    public void setChildren(List<Org> children) { this.children = children; }

    public List<Calling> getCallings() { return callings; }
    public void setCallings(List<Calling> callings) { this.callings = callings; }

    public ConflictCause getConflictCause() { return conflictCause; }
    public void setConflictCause(ConflictCause conflictCause) { this.conflictCause = conflictCause; }

    public boolean hasUnsavedChanges() { return hasUnsavedChanges; }
    public void setHasUnsavedChanges(boolean hasUnsavedChanges) { this.hasUnsavedChanges = hasUnsavedChanges; }

    public List<Long> allOrgCallingIds() {
        if(callingIds == null || callingIds.size() == 0) {
            callingIds = new ArrayList<>(allOrgCallings().size());
            for(Calling calling : allOrgCallings()) {
                callingIds.add(calling.getId());
            }
        }
        return callingIds;
    }

    public List<Calling> allOrgCallings() {
        List<Calling> callings = this.callings;
        for (Org org : this.children) {
            callings.addAll(org.allOrgCallings());
        }
        return callings;
    }

    public List<Org> allSubOrgs() {
        List<Org> subOrgs = this.children;
        for(Org org : this.children) {
            subOrgs.addAll(org.allSubOrgs());
        }
        return subOrgs;
    }

    public Calling find(Long param) {
        for(Calling calling : allOrgCallings()) {
            if(calling.equals(param)) {
                return calling;
            }
        }
        return null;
    }

    public HashSet<Position> getPositions() {
        return this.positions;
    }
    public void setPositions(Position position) {
        if(position != null) {
            this.positions.add(position);
        }
    }

    public List<Position> potentialNewPositions() {
        List<Integer> existingPositionTypeIds = new ArrayList<>();
        for(Calling calling : callings) {
            existingPositionTypeIds.add(calling.getPosition().getPositionTypeId());
        }
        List<Position> potentialPositions = new ArrayList<>();
        for(Position position : this.positions) {
            if(position.getAllowMultiple() || !existingPositionTypeIds.contains(position.getPositionTypeId())) {
                potentialPositions.add(position);
            }
        }
        return potentialPositions;
    }

    /* Methods */
    public boolean equals(Org org) {
        return this.subOrgId == org.subOrgId;
    }

    private Org updateWithCallingChange(Calling updatedCalling, Operation operation) {
        if(updatedCalling != null && operation != null) {
            for(Calling calling : allOrgCallings()) {
                if(calling.equals(updatedCalling)) {

                    switch(operation) {
                        case UPDATE :
                            break;
                        case CREATE :
                            break;
                        case DELETE :
                            break;
                    }
                    break;
                }
            }
        }
        return null;
    }
}