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
import org.ldscd.callingworkflow.utils.SecurityUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.io.InputStream;
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
    private static final String CONFIG_URL = "http://dev-config-server-ldscd.7e14.starter-us-west-2.openshiftapps.com/cwf/config?env=stage";
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

    private RequestQueue requestQueue;
    private SharedPreferences preferences;

    private ConfigInfo configInfo = null;
    private String authCookie = null;
    private LdsUser userInfo = null;
    private List<Org> orgsInfo = null;
    private List<Member> wardMemberList = null;
    private String unitNumber = null;

    private final CookieManager cookieManager;
    private HttpCookie httpCookie;
    private URI uri;
    private Context context;
    private String userName = null;
    private String password = null;

    public WebResources(Context context, RequestQueue requestQueue, SharedPreferences preferences) {
        this.requestQueue = requestQueue;
        this.preferences = preferences;
        this.httpCookie = null;
        this.cookieManager = new CookieManager();
        this.context = context;
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
            userName = SecurityUtil.decrypt(context, cryptUser);
            password = SecurityUtil.decrypt(context, cryptPassword);
        }
    }

    @Override
    public void setCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void getSharedPreferences(Response.Listener<SharedPreferences> listener) {
        listener.onResponse(preferences);
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

    @Override
    public void getUserInfo(boolean getClean, final Response.Listener<LdsUser> userCallback) {
        if(userInfo != null && !getClean) {
            userCallback.onResponse(userInfo);
        } else {
            /* Get config info because it has the URL strings */
            getConfigInfo(new Response.Listener<ConfigInfo>() {
                @Override
                public void onResponse(ConfigInfo config) {
                    /* If the current username and password are blank they need to be retrieved and set from somewhere. */
                    if (userName == null || password == null) {
                      loadCredentials();
                    }
                    /* re-check credentials.  If invalid return null LdsUser. */
                    if(userName == null || userName.length() == 0 || password == null || password.length() == 0) {
                        userCallback.onResponse(null);
                    } else {
                        /* get the cookie because it has the information for the rest headers */
                        getAuthCookie(new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String authCookie) {
                                if(authCookie != null) {
                                    /* Make the rest call to get the current users information. */
                                    getUser(new Response.Listener<Boolean>() {
                                        @Override
                                        public void onResponse(Boolean response) {
                                            userCallback.onResponse(userInfo);
                                        }
                                    }, authCookie);
                                } else {
                                    userCallback.onResponse(null);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void getAuthCookie(final Response.Listener<String> authCallback) {
        /* Capture the cookie info stored in preferences as well check to verify it's not expired. */
        if(preferences.contains("Cookie") && httpCookie != null && !httpCookie.hasExpired()) {
            authCookie = preferences.getString("Cookie", null);
            //TODO: Not sure if the httpCookie needs to be refreshed when kept active or it does it automagically
            authCallback.onResponse(authCookie);
        } else {
            /* Authentication Request to the LDS church. */
            AuthenticationRequest authRequest = new AuthenticationRequest(userName, password, "https://signin-int.lds.org/login.html", //configInfo.getEndpointUrl("SIGN_IN"),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        /* This cookie url has specific params for the TEST environment */
                            authCookie = "clerk-resources-beta-terms=true; clerk-resources-beta-eula=4.2; " + response;
                            Log.i(TAG, "auth call successful");
                            httpCookie = new HttpCookie("cwf", authCookie);

                            /* Sets the maximum age of the cookie in seconds. */
                            httpCookie.setMaxAge(1500);
                            cookieManager.getCookieStore().add(uri, httpCookie);
                            /* Sets the cookie, username and password in long term storage.
                             * This is only accessible through this application.  */
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(prefUsername, SecurityUtil.encrypt(context, userName));
                            editor.putString(prefPassword, SecurityUtil.encrypt(context, password));
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

    private void getUser(final Response.Listener<Boolean> userCallback, final String authCookie) {
        /* Create a list of calling(s)/Positions(s) for the current user. */
        final List<Position> positions = new ArrayList<Position>();
        /* Make a rest call to the lds church to capture the last information on the specified user. */
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
                            /* Parse out the returned json to capture the positions occupied by this person. */
                            JSONArray assignments = json.getJSONArray("memberAssignments");
                            OrgCallingBuilder builder = new OrgCallingBuilder();
                            for(int i = 0; i < assignments.length(); i++) {
                                positions.add(builder.extractPosition((JSONObject) assignments.get(i)));
                            }
                            /* Gets the Unit Number for this person by way of the position they hold.
                            *  Currently we are only storing the unit number for the first calling we get.
                            *  If they have callings in multiple units this will not be supported.
                            */
                            unitNumber = json.getJSONArray("memberAssignments").getJSONObject(0).getString("unitNo");
                            long individualId = json.getLong("individualId");
                            user = new LdsUser(individualId, positions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            userCallback.onResponse(false);
                        }
                        /* Set the class level user to the newly established user. */
                        userInfo = user;
                        /* Returns true when all is done. */
                        userCallback.onResponse(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "load user error");
                        error.printStackTrace();
                        userCallback.onResponse(false);
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

    @Override
    public void getOrgs(boolean getCleanCopy, final Response.Listener<List<Org>> orgsCallback) {
        if(orgsInfo != null && !getCleanCopy) {
            orgsCallback.onResponse(orgsInfo);
        } else {
            getUserInfo(false, new Response.Listener<LdsUser>() {
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
            getUserInfo(false, new Response.Listener<LdsUser>() {
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

        if(calling.getId() != null && calling.getId() > 0) {
            LocalDate date = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
            json.put("releaseDate", date.toString(fmt));
            JSONArray positionIds = new JSONArray();
            positionIds.put(calling.getId());
            json.put("releasePositionIds", positionIds);
        }
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", authCookie);
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
        /*
        "unitNumber": 56030,
        "subOrgTypeId": 1252,
        "subOrgId": 2081422,
         "position": "does not matter",
        "positionId": 38816967,
        "positionTypeId":208,
        "releaseDate": "20170801"
         */
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("unitNumber", unitNumber);
        json.put("subOrgTypeId", orgTypeId);
        json.put("subOrgId", calling.getParentOrg());
        json.put("position", calling.getPosition().getName());
        json.put("positionId", calling.getId());
        json.put("positionTypeId", calling.getPosition().getPositionTypeId());
        LocalDate date = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
        json.put("releaseDate", date.toString(fmt));

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", authCookie);
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
                            jsonObject =  new JSONObject("{ 'errors' : '" + error.getMessage() + "'}");
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

    public void deleteCalling(Calling calling, Long unitNumber, int orgTypeId, final Response.Listener<JSONObject> callback) throws JSONException {
        /*
         "unitNumber": 56030,
         "subOrgTypeId": 1252,
         "subOrgId": 2081422,
         "positionId": 38816967,
         "position": "does not matter",
         "releaseDate": "20170801",
         "hidden" : true

         If the calling is empty you have to include positionTypeId (rather than positionId), you don't include a releaseDate:

         "unitNumber": 56030,
         "subOrgTypeId": 1252,
         "subOrgId": 2081422,
         "positionTypeId": 208,
         "position": "does not matter",
         "hidden" : true
         */

        org.json.JSONObject json = new org.json.JSONObject();
        json.put("unitNumber", unitNumber);
        json.put("subOrgTypeId", orgTypeId);
        json.put("subOrgId", calling.getParentOrg());
        json.put("position", calling.getPosition().getName());
        if(calling.getId() != null && calling.getId() > 0 && calling.getMemberId() > 0) {
            json.put("positionId", calling.getId());
            LocalDate date = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
            json.put("releaseDate", date.toString(fmt));
        } else {
            json.put("positionTypeId", calling.getPosition().getPositionTypeId());
        }

        json.put("hidden", true);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", authCookie);
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