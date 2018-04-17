package org.ldscd.callingworkflow.web;

import android.content.SharedPreferences;

import com.android.volley.Response;

import org.json.JSONObject;
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

    void getSharedPreferences(Response.Listener<SharedPreferences> listener);

    void getConfigInfo(Response.Listener<ConfigInfo> configCallback, Response.Listener<WebResourcesException> errorCallback);

    void getUserInfo(boolean getClean, Response.Listener<LdsUser> userCallback, Response.Listener<WebResourcesException> errorCallback);

    void getOrgs(boolean getCleanCopy, Response.Listener<List<Org>> orgsCallback, Response.Listener<WebResourcesException> errorCallback);

    void getWardList(Response.Listener<List<Member>> wardCallback, Response.Listener<WebResourcesException> errorCallback);

    void getPositionMetaData(Response.Listener<String> callback);

    void releaseCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebResourcesException> errorCallback);

    void updateCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebResourcesException> errorCallback);

    void deleteCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebResourcesException> errorCallback);
}