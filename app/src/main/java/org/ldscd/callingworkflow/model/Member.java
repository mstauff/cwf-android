package org.ldscd.callingworkflow.model;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, Calling> proposedCallings;

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
        if(!currentCallings.contains(currentCalling)) {
            currentCallings.add(currentCalling);
        }
    }
    public void removeCurrentCalling(Calling currentCalling) {
        if(currentCallings != null && currentCalling != null) {
            currentCallings.remove(currentCalling);
        }
    }
    public List<String> getCurrentCallingsWithTime() {
        List<String> names = new ArrayList<String>();
        if(currentCallings != null) {
            for(Calling calling : currentCallings) {
                names.add(calling.getPosition().getName() + "(" + DataUtil.getMonthsSinceActiveDate(calling.getActiveDate()) + "M)");
            }
        }
        return names;
    }

    public Collection<Calling> getProposedCallings() { return proposedCallings.values(); }
    public void setProposedCallings(List<Calling> proposedCallings) {
        if(this.proposedCallings == null) {
            this.proposedCallings = new HashMap<>();
        }
        if(proposedCallings != null && !proposedCallings.isEmpty()) {
            for(Calling calling : proposedCallings) {
                this.proposedCallings.put(calling.getCallingId(), calling);
            }
        }
    }
    public void addProposedCalling(Calling proposedCalling) {
        if(proposedCallings == null) {
            proposedCallings = new HashMap<>();
        }
        proposedCallings.put(proposedCalling.getCallingId(), proposedCalling);
    }
    public void removeProposedCalling(Calling proposedCalling) {
        if(proposedCallings != null && proposedCalling != null) {
            proposedCallings.remove(proposedCalling.getCallingId());
        }
    }
    public List<String> getProposedCallingsWithStatus() {
        List<String> names = new ArrayList<String>();
        if(this.proposedCallings != null) {
            for(Calling calling : proposedCallings.values()) {
                names.add(calling.getPosition().getName() + "(" + calling.getProposedStatus() + ")");
            }
        }
        return names;
    }

    public int getCurrentAge() {
        return getBirthDate() != null ? Years.yearsBetween(getBirthDate(), DateTime.now()).getYears() : -1;
    }
}