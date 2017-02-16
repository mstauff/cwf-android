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
    private LocalFileResources localFileResources;

    private List<Org> orgs;
    private Map<Long, Org> orgsById;
    private Map<Long, List<Calling>> callingsByOrg;
    private Map<Long, Calling> callingsById;

    public CallingData(IWebResources webResources, LocalFileResources localFileResources) {
        this.webResources = webResources;
        this.localFileResources = localFileResources;
    }

    // TODO: This needs to pull in info from google as well and merge the two
    private void loadOrgs(final Response.Listener<List<Org>> orgsCallback) {
        localFileResources.getOrgs(new Response.Listener<List<Org>>() {
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

    public void getOrgs(Response.Listener<List<Org>> orgsCallback) {
        if(orgs != null) {
            orgsCallback.onResponse(orgs);
        } else {
            loadOrgs(orgsCallback);
        }
    }

    public void getOrg(final long orgId, final Response.Listener<Org> orgCallback) {
        if(orgsById != null) {
            orgCallback.onResponse(orgsById.get(orgId));
        } else {
            loadOrgs(new Response.Listener<List<Org>> () {
                @Override
                public void onResponse(List<Org> response) {
                    orgCallback.onResponse(orgsById.get(orgId));
                }
            });
        }
    }

    public void getCallings(final long orgId, final Response.Listener<List<Calling>> callingsCallback) {
        if(callingsByOrg != null) {
            callingsCallback.onResponse(callingsByOrg.get(orgId));
        } else {
            loadOrgs(new Response.Listener<List<Org>> () {
                @Override
                public void onResponse(List<Org> response) {
                    callingsCallback.onResponse(callingsByOrg.get(orgId));
                }
            });
        }
    }

    public void getCalling(final long positionId, final Response.Listener<Calling> callingCallback) {
        if(callingsById != null) {
            callingCallback.onResponse(callingsById.get(positionId));
        } else {
            loadOrgs(new Response.Listener<List<Org>>() {
                @Override
                public void onResponse(List<Org> response) {
                    callingCallback.onResponse(callingsById.get(positionId));
                }
            });
        }
    }
}
