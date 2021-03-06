package org.ldscd.callingworkflow.model.permissions.constants;

/**
 * Required Permission
 */
public enum Permission {
    /* Org Info is LCR Specific */
    ORG_INFO_READ,
    ORG_INFO_UPDATE,
    /* Potential Calling */
    POTENTIAL_CALLING_CREATE,
    POTENTIAL_CALLING_UPDATE,
    POTENTIAL_CALLING_DELETE,
    /* Active Calling */
    ACTIVE_CALLING_CREATE,
    ACTIVE_CALLING_UPDATE,
    ACTIVE_CALLING_DELETE,
    ACTIVE_CALLING_RELEASE,
    /* Unit Google Account */
    UNIT_GOOGLE_ACCOUNT_CREATE,
    UNIT_GOOGLE_ACCOUNT_UPDATE,
    /* Priesthood Office */
    PRIESTHOOD_OFFICE_READ
}