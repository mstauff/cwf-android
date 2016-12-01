package org.ldscd.callingworkflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.dataObjects.ConfigInfo;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.inject.Inject;

public class WebResources {
    private static final String TAG = "WebResourcesLog";
    private static final String CONFIG_URL = "http://dev-ldscd.rhcloud.com/cwf/config?env=test";
    private static final String UTF8 = "UTF-8";
    private static final String prefUsername = "username";
    private static final String prefPassword = "password";

    private RequestQueue requestQueue;
    private SharedPreferences preferences;

    private ConfigInfo configInfo = null;
    private String authCookie = null;
    private String unitNumber = null;
    private char[] sKey = null;
    private byte[] salt = null;

    public WebResources(Context context, RequestQueue requestQueue, SharedPreferences preferences) {
        this.requestQueue = requestQueue;
        this.preferences = preferences;
        String key = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        sKey = key.toCharArray();
        try {
            salt = key.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void addToRequestQueue(Request<T> req) {
        requestQueue.add(req);
    }

    public void loadResources() {
        String cryptUser = preferences.getString(prefUsername, null);
        String cryptPassword = preferences.getString(prefPassword, null);
        if (cryptUser != null && cryptPassword != null) {
            loadConfigInfo(decrypt(cryptUser), decrypt(cryptPassword));
        }
    }

    public void setCredentialsAndLoadResources(String userName, String password){
        loadConfigInfo(userName, password);
    }

    private void loadConfigInfo(final String user, final String password) {
        GsonRequest<ConfigInfo> configRequest = new GsonRequest<>(
                CONFIG_URL,
                ConfigInfo.class,
                null,
                new Response.Listener<ConfigInfo>() {
                    @Override
                    public void onResponse(ConfigInfo response) {
                        Log.i(TAG, "config call successful");
                        configInfo = response;
                        loadAuthCookieAndResources(user, password);
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
        addToRequestQueue(configRequest);
    }

    private void loadAuthCookieAndResources(final String user, final String password) {
        AuthenticationRequest authRequest = new AuthenticationRequest(user, password, configInfo.getEndpointUrl("SIGN_IN"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        authCookie = "clerk-resources-beta-terms=true; " + response;
                        Log.i(TAG, "auth call successful");

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(prefUsername, encrypt(user));
                        editor.putString(prefPassword, encrypt(password));
                        editor.apply();

                        loadUserInfo(authCookie);
                        loadOrgs(authCookie);
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
        addToRequestQueue(authRequest);
    }

    private void loadUserInfo(final String authCookie) {
        JsonObjectRequest userRequest = new JsonObjectRequest(
                Request.Method.GET,
                configInfo.getEndpointUrl("USER_DATA"),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "User call successful");
                        Log.i(TAG, response.toString());
                        try {
                            unitNumber = response.getJSONArray("memberAssignments").getJSONObject(0).getString("unitNo");
                            loadWardListWithCallings(authCookie, unitNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "load user error");
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", authCookie);
                return headers;
            }
        };
        addToRequestQueue(userRequest);
    }

    private void loadOrgs(final String cookie) {
        final JsonArrayRequest wardListRequest = new JsonArrayRequest(
                Request.Method.GET,
                "https://test.lds.org/mls/mbr/services/orgs/sub-orgs-with-callings?lang=eng",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG, "Positions call successful");
                        Log.i(TAG, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "load Orgs list error");
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", cookie);
                return headers;
            }

        };
        addToRequestQueue(wardListRequest);
    }

    private void loadWardListWithCallings(final String authCookie, final String unitNumber) {
        final JsonArrayRequest wardListRequest = new JsonArrayRequest(
                Request.Method.GET,
                configInfo.getEndpointUrl("MEMBER_LIST").replace(":unitNum", unitNumber),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG, "Member call successful");
                        Log.i(TAG, response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "load ward list error");
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", authCookie);
                return headers;
            }

        };
        addToRequestQueue(wardListRequest);
    }

    protected String encrypt( String value ) {

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

    protected String decrypt(String value){
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
