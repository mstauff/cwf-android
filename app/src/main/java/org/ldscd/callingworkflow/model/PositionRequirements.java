package org.ldscd.callingworkflow.model;

import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.MemberClass;
import org.ldscd.callingworkflow.constants.Priesthood;

import java.util.List;

/**
 * Required fields for Position access.  Used by PositionMetaData class
 */
public class PositionRequirements {
    /* Fields */
    Gender gender;
    int age;
    List<Priesthood> priesthood;
    /* Represents the sister classes. */
    List<MemberClass> memberClasses;

    /* Constructor */
    public PositionRequirements(Gender gender, int age, List<Priesthood> priesthood, List<MemberClass> memberClasses) {
        this.gender = gender;
        this.age = age;
        this.priesthood = priesthood;
        this.memberClasses = memberClasses;
    }

    /* Properties */
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public boolean getIsAdult() {
        return age > 17;
    }
    public List<Priesthood> getPriesthood() {
        return priesthood;
    }
    public void setPriesthood(List<Priesthood> priesthood) {
        this.priesthood = priesthood;
    }

    public List<MemberClass> getMemberClasses() {
        return memberClasses;
    }
    public void setMemberClasses(List<MemberClass> memberClasses) {
        this.memberClasses = memberClasses;
    }

    @Override
    public boolean equals(Object obj) {
        PositionRequirements requirements = (PositionRequirements) obj;
        return  this.age == requirements.age &&
                this.gender.equals(requirements.gender) &&
                this.priesthood.equals(requirements.priesthood);
    }

    public boolean meetsRequirements(Member member) {
        boolean result = true;
        if(age > 0 && member.getCurrentAge() < age) {
            result = false;
        }
        if(gender != null && member.getGender() != gender) {
            result = false;
        }
        if(priesthood != null && !priesthood.isEmpty() && !priesthood.contains(member.getPriesthood())) {
            result = false;
        }
        if(memberClasses != null && !memberClasses.isEmpty()) {
            int memberAge = member.getCurrentAge();
            if(memberAge < 12) {
                result = false;
            } else if(memberAge < 14) {
                if (!memberClasses.contains(MemberClass.BEEHIVE)) {
                    result = false;
                }
            } else if(memberAge < 16) {
                if (!memberClasses.contains(MemberClass.MIA_MAID)) {
                    result = false;
                }
            } else if(memberAge < 18) {
                if (!memberClasses.contains(MemberClass.LAUREL)) {
                    result = false;
                }
            } else {
                if(!memberClasses.contains(MemberClass.RELIEF_SOCIETY)) {
                    result = false;
                }
            }
        }
        return result;
    }
}