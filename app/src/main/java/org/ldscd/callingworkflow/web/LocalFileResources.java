package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.JsonObject;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.BuildConfig;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.ConfigInfo;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class to interact with assets files within application project.
 */
public class LocalFileResources implements IWebResources {
    private static final String TAG = "LocalFileResourcesLog";
    private static final String USER_INFO_JSON = "user-info.json";
    private static final String MEMBER_ASSIGNMENTS = "memberAssignments";
    private static final String INDIVIDUAL_ID = "individualId";
    private static final String ORG_CALLINGS_JSON = "org-callings.json";
    private static final String MEMBER_OBJECTS_JSON = "member-objects.json";
    private static final String POSITION_METADATA_JSON = "position-metadata.json";
    private static final String UTF_8 = "UTF-8";
    private static final String UNIT_NUMBER = "unitNumber";
    private static final String SUB_ORG_TYPE_ID = "subOrgTypeId";
    private static final String SUB_ORG_ID = "subOrgId";
    private static final String POSITION_TYPE_ID = "positionTypeId";
    private static final String POSITION = "position";
    private static final String MEMBER_ID = "memberId";
    private static final String YYYY_MD = "yyyyMd";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String RELEASE_POSITION_IDS = "releasePositionIds";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private Context context;
    private RequestQueue requestQueue;
    private static final String CONFIG_URL = BuildConfig.appConfigUrl;// "http://dev-config-server-ldscd.7e14.starter-us-west-2.openshiftapps.com/cwf/config?env=test";
    private ConfigInfo configInfo = null;

    public LocalFileResources(Context context, RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
        this.context = context;
    }

    @Override
    public void setCredentials(String userName, String password) {
    }

    @Override
    public void getSharedPreferences(Response.Listener<SharedPreferences> listener) {
        listener.onResponse(null);
    }

    @Override
    public void getConfigInfo(final Response.Listener<ConfigInfo> configCallback, final Response.Listener<WebException> errorCallback) {
        if(configInfo != null) {
            configCallback.onResponse(configInfo);
        } else {
            GsonRequest<ConfigInfo> configRequest = new GsonRequest<>(
                    Request.Method.GET,
                    CONFIG_URL,
                    ConfigInfo.class,
                    null,
                    null,
                    new Response.Listener<ConfigInfo>() {
                        @Override
                        public void onResponse(ConfigInfo response) {
                            Log.i(TAG, "config call successful");
                            configInfo = response;
                            configCallback.onResponse(configInfo);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "load config error");
                            error.printStackTrace();
                        }
                    }
            );
            requestQueue.add(configRequest);
        }
    }

    public void getUserInfo(Response.Listener<LdsUser> userCallback, Response.Listener<WebException> errorCallback) {
        try {
            if(configInfo == null) {
                getConfigInfo(new Response.Listener<ConfigInfo>() {
                    @Override
                    public void onResponse(ConfigInfo response) {
                        configInfo = response;
                    }
                }, errorCallback);
            }
            JSONObject json = new JSONObject(getJSONFromAssets(USER_INFO_JSON));
            Log.i(TAG, "User call successful");
            Log.i(TAG, json.toString());
            LdsUser user = null;
            try {
                JSONArray assignments = json.getJSONArray(MEMBER_ASSIGNMENTS);
                OrgCallingBuilder builder = new OrgCallingBuilder();
                List<Position> positions = new ArrayList<>();
                for(int i = 0; i < assignments.length(); i++) {
                    positions.add(builder.extractPosition((JSONObject) assignments.get(i)));
                }
                long individualId = json.getLong(INDIVIDUAL_ID);
                user = new LdsUser(individualId, positions);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            userCallback.onResponse(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void signOut(final Response.Listener<Boolean> authCallback, final Response.Listener<WebException> errorCallback) {
        authCallback.onResponse(true);
    }

    public void getOrgs(boolean getCleanCopy, Response.Listener<List<Org>> orgsCallback, Response.Listener<WebException> errorCallback) {
        OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
        List<Org> orgs = new ArrayList<>();
        try {
            String json = getJSONFromAssets(ORG_CALLINGS_JSON);
            JSONArray jsonArray = new JSONArray(json);
            orgs = orgCallingBuilder.extractOrgs(jsonArray, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        orgsCallback.onResponse(orgs);
    }

    @Override
    public Task<List<Org>> getOrgWithMembers(Long subOrgId) {
        TaskCompletionSource<List<Org>> taskCompletionSource = new TaskCompletionSource<>();
        return taskCompletionSource.getTask();
    }

    public void getWardList(Response.Listener<List<Member>> wardCallback, Response.Listener<WebException> errorCallback) {
        MemberListRequest memberListRequest = new MemberListRequest(null, null, null);
        List<Member> allMembers = memberListRequest.getMembers(getJSONFromAssets(MEMBER_OBJECTS_JSON));
        List<Member> members = new ArrayList<>();
        for(Member member : allMembers) {
            if(member.getIndividualId() > 0) {
                members.add(member);
            }
        }
        wardCallback.onResponse(members);
    }

    public void getPositionMetaData(Response.Listener<String> callback) {
        callback.onResponse(getJSONFromAssets(POSITION_METADATA_JSON));
    }

    private String getJSONFromAssets(String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void updateCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, final Response.Listener<WebException> errorCallback) {
        /*
        json: {
         "unitNumber": 56030,
         "subOrgTypeId": 1252,
         "subOrgId": 2081422,
         "positionTypeId": 216,
         "position": "any non-empty string"
         "memberId": "17767512672",
         "releaseDate": "20170801",
         "releasePositionIds": [
         38816970
         ]
         */
        org.json.JSONObject json = new org.json.JSONObject();
        try {
            json.put(UNIT_NUMBER, unitNumber);
            json.put(SUB_ORG_TYPE_ID, orgTypeId);
            json.put(SUB_ORG_ID, calling.getParentOrg());
            json.put(POSITION_TYPE_ID, calling.getPosition().getPositionTypeId());
            json.put(POSITION, calling.getPosition().getName());
            json.put(MEMBER_ID, calling.getProposedIndId());
            LocalDate date = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormat.forPattern(YYYY_MD);
            json.put(RELEASE_DATE, date.toString(fmt));
            JSONArray positionIds = new JSONArray();
            positionIds.put(calling.getId());
            json.put(RELEASE_POSITION_IDS, positionIds);
            Map<String, String> header = new HashMap<String, String>();
            header.put(CONTENT_TYPE, APPLICATION_JSON);
            GsonRequest<String> gsonRequest = new GsonRequest<String>(
                    Request.Method.POST,
                    configInfo.getUpdateCallingUrl(),
                    String.class,
                    header,
                    json,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                callback.onResponse(new JSONObject(response));
                            } catch (JSONException e) {
                                Log.e("Json Parse LDS update", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "load config error");
                            error.printStackTrace();
                        }
                    }
            );
            requestQueue.add(gsonRequest);
        } catch(JSONException e) {
            errorCallback.onResponse(new WebException(ExceptionType.PARSING_ERROR, e));
        }
    }

    public void releaseCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebException> errorCallback) {

    }

    public void deleteCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback, Response.Listener<WebException> errorCallback) {

    }

    @Override
    public Task<JSONArray> getOrgHierarchy() {
        return null;
    }

    private void removeCalling(JsonObject jsonObject) {

    }

    private JSONObject createJsonObject(Long unitNumber, Calling calling) throws JSONException {
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        jsonObject.put(UNIT_NUMBER, unitNumber);
        //jsonObject.put("subOrgTypeId", unitLevelOrgType.getOrgTypeId());
        jsonObject.put(SUB_ORG_ID, calling.getParentOrg());
        jsonObject.put(POSITION_TYPE_ID, calling.getPosition().getPositionTypeId());
        jsonObject.put(POSITION, calling.getPosition().getName());
        jsonObject.put(MEMBER_ID, calling.getProposedIndId());
        LocalDate date = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern(YYYY_MD);
        jsonObject.put(RELEASE_DATE, date.toString(fmt));
        JSONArray positionIds = new JSONArray();
        positionIds.put(calling.getId());
        jsonObject.put(RELEASE_POSITION_IDS, positionIds);
        return jsonObject;
    }

    private void postJson(String url, JSONObject data, final Response.Listener<JSONObject> listener) {
        JsonObjectRequest jsonObjectRequest =
            new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Post Json", error.getMessage());
                    }
                }
            );
        requestQueue.add(jsonObjectRequest);
    }
}