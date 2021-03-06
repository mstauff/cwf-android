package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
import org.ldscd.callingworkflow.utils.SecurityUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebResources implements IWebResources {
    private static final String TAG = "WebResourcesLog";
    private static final String CONFIG_URL = BuildConfig.appConfigUrl;
    private static final String prefUsername = "username";
    private static final String prefPassword = "password";
    private static final String UNIT_NUMBER = "unitNumber";
    private static final String SUB_ORG_TYPE_ID = "subOrgTypeId";
    private static final String SUB_ORG_ID = "subOrgId";
    private static final String POSITION = "position";
    private static final String POSITION_ID = "positionId";
    private static final String YYYY_M_MDD = "yyyyMMdd";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String POSITION_TYPE_ID = "positionTypeId";
    private static final String HIDDEN = "hidden";
    private static final String MEMBER_ID = "memberId";
    private static final String RELEASE_POSITION_IDS = "releasePositionIds";
    private static final String UTF_8 = "UTF-8";
    private static final String HOME_UNIT_NBR = "homeUnitNbr";
    private static final String INDIVIDUAL_ID = "individualId";
    private static final String MEMBER_ASSIGNMENTS = "memberAssignments";
    private static final String UNIT_NO = "unitNo";
    private static final String COOKIE = "Cookie";
    private static final String CWF = "cwf";

    private RequestQueue requestQueue;
    private RequestQueue authRequestQueue;
    private SharedPreferences preferences;
    private NetworkInfo networkInfo;

    private ConfigInfo configInfo = null;
    private LdsUser userInfo = null;
    private List<Org> orgsInfo = null;
    private List<Member> wardMemberList = null;
    private String unitNumber = null;

    private CookieManager cookieManager;
    private HttpCookie httpCookie;
    private URI uri;
    private Context context;
    private String userName = null;
    private String password = null;
    private boolean attemptSessionRefresh = true;

    public WebResources(Context context, RequestQueue requestQueue, SharedPreferences preferences, CookieManager cookieManager) {
        this.requestQueue = requestQueue;
        this.preferences = preferences;
        this.httpCookie = null;
        this.cookieManager = cookieManager;
        this.context = context;
        try {
            uri = new URI(CONFIG_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        /* We are creating a separate queue which doesn't automatically follow redirects for the login call
        since it results in endless redirects for a failed login attempt. Following redirects is useful for
        the other calls where language url-params are being added through a redirect */
        authRequestQueue = Volley.newRequestQueue(context.getApplicationContext(), new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpURLConnection connection = super.createConnection(url);
                connection.setInstanceFollowRedirects(false);

                return connection;
            }
        });
    }

    private boolean isDataConnected() {
        if(networkInfo == null) {
            ConnectivityManager connectivityManager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
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
    public void getConfigInfo(final Response.Listener<ConfigInfo> configCallback, final Response.Listener<WebException> errorCallback) {
        if(configInfo != null) {
            configCallback.onResponse(configInfo);
        } else if(isDataConnected()) {
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
                        getLocalConfigInfo(configCallback, errorCallback);
                    }
                }
            );
            configRequest.setRetryPolicy(getRetryPolicy());
            requestQueue.add(configRequest);
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    private void getLocalConfigInfo(Response.Listener<ConfigInfo> configInfoCallback, Response.Listener<WebException> errorCallback) {
        String json = getJSONFromAssets(BuildConfig.appConfigFile);
        Gson gson = new Gson();
        try {
            configInfo = gson.fromJson(json, ConfigInfo.class);
            configInfoCallback.onResponse(configInfo);
        } catch (JsonSyntaxException e) {
            errorCallback.onResponse(new WebException(ExceptionType.PARSING_ERROR, e));
        }
    }

    @Override
    public void getUserInfo(final Response.Listener<LdsUser> userCallback, final Response.Listener<WebException> errorCallback) {
        if(userInfo != null) {
            userCallback.onResponse(userInfo);
        } else if(isDataConnected()) {
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
                        errorCallback.onResponse(new WebException(ExceptionType.LDS_AUTH_REQUIRED));
                    } else {
                        /* get the cookie because it has the information for the rest headers */
                        getAuthCookie(new Response.Listener<Boolean>() {
                            @Override
                            public void onResponse(final Boolean success) {
                                /* Make the rest call to get the current users information. */
                                getUser(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        userCallback.onResponse(userInfo);
                                    }
                                }, new Response.Listener<WebException>() {
                                    @Override
                                    public void onResponse(WebException error) {
                                        Log.e(TAG, "load user error");
                                        error.printStackTrace();
                                        if(error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                                            httpCookie = null;
                                            attemptSessionRefresh = false;
                                            getUserInfo(userCallback, errorCallback);
                                        } else {
                                            errorCallback.onResponse(error);
                                        }
                                    }
                                });
                            }
                        }, errorCallback);
                    }
                }
            }, errorCallback);
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    private void getAuthCookie(final Response.Listener<Boolean> authCallback, final Response.Listener<WebException> errorCallback) {
        /* Capture the cookie info stored in preferences as well check to verify it's not expired. */
        if(httpCookie != null && !httpCookie.hasExpired()) {
            authCallback.onResponse(true);
        } else {
            /* Authentication Request to the LDS church. */
            final AuthenticationRequest authRequest = new AuthenticationRequest(userName, password, configInfo.getSignInUrl(),
                new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        if(response) {
                            Log.i(TAG, "auth call successful");
                            attemptSessionRefresh = true;
                            httpCookie = new HttpCookie(CWF, "true");

                            /* Sets the maximum age of the cookie in seconds. */
                            httpCookie.setMaxAge(1500);
                            /* Sets the username and password in long term storage.
                             * This is only accessible through this application.  */
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(prefUsername, SecurityUtil.encrypt(context, userName));
                            editor.putString(prefPassword, SecurityUtil.encrypt(context, password));
                            editor.apply();

                            authCallback.onResponse(true);
                        }
                    }
                },
                new Response.Listener<WebException>() {
                    @Override
                    public void onResponse(WebException error) {
                        Log.e(TAG, "auth error");
                        error.printStackTrace();
                        errorCallback.onResponse(error);
                    }
                }
            );
            authRequest.setRetryPolicy(getRetryPolicy());
            authRequestQueue.add(authRequest);
        }
    }

    public void signOut(final Response.Listener<Boolean> authCallback, final Response.Listener<WebException> errorCallback) {
        /* Authentication Request to the LDS church. */
        StringRequest authRequest = new StringRequest(Request.Method.GET, configInfo.getSignOutUrl(),
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    Log.d("Response", response);
                    clearAuthCache();
                    authCallback.onResponse(true);
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // error
                    Log.e("Error.Response", error.networkResponse.statusCode + "");
                    clearAuthCache();
                    authCallback.onResponse(true);
                }
            }
        );
        authRequest.setRetryPolicy(getRetryPolicy());
        requestQueue.add(authRequest);
    }

    private void clearAuthCache() {
        httpCookie = null;
        userInfo = null;
        userName = null;
        password = null;
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(prefUsername);
        editor.remove(prefPassword);
        editor.remove(COOKIE);
        editor.commit();
        cookieManager.getCookieStore().removeAll();
    }

    private void getUser(final Response.Listener<Boolean> userCallback, final Response.Listener<WebException> errorCallback) {
        // Create a list of calling(s)/Positions(s) for the current user.
        final List<Position> positions = new ArrayList<>();
//        Position position = new Position("Bishop", 4, false, false, 1L, 56030L);
//        userInfo = new LdsUser(222222222L, Collections.singletonList(position));
//        unitNumber = "56030";
//        userCallback.onResponse(true);
        // Make a rest call to the lds church to capture the last information on the specified user.
        LcrJsonRequest userRequest = new LcrJsonRequest(
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
                            // Parse out the returned json to capture the positions occupied by this person.
                            if(json.has(MEMBER_ASSIGNMENTS)) {
                                JSONArray assignments = json.getJSONArray(MEMBER_ASSIGNMENTS);
                                OrgCallingBuilder builder = new OrgCallingBuilder();
                                for (int i = 0; i < assignments.length(); i++) {
                                    positions.add(builder.extractPosition((JSONObject) assignments.get(i)));
                                }
                                unitNumber = json.getJSONArray(MEMBER_ASSIGNMENTS).getJSONObject(0).getString(UNIT_NO);
                            }
                            /* Gets the Unit Number for this person by way of the position they hold.
                            *  Currently we are only storing the unit number for the first calling we get.
                            *  If they have callings in multiple units this will not be supported.
                            */
                            if(unitNumber == null) {
                                unitNumber = json.get(HOME_UNIT_NBR).toString();
                            }
                            long individualId = json.getLong(INDIVIDUAL_ID);
                            user = new LdsUser(individualId, positions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorCallback.onResponse(new WebException(ExceptionType.PARSING_ERROR, e));
                        }
                        /* Set the class level user to the newly established user. */
                        userInfo = user;
                        /* Returns true when all is done. */
                        userCallback.onResponse(true);
                    }
                },
                errorCallback);
        userRequest.setRetryPolicy(getRetryPolicy());
        requestQueue.add(userRequest);
    }

    @Override
    public void getOrgs(final boolean getCleanCopy, final Response.Listener<List<Org>> orgsCallback, final Response.Listener<WebException> errorCallback) {
        if(orgsInfo != null && !getCleanCopy) {
            orgsCallback.onResponse(orgsInfo);
        } else if(isDataConnected()) {
            getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    if(BuildConfig.DEBUG) {
                        /*This code is to handle an issue we're seeing while hitting test servers where we're given a beta
                        term acceptance page instead of the info we need until these cookies are set. In the future we may
                        sense the 302 redirect and beta accept page in the request code and add these reactively on in case
                        this happens in prroduction*/
                        try {
                            URI uri = new URI(configInfo.getCallingListUrl());
                            HttpCookie betaAcceptCookie = new HttpCookie("clerk-resources-beta-terms", "true");
                            HttpCookie betaVersionCookie = new HttpCookie("clerk-resources-beta-eula", "4.2");
                            cookieManager.getCookieStore().add(uri, betaAcceptCookie);
                            cookieManager.getCookieStore().add(uri, betaVersionCookie);
                        } catch (URISyntaxException error) {
                            error.printStackTrace();
                        }
                    }
                    OrgsListRequest orgsRequest = new OrgsListRequest(
                            configInfo.getCallingListUrl(),
                            new Response.Listener<List<Org>>() {
                                @Override
                                public void onResponse(List<Org> response) {
                                    attemptSessionRefresh = true;
                                    orgsInfo = response;
                                    Log.i(TAG, "Positions call successful");
                                    orgsCallback.onResponse(orgsInfo);
                                }
                            },
                            new Response.Listener<WebException>() {
                                @Override
                                public void onResponse(WebException error) {
                                    Log.e(TAG, "load Orgs list error");
                                    error.printStackTrace();
                                    if(error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                                        httpCookie = null;
                                        attemptSessionRefresh = false;
                                        getOrgs(getCleanCopy, orgsCallback, errorCallback);
                                    } else {
                                        errorCallback.onResponse(error);
                                    }
                                }
                            }
                    );
                    orgsRequest.setRetryPolicy(getRetryPolicy());
                    requestQueue.add(orgsRequest);
                }
            }, errorCallback);

        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    @Override
    public Task<List<Org>> getOrgWithMembers(final Long subOrgId) {
        final TaskCompletionSource<List<Org>> completionSource = new TaskCompletionSource<>();
        JSONArrayRequest getOrgMembersRequest = new JSONArrayRequest(
                Request.Method.POST,
                configInfo.getClassAssignments().replace(":subOrgId", subOrgId.toString()),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray result) {
                        completionSource.setResult(new OrgCallingBuilder().extractOrgs(result, true));
                    }
                },
                new Response.Listener<WebException>() {
                    @Override
                    public void onResponse(WebException error) {
                        Log.e(TAG, "load Org Hierarchy list error");
                        error.printStackTrace();
                        if (error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                            httpCookie = null;
                            attemptSessionRefresh = false;
                            cookieManager.getCookieStore().removeAll();
                        } else {
                            completionSource.setException(error);
                        }
                    }
                }
        );
        getOrgMembersRequest.setRetryPolicy(getRetryPolicy());
        requestQueue.add(getOrgMembersRequest);
        return completionSource.getTask();
    }

    @Override
    public void getWardList(final Response.Listener<List<Member>> wardCallback, final Response.Listener<WebException> errorCallback) {
        if(wardMemberList != null) {
            wardCallback.onResponse(wardMemberList);
        } else if(isDataConnected()) {
            getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    MemberListRequest wardListRequest = new MemberListRequest(
                            configInfo.getMemberListUrl().replace(":unitNum", unitNumber),
                            new Response.Listener<List<Member>>() {
                                @Override
                                public void onResponse(List<Member> response) {
                                    wardMemberList = response;
                                    Log.i(TAG, "Member call successful");
                                    wardCallback.onResponse(wardMemberList);
                                }
                            },
                            new Response.Listener<WebException>() {
                                @Override
                                public void onResponse(WebException error) {
                                    Log.e(TAG, "load ward list error");
                                    error.printStackTrace();
                                    if(error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                                        httpCookie = null;
                                        attemptSessionRefresh = false;
                                        getWardList(wardCallback, errorCallback);
                                    } else {
                                        errorCallback.onResponse(error);
                                    }
                                }
                            }
                    );
                    wardListRequest.setRetryPolicy(getRetryPolicy());
                    requestQueue.add(wardListRequest);

                }
            }, errorCallback);

        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    public void getPositionMetaData(Response.Listener<String> callback) {
        callback.onResponse(getJSONFromAssets("position-metadata.json"));
    }

    private String getJSONFromAssets(String fileName) {
        String json;
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

    @Override
    public void updateCalling(final Calling calling, final Long unitNumber, final int orgTypeId, final Response.Listener<JSONObject> callback, final Response.Listener<WebException> errorCallback) {
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
        if(isDataConnected()) {
            org.json.JSONObject json = new org.json.JSONObject();
            try {
                json.put(UNIT_NUMBER, unitNumber);
                json.put(SUB_ORG_TYPE_ID, orgTypeId);
                json.put(SUB_ORG_ID, calling.getParentOrg());
                json.put(POSITION_TYPE_ID, calling.getPosition().getPositionTypeId());
                json.put(POSITION, calling.getPosition().getName());
                json.put(MEMBER_ID, calling.getProposedIndId());

                if (calling.getId() != null && calling.getId() > 0) {
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(YYYY_M_MDD);
                    json.put(RELEASE_DATE, date.toString(fmt));
                    JSONArray positionIds = new JSONArray();
                    positionIds.put(calling.getId());
                    json.put(RELEASE_POSITION_IDS, positionIds);
                }
                LcrJsonRequest updateCallingRequest = new LcrJsonRequest(
                        Request.Method.POST,
                        configInfo.getUpdateCallingUrl(),
                        json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (response != null) {
                                    callback.onResponse(response);
                                } else {
                                    errorCallback.onResponse(new WebException(ExceptionType.UNKNOWN_EXCEPTION));
                                }
                            }
                        }, new Response.Listener<WebException>() {
                    @Override
                    public void onResponse(WebException error) {
                        //if the error is from expired session try to refresh once, return error if it fails twice
                        if (error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                            httpCookie = null;
                            attemptSessionRefresh = false;
                            getAuthCookie(new Response.Listener<Boolean>() {
                                @Override
                                public void onResponse(Boolean success) {
                                    updateCalling(calling, unitNumber, orgTypeId, callback, errorCallback);
                                }
                            }, errorCallback);
                        } else {
                            errorCallback.onResponse(error);
                        }
                    }
                }
                );
                updateCallingRequest.setRetryPolicy(
                        new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                );
                requestQueue.add(updateCallingRequest);

            } catch (JSONException exception) {
                errorCallback.onResponse(new WebException(ExceptionType.PARSING_ERROR, exception));
            }
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    @Override
    public void releaseCalling(final Calling calling, final Long unitNumber, final int orgTypeId, final Response.Listener<JSONObject> callback, final Response.Listener<WebException> errorCallback) {
        /*
        "unitNumber": 56030,
        "subOrgTypeId": 1252,
        "subOrgId": 2081422,
         "position": "does not matter",
        "positionId": 38816967,
        "positionTypeId":208,
        "releaseDate": "20170801"
         */
        if(isDataConnected()) {
            org.json.JSONObject json = new org.json.JSONObject();
            try {
                json.put(UNIT_NUMBER, unitNumber);
                json.put(SUB_ORG_TYPE_ID, orgTypeId);
                json.put(SUB_ORG_ID, calling.getParentOrg());
                json.put(POSITION, calling.getPosition().getName());
                json.put(POSITION_ID, calling.getId());
                json.put(POSITION_TYPE_ID, calling.getPosition().getPositionTypeId());
                LocalDate date = LocalDate.now();
                DateTimeFormatter fmt = DateTimeFormat.forPattern(YYYY_M_MDD);
                json.put(RELEASE_DATE, date.toString(fmt));

                LcrJsonRequest releaseCallingRequest = new LcrJsonRequest(
                        Request.Method.POST,
                        configInfo.getUpdateCallingUrl(),
                        json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (response != null) {
                                    callback.onResponse(response);
                                } else {
                                    errorCallback.onResponse(new WebException(ExceptionType.UNKNOWN_EXCEPTION));
                                }
                            }
                        }, new Response.Listener<WebException>() {
                    @Override
                    public void onResponse(WebException error) {
                        //if the error is from expired session try to refresh once, return error if it fails twice
                        if (error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                            httpCookie = null;
                            attemptSessionRefresh = false;
                            getAuthCookie(new Response.Listener<Boolean>() {
                                @Override
                                public void onResponse(Boolean response) {
                                    releaseCalling(calling, unitNumber, orgTypeId, callback, errorCallback);
                                }
                            }, errorCallback);
                        } else {
                            errorCallback.onResponse(error);
                        }
                    }
                }
                );
                releaseCallingRequest.setRetryPolicy(
                        new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                );
                requestQueue.add(releaseCallingRequest);
            } catch (JSONException exception) {
                errorCallback.onResponse(new WebException(ExceptionType.PARSING_ERROR, exception));
            }
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    @Override
    public void deleteCalling(final Calling calling, final Long unitNumber, final int orgTypeId, final Response.Listener<JSONObject> callback, final Response.Listener<WebException> errorCallback) {
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
        if(isDataConnected()) {
            org.json.JSONObject json = new org.json.JSONObject();
            try {
                json.put(UNIT_NUMBER, unitNumber);
                json.put(SUB_ORG_TYPE_ID, orgTypeId);
                json.put(SUB_ORG_ID, calling.getParentOrg());
                json.put(POSITION, calling.getPosition().getName());
                if (calling.getId() != null && calling.getId() > 0 && calling.getMemberId() > 0) {
                    json.put(POSITION_ID, calling.getId());
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter fmt = DateTimeFormat.forPattern(YYYY_M_MDD);
                    json.put(RELEASE_DATE, date.toString(fmt));
                } else {
                    json.put(POSITION_TYPE_ID, calling.getPosition().getPositionTypeId());
                }

                json.put(HIDDEN, true);

                LcrJsonRequest deleteCallingRequest = new LcrJsonRequest(
                        Request.Method.POST,
                        configInfo.getUpdateCallingUrl(),
                        json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                callback.onResponse(response);
                            }
                        }, new Response.Listener<WebException>() {
                    @Override
                    public void onResponse(WebException error) {
                        //if the error is from expired session try to refresh once, return error if it fails twice
                        if (error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                            httpCookie = null;
                            attemptSessionRefresh = false;
                            getAuthCookie(new Response.Listener<Boolean>() {
                                @Override
                                public void onResponse(Boolean response) {
                                    deleteCalling(calling, unitNumber, orgTypeId, callback, errorCallback);
                                }
                            }, errorCallback);
                        } else {
                            errorCallback.onResponse(error);
                        }
                    }
                }
                );
                deleteCallingRequest.setRetryPolicy(
                        new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                );
                requestQueue.add(deleteCallingRequest);
            } catch (JSONException exception) {
                errorCallback.onResponse(new WebException(ExceptionType.PARSING_ERROR, exception));
            }
        } else {
            errorCallback.onResponse(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
    }

    @Override
    public Task<JSONArray> getOrgHierarchy() {
        final TaskCompletionSource<JSONArray> taskCompletionSource = new TaskCompletionSource<>();
        if(isDataConnected()) {
            getUserInfo(new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    JSONArrayRequest orgsRequest = new JSONArrayRequest(
                            Request.Method.GET,
                            "https://stage.lds.org/mls/mbr/services/orgs/sub-org-name-hierarchy?lang=eng",
                            null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray jsonArray) {
                                    taskCompletionSource.setResult(jsonArray);
                                }
                            },
                            new Response.Listener<WebException>() {
                                @Override
                                public void onResponse(WebException error) {
                                    Log.e(TAG, "load Org Hierarchy list error");
                                    error.printStackTrace();
                                    if (error.getExceptionType() == ExceptionType.SESSION_EXPIRED && attemptSessionRefresh) {
                                        httpCookie = null;
                                        attemptSessionRefresh = false;
                                    } else {
                                        taskCompletionSource.setResult(null);
                                        taskCompletionSource.setException(new WebException(ExceptionType.PARSING_ERROR, error));
                                    }
                                }
                            }
                    );
                    orgsRequest.setRetryPolicy(getRetryPolicy());
                    requestQueue.add(orgsRequest);
                }
            }, new Response.Listener<WebException>() {
                @Override
                public void onResponse(WebException error) {
                    taskCompletionSource.setResult(null);
                    taskCompletionSource.setException(new WebException(ExceptionType.LDS_SERVER_PROVIDED_MESSAGE, error));
                }
            });

        } else {
            taskCompletionSource.setResult(null);
            taskCompletionSource.setException(new WebException(ExceptionType.NO_DATA_CONNECTION));
        }
        return taskCompletionSource.getTask();
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