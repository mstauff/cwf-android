package org.ldscd.callingworkflow.web;


import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallingData {

    private IWebResources webResources;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<Long, List<Calling>> callingsByOrg;
    private Map<Long, Calling> callingsById;

    public CallingData(IWebResources webResources) {
        this.webResources = webResources;
    }

    // TODO: This needs to pull in info from google as well and merge the two
    public void loadOrgs(final Response.Listener<List<Org>> orgsCallback) {
        webResources.getOrgs(new Response.Listener<List<Org>>() {
            @Override
            public void onResponse(List<Org> response) {
                orgs = response;
                orgsById = new HashMap<Long, Org>();
                callingsByOrg = new HashMap<Long, List<Calling>>();
                callingsById = new HashMap<Long, Calling>();
                for(Org org: orgs) {
                    callingsByOrg.put(org.getId(), new ArrayList<Calling>());
                    extractOrg(org, org.getId());
                }
                orgsCallback.onResponse(orgs);
            }
        });
    }

    private void extractOrg(Org org, long baseOrgId) {
        orgsById.put(org.getId(), org);
        List<Calling> callingsList = callingsByOrg.get(baseOrgId);
        for(Calling calling: org.getCallings()) {
            callingsById.put(calling.getPositionId(), calling);
            callingsList.add(calling);
        }
        for(Org subOrg: org.getChildren()) {
            extractOrg(subOrg, baseOrgId);
        }
    }

    public List<Org> getOrgs() {
        return orgs;
    }

    public Org getOrg(long orgId) {
        return orgsById.get(orgId);
    }

    //returns all callings in an org, including callings from subOrgs
    //this only works for base organizations
    public List<Calling> getCallings(long orgId) {
        return callingsByOrg.get(orgId);
    }

    public Calling getCalling(long positionId) {
        return callingsById.get(positionId);
    }
}
