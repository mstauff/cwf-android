package org.ldscd.callingworkflow.web;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

public class WebException extends VolleyError {
    private static final String SSO_UI_LOGIN = "sso/UI/Login";
    private static final String OUTOFSERVICE_LDS_ORG = "outofservice.lds.org";
    private ExceptionType exceptionType;
    private Exception exception;

    public WebException(ExceptionType exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public WebException(ExceptionType exceptionType, Exception exception) {
        this.exception = exception;
        this.exceptionType = exceptionType;
    }

    public WebException(ExceptionType exceptionType, NetworkResponse networkResponse) {
        super(networkResponse);
        this.exceptionType = exceptionType;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public static boolean isSessionExpiredResponse(String responseBody) {
        boolean result = false;
        if(responseBody.contains(SSO_UI_LOGIN)) {
            result = true;
        }
        return result;
    }

    public static boolean isWebsiteDownResponse(String responseBody) {
        boolean result = false;
        if(responseBody.contains(OUTOFSERVICE_LDS_ORG)) {
            result = true;
        }
        return result;
    }
}