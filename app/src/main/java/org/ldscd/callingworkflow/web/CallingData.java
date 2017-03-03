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
                    orgsById.put(org.getId(), org);
                    List<Calling> callings = extractCallings(org);
                    for(Calling calling: callings) {
                        callingsById.put(calling.getPositionId(), calling);
                    }
                    callingsByOrg.put(org.getId(), callings);
                }
                orgsCallback.onResponse(orgs);
            }
        });
    }

    private List<Calling> extractCallings(Org org) {
        List<Calling> orgCallings = new ArrayList<>();
        for(Calling calling: org.getCallings()) {
            orgCallings.add(calling);
        }
        for(Org subOrg: org.getChildren()) {
            orgCallings.addAll(extractCallings(subOrg));
        }
        return orgCallings;
    }

    public List<Org> getOrgs() {
        return orgs;
    }

    public Org getOrg(long orgId) {
        return orgsById.get(orgId);
    }

    //returns all callings in an org, including callings from subOrgs
    public List<Calling> getCallings(long orgId) {
        return callingsByOrg.get(orgId);
    }

    public Calling getCalling(long positionId) {
        return callingsById.get(positionId);
    }
}
