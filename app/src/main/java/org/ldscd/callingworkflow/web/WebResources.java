package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.ConfigInfo;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebResources implements IWebResources {
    private static final String TAG = "WebResourcesLog";
    private static final String CONFIG_URL = "http://dev-config-server-ldscd.7e14.starter-us-west-2.openshiftapps.com/cwf/config?env=test";
    private static final String LDS_ENDPOINTS = "ldsEndpointUrls";
    private static final String USER_DATA = "USER_DATA";
    private static final String SIGN_IN = "SIGN_IN";
    private static final String SIGN_OUT = "SIGN_OUT";
    private static final String MEMBER_LIST = "MEMBER_LIST";
    private static final String MEMBER_LIST_SECONDARY = "MEMBER_LIST_SECONDARY";
    private static final String UPDATE_CALLING = "UPDATE_CALLING";
    private static final String CALLING_LIST = "CALLING_LIST";
    private static final String prefUsername = "username";
    private static final String prefPassword = "password";
    private static final String UTF8 = "UTF-8";
    private static final String ALGORITHM = "AES";
    private static final String DIGEST = "SHA-1";
    private static final String ENCRYPT_TYPE = "AES/CBC/PKCS5Padding";

    private RequestQueue requestQueue;
    private SharedPreferences preferences;

    private ConfigInfo configInfo = null;
    private String authCookie = null;
    private LdsUser userInfo = null;
    private List<Org> orgsInfo = null;
    private List<Member> wardMemberList = null;
    private String unitNumber = null;

    private String userName = null;
    private String password = null;
    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static byte[] iv;
    private static String secret;
    private final CookieManager cookieManager;
    private URI uri;

    public WebResources(Context context, RequestQueue requestQueue, SharedPreferences preferences) {
        this.requestQueue = requestQueue;
        this.preferences = preferences;
        secret = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        iv = new byte[16];
        Arrays.fill(iv, (byte) 0x00);
        this.userName = "ngiwb1";
        this.password = "password1";
        this.cookieManager = new CookieManager();
        try {
            uri = new URI(CONFIG_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
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

    @Override
    public void setCredentials(String userName, String password) {
        this.userName = "ngiwb1";
        this.password = "password1";
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
            configRequest.setRetryPolicy(getRetryPolicy());
            requestQueue.add(configRequest);
        }
    }

    private void getAuthCookie(final Response.Listener<String> authCallback) {
        //preferences.edit().remove("Cookie");
        if(preferences.contains("Cookie")) {
            authCookie = preferences.getString("Cookie", null);
            authCallback.onResponse(authCookie);
        } else {
            AuthenticationRequest authRequest = new AuthenticationRequest(userName, password, configInfo.getEndpointUrl("SIGN_IN"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        authCookie = "clerk-resources-beta-terms=true; clerk-resources-beta-eula=4.2; " + response;
                        Log.i(TAG, "auth call successful");
                        HttpCookie httpCookie = new HttpCookie("cwf", authCookie);
                        cookieManager.getCookieStore().add(uri, httpCookie);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(prefUsername, encrypt(userName));
                        editor.putString(prefPassword, encrypt(password));
                        editor.putString("Cookie", authCookie);
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
            authRequest.setRetryPolicy(getRetryPolicy());
            requestQueue.add(authRequest);
        }
    }

    @Override
    public void getUserInfo(final Response.Listener<LdsUser> userCallback) {
        if(userInfo != null) {
            userCallback.onResponse(userInfo);
        } else {
            getConfigInfo(new Response.Listener<ConfigInfo>() {
                @Override
                public void onResponse(ConfigInfo config) {
                    if (userName == null || password == null) {
                      loadCredentials();
                    }
                    getAuthCookie(new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String authCookie) {
                            final List<Position> positions = new ArrayList<Position>();
                            JsonObjectRequest userRequest = new JsonObjectRequest(
                                    Request.Method.GET,
                                    configInfo.getUserDataUrl(),
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject json) {
                                            LdsUser user = null;
                                            Log.i(TAG, "User call successful");
                                            Log.i(TAG, json.toString());
                                            try {
                                                JSONArray assignments = json.getJSONArray("memberAssignments");
                                                OrgCallingBuilder builder = new OrgCallingBuilder();
                                                for(int i = 0; i < assignments.length(); i++) {
                                                    positions.add(builder.extractPosition((JSONObject) assignments.get(i)));
                                                }
                                                unitNumber = json.getJSONArray("memberAssignments").getJSONObject(0).getString("unitNo");
                                                long individualId = json.getLong("individualId");
                                                user = new LdsUser(individualId, positions);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            userCallback.onResponse(user);
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(TAG, "load user error");
                                            error.printStackTrace();
                                            unitNumber = "56030";
                                            userCallback.onResponse(null);
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
                            userRequest.setRetryPolicy(getRetryPolicy());
                            requestQueue.add(userRequest);

                        }
                    });
                }
            });
        }
    }

    @Override
    public void getOrgs(final Response.Listener<List<Org>> orgsCallback) {
        if(orgsInfo != null) {
            orgsCallback.onResponse(orgsInfo);
        } else {
            getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Cookie", authCookie);
                    headers.put("Content-type", "application/json");
                    headers.put("Accept", "application/json");
                    OrgsListRequest orgsRequest = new OrgsListRequest(
                            configInfo.getCallingListUrl(),
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
                    orgsRequest.setRetryPolicy(getRetryPolicy());
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
            getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Cookie", authCookie);
                    headers.put("Content-type", "application/json");
                    headers.put("Accept", "application/json");
                    MemberListRequest wardListRequest = new MemberListRequest(
                            configInfo.getMemberListUrl().replace(":unitNum", unitNumber),
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
                    wardListRequest.setRetryPolicy(getRetryPolicy());
                    requestQueue.add(wardListRequest);

                }
            });

        }
    }

    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes(UTF8);
            sha = MessageDigest.getInstance(DIGEST);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String value) {
        try {
            setKey(secret);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            return Base64.encodeToString(cipher.doFinal(value.getBytes(UTF8)), Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Could not encrypt the value");
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String value) {
        try {
            setKey(secret);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return new String(cipher.doFinal(Base64.decode(value, Base64.DEFAULT)));
        } catch (Exception e) {
            Log.e(TAG, "Could not decrypt the value: " + e.getMessage());
            return null;
        }
    }

    public void getPositionMetaData(Response.Listener<String> callback) {}

    private LdsUser extractUser(JSONObject json) throws JSONException {
        List<Position> positions = new ArrayList<>();
        JSONArray assignments = json.getJSONArray("memberAssignments");
        OrgCallingBuilder builder = new OrgCallingBuilder();
        for(int i = 0; i < assignments.length(); i++) {
            positions.add(builder.extractPosition((JSONObject) assignments.get(i)));
        }
        unitNumber = json.getJSONArray("memberAssignments").getJSONObject(0).getString("unitNo");
        long individualId = json.getJSONObject("individualId").getLong("individualId");
        return new LdsUser(individualId, positions);
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
        json.put("memberId", calling.getProposedIndId().toString());

        if(calling.getId() != null && calling.getId() > 0) {
            LocalDate date = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMd");
            json.put("releaseDate", date.toString(fmt));
            JSONArray positionIds = new JSONArray();
            positionIds.put(calling.getId());
            json.put("releasePositionIds", positionIds);
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", authCookie);
        //headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Accept", "application/json");
        GsonRequest<String> gsonRequest = new GsonRequest<String>(
            Request.Method.POST,
            configInfo.getUpdateCallingUrl(),
            String.class,
            headers,
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
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(error.getMessage());
                    } catch (JSONException jsonError) {
                        jsonError.printStackTrace();
                    }
                    callback.onResponse(jsonObject);
                }
            }
        );
        gsonRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                );
        requestQueue.add(gsonRequest);
    }

    public void releaseCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback) throws JSONException {

    }

    public void deleteCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback) throws JSONException {

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

    private static RetryPolicy getRetryPolicy() {
        return new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError { }
        };
    }
}
