package org.ldscd.callingworkflow.web;

import android.content.SharedPreferences;

import com.android.volley.Response;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.ConfigInfo;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Interface to WebResources.  Only because we are implementing the same interface on LocalFileResources.
 */
@Singleton
public interface IWebResources {
    void setCredentials(String userName, String password);

    void getSharedPreferences(Response.Listener<SharedPreferences> listener);

    void getConfigInfo(Response.Listener<ConfigInfo> configCallback, Response.Listener<WebException> errorCallback);

    void getUserInfo(boolean getClean, Response.Listener<LdsUser> userCallback, Response.Listener<WebException> errorCallback);

    void getOrgs(boolean getCleanCopy, Response.Listener<List<Org>> orgsCallback, Response.Listener<WebException> errorCallback);

    Task<List<Org>> getOrgWithMembers(Long subOrgId);

    void getWardList(Response.Listener<List<Member>> wardCallback, Response.Listener<WebException> errorCallback);

    void getPositionMetaData(Response.Listener<String> callback);

    void releaseCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebException> errorCallback);

    void updateCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebException> errorCallback);

    void deleteCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebException> errorCallback);

    Task<JSONArray> getOrgHierarchy();
}