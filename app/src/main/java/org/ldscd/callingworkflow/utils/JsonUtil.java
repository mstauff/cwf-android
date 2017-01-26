package org.ldscd.callingworkflow.utils;

import com.google.gson.JsonObject;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;

public class JsonUtil {
    public static Org getOrgFromJson(JsonObject json) {
        return new Org();
    }
    public static Member getMemberFromJson(JsonObject json) {
        return new Member();
    }
}