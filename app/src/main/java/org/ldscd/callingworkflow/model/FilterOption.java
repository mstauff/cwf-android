package org.ldscd.callingworkflow.model;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilterOption {
    /* Fields */
    private boolean[] numberCallings;
    private double timeInCalling;
    private boolean highPriest;
    private boolean elders;
    private boolean priests;
    private boolean teachers;
    private boolean deacons;
    private boolean reliefSociety;
    private boolean laurel;
    private boolean miaMaid;
    private boolean beehive;
    private boolean twelveEighteen;
    private boolean eighteenPlus;
    private boolean male;
    private boolean female;

    /* Constructor(s) */
    public FilterOption(boolean setDefaults) {
        setDefault();
    }
    public FilterOption(boolean[] numberCallings, double timeInCalling, boolean highPriest, boolean elders,
                        boolean priests, boolean teachers, boolean deacons, boolean reliefSociety,
                        boolean laurel, boolean miaMaid, boolean beehive, boolean twelveEighteen,
                        boolean eighteenPlus, boolean male, boolean female) {
        this.numberCallings = numberCallings;
        this.timeInCalling = timeInCalling;
        this.highPriest = highPriest;
        this.elders = elders;
        this.priests = priests;
        this.teachers = teachers;
        this.deacons = deacons;
        this.reliefSociety = reliefSociety;
        this.laurel = laurel;
        this.miaMaid = miaMaid;
        this.beehive = beehive;
        this.twelveEighteen = twelveEighteen;
        this.eighteenPlus = eighteenPlus;
        this.male = male;
        this.female = female;
    }

    /* Properties */
    public boolean[] getNumberCallings() {
        return numberCallings;
    }

    public void setNumberCallings(boolean[] numberCallings) {
        if(numberCallings == null) {
            numberCallings = new boolean[] {false, false, false, false};
        }
        this.numberCallings = numberCallings;
    }

    public double getTimeInCalling() {
        return timeInCalling;
    }

    public void setTimeInCalling(double timeInCalling) {
        this.timeInCalling = timeInCalling;
    }

    public boolean isHighPriest() {
        return highPriest;
    }

    public void setHighPriest(boolean highPriest) {
        this.highPriest = highPriest;
    }

    public boolean isElders() {
        return elders;
    }

    public void setElders(boolean elders) {
        this.elders = elders;
    }

    public boolean isPriests() {
        return priests;
    }

    public void setPriests(boolean priests) {
        this.priests = priests;
    }

    public boolean isTeachers() {
        return teachers;
    }

    public void setTeachers(boolean teachers) {
        this.teachers = teachers;
    }

    public boolean isDeacons() {
        return deacons;
    }

    public void setDeacons(boolean deacons) {
        this.deacons = deacons;
    }

    public boolean isReliefSociety() {
        return reliefSociety;
    }

    public void setReliefSociety(boolean reliefSociety) {
        this.reliefSociety = reliefSociety;
    }

    public boolean isLaurel() {
        return laurel;
    }

    public void setLaurel(boolean laurel) {
        this.laurel = laurel;
    }

    public boolean isMiaMaid() {
        return miaMaid;
    }

    public void setMiaMaid(boolean miaMaid) {
        this.miaMaid = miaMaid;
    }

    public boolean isBeehive() {
        return beehive;
    }

    public void setBeehive(boolean beehive) {
        this.beehive = beehive;
    }

    public boolean isTwelveEighteen() {
        return twelveEighteen;
    }

    public void setTwelveEighteen(boolean twelveEighteen) {
        this.twelveEighteen = twelveEighteen;
    }

    public boolean isEighteenPlus() {
        return eighteenPlus;
    }

    public void setEighteenPlus(boolean eighteenPlus) {
        this.eighteenPlus = eighteenPlus;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public boolean isFemale() {
        return female;
    }

    public void setFemale(boolean female) {
        this.female = female;
    }

    /* Methods */
    public void setDefault() {
        setNumberCallings(new boolean[] {false, false, false, false});
        setTimeInCalling(0);
        setHighPriest(false);
        setElders(false);
        setPriests(false);
        setTeachers(false);
        setDeacons(false);
        setReliefSociety(false);
        setLaurel(false);
        setMiaMaid(false);
        setBeehive(false);
        setTwelveEighteen(false);
        setEighteenPlus(false);
        setMale(false);
        setFemale(false);
    }

    public boolean isSetToDefault() {
        return  !this.getNumberCallings()[0] &&
                !this.getNumberCallings()[1] &&
                !this.getNumberCallings()[2] &&
                !this.getNumberCallings()[3] &&
                this.getTimeInCalling() == 0 &&
                !this.isHighPriest()         &&
                !this.isElders()             &&
                !this.isPriests()            &&
                !this.isTeachers()           &&
                !this.isDeacons()            &&
                !this.isReliefSociety()      &&
                !this.isLaurel()             &&
                !this.isMiaMaid()            &&
                !this.isBeehive()            &&
                !this.isTwelveEighteen()     &&
                !this.isEighteenPlus()       &&
                !this.isMale()               &&
                !this.isFemale();
    }

    public ArrayList<Member> filterMembers(List<Member> members) {
        ArrayList<Member> filteredMembers = new ArrayList<>(members.size());
        for(Member member : members) {
            if(passesFilterCriteria(member)) {
                filteredMembers.add(member);
            }
        }
        return filteredMembers;
    }
    private boolean passesFilterCriteria(Member member) {
        boolean showMember = true;
        if(!this.isSetToDefault()) {
            showMember = false;
            /****** Number Of Callings ******/
            if (this.getNumberCallings()[0]) {
                showMember = member.getCurrentCallings() == null || member.getCurrentCallings().size() == 0;
            }
            if (this.getNumberCallings()[1]) {
                showMember = member.getCurrentCallings() != null && member.getCurrentCallings().size() == 1;
            }
            if (this.getNumberCallings()[2]) {
                showMember = member.getCurrentCallings() != null && member.getCurrentCallings().size() == 2;
            }
            if (this.getNumberCallings()[3]) {
                showMember = member.getCurrentCallings() != null && member.getCurrentCallings().size() == 3;
            }
            /****** Time In Calling(s) ******/
            if (this.getTimeInCalling() > 0) {
                if (showMember) {
                    for (Calling calling : member.getCurrentCallings()) {
                        /*if (calling.getActiveDateTime().isBefore(this.getTimeInCalling())) {

                        }*/
                    }
                }
            }
            /****** Is Twelve To Eighteen ******/
            if (this.isTwelveEighteen()) {
                showMember = getCurrentAge(member.getBirthDate()) >= 12 &&
                             getCurrentAge(member.getBirthDate()) <= 18;
            }
            /****** Eighteen Or Older *******/
            if (this.isEighteenPlus()) {
                showMember = getCurrentAge(member.getBirthDate()) >= 18;
            }
            /****** Is Male *******/
            if (this.isMale()) {
                showMember = member.getGender().equals(Gender.MALE);
            }
            /****** Is Female *******/
            if(this.isFemale()) {
                showMember = member.getGender().equals(Gender.FEMALE);
            }
            /****** Is High Priest *******/
            if(this.isHighPriest()) {
                showMember = member.getPriesthood().equals(Priesthood.HIGH_PRIEST);
            }
            /****** Is Elder *******/
            if(this.isElders()) {
                showMember = member.getPriesthood().equals(Priesthood.ELDER);
            }
            /****** Is Priest *******/
            if(this.isPriests()) {
                showMember = member.getPriesthood().equals(Priesthood.PRIEST);
            }
            /****** Is Teacher *******/
            if(this.isTeachers()) {
                showMember = member.getPriesthood().equals(Priesthood.TEACHER);
            }
            /****** Is Deacon *******/
            if(this.isDeacons()) {
                showMember = member.getPriesthood().equals(Priesthood.DEACON);
            }
            /****** Is Relief Society *******/
            if(this.isReliefSociety()) {
                showMember = getCurrentAge(member.getBirthDate()) >= 18 &&
                             member.getGender().equals(Gender.FEMALE);
            }
            /****** Is Laurel *******/
            if(this.isLaurel()) {
                showMember = getCurrentAge(member.getBirthDate()) == 16 ||
                             getCurrentAge(member.getBirthDate()) == 17 &&
                             member.getGender().equals(Gender.FEMALE);
            }
            /****** Is MiaMaid *******/
            if(this.isMiaMaid()) {
                showMember = getCurrentAge(member.getBirthDate()) == 14 ||
                             getCurrentAge(member.getBirthDate()) == 15 &&
                             member.getGender().equals(Gender.FEMALE);
            }
            /****** Is Beehive *******/
            if(this.isBeehive()) {
                showMember = getCurrentAge(member.getBirthDate()) == 12 ||
                             getCurrentAge(member.getBirthDate()) == 13 &&
                             member.getGender().equals(Gender.FEMALE);
            }
        }
        return showMember;
    }

    private int getCurrentAge(DateTime birthdate) {
        return Years.yearsBetween(birthdate, DateTime.now()).getYears();
    }
}