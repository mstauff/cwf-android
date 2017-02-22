package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
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
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.MemberListRequest;
import org.ldscd.callingworkflow.web.OrgsListRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Service class to interact with assets files within application project.
 */
public class LocalFileResources implements IWebResources {
    private static final String TAG = "LocalFileResourcesLog";
    protected Map<String, File> fileMap;
    private Context context;
    private RequestQueue requestQueue;
    private SharedPreferences preferences;
    private static final String CONFIG_URL = "http://dev-ldscd.rhcloud.com/cwf/config?env=test";
    private static final String UTF8 = "UTF-8";
    private static final String prefUsername = "username";
    private static final String prefPassword = "password";

    private ConfigInfo configInfo = null;
    private String authCookie = null;
    private JSONObject userInfo = null;
    private List<Org> orgsInfo = null;
    private String userName = null;
    private String password = null;
    private char[] sKey = null;
    private byte[] salt = null;
    private List<Member> wardMemberList = null;
    private String unitNumber = null;

    public LocalFileResources(Context context) {
        this.context = context;
    }

    //load username and password which has been set previously
    private void loadCredentials() {
        String cryptUser = preferences.getString(prefUsername, null);
        String cryptPassword = preferences.getString(prefPassword, null);
        if (cryptUser != null && cryptPassword != null) {
            userName = decrypt(cryptUser);
            password = decrypt(cryptPassword);
        }
    }

    @Override
    public void setCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
        getAuthCookie(new Response.Listener<String>() {
            @Override
            public void onResponse(String cookie) {

            }
        });
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

    private void getAuthCookie(final Response.Listener<String> authCallback) {
        if(authCookie != null) {
            authCallback.onResponse(authCookie);
        } else {

            getConfigInfo(new Response.Listener<ConfigInfo>() {
                @Override
                public void onResponse(ConfigInfo config) {
                    if(userName == null || password == null) {
                        loadCredentials();
                    }

                    AuthenticationRequest authRequest = new AuthenticationRequest(userName, password, configInfo.getEndpointUrl("SIGN_IN"),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    authCookie = "clerk-resources-beta-terms=true; " + response;
                                    Log.i(TAG, "auth call successful");

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(prefUsername, encrypt(userName));
                                    editor.putString(prefPassword, encrypt(password));
                                    editor.apply();

                                    authCallback.onResponse(authCookie);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "auth error");
                                    error.printStackTrace();
                                }
                            }
                    );
                    requestQueue.add(authRequest);
                }
            });
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
            String json = getJSONFromAssets("cwf-object.json");
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(new JSONObject(json).getJSONObject("orgWithCallingsInSubOrg"));
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

    private String encrypt( String value ) {

        try {
            final byte[] bytes = value!=null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(sKey));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),UTF8);
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

    }

    private String decrypt(String value){
        try {
            final byte[] bytes = value!=null ? Base64.decode(value,Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(sKey));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(salt, 20));
            return new String(pbeCipher.doFinal(bytes),UTF8);
        } catch( Exception e) {
            Log.e(this.getClass().getName(), "Warning, could not decrypt the value.  It may be stored in plaintext.  "+e.getMessage());
            return value;
        }
    }
}