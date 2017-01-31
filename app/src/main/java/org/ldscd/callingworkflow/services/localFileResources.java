package org.ldscd.callingworkflow.services;

import android.content.Context;
import com.android.volley.Response;
import dagger.Module;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.MemberListRequest;
import org.ldscd.callingworkflow.web.OrgsListRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service class to interact with assets files within application project.
 */
 @Module
public class localFileResources {
    protected Map<String, File> fileMap;
    private Context context;

    public localFileResources(Context context) {
        this.context = context;
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
            orgs = orgsListRequest.extractOrgs(new JSONArray(getJSONFromAssets("org-callings.json")));
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