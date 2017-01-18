package org.ldscd.callingworkflow.model;

import java.util.List;
import java.util.Map;

public class ConfigInfo {
    private String[] statuses;
    private Map<String,String> ldsEndpointUrls;
    private List<OrgType> orgTypes;

    public String[] getStatuses() {
        return statuses;
    }

    public void setStatuses(String[] statuses) {
        this.statuses = statuses;
    }

    public Map<String,String> getLdsEndpointUrls() {
        return ldsEndpointUrls;
    }

    public String getEndpointUrl(String key) {
        return ldsEndpointUrls.get(key);
    }

    public void setLdsEndpointUrls(Map<String,String> ldsEndpointUrls) {
        this.ldsEndpointUrls = ldsEndpointUrls;
    }

    public List<OrgType> getOrgTypes() {
        return orgTypes;
    }

    public void setOrgTypes(List<OrgType> orgTypes) {
        this.orgTypes = orgTypes;
    }
}
