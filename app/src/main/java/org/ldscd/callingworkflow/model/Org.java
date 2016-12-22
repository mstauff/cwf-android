package org.ldscd.callingworkflow.model;

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
}