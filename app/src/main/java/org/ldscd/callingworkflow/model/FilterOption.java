package org.ldscd.callingworkflow.model;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;

import java.util.ArrayList;
import java.util.List;

public class FilterOption {
    /* Fields */
    private boolean[] numberCallings;
    private int timeInCalling;
    private boolean highPriest;
    private boolean elders;
    private boolean priests;
    private boolean teachers;
    private boolean deacons;
    private boolean reliefSociety;
    private boolean laurel;
    private boolean miaMaid;
    private boolean beehive;
    private boolean twelveSeventeen;
    private boolean eighteenPlus;
    private boolean male;
    private boolean female;

    private static final int TWELVE     = 12;
    private static final int THIRTEEN   = 13;
    private static final int FOURTEEN   = 14;
    private static final int FIFTEEN    = 15;
    private static final int SIXTEEN    = 16;
    private static final int SEVENTEEN  = 17;
    private static final int EIGHTEEN   = 18;

    /* Constructor(s) */
    public FilterOption(boolean setDefaults) {
        if(setDefaults) {
            setDefault();
        }
    }
    public FilterOption(boolean[] numberCallings, int timeInCalling, boolean highPriest, boolean elders,
                        boolean priests, boolean teachers, boolean deacons, boolean reliefSociety,
                        boolean laurel, boolean miaMaid, boolean beehive, boolean twelveSeventeen,
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
        this.twelveSeventeen = twelveSeventeen;
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

    public int getTimeInCalling() {
        return timeInCalling;
    }

    public void setTimeInCalling(int timeInCalling) {
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

    public boolean isTwelveSeventeen() {
        return twelveSeventeen;
    }

    public void setTwelveSeventeen(boolean twelveSeventeen) {
        this.twelveSeventeen = twelveSeventeen;
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
    private void setDefault() {
        /* Initialize all the filters to default. */
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
        setTwelveSeventeen(false);
        setEighteenPlus(false);
        setMale(false);
        setFemale(false);
    }

    public ArrayList<Member> filterMembers(ArrayList<Member> members) {
        /* Create a list of selected filters. */
        List<SearchFilter> searchFilters = createSearchFilters();
        /* If filters haven't been selected avoid going through the checks. */
        if(searchFilters.isEmpty() || members == null || members.isEmpty()) {
            return members;
        }
        /* Loop through all members and check the criteria in the list of filters. */
        ArrayList<Member> filteredMembers = new ArrayList<>(members.size());
        for (Member member : members) {
            if (passesFilterCriteria(member, searchFilters)) {
                /* If the member passes filter criteria allow them to be viewed. */
                filteredMembers.add(member);
            }
        }
        return filteredMembers;
    }

    private List<SearchFilter> createSearchFilters() {
        List<SearchFilter> searchFilters = new ArrayList<>();
        /* Number of Callings */
        if(this.getNumberCallings() != null) {
            for(boolean item : this.getNumberCallings()) {
                if(item) {
                    searchFilters.add(new NumberOfCallingsFilter(this.getNumberCallings()));
                    break;
                }
            }
        }
        /* Time in Calling */
        if(this.getTimeInCalling() > 0) {
            searchFilters.add(new TimeInCallingFilter(this.getTimeInCalling()));
        }
        /* Age twelve to eighteen and eighteen plus */
        if(this.isTwelveSeventeen() || this.isEighteenPlus()) {
            searchFilters.add(new AgeFilter(this.isTwelveSeventeen(), this.isEighteenPlus()));
        }
        /* Gender filter: male or female */
        if(this.isMale() || this.isFemale()) {
            searchFilters.add(new GenderFilter(this.isMale(), this.isFemale()));
        }
        /* Priesthood filter */
        if(this.isHighPriest() || this.isElders() || this.isPriests() || this.isTeachers() || this.isDeacons()) {
            searchFilters.add(new PriesthoodFilter(this.isHighPriest(), this.isElders(), this.isPriests(), this.isTeachers(), this.isDeacons()));
        }
        /* Sister Auxiliary filter */
        if(this.isReliefSociety() || this.isLaurel() || this.isMiaMaid() || this.isBeehive()) {
            searchFilters.add(new SisterFilter(this.isReliefSociety(), this.isLaurel(), this.isMiaMaid(), this.isBeehive()));
        }

        return searchFilters;
    }

    private boolean passesFilterCriteria(Member member, List<SearchFilter> searchFilters) {
        for(SearchFilter filter : searchFilters) {
            if(!filter.filterMember(member)) {
                return false;
            }
        }
        return true;
    }

    /* Inner Interface */
    private interface SearchFilter {
        boolean filterMember(Member member);
    }

    /* Inner Classes */
    /* Each filter is broken out into it's own class. */
    private class NumberOfCallingsFilter implements SearchFilter {
        /* Fields */
        boolean[] numberOfCallings;
        /* Constructor */
        public NumberOfCallingsFilter(boolean[] numberOfCallings) {
            this.numberOfCallings = numberOfCallings;
        }
        /* Properties */
        private boolean[] getNumberOfCallings() {
            return numberOfCallings;
        }
        /* Methods */
        @Override
        public boolean filterMember(Member member) {
            /****** Number Of Callings ******/
            if (getNumberOfCallings()[0]) {
                if(member.getCurrentCallings().size() == 0) {
                    return true;
                }
            }
            if (getNumberOfCallings()[1]) {
                if(member.getCurrentCallings().size() == 1) {
                    return true;
                }
            }
            if (getNumberOfCallings()[2]) {
                if(member.getCurrentCallings().size() == 2) {
                    return true;
                }
            }
            if (getNumberOfCallings()[3]) {
                if(member.getCurrentCallings().size() >= 3) {
                    return true;
                }
            }
            return false;
        }
    }

    private class TimeInCallingFilter implements  SearchFilter {
        /* Fields */
        private int timeInCalling;

        /* Constructor */
        public TimeInCallingFilter(int timeInCalling) {
            this.timeInCalling = timeInCalling;
        }

        /* Properties */
        private int getTimeInCalling() {
            return timeInCalling;
        }

        /* Methods */
        @Override
        public boolean filterMember(Member member) {
            boolean showMember = false;
            for (Calling calling : member.getCurrentCallings()) {
                if (calling.getActiveDateTime() != null &&
                        Months.monthsBetween(calling.getActiveDateTime(), DateTime.now()).getMonths() >= (this.getTimeInCalling()*.5)*12) {
                    showMember = true;
                    break;
                }
            }
            return showMember;
        }
    }

    private class AgeFilter implements SearchFilter {
        /* Fields */
        boolean twelveSeventeen;
        boolean eighteenPlus;

        /* Constructor */
        public AgeFilter(boolean twelveSeventeen, boolean eighteenPlus) {
            this.twelveSeventeen = twelveSeventeen;
            this.eighteenPlus = eighteenPlus;
        }

        /* Properties */
        private boolean isTwelveSeventeen() {
            return twelveSeventeen;
        }

        private boolean isEighteenPlus() {
            return eighteenPlus;
        }

        @Override
        public boolean filterMember(Member member) {
            /****** Is Twelve To Seventeen ******/
            if (this.isTwelveSeventeen()) {
                return member.getCurrentAge() >= TWELVE &&
                        member.getCurrentAge() < EIGHTEEN;
            }
            /****** Eighteen Or Older *******/
            if (this.isEighteenPlus()) {
                return member.getCurrentAge() >= EIGHTEEN;
            }
            return false;
        }
    }

    private class GenderFilter implements SearchFilter {
        /* Fields */
        private boolean isMale;
        private boolean isFemale;

        /* Constructor */
        public GenderFilter(boolean isMale, boolean isFemale) {
            this.isMale = isMale;
            this.isFemale = isFemale;
        }

        /* Properties */
        private boolean isMale() {
            return isMale;
        }

        private boolean isFemale() {
            return isFemale;
        }

        @Override
        public boolean filterMember(Member member) {
            if(isMale()) {
                return member.getGender().equals(Gender.MALE);
            }
            if(isFemale()) {
                return member.getGender().equals(Gender.FEMALE);
            }
            return false;
        }
    }

    private class PriesthoodFilter implements SearchFilter {
        /* Fields */
        boolean highPriest;
        boolean elders;
        boolean priests;
        boolean teachers;
        boolean deacons;

        /* Constructor */
        public PriesthoodFilter(boolean highPriest, boolean elders, boolean priests, boolean teachers, boolean deacons) {
            this.highPriest = highPriest;
            this.elders = elders;
            this.priests = priests;
            this.teachers = teachers;
            this.deacons = deacons;
        }

        /* Properties */
        private boolean isHighPriest() {
            return highPriest;
        }

        private boolean isElders() {
            return elders;
        }

        private boolean isPriests() {
            return priests;
        }

        private boolean isTeachers() {
            return teachers;
        }

        private boolean isDeacons() {
            return deacons;
        }

        /* Methods */
        @Override
        public boolean filterMember(Member member) {
            /****** Is High Priest *******/
            if(this.isHighPriest()) {
                if(member.getPriesthood().equals(Priesthood.HIGH_PRIEST)) {
                    return true;
                }
            }
            /****** Is Elder *******/
            if(this.isElders()) {
                if(member.getPriesthood().equals(Priesthood.ELDER)) {
                    return true;
                }
            }
            /****** Is Priest *******/
            if(this.isPriests()) {
                if(member.getPriesthood().equals(Priesthood.PRIEST)) {
                    return true;
                }
            }
            /****** Is Teacher *******/
            if(this.isTeachers()) {
                if(member.getPriesthood().equals(Priesthood.TEACHER)) {
                    return true;
                }
            }
            /****** Is Deacon *******/
            if(this.isDeacons()) {
                if(member.getPriesthood().equals(Priesthood.DEACON)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class SisterFilter implements SearchFilter {
        /* Fields */
        boolean isReliefSociety;
        boolean isLaurels;
        boolean isMiaMaid;
        boolean isBeehive;

        /* Constructor */
        public SisterFilter(boolean isReliefSociety, boolean isLaurels, boolean isMiaMaid, boolean isBeehive) {
            this.isReliefSociety = isReliefSociety;
            this.isLaurels = isLaurels;
            this.isMiaMaid = isMiaMaid;
            this.isBeehive = isBeehive;
        }
        /* Properties */
        private boolean isReliefSociety() {
            return isReliefSociety;
        }

        private boolean isLaurels() {
            return isLaurels;
        }

        private boolean isMiaMaid() {
            return isMiaMaid;
        }

        private boolean isBeehive() {
            return isBeehive;
        }

        /* Methods */
        @Override
        public boolean filterMember(Member member) {
            /****** Is Relief Society *******/
            if(this.isReliefSociety()) {
                if(member.getCurrentAge() >= EIGHTEEN &&
                    member.getGender().equals(Gender.FEMALE)) {
                    return true;
                }
            }
            /****** Is Laurel *******/
            if(this.isLaurels()) {
                if(member.getCurrentAge() == SIXTEEN ||
                        member.getCurrentAge() == SEVENTEEN &&
                    member.getGender().equals(Gender.FEMALE)) {
                    return true;
                }
            }
            /****** Is MiaMaid *******/
            if(this.isMiaMaid()) {
                if(member.getCurrentAge() == FOURTEEN ||
                    member.getCurrentAge() == FIFTEEN &&
                    member.getGender().equals(Gender.FEMALE)) {
                    return true;
                }
            }
            /****** Is Beehive *******/
            if(this.isBeehive()) {
                if(member.getCurrentAge() == TWELVE ||
                    member.getCurrentAge() == THIRTEEN &&
                    member.getGender().equals(Gender.FEMALE)) {
                    return true;
                }
            }
            return false;
        }
    }
}