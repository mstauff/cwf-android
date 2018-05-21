package org.ldscd.callingworkflow;

import com.android.volley.Response;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.web.OrgCallingBuilder;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class OrgsListRequestTest {
    /* Fields. */
    OrgCallingBuilder orgCallingBuilder;
    JSONObject jsonObject;
    JSONArray jsonArray;
    String json = "";
    List<Org> orgs = new ArrayList<>();
    /* Methods. */

    @Before
    public void prepData() throws JSONException {
        getJSONFromAssets(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                json = response;
                try {
                    jsonArray = new JSONArray(json);
                    orgCallingBuilder = new OrgCallingBuilder();
                    orgs = orgCallingBuilder.extractOrgs(jsonArray, false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void extractOrgsTest() {
        assertNotNull("the field Orgs", orgs);
        assertTrue("the field Orgs", orgs.size() > 0);
        for(Org org : orgs) {
            evaluateOrg(org);
        }
    }

    private void evaluateOrg(Org org) {
        evaluateNumber(org.getId(), "OrgId");
        evaluateString(org.getDefaultOrgName(), "OrgName");
        evaluateNumber(org.getOrgTypeId(), "OrgTypeId");
        evaluateNumber(org.getDisplayOrder(), "DisplayOrder");
        evaluateCalling(org.getCallings());
        evaluateCalling(org.getCallings());
        for(Org child : org.getChildren()) {
            evaluateOrg(child);
        }
    }

    private void evaluateCalling(List<Calling> callings) {
        if(callings != null) {
            for(Calling calling : callings) {
                //evaluateString(calling.getCallingId(), "calling.getCallingId");
                assertNotNull("the Field: calling.getPosition", calling.getPosition());
                evaluateNumber(calling.getId(), "calling.getId");
                //evaluateString(calling.getCwfId(), "calling.getCwfId");

                if(calling.getActiveDate() != null) {
                    assertTrue("the Field: calling.activeDate after 20 years ago", calling.getActiveDateTime().isAfter(DateTime.now().minusYears(20)));
                    assertTrue("the Field: calling.activeDate before current date", calling.getActiveDateTime().isBefore(DateTime.now()));
                }

                if(calling.getCwfId() == null || calling.getCwfId().length() == 0) {
                    evaluateNumber(calling.getMemberId(), "calling.getMemberId. Calling.ID = " + calling.getCallingId());
                }
                evaluatePosition(calling.getPosition());
            }
        }
    }

   private void evaluatePosition(Position position) {
       assertNotNull(position);
       assertNotNull(position.getName());
       assertTrue(position.getPositionTypeId() > 0);
   }

   
    private void evaluateNumber(Number field, String fieldName) {
        assertNotNull("the Field: " + fieldName, field);
        assertTrue("the Field: " + fieldName, field.longValue() > 0);
    }

    private void evaluateString(String field, String fieldName) {
        assertNotNull("the Field: " + fieldName, field);
        assertFalse("the Field: " + fieldName, field.equals("null"));
        assertTrue("the Field: " + fieldName, field.length() > 0);
    }

    private void evaluateBoolean(Boolean field, String fieldName) {
        assertNotNull("the Field: " + fieldName, field);
    }

    private void getJSONFromAssets(Response.Listener<String> listener) {
        String json = null;
        try {
            InputStream is = new FileInputStream("./app/src/main/assets/org-callings.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            listener.onResponse(null);
        }
        listener.onResponse(json);
    }
}