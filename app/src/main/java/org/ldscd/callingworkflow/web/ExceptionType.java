package org.ldscd.callingworkflow.web;


public enum ExceptionType {
    NO_DATA_CONNECTION,
    LDS_AUTH_REQUIRED,
    LDS_SERVER_PROVIDED_MESSAGE,
    GOOGLE_AUTH_REQUIRED,
    SESSION_EXPIRED,
    SERVER_UNAVAILABLE,
    PARSING_ERROR,
    UNKOWN_GOOGLE_EXCEPTION,
    UNKNOWN_EXCEPTION
}
