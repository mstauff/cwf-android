package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.utils.JsonUtil;

import java.util.Date;
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
    Date birthDate;
    Gender gender;
    Priesthood priesthood;
    List<Calling> currentCallings;

    /* Constructors */
    public Member() {}

    public Member(long individualId, String formattedName, String individualPhone, String householdPhone,
                  String individualEmail, String householdEmail, String streetAddress, Date birthDate, Gender gender,
                  Priesthood priesthood, List<Calling> currentCallings) {
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
        this.currentCallings = currentCallings;
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

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Priesthood getPriesthood() { return priesthood; }
    public void setPriesthood(Priesthood priesthood) { this.priesthood = priesthood; }

    public List<Calling> getCurrentCallings() { return currentCallings; }
    public void setCurrentCallings(List<Calling> currentCallings) { this.currentCallings = currentCallings; }
}