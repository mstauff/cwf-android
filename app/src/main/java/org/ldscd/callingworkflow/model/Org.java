package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.constants.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an auxiliary.  i.e. primary, R.S. or E.Q.
 */
public class Org {
    /* Fields */
    long id;
    String orgName;
    int orgTypeId;
    int displayOrder;
    List<Org> children;
    List<Calling> callings;
    private transient List<Long> callingIds;

    /* Constructors */
    public Org() {}

    public Org(long id, String orgName, int orgTypeId, int displayOrder, List<Org> children, List<Calling> callings) {
        this.id = id;
        this.orgName = orgName;
        this.orgTypeId = orgTypeId;
        this.displayOrder = displayOrder;
        this.children = children;
        this.callings = callings;
    }

    /* Properties */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getOrgName() { return  orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public int getOrgTypeId() { return orgTypeId; }
    public void setOrgTypeId(int orgTypeId) { this.orgTypeId = orgTypeId; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<Org> getChildren() { return children; }
    public void setChildren(List<Org> children) { this.children = children; }

    public List<Calling> getCallings() { return callings; }
    public void setCallings(List<Calling> callings) { this.callings = callings; }

    public List<Long> allOrgCallingIds() {
        if(callingIds == null || callingIds.size() == 0) {
            callingIds = new ArrayList<>(allOrgCallings().size());
            for(Calling calling : allOrgCallings()) {
                callingIds.add(calling.getId());
            }
        }
        return callingIds;
    }
    List<Calling> allCallings;
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
        if(allCallings == null) {
            allCallings = allOrgCallings();
        }
        for(Calling calling : allCallings) {
            if(calling.equals(param)) {
                return calling;
            }
        }
        return null;
    }

    /* Methods */
    public boolean equals(Org org) {
        return this.id == org.id;
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