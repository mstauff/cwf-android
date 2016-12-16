package org.ldscd.callingworkflow.model;

/**
 * Represents a Ward Member.
 */
public class Member {
    /* Fields */
    long individualId;
    String formattedName;
    String individualPhone;
    String householdPhone;
    String email;

    /* Constructors */
    public Member() {}
    public Member(long individualId, String formattedName, String individualPhone, String householdPhone, String email) {
        this.individualId = individualId;
        this.formattedName = formattedName;
        this.individualPhone = individualPhone;
        this.householdPhone = householdPhone;
        this.email = email;
    }

    /* Properties */
    public long getIndividualId() { return individualId; }
    public void setIndividualId(long individualId) { this.individualId = individualId;}

    public String getFormattedName() { return formattedName; }
    public void setFormattedName(String formattedName ) {this.formattedName = formattedName;}

    public String getIndividualPhone() { return individualPhone; }
    public void setIndividualPhone(String individualPhone ) {this.individualPhone = individualPhone;}

    public String getHouseholdPhone() { return householdPhone; }
    public void setHouseholdPhone(String householdPhone) {this.householdPhone  = householdPhone;}

    public String getEmail() { return email; }
    public void setEmail(String email) {this.email  = email;}
}