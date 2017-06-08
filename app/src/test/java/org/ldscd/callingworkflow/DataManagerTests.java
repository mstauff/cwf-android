package org.ldscd.callingworkflow;

import android.app.Activity;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.junit.Before;
import org.junit.runner.RunWith;
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

    DataManager dataManager;
    List<Org> sampleLCROrgs;
    Org sampleCWFOrg;

    @Before
    public void prepData() {
        CallingData callingData = new CallingData(mockWebResources, mockGoogleDataService, memberData);
        MemberData memberData = new MemberData(mockWebResources);
        dataManager = new DataManagerImpl(callingData, memberData, mockGoogleDataService);

        List<Org> sampleSubOrgs = new ArrayList<>();
        List<Calling> sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(734820L, null, 11111L, null, null, new Position("Primary Teacher", 1481, false, true, 0L), "ACTIVE", null, null, 38432972L));
        sampleCallings.add(new Calling(275893L, null, 22222L, null, null, new Position("Varsity Coach", 1459, false, true, 0L), "ACTIVE", null, null, 38432972L));
        sampleSubOrgs.add(new Org(38432972L, "CTR 7", 35, 320, new ArrayList<Org>(), sampleCallings));

        sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(728160L, null, null, null, null, new Position("Primary Teacher", 1482, false, true, 0L), "ACTIVE", null, null, 752892L));
        sampleCallings.add(new Calling(728220L, null, 678L, null, null, new Position("Primary Teacher", 1482, false, true, 0L), "ACTIVE", null, null, 752892L));
        sampleSubOrgs.add(new Org(752892L, "CTR 8", 40, 350, new ArrayList<Org>(), sampleCallings));

        sampleLCROrgs = new ArrayList<>();
        sampleLCROrgs.add(new Org(7428354L, "Primary", 77, 700, sampleSubOrgs, new ArrayList<Calling>()));

        sampleSubOrgs = new ArrayList<>();
        sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(734820L, null, 11111L, 1L, null, new Position("Primary Teacher", 1481, false, true, 0L), "ExistingStatus1", "ProposedStatus1", "notes1", 38432972L));
        sampleCallings.get(0).setCwfId("cwfId1"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleCallings.add(new Calling(275893L, null, 22222L, 2L, null, new Position("Varsity Coach", 1459, false, true, 0L), "ExistingStatus2", "ProposedStatus2", "notes2", 38432972L));
        sampleCallings.get(1).setCwfId("cwfId2"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleSubOrgs.add(new Org(38432972L, "CTR 7", 35, 320, new ArrayList<Org>(), sampleCallings));

        sampleCallings = new ArrayList<>();
        sampleCallings.add(new Calling(728160L, null, null, 3L, null, new Position("Primary Teacher", 1482, false, true, 0L), "ExistingStatus3", "ProposedStatus3", "notes3", 752892L));
        sampleCallings.get(0).setCwfId("cwfId3"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleCallings.add(new Calling(728220L, null, 678L, 4L, null, new Position("Primary Teacher", 1482, false, true, 0L), "ExistingStatus4", "ProposedStatus4", "notes4", 752892L));
        sampleCallings.get(1).setCwfId("cwfId4"); //this has to be set separately because the constructor doesn't set it when there's a regular id
        sampleSubOrgs.add(new Org(752892L, "CTR 8", 40, 350, new ArrayList<Org>(), sampleCallings));

        sampleCWFOrg = new Org(7428354L, "Primary", 77, 700, sampleSubOrgs, new ArrayList<Calling>());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Response.Listener<Boolean>)invocation.getArguments()[0]).onResponse(true);
                return null;
            }
        }).when(mockGoogleDataService).init(any(Response.Listener.class), any(Activity.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Response.Listener<List<Org>>)invocation.getArguments()[0]).onResponse(sampleLCROrgs);
                return null;
            }
        }).when(mockWebResources).getOrgs(any(Response.Listener.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Response.Listener<Boolean>)invocation.getArguments()[0]).onResponse(true);
                return null;
            }
        }).when(mockGoogleDataService).syncDriveIds(any(Response.Listener.class), any(List.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Response.Listener<Org>)invocation.getArguments()[0]).onResponse(sampleCWFOrg);
                return null;
            }
        }).when(mockGoogleDataService).getOrgData(any(Response.Listener.class), any(Response.ErrorListener.class), any(Org.class));
    }

    @Test
    public void mergeMatchingIds() {
        //the data groups have already been set up, we just need to make note of the number of orgs we are
        // starting with to compare later and start the merge
        final int startingSize = sampleLCROrgs.get(0).getChildren().size();
        dataManager.loadOrgs(new Response.Listener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                //loadOrgs should return true on success
                assertTrue(response);
                Org resultOrg = dataManager.getOrgs().get(0);

                //all orgs should have matched by id so we should have the same number of orgs and callings
                assertEquals(resultOrg.getChildren().size(), startingSize);
                for(int subOrgIndex=0; subOrgIndex < resultOrg.getChildren().size(); subOrgIndex++) {
                    Org resultSubOrg = resultOrg.getChildren().get(subOrgIndex);
                    Org cwfSubOrg = sampleCWFOrg.getChildren().get(subOrgIndex);
                    assertEquals(resultSubOrg.getCallings().size(), cwfSubOrg.getCallings().size());

                    //Now check that the callings were updated with CWF data
                    for(int callingIndex=0; callingIndex < resultSubOrg.getCallings().size(); callingIndex++) {
                        Calling resultCalling = resultSubOrg.getCallings().get(callingIndex);
                        Calling cwfCalling = cwfSubOrg.getCallings().get(callingIndex);
                        assertEquals(resultCalling.getCallingId(), cwfCalling.getCallingId());
                        assertEquals(resultCalling.getProposedIndId(), cwfCalling.getProposedIndId());
                        assertEquals(resultCalling.getProposedStatus(), cwfCalling.getProposedStatus());
                        assertEquals(resultCalling.getExistingStatus(), cwfCalling.getExistingStatus());
                        assertEquals(resultCalling.getNotes(), cwfCalling.getNotes());
                    }
                }
            }
        }, mockProgressBar, mockActivity);
    }

    @Test
    public void mergeMatchingTypeWithOneMatch() {
        //this time ids shouldn't match so we'll change them and remove the subOrg which has callings of the same type
        sampleCWFOrg.getChildren().get(0).getCallings().get(0).setId(111L);
        sampleCWFOrg.getChildren().get(0).getCallings().get(1).setId(222L);
        sampleCWFOrg.getChildren().remove(1);
        sampleLCROrgs.get(0).getChildren().remove(1);

        final int startingSize = sampleLCROrgs.get(0).getChildren().size();
        dataManager.loadOrgs(new Response.Listener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                //loadOrgs should return true on success
                assertTrue(response);
                Org resultOrg = dataManager.getOrgs().get(0);

                //all orgs should have matched so we should have the same number of orgs and callings
                assertEquals(resultOrg.getChildren().size(), startingSize);
                Org resultSubOrg = resultOrg.getChildren().get(0);
                Org cwfSubOrg = sampleCWFOrg.getChildren().get(0);
                assertEquals(resultSubOrg.getCallings().size(), cwfSubOrg.getCallings().size());

                //Now check that the callings were updated with CWF data
                for(int callingIndex=0; callingIndex < resultSubOrg.getCallings().size(); callingIndex++) {
                    Calling resultCalling = resultSubOrg.getCallings().get(callingIndex);
                    Calling cwfCalling = cwfSubOrg.getCallings().get(callingIndex);
                    assertEquals(resultCalling.getProposedIndId(), cwfCalling.getProposedIndId());
                    assertEquals(resultCalling.getProposedStatus(), cwfCalling.getProposedStatus());
                    assertEquals(resultCalling.getExistingStatus(), cwfCalling.getExistingStatus());
                    assertEquals(resultCalling.getNotes(), cwfCalling.getNotes());
                }
            }
        }, mockProgressBar, mockActivity);
    }

    @Test
    public void mergeMatchingTypeWithMultipleMatches() {
        //this time ids shouldn't match so we'll change them and remove the subOrg with different calling types
        sampleCWFOrg.getChildren().get(1).getCallings().get(0).setId(111L);
        sampleCWFOrg.getChildren().get(1).getCallings().get(1).setId(222L);
        sampleCWFOrg.getChildren().remove(0);
        sampleLCROrgs.get(0).getChildren().remove(0);

        final int startingSize = sampleLCROrgs.get(0).getChildren().size();
        dataManager.loadOrgs(new Response.Listener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                //loadOrgs should return true on success
                assertTrue(response);
                Org resultOrg = dataManager.getOrgs().get(0);

                //orgs should match so we should have the same number of orgs, but the result should include more callings
                assertEquals(resultOrg.getChildren().size(), startingSize);
                Org resultSubOrg = resultOrg.getChildren().get(0);
                Org cwfSubOrg = sampleCWFOrg.getChildren().get(0);
                assertTrue(resultSubOrg.getCallings().size() > cwfSubOrg.getCallings().size());

                //The cwf callings should have been added to the list so we should have 2 flagged callings with CWF data
                //and 2 without flags or CWF data
                int flaggedCount = 0;
                int nonFlaggedCount = 0;
                for(int callingIndex=0; callingIndex < resultSubOrg.getCallings().size(); callingIndex++) {
                    Calling resultCalling = resultSubOrg.getCallings().get(callingIndex);
                    if(resultCalling.getConflictCause() == null) {
                        assertNull(resultCalling.getCwfId());
                        assertNull(resultCalling.getProposedIndId());
                        assertNull(resultCalling.getProposedStatus());
                        assertNull(resultCalling.getNotes());
                        nonFlaggedCount++;
                    } else {
                        assertNotNull(resultCalling.getCwfId());
                        assertNotNull(resultCalling.getProposedIndId());
                        assertNotNull(resultCalling.getProposedStatus());
                        assertNotNull(resultCalling.getNotes());
                        flaggedCount++;
                    }
                }
                assertEquals(nonFlaggedCount, 2);
                assertEquals(flaggedCount, 2);
            }
        }, mockProgressBar, mockActivity);
    }

    @Test
    public void mergeNonMatchingType() {
        //they shouldn't match at all so we'll change the ids and positions
        List<Calling> subOrgCallings = sampleCWFOrg.getChildren().get(0).getCallings();
        subOrgCallings.get(0).setId(111L);
        subOrgCallings.get(0).setPosition(new Position("Primary Pianist", 215, false, true, 0L));
        subOrgCallings.get(1).setId(222L);
        subOrgCallings.get(1).setPosition(new Position("Primary Music Leader", 214, false, true, 0L));

        final int startingSize = sampleLCROrgs.get(0).getChildren().size();
        dataManager.loadOrgs(new Response.Listener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                //loadOrgs should return true on success
                assertTrue(response);
                Org resultOrg = dataManager.getOrgs().get(0);

                //orgs should match so we should have the same number of orgs, but the result should include more callings
                assertEquals(resultOrg.getChildren().size(), startingSize);
                Org resultSubOrg = resultOrg.getChildren().get(0);
                Org cwfSubOrg = sampleCWFOrg.getChildren().get(0);
                assertTrue(resultSubOrg.getCallings().size() > cwfSubOrg.getCallings().size());

                //The cwf callings should have been added to the list so we should have 2 flagged callings with CWF data
                //and 2 without flags or CWF data
                int flaggedCount = 0;
                int nonFlaggedCount = 0;
                for(int callingIndex=0; callingIndex < resultSubOrg.getCallings().size(); callingIndex++) {
                    Calling resultCalling = resultSubOrg.getCallings().get(callingIndex);
                    if(resultCalling.getConflictCause() == null) {
                        assertNull(resultCalling.getCwfId());
                        assertNull(resultCalling.getProposedIndId());
                        assertNull(resultCalling.getProposedStatus());
                        assertNull(resultCalling.getNotes());
                        nonFlaggedCount++;
                    } else {
                        assertNotNull(resultCalling.getCwfId());
                        assertNotNull(resultCalling.getProposedIndId());
                        assertNotNull(resultCalling.getProposedStatus());
                        assertNotNull(resultCalling.getNotes());
                        flaggedCount++;
                    }
                }
                assertEquals(nonFlaggedCount, 2);
                assertEquals(flaggedCount, 2);
            }
        }, mockProgressBar, mockActivity);
    }
}