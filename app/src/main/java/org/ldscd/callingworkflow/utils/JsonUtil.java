package org.ldscd.callingworkflow.utils;

public class JsonUtil {
    public static String evalNull(String val) {
        return val.toLowerCase().equals("null") ? null : val;
    }
}