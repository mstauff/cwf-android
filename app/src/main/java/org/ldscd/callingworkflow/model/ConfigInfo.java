package org.ldscd.callingworkflow.model;

import java.util.List;
import java.util.Map;

public class ConfigInfo {
    public static final String LDS_ENDPOINTS = "ldsEndpointUrls";
    public static final String USER_DATA = "USER_DATA";
    public static final String SIGN_IN = "SIGN_IN";
    public static final String SIGN_OUT = "SIGN_OUT";
    public static final String MEMBER_LIST = "MEMBER_LIST";
    public static final String MEMBER_LIST_SECONDARY = "MEMBER_LIST_SECONDARY";
    public static final String UPDATE_CALLING = "UPDATE_CALLING";
    public static final String CALLING_LIST = "CALLING_LIST";
    public static final String CLASS_ASSIGNMENTS = "CLASS_ASSIGNMENTS";

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

    public String getClassAssignments() {
        return this.ldsEndpointUrls.isEmpty() ? null : this.ldsEndpointUrls.get(CLASS_ASSIGNMENTS);
    }
}