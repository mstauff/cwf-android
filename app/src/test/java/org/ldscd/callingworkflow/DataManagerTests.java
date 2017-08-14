package org.ldscd.callingworkflow;

import android.app.Activity;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.model.*;
import org.junit.Test;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.DataManagerImpl.DataManagerImpl;
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.MemberData;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class DataManagerTests {

    @Mock
    IWebResources mockWebResources;
    @Mock
    GoogleDataService mockGoogleDataService;
    @Mock
    MemberData memberData;
    @Mock
    ProgressBar mockProgressBar;
    @Mock
    Activity mockActivity;

    CallingData callingData;
    Org sampleLCROrgMultiplesAllowed;
    Org sampleLCROrgNoMultiplesAllowed;
    Org sampleCWFOrgMultiplesAllowed;
    Org sampleCWFOrgNoMultiplesAllowed;
    private static final Long unitNumber = 1234L;
    @Before
    public void prepData() {
        callingData = new CallingData(mockWebResources, mockGoogleDataService, memberData);

        List<Calling> sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(734820L, null, 11111L, null, null, new Position("Primary Teacher", 1481, false, false, 0L, 0L), "ACTIVE", null, null, 38432972L));
        sampleCallings.add(new Calling(275893L, null, 22222L, null, null, new Position("Varsity Coach", 1459, false, false, 0L, 0L), "ACTIVE", null, null, 38432972L));
        sampleLCROrgNoMultiplesAllowed = new Org(unitNumber, 38432972L, "CTR 7", 35, 320, new ArrayList<Org>(), sampleCallings);

        sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(728160L, null, 345L, null, null, new Position("Primary Teacher", 1482, false, true, 0L, 0L), "ACTIVE", null, null, 752892L));
        sampleCallings.add(new Calling(728220L, null, 678L, null, null, new Position("Primary Teacher", 1482, false, true, 0L, 0L), "ACTIVE", null, null, 752892L));
        sampleLCROrgMultiplesAllowed = new Org(unitNumber, 752892L, "CTR 8", 40, 350, new ArrayList<Org>(), sampleCallings);

        sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(734820L, null, 11111L, 1L, null, new Position("Primary Teacher", 1481, false, false, 0L, 0L), "ExistingStatus1", CallingStatus.PROPOSED, "notes1", 38432972L));
        sampleCallings.get(0).setCwfId("cwfId1"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleCallings.add(new Calling(275893L, null, 22222L, 2L, null, new Position("Varsity Coach", 1459, false, false, 0L, 0L), "ExistingStatus2", CallingStatus.ACCEPTED, "notes2", 38432972L));
        sampleCallings.get(1).setCwfId("cwfId2"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleCWFOrgNoMultiplesAllowed = new Org(unitNumber, 38432972L, "CTR 7", 35, 320, new ArrayList<Org>(), sampleCallings);

        sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(728160L, null, 345L, 3L, null, new Position("Primary Teacher", 1482, false, true, 0L, 0L), "ExistingStatus3", CallingStatus.APPOINTMENT_SET, "notes3", 752892L));
        sampleCallings.get(0).setCwfId("cwfId3"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleCallings.add(new Calling(728220L, null, 678L, 4L, null, new Position("Primary Teacher", 1482, false, true, 0L, 0L), "ExistingStatus4", CallingStatus.APPROVED, "notes4", 752892L));
        sampleCallings.get(1).setCwfId("cwfId4"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleCWFOrgMultiplesAllowed = new Org(unitNumber, 752892L, "CTR 8", 40, 350, new ArrayList<Org>(), sampleCallings);
    }

    private void confirmCWFDataMerged(Calling resultCalling, Calling sourceCalling) {
        assertEquals(sourceCalling.getCwfId(), resultCalling.getCwfId());
        assertEquals(sourceCalling.getProposedIndId(), resultCalling.getProposedIndId());
        assertEquals(sourceCalling.getProposedStatus(), resultCalling.getProposedStatus());
        assertEquals(sourceCalling.getExistingStatus(), resultCalling.getExistingStatus());
        assertEquals(sourceCalling.getNotes(), resultCalling.getNotes());
    }

    @Test
    public void mergeWithNothingChangedMultiplesAllowed() {
        mergeWithNothingChanged(sampleLCROrgMultiplesAllowed, sampleCWFOrgMultiplesAllowed);
    }

    @Test
    public void mergeWithNothingChangedNoMultiplesAllowed() {
        mergeWithNothingChanged(sampleLCROrgNoMultiplesAllowed, sampleCWFOrgNoMultiplesAllowed);
    }

    private void mergeWithNothingChanged(Org lcrOrg, Org cwfOrg) {
        int startingSize = lcrOrg.getCallings().size();
        callingData.mergeOrgs(lcrOrg, cwfOrg);

        //all the callings should have matched up so the number should be the same
        assertEquals(startingSize, lcrOrg.getCallings().size());

        //Now check that the callings were updated with CWF data
        for(int callingIndex=0; callingIndex < lcrOrg.getCallings().size(); callingIndex++) {
            confirmCWFDataMerged(lcrOrg.getCallings().get(callingIndex), cwfOrg.getCallings().get(callingIndex));
        }
    }

    @Test
    public void mergeWhenDeletedInLCRMultiplesAllowed() {
        mergeWhenDeletedInLCR(sampleLCROrgMultiplesAllowed, sampleCWFOrgMultiplesAllowed);
    }

    @Test
    public void mergeWhenDeletedInLCRNoMultiplesAllowed() {
        mergeWhenDeletedInLCR(sampleLCROrgNoMultiplesAllowed, sampleCWFOrgNoMultiplesAllowed);
    }

    private void mergeWhenDeletedInLCR(Org lcrOrg, Org cwfOrg) {
        //first we'll remove a calling from the end of the lcrOrg callings so the order still matches cwfOrg callings
        lcrOrg.getCallings().remove(1);

        int startingSize = lcrOrg.getCallings().size();
        callingData.mergeOrgs(lcrOrg, cwfOrg);

        //the extra calling in cwfOrg should be flagged and added to the lcrOrg
        assertTrue(lcrOrg.getCallings().size() - startingSize == 1);
        Calling resultCalling = lcrOrg.getCallings().get(1);
        assertEquals(ConflictCause.LDS_EQUIVALENT_DELETED, resultCalling.getConflictCause());

        //Now check that the callings were updated with CWF data
        for(int callingIndex=0; callingIndex < lcrOrg.getCallings().size(); callingIndex++) {
            confirmCWFDataMerged(lcrOrg.getCallings().get(callingIndex), cwfOrg.getCallings().get(callingIndex));
        }
    }

    @Test
    public void mergeCurrentReleasedInLCRMultiplesAllowed() {
        mergeCurrentReleasedInLCR(sampleLCROrgMultiplesAllowed, sampleCWFOrgMultiplesAllowed);
    }

    @Test
    public void mergeCurrentReleasedInLCRNoMultiplesAllowed() {
        mergeCurrentReleasedInLCR(sampleLCROrgNoMultiplesAllowed, sampleCWFOrgNoMultiplesAllowed);
    }

    private void mergeCurrentReleasedInLCR(Org lcrOrg, Org cwfOrg) {
        //When the current member in lcr changed so does the positionId so we'll change that and clear the memberId
        lcrOrg.getCallings().get(0).setId(111L);
        lcrOrg.getCallings().get(0).setMemberId(0);

        int startingSize = lcrOrg.getCallings().size();
        callingData.mergeOrgs(lcrOrg, cwfOrg);

        //In this case we can merge matching position types only if multiples are not allowed
        if(lcrOrg.getCallings().get(0).getPosition().getAllowMultiple()) {
            assertTrue(startingSize < lcrOrg.getCallings().size());
            assertEquals(ConflictCause.LDS_EQUIVALENT_DELETED, lcrOrg.getCallings().get(2).getConflictCause());

            //make sure the calling matched by id still merged correctly
            confirmCWFDataMerged(lcrOrg.getCallings().get(1), cwfOrg.getCallings().get(1));
        } else {
            assertEquals(startingSize, lcrOrg.getCallings().size());

            //Now check that the callings were updated with CWF data
            for(int callingIndex=0; callingIndex < lcrOrg.getCallings().size(); callingIndex++) {
                confirmCWFDataMerged(lcrOrg.getCallings().get(callingIndex), cwfOrg.getCallings().get(callingIndex));
            }
        }

    }

    @Test
    public void mergeCurrentChangedToProposedMultiplesAllowed() {
        mergeCurrentChangedToProposed(sampleLCROrgMultiplesAllowed, sampleCWFOrgMultiplesAllowed);
    }

    @Test
    public void mergeCurrentChangedToProposedNoMultiplesAllowed() {
        mergeCurrentChangedToProposed(sampleLCROrgNoMultiplesAllowed, sampleCWFOrgNoMultiplesAllowed);
    }

    public void mergeCurrentChangedToProposed(Org lcrOrg, Org cwfOrg) {
        //When the current member in lcr changes so does the positionId so we'll change that and set the memberId to the potential
        lcrOrg.getCallings().get(0).setId(111L);
        lcrOrg.getCallings().get(0).setMemberId(cwfOrg.getCallings().get(0).getProposedIndId());

        int startingSize = lcrOrg.getCallings().size();
        callingData.mergeOrgs(lcrOrg, cwfOrg);

        //In this case we can merge matching position types only if multiples are not allowed
        if(lcrOrg.getCallings().get(0).getPosition().getAllowMultiple()) {
            assertTrue(startingSize < lcrOrg.getCallings().size());
            assertEquals(ConflictCause.EQUIVALENT_POTENTIAL_AND_ACTUAL, lcrOrg.getCallings().get(2).getConflictCause());

            //make sure the calling matched by id still merged correctly
            confirmCWFDataMerged(lcrOrg.getCallings().get(1), cwfOrg.getCallings().get(1));
        } else {
            assertEquals(startingSize, lcrOrg.getCallings().size());
            //in the case potential matches the new current we don't keep the old cwf info
            Calling resultCalling = lcrOrg.getCallings().get(0);
            assertNull(resultCalling.getCwfId());
            assertNull(resultCalling.getProposedIndId());
            assertNull(resultCalling.getProposedStatus());
            assertNull(resultCalling.getNotes());

            //make sure the calling matched by id still merged correctly
            confirmCWFDataMerged(lcrOrg.getCallings().get(1), cwfOrg.getCallings().get(1));
        }
    }

    @Test
    public void mergeCurrentChangedToOtherMultiplesAllowed() {
        mergeCurrentChangedToOther(sampleLCROrgMultiplesAllowed, sampleCWFOrgMultiplesAllowed);
    }

    @Test
    public void mergeCurrentChangedToOtherNoMultiplesAllowed() {
        mergeCurrentChangedToOther(sampleLCROrgNoMultiplesAllowed, sampleCWFOrgNoMultiplesAllowed);
    }

    public void mergeCurrentChangedToOther(Org lcrOrg, Org cwfOrg) {
        //When the current member in lcr changes so does the positionId so we'll change that and change the memberId
        lcrOrg.getCallings().get(0).setId(111L);
        lcrOrg.getCallings().get(0).setMemberId(33333L);

        int startingSize = lcrOrg.getCallings().size();
        callingData.mergeOrgs(lcrOrg, cwfOrg);

        //In this case we can merge matching position types only if multiples are not allowed
        if(lcrOrg.getCallings().get(0).getPosition().getAllowMultiple()) {
            assertTrue(startingSize < lcrOrg.getCallings().size());
            assertEquals(ConflictCause.LDS_EQUIVALENT_DELETED, lcrOrg.getCallings().get(2).getConflictCause());

            //make sure the calling matched by id still merged correctly
            confirmCWFDataMerged(lcrOrg.getCallings().get(1), cwfOrg.getCallings().get(1));
        } else {
            assertEquals(startingSize, lcrOrg.getCallings().size());

            //Now check that the callings were updated with CWF data
            for(int callingIndex=0; callingIndex < lcrOrg.getCallings().size(); callingIndex++) {
                confirmCWFDataMerged(lcrOrg.getCallings().get(callingIndex), cwfOrg.getCallings().get(callingIndex));
            }
        }
    }

    @Test
    public void mergeWithNewCallingInCWFMultiplesAllowed() {
        mergeWithNewCallingInCWF(sampleLCROrgMultiplesAllowed, sampleCWFOrgMultiplesAllowed);
    }

    @Test
    public void mergeWithNewCallingInCWFNoMultiplesAllowed() {
        mergeWithNewCallingInCWF(sampleLCROrgNoMultiplesAllowed, sampleCWFOrgNoMultiplesAllowed);
    }

    public void mergeWithNewCallingInCWF(Org lcrOrg, Org cwfOrg) {
        //We add a new calling to cwfOrg as if created by the app
        Calling newCalling;
        if(lcrOrg.getCallings().get(0).getPosition().getAllowMultiple()) {
            newCalling = new Calling(null, null, null, 5L, null, new Position("Primary Teacher", 1481, false, true, 0L, 0L), null, CallingStatus.EXTENDED, "notes5", 752892L);
            newCalling.setCwfId("cwfId5"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        } else {
            newCalling = new Calling(null, null, null, 5L, null, new Position("Activity Days Leader", 217, false, false, 0L, 0L), null, CallingStatus.EXTENDED, "notes5", 38432972L);
            newCalling.setCwfId("cwfId5"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        }
        cwfOrg.getCallings().add(newCalling);

        int startingSize = lcrOrg.getCallings().size();
        callingData.mergeOrgs(lcrOrg, cwfOrg);

        //the new calling should be unchanged and added to the org, other callings should have merged
        assertTrue(startingSize < lcrOrg.getCallings().size());
        for(int i=0; i < lcrOrg.getCallings().size(); i++) {
            confirmCWFDataMerged(lcrOrg.getCallings().get(i), cwfOrg.getCallings().get(i));
        }
        Calling resultCalling = lcrOrg.getCallings().get(lcrOrg.getCallings().size()-1);
        assertNull(resultCalling.getId());
        assertNull(resultCalling.getMemberId());
        assertNull(resultCalling.getConflictCause());

    }
}