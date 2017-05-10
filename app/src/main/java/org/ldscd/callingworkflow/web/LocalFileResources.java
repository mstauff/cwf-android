package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.ConfigInfo;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class to interact with assets files within application project.
 */
public class LocalFileResources implements IWebResources {
    private static final String TAG = "LocalFileResourcesLog";
    private Context context;
    private RequestQueue requestQueue;
    private static final String CONFIG_URL = "http://dev-ldscd.rhcloud.com/cwf/config?env=test";
    private ConfigInfo configInfo = null;

    public LocalFileResources(Context context) {
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
                    CONFIG_URL,
                    ConfigInfo.class,
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

    public void getUserInfo(Response.Listener<JSONObject> userCallback) {
        try {
            userCallback.onResponse(new JSONObject(getJSONFromAssets("user-info.json")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getOrgs(Response.Listener<List<Org>> orgsCallback) {
        OrgsListRequest orgsListRequest = new OrgsListRequest(null, null, null, null);
        List<Org> orgs = new ArrayList<>();
        try {
            String json = getJSONFromAssets("org-callings.json");
            JSONArray jsonArray = new JSONArray(json);
            orgs = orgsListRequest.extractOrgs(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        orgsCallback.onResponse(orgs);
    }

    public void getWardList(Response.Listener<List<Member>> wardCallback) {
        MemberListRequest memberListRequest = new MemberListRequest(null, null, null, null);
        List<Member> members = memberListRequest.getMembers(getJSONFromAssets("member-objects.json"));
        wardCallback.onResponse(members);
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
}