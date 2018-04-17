package org.ldscd.callingworkflow.web;


import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

public class WebResourcesException  extends VolleyError {
    private ExceptionType exceptionType;
    private Exception exception;

    public WebResourcesException(ExceptionType exceptionType) {
        super();
        this.exceptionType = exceptionType;
    }

    public WebResourcesException(ExceptionType exceptionType, Exception exception) {
        this.exception = exception;
        this.exceptionType = exceptionType;
    }

    public WebResourcesException(ExceptionType exceptionType, NetworkResponse networkResponse) {
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
        if(responseBody.contains("sso/UI/Login")) {
            result = true;
        }
        return result;
    }

    public static boolean isWebsiteDownResponse(String responseBody) {
        boolean result = false;
        if(responseBody.contains("outofservice.lds.org")) {
            result = true;
        }
        return result;
    }
}
