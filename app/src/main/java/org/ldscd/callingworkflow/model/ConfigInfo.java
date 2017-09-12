package org.ldscd.callingworkflow.model;

import java.util.List;
import java.util.Map;

public class ConfigInfo {
    private static final String LDS_ENDPOINTS = "ldsEndpointUrls";
    private static final String USER_DATA = "USER_DATA";
    private static final String SIGN_IN = "SIGN_IN";
    private static final String SIGN_OUT = "SIGN_OUT";
    private static final String MEMBER_LIST = "MEMBER_LIST";
    private static final String MEMBER_LIST_SECONDARY = "MEMBER_LIST_SECONDARY";
    private static final String UPDATE_CALLING = "UPDATE_CALLING";
    private static final String CALLING_LIST = "CALLING_LIST";

    private String[] statuses;
    private Map<String,String> ldsEndpointUrls;

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

    public String getUserDataUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(USER_DATA);
    }

    public String getSignInUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(SIGN_IN);
    }

    public String getSignOutUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(SIGN_OUT);
    }

    public String getMemberListUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(MEMBER_LIST);
    }

    public String getMemberListSecondaryUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(MEMBER_LIST_SECONDARY);
    }

    public String getUpdateCallingUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(UPDATE_CALLING);
    }

    public String getCallingListUrl() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(CALLING_LIST);
    }
}