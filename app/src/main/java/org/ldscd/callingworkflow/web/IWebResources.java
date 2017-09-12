package org.ldscd.callingworkflow.web;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.ConfigInfo;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;

import javax.inject.Singleton;
import java.util.List;

/**
 * Interface to WebResources.  Only because we are implementing the same interface on LocalFileResources.
 */
@Singleton
public interface IWebResources {
    void setCredentials(String userName, String password);

    void getConfigInfo(Response.Listener<ConfigInfo> configCallback);

    void getUserInfo(Response.Listener<LdsUser> userCallback);

    void getOrgs(Response.Listener<List<Org>> orgsCallback);

    void getWardList(Response.Listener<List<Member>> wardCallback);

    void getPositionMetaData(Response.Listener<String> callback);

    void updateCalling(Calling calling, Long unitNumber, UnitLevelOrgType unitLevelOrgType, final Response.Listener<JSONObject> callback) throws JSONException;
}