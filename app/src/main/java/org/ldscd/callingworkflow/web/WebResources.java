package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.ConfigInfo;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebResources implements IWebResources {
    private static final String TAG = "WebResourcesLog";
    private static final String CONFIG_URL = "http://dev-ldscd.rhcloud.com/cwf/config?env=test";
    private static final String UTF8 = "UTF-8";
    private static final String prefUsername = "username";
    private static final String prefPassword = "password";

    private RequestQueue requestQueue;
    private SharedPreferences preferences;

    private ConfigInfo configInfo = null;
    private String authCookie = null;
    private JSONObject userInfo = null;
    private List<Org> orgsInfo = null;
    private List<Member> wardMemberList = null;
    private String unitNumber = null;

    private String userName = null;
    private String password = null;
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

    //load username and password which has been set previously
    private void loadCredentials() {
        String cryptUser = preferences.getString(prefUsername, null);
        String cryptPassword = preferences.getString(prefPassword, null);
        if (cryptUser != null && cryptPassword != null) {
            userName = decrypt(cryptUser);
            password = decrypt(cryptPassword);
        }
    }

    public void setCredentials(String userName, String password){
        this.userName = userName;
        this.password = password;
        getAuthCookie(new Response.Listener<String>() {
            @Override
            public void onResponse(String cookie) {

            }
        });
    }

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

    @Override
    public void getUserInfo(final Response.Listener<JSONObject> userCallback) {
        if(userInfo != null) {
            userCallback.onResponse(userInfo);
        } else {

            getAuthCookie(new Response.Listener<String>() {
                @Override
                public void onResponse(final String authCookie) {

                    JsonObjectRequest userRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            configInfo.getEndpointUrl("USER_DATA"),
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    userInfo = response;
                                    Log.i(TAG, "User call successful");
                                    Log.i(TAG, response.toString());
                                    try {
                                        unitNumber = response.getJSONArray("memberAssignments").getJSONObject(0).getString("unitNo");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    userCallback.onResponse(userInfo);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "load user error");
                                    error.printStackTrace();
                                    unitNumber = "56030";
                                    userCallback.onResponse(new JSONObject());
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
                    requestQueue.add(userRequest);

                }
            });

        }
    }

    @Override
    public void getOrgs(final Response.Listener<List<Org>> orgsCallback) {
        if(orgsInfo != null) {
            orgsCallback.onResponse(orgsInfo);
        } else {

            getUserInfo(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject userInfo) {

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Cookie", authCookie);
                    OrgsListRequest orgsRequest = new OrgsListRequest(
                            configInfo.getEndpointUrl("CALLING_LIST"),
                            headers,
                            new Response.Listener<List<Org>>() {
                                @Override
                                public void onResponse(List<Org> response) {
                                    orgsInfo = response;
                                    Log.i(TAG, "Positions call successful");
                                    orgsCallback.onResponse(orgsInfo);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "load Orgs list error");
                                    error.printStackTrace();
                                }
                            }
                    );
                    requestQueue.add(orgsRequest);

                }
            });

        }
    }

    @Override
    public void getWardList(final Response.Listener<List<Member>> wardCallback) {
        if(wardMemberList != null) {
            wardCallback.onResponse(wardMemberList);
        } else {

            getUserInfo(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject userInfo) {

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Cookie", authCookie);
                    MemberListRequest wardListRequest = new MemberListRequest(
                            configInfo.getEndpointUrl("MEMBER_LIST").replace(":unitNum", unitNumber),
                            headers,
                            new Response.Listener<List<Member>>() {
                                @Override
                                public void onResponse(List<Member> response) {
                                    wardMemberList = response;
                                    Log.i(TAG, "Member call successful");
                                    wardCallback.onResponse(wardMemberList);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "load ward list error");
                                    error.printStackTrace();
                                }
                            }
                    );
                    requestQueue.add(wardListRequest);

                }
            });

        }
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
