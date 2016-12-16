package org.ldscd.callingworkflow.model;

/**
 * Represents an auxiliary.  i.e. primary, R.S. or E.Q.
 */
public class Org {
    /* Fields */
    long id;
    String orgName;
    int orgTypeId;

    /* Constructors */
    public Org() {}
    public Org(long id, String orgName, int orgTypeId) {
        this.id = id;
        this.orgName = orgName;
        this.orgTypeId = orgTypeId;
    }

    /* Properties */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getOrgName() { return  orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public int getOrgTypeId() { return orgTypeId; }
    public void setOrgTypeId(int orgTypeId) { this.orgTypeId = orgTypeId; }
}