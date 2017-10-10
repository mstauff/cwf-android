package org.ldscd.callingworkflow;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.FilterOption;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Position;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FilterOptionTest {

    @Test
    public void testFilterForAge() {
        Member member10 = createMember(10L, 10);
        Member member15 = createMember(15L, 15);
        Member member30 = createMember(30L, 30);
        Member member60 = createMember(60L, 60);
        Member memberNoAge = createMember(70L, 0);

        ArrayList<Member> memberList = new ArrayList<Member>(Arrays.asList(member10, member15, member30, member60, memberNoAge));
        FilterOption filterOptions = new FilterOption(true);

        /* List should not be changed with no filter options */
        List<Member> filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, memberList);

        /* Filter from 12 to 17 */
        filterOptions.setTwelveSeventeen(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(member15));

        /* Filter from 18 and no max age */
        filterOptions.setTwelveSeventeen(false);
        filterOptions.setEighteenPlus(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Arrays.asList(member30, member60));

        /* Nothing matching */
        filteredList = filterOptions.filterMembers(new ArrayList<Member>(Arrays.asList(member10, member15)));
        assertTrue(filteredList.isEmpty());

        filterOptions.setTwelveSeventeen(true);
        filterOptions.setEighteenPlus(false);
        filteredList = filterOptions.filterMembers(new ArrayList<Member>(Arrays.asList(member30, member60)));
        assertTrue(filteredList.isEmpty());
    }

    @Test
    public void testFilterForGender() {
        Member male = new Member(100, null, null, null, null, null, null, null, Gender.MALE, null, null, null);
        Member female = new Member(100, null, null, null, null, null, null, null, Gender.FEMALE, null, null, null);
        Member badData = new Member(100, null, null, null, null, null, null, null, Gender.UNKNOWN, null, null, null);

        ArrayList<Member> memberList = new ArrayList<Member>(Arrays.asList(male, female, badData));
        FilterOption filterOptions = new FilterOption(true);

        /* All should Return */
        ArrayList<Member> filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, memberList);

        /* Only male should return */
        filterOptions.setMale(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(male));

        /* Only female should return */
        filterOptions.setMale(false);
        filterOptions.setFemale(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(female));
    }

    @Test
    public void testFilterForCallings() {
        Member noCallings = createMemberWithCallings(1L, 0);
        Member oneCalling = createMemberWithCallings(100L, 1);
        Member twoCallings = createMemberWithCallings(200L, 2);
        Member threeCallings = createMemberWithCallings(300L, 3);
        Member fourCallings = createMemberWithCallings(400L, 4);
        
        ArrayList<Member> memberList = new ArrayList<Member>(Arrays.asList(noCallings, oneCalling, twoCallings, threeCallings, fourCallings));
        FilterOption filterOptions = new FilterOption(true);

        /* All should Return */
        ArrayList<Member> filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, memberList);

        /* One Calling */
        filterOptions.setNumberCallings(new boolean[] {false, true, false, false});
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(oneCalling));

        /* Two Callings */
        filterOptions.setNumberCallings(new boolean[] {false, false, true, false});
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(twoCallings));

        /* Three plus callings */
        filterOptions.setNumberCallings(new boolean[] {false, false, false, true});
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Arrays.asList(threeCallings, fourCallings));

        /* Zero and One callings */
        filterOptions.setNumberCallings(new boolean[] {true, true, false, false});
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Arrays.asList(noCallings, oneCalling));
    }

    @Test
    public void testFilterForTimeInCalling() {
        Member member10Months = createMember(10L, new int[] {10});
        Member memberLongCallingEndList = createMember(15L, new int[] {6, 15});
        Member memberLongCallingStartList = createMember(30L, new int[] {15, 6});
        Member memberLongCallingMiddleList = createMember(60L, new int[] {10, 15, 6});
        Member memberNoCallings = createMember(70L, new int[] {});

        ArrayList<Member> memberList = new ArrayList<Member>(Arrays.asList(member10Months, memberLongCallingEndList, memberLongCallingStartList, memberLongCallingMiddleList, memberNoCallings));
        FilterOption filterOptions = new FilterOption(true);

        /* All should Return */
        ArrayList<Member> filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, memberList);

        /* Filter with nothing matching */
        filterOptions.setTimeInCalling(20);
        filteredList = filterOptions.filterMembers(memberList);
        assertTrue(filteredList.isEmpty());

        /* Filter with matches where the match is at start, end or middle */
        filterOptions.setTimeInCalling(12);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Arrays.asList(memberLongCallingEndList, memberLongCallingStartList, memberLongCallingMiddleList));
    }

    @Test
    public void testFilterForPriesthood() {
        Member highPriestMember = createMember(70L, Priesthood.HIGH_PRIEST);
        Member elderMember = createMember(30L, Priesthood.ELDER);
        Member priestMember = createMember(10L, Priesthood.PRIEST);
        Member teacherMember = createMember(10L, Priesthood.TEACHER);
        Member deaconMember = createMember(15L, Priesthood.DEACON);
        Member noPriesthoodMember = createMember(60L, 40);

        ArrayList<Member> memberList = new ArrayList<Member>(Arrays.asList(highPriestMember, elderMember, priestMember, teacherMember, deaconMember, noPriesthoodMember));
        FilterOption filterOptions = new FilterOption(true);

        /* All should Return */
        ArrayList<Member> filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, memberList);

        /* High Priest */
        filterOptions.setHighPriest(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(highPriestMember));

        /* Elder */
        filterOptions.setHighPriest(false);
        filterOptions.setElders(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(elderMember));

        /* Priest */
        filterOptions.setHighPriest(false);
        filterOptions.setElders(false);
        filterOptions.setPriests(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(priestMember));

        /* Teacher */
        filterOptions.setHighPriest(false);
        filterOptions.setElders(false);
        filterOptions.setPriests(false);
        filterOptions.setTeachers(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(teacherMember));

        /* Deacon */
        filterOptions.setHighPriest(false);
        filterOptions.setElders(false);
        filterOptions.setPriests(false);
        filterOptions.setTeachers(false);
        filterOptions.setDeacons(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(deaconMember));

        /* No Priesthood */
        filterOptions.setHighPriest(true);
        filterOptions.setElders(true);
        filterOptions.setPriests(true);
        filterOptions.setTeachers(true);
        filterOptions.setDeacons(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertTrue(!filteredList.contains(noPriesthoodMember));
    }

    @Test
    public void testMultipleFilters() {
        Member member10Male = createMember(10L, 10, Gender.MALE);
        Member member15Female = createMember(15L, 15, Gender.FEMALE);
        Member member30Male = createMember(30L, 30, Gender.MALE);
        Member member30Female = createMember(30L, 30, Gender.FEMALE);
        Member member60Male = createMember(60L, 60, Gender.MALE);
        Member member60Female = createMember(60L, 60, Gender.FEMALE);
        Member memberNoAge = createMember(60L, 0);

        ArrayList<Member> memberList = new ArrayList<Member>(Arrays.asList(member10Male, member15Female, member30Male, member30Female, member60Male, member60Female, memberNoAge));
        FilterOption filterOptions = new FilterOption(true);

        /* All should Return */
        ArrayList<Member> filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, memberList);

        /* Test for 15 year old female */
        filterOptions.setTwelveSeventeen(true);
        filterOptions.setFemale(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Collections.singletonList(member15Female));

        /* Test for older than 17 male */
        filterOptions.setTwelveSeventeen(false);
        filterOptions.setEighteenPlus(true);
        filterOptions.setFemale(false);
        filterOptions.setMale(true);
        filteredList = filterOptions.filterMembers(memberList);
        assertEquals(filteredList, Arrays.asList(member30Male, member60Male));

    }

    private Member createMember(Long id, Integer age, Gender gender) {
        DateTime birthDate = null;
        if (age > 0) {
            birthDate = DateTime.now().minusYears(age);
        }

        return new Member(id, null, null, null, null, null, null, birthDate, gender, null, null, null);
    }

    private Member createMember(Long id, Integer age) {
        return createMember(id, age, null);
    }

    private Member createMember(Long id, Priesthood priesthood) {
        return new Member(id, null, null, null, null, null, null, null, null, priesthood, null, null);
    }

    private Member createMember(Long id, int[] numberOfMonthsInCalling) {
        Position position = new Position("Teacher", 100, false, true, 100L, 1234L);
        List<Calling> callings = new ArrayList<Calling>();
        if (numberOfMonthsInCalling.length > 0) {
            for(int i = 0; i < numberOfMonthsInCalling.length; i++) {
                DateTime activeDate = DateTime.now().minusMonths(numberOfMonthsInCalling[i]);
                callings.add(new Calling(id * 100 + numberOfMonthsInCalling[i], null, false, id, 0L, activeDate, position, null, null, null, 0L) );
            }
        }

        return new Member(id, null, null, null, null, null, null, null, null, null, callings, null);
    }

    private Member createMemberWithCallings(Long id, Integer numCallings) {
        Position position = new Position("Teacher", 100, false, true, 100L, 1234L);
        List<Calling> callings = new ArrayList<Calling>();
        if (numCallings > 0) {
            for(int i = 0; i < numCallings; i++) {
                callings.add(new Calling(id * 100 + i, null, false, id, 0L, null, position, null, null, null, 0L) );
            }
        }

        return new Member(id, null, null, null, null, null, null, null, null, null, callings, null);
    }
}