package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.UnitLevelOrgType;
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

import static java.net.Proxy.Type.HTTP;

/**
 * Service class to interact with assets files within application project.
 */
public class LocalFileResources implements IWebResources {
    private static final String TAG = "LocalFileResourcesLog";
    private Context context;
    private RequestQueue requestQueue;
    private static final String CONFIG_URL = "http://dev-config-server-ldscd.7e14.starter-us-west-2.openshiftapps.com/cwf/config?env=test";
    private ConfigInfo configInfo = null;

    public LocalFileResources(Context context, RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
        this.context = context;
    }

    @Override
    public void setCredentials(String userName, String password) {
    }

    @Override
    public void getConfigInfo(final Response.Listener<ConfigInfo> configCallback) {
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

    public void getUserInfo(Response.Listener<LdsUser> userCallback) {
        try {
            if(configInfo == null) {
                getConfigInfo(new Response.Listener<ConfigInfo>() {
                    @Override
                    public void onResponse(ConfigInfo response) {
                        configInfo = response;
                    }
                });
            }
            JSONObject json = new JSONObject(getJSONFromAssets("user-info.json"));
            Log.i(TAG, "User call successful");
            Log.i(TAG, json.toString());
            LdsUser user = null;
            try {
                JSONArray assignments = json.getJSONArray("memberAssignments");
                OrgCallingBuilder builder = new OrgCallingBuilder();
                List<Position> positions = new ArrayList<>();
                for(int i = 0; i < assignments.length(); i++) {
                    positions.add(builder.extractPosition((JSONObject) assignments.get(i)));
                }
                long individualId = json.getLong("individualId");
                user = new LdsUser(individualId, positions);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            userCallback.onResponse(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getOrgs(Response.Listener<List<Org>> orgsCallback) {
        OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
        List<Org> orgs = new ArrayList<>();
        try {
            String json = getJSONFromAssets("org-callings.json");
            JSONArray jsonArray = new JSONArray(json);
            orgs = orgCallingBuilder.extractOrgs(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        orgsCallback.onResponse(orgs);
    }

    public void getWardList(Response.Listener<List<Member>> wardCallback) {
        MemberListRequest memberListRequest = new MemberListRequest(null, null, null, null);
        List<Member> allMembers = memberListRequest.getMembers(getJSONFromAssets("member-objects.json"));
        List<Member> members = new ArrayList<>();
        for(Member member : allMembers) {
            if(member.getIndividualId() > 0) {
                members.add(member);
            }
        }
        wardCallback.onResponse(members);
    }

    public void getPositionMetaData(Response.Listener<String> callback) {
        callback.onResponse(getJSONFromAssets("position-metadata.json"));
    }

    private String getJSONFromAssets(String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void updateCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback) throws JSONException {
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
        json.put("unitNumber", unitNumber);
        json.put("subOrgTypeId", orgTypeId);
        json.put("subOrgId", calling.getParentOrg());
        json.put("positionTypeId", calling.getPosition().getPositionTypeId());
        json.put("position", calling.getPosition().getName());
        json.put("memberId", calling.getProposedIndId());
        LocalDate date = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMd");
        json.put("releaseDate", date.toString(fmt));
        JSONArray positionIds = new JSONArray();
        positionIds.put(calling.getId());
        json.put("releasePositionIds", positionIds);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", "application/json");
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
                    Log.e("Network", error.getMessage());
                }
            }
        );
        requestQueue.add(gsonRequest);
    }

    public void releaseCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback) throws JSONException {

    }

    public void deleteCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback) throws JSONException {

    }

    private void removeCalling(JsonObject jsonObject) {

    }

    private JSONObject createJsonObject(Long unitNumber, Calling calling) throws JSONException {
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        jsonObject.put("unitNumber", unitNumber);
        //jsonObject.put("subOrgTypeId", unitLevelOrgType.getOrgTypeId());
        jsonObject.put("subOrgId", calling.getParentOrg());
        jsonObject.put("positionTypeId", calling.getPosition().getPositionTypeId());
        jsonObject.put("position", calling.getPosition().getName());
        jsonObject.put("memberId", calling.getProposedIndId());
        LocalDate date = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMd");
        jsonObject.put("releaseDate", date.toString(fmt));
        JSONArray positionIds = new JSONArray();
        positionIds.put(calling.getId());
        jsonObject.put("releasePositionIds", positionIds);
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