package org.ldscd.callingworkflow.web;


public enum ExceptionType {
    NO_DATA_CONNECTION,
    LDS_AUTH_REQUIRED,
    GOOGLE_AUTH_REQUIRED,
    SESSION_EXPIRED,
    SERVER_UNAVAILABLE,
    PARSING_ERROR,
    UNKNOWN_EXCEPTION
}
