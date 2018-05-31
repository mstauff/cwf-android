package org.ldscd.callingworkflow.web;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemberListRequest extends Request<List<Member>> {
    private static final String TAG = "WardListRequest";
    private static final String familiesArrayName = "families";
    private static final String hohFieldName = "headOfHouse";
    private static final String spouseFieldName = "spouse";
    private static final String childrenArrayName = "children";
    private static final String idFieldName = "individualId";
    private static final String nameFieldName = "formattedName";
    private static final String phoneFieldName = "phone";
    private static final String emailFieldName = "email";
    private static final String householdEmailFieldName = "emailAddress";
    private static final String addressObjectName = "address";
    private static final String streetAddressFieldName = "streetAddress";
    private static final String birthdateFieldName = "birthdate";
    private static final String genderFieldName = "gender";
    private static final String priesthoodFieldName = "priesthoodOffice";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final int minimumChildAge = 11;

    private final Response.Listener<List<Member>> listener;

    public MemberListRequest(String url, Response.Listener<List<Member>> listener, final Response.Listener<WebException> errorListener) {
        super(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //all exceptions should be of type WebResourceException at this point for use in the rest of the app
                errorListener.onResponse((WebException)error);
            }
        });
        this.listener = listener;
    }

    @Override
    protected void deliverResponse(List<Member> response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<List<Member>> parseNetworkResponse(NetworkResponse response) {
        if(WebException.isSessionExpiredResponse(getUrl())) {
            return Response.error(new WebException(ExceptionType.SESSION_EXPIRED, response));
        } else if(WebException.isWebsiteDownResponse(getUrl())) {
            return Response.error(new WebException(ExceptionType.SERVER_UNAVAILABLE, response));
        } else {
            List<Member> memberList;
            try {
                memberList = getMembers(new String(response.data, HttpHeaderParser.parseCharset(response.headers)));
            } catch (Exception e) {
                return Response.error(new WebException(ExceptionType.PARSING_ERROR, e));
            }
            return Response.success(memberList, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 403) {
            return new WebException(ExceptionType.LDS_AUTH_REQUIRED, volleyError.networkResponse);
        } else {
            return new WebException(ExceptionType.UNKNOWN_EXCEPTION, volleyError.networkResponse);
        }
    }

    public List<Member> getMembers(String jsonString) {
        List<Member> memberList = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray families = json.getJSONArray(familiesArrayName);
            for(int i=0; i < families.length(); i++) {
                JSONObject family = families.getJSONObject(i);
                String householdPhone = family.getString(phoneFieldName);
                String householdEmail = family.getString(householdEmailFieldName);
                JSONObject addressObj = family.getJSONObject(addressObjectName);
                String address = addressObj.getString(streetAddressFieldName);
                if(!family.isNull(hohFieldName)) {
                    memberList.add(extractMember(family.getJSONObject(hohFieldName), householdPhone, householdEmail, address));
                }
                if(!family.isNull(spouseFieldName)) {
                    memberList.add(extractMember(family.getJSONObject(spouseFieldName), householdPhone, householdEmail, address));
                }
                JSONArray children = family.getJSONArray(childrenArrayName);
                for(int x=0; x < children.length(); x++) {
                    JSONObject child = children.getJSONObject(x);
                    try {
                        DateTime birthdate = dateTimeFormatter.parseDateTime(child.getString(birthdateFieldName));
                        if(birthdate == null || calculateAge(birthdate) >= minimumChildAge) {
                            memberList.add(extractMember(child, householdPhone, householdEmail, address));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error Parsing Child Birthdate: " + child.getString(birthdateFieldName));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return memberList;
    }
    private Member extractMember(JSONObject json, String householdPhone, String householdEmail, String streetAddress) throws JSONException {
        long id = 0;
        if(!json.isNull(idFieldName)){
            id = json.getLong(idFieldName);
        }
        String name = json.getString(nameFieldName);
        String phone = json.getString(phoneFieldName);
        String email = json.getString(emailFieldName);

        DateTime birthdate;
        try {
            birthdate = dateTimeFormatter.parseDateTime(json.getString(birthdateFieldName));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing birthdate: " + json.getString(birthdateFieldName));
            birthdate = null;
        }

        String genderString = json.getString(genderFieldName);
        Gender gender;
        switch (genderString) {
            case "MALE":
                gender = Gender.MALE;
                break;
            case "FEMALE":
                gender = Gender.FEMALE;
                break;
            default:
                gender = Gender.UNKNOWN;
                break;
        }

        String priesthoodString = json.getString(priesthoodFieldName);
        Priesthood priesthood;
        switch (priesthoodString) {
            case "DEACON":
                priesthood = Priesthood.DEACON;
                break;
            case "TEACHER":
                priesthood = Priesthood.TEACHER;
                break;
            case "PRIEST":
                priesthood = Priesthood.PRIEST;
                break;
            case "ELDER":
                priesthood = Priesthood.ELDER;
                break;
            case "HIGH_PRIEST":
                priesthood = Priesthood.HIGH_PRIEST;
                break;
            case "SEVENTY":
                priesthood = Priesthood.SEVENTY;
                break;
            default:
                priesthood = Priesthood.UNKNOWN;
                break;
        }

        return new Member(id, name, phone, householdPhone, email, householdEmail, streetAddress,
                birthdate, gender, priesthood, null, null);
    }

    private int calculateAge(DateTime birthdate) {
        return Years.yearsBetween(birthdate, DateTime.now()).getYears();
    }
}
