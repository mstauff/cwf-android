package org.ldscd.callingworkflow.web;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.Gender;
import org.ldscd.callingworkflow.constants.Priesthood;
import org.ldscd.callingworkflow.model.Member;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final int minimumChildAge = 11;

    private final Response.Listener<List<Member>> listener;
    private Map<String, String> headers;

    public MemberListRequest(String url, Map<String, String> headers, Response.Listener<List<Member>> listener, Response.ErrorListener errorListener) {
        super(Request.Method.GET, url, errorListener);
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(List<Member> response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<List<Member>> parseNetworkResponse(NetworkResponse response) {
        List<Member> memberList;
        try {
            memberList = getMembers(new String(response.data, HttpHeaderParser.parseCharset(response.headers)));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(memberList, HttpHeaderParser.parseCacheHeaders(response));
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
                        Date birthdate = dateFormat.parse(child.getString(birthdateFieldName));
                        if(calculateAge(birthdate) >= minimumChildAge) {
                            memberList.add(extractMember(child, householdPhone, householdEmail, address));
                        }
                    } catch (ParseException e) {
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

        Date birthdate;
        try {
            birthdate = dateFormat.parse(json.getString(birthdateFieldName));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing birthdate: " + json.getString(birthdateFieldName));
            birthdate = null;
        }

        String genderString = json.getString(genderFieldName);
        Gender gender;
        switch (genderString) {
            case "MALE":
                gender = Gender.M;
                break;
            case "FEMALE":
                gender = Gender.F;
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

    private int calculateAge(Date birthdate) {
        Calendar cal = Calendar.getInstance();
        int currYear = cal.get(Calendar.YEAR);
        int currMonth = cal.get(Calendar.MONTH);
        int currDate = cal.get(Calendar.DATE);

        cal.setTime(birthdate);
        int age = currYear - cal.get(Calendar.YEAR);
        //if they haven't had their birthday yet this year subtract 1
        if(currMonth < cal.get(Calendar.MONTH) ||
                (currDate == cal.get(Calendar.MONTH) && currDate < cal.get(Calendar.DATE))) {
            age--;
        }
        return age;
    }
}