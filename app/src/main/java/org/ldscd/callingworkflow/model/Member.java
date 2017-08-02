package org.ldscd.callingworkflow.model;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ward Member.
 */
public class Member {
    /* Fields */
    long individualId;
    String formattedName;
    String individualPhone;
    String householdPhone;
    String individualEmail;
    String householdEmail;
    String streetAddress;
    DateTime birthDate;
    Gender gender;
    Priesthood priesthood;
    List<Calling> currentCallings;
    List<Calling> proposedCallings;

    /* Constructors */
    public Member() {}

    public Member(long individualId, String formattedName, String individualPhone, String householdPhone,
                  String individualEmail, String householdEmail, String streetAddress, DateTime birthDate, Gender gender,
                  Priesthood priesthood, List<Calling> currentCallings, List<Calling> proposedCallings) {
        this.individualId = individualId;
        this.formattedName = JsonUtil.evalNull(formattedName);
        this.individualPhone = JsonUtil.evalNull(individualPhone);
        this.householdPhone = JsonUtil.evalNull(householdPhone);
        this.individualEmail = JsonUtil.evalNull(individualEmail);
        this.householdEmail = JsonUtil.evalNull(householdEmail);
        this.streetAddress = JsonUtil.evalNull(streetAddress);
        this.birthDate = birthDate;
        this.gender = gender;
        this.priesthood = priesthood;
        /* Use the setter to allow for initialization of the list vs leaving it null if the values are null. */
        this.setCurrentCallings(currentCallings);
        this.setProposedCallings(proposedCallings);
    }

    /* Properties */
    public long getIndividualId() { return individualId; }
    public void setIndividualId(long individualId) { this.individualId = individualId;}

    public String getFormattedName() { return formattedName; }
    public void setFormattedName(String formattedName ) {this.formattedName = JsonUtil.evalNull(formattedName);}

    public String getIndividualPhone() { return individualPhone; }
    public void setIndividualPhone(String individualPhone ) {this.individualPhone = JsonUtil.evalNull(individualPhone);}

    public String getHouseholdPhone() { return householdPhone; }
    public void setHouseholdPhone(String householdPhone) {this.householdPhone  = JsonUtil.evalNull(householdPhone);}

    public String getIndividualEmail() { return individualEmail; }
    public void setIndividualEmail(String individualEmail) { this.individualEmail = JsonUtil.evalNull(individualEmail); }

    public String getHouseholdEmail() { return householdEmail; }
    public void setHouseholdEmail(String householdEmail) { this.householdEmail = JsonUtil.evalNull(householdEmail); }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = JsonUtil.evalNull(streetAddress); }

    public DateTime getBirthDate() { return birthDate; }
    public void setBirthDate(DateTime birthDate) { this.birthDate = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Priesthood getPriesthood() { return priesthood; }
    public void setPriesthood(Priesthood priesthood) { this.priesthood = priesthood; }

    public List<Calling> getCurrentCallings() { return currentCallings; }
    public void setCurrentCallings(List<Calling> currentCallings) {
        if(this.currentCallings == null) {
            this.currentCallings = new ArrayList<>();
        }
        if(currentCallings != null && !currentCallings.isEmpty()) {
            this.currentCallings.addAll(currentCallings);
        }
    }
    public void addCurrentCalling(Calling currentCalling) {
        if(this.currentCallings == null) {
            currentCallings = new ArrayList<>();
        }
        currentCallings.add(currentCalling);
    }

    public List<Calling> getProposedCallings() { return proposedCallings; }
    public void setProposedCallings(List<Calling> proposedCallings) {
        if(this.proposedCallings == null) {
            this.proposedCallings = new ArrayList<>();
        }
        if(proposedCallings != null && !proposedCallings.isEmpty()) {
            this.proposedCallings.addAll(proposedCallings);
        }
    }
    public void addProposedCallings(Calling proposedCalling) {
        if(proposedCallings == null) {
            proposedCallings = new ArrayList<>();
        }
        proposedCallings.add(proposedCalling);
    }

    public int getCurrentAge() {
        return Years.yearsBetween(getBirthDate(), DateTime.now()).getYears();
    }
}