package org.ldscd.callingworkflow.web;


import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberData {
    private IWebResources webResources;

    private List<Member> members;
    private Map<Long, Member> membersByIndividualId;

    public MemberData(IWebResources webResources) {
        this.webResources = webResources;
    }

    public void loadMembers(final Response.Listener<List<Member>> membersCallback) {
        webResources.getWardList(new Response.Listener<List<Member>>() {
            @Override
            public void onResponse(List<Member> response) {
                members = response;
                membersByIndividualId = new HashMap<Long, Member>();
                for(Member member: members) {
                    membersByIndividualId.put(member.getIndividualId(), member);
                }
                membersCallback.onResponse(members);
            }
        });
    }

    public String getMemberName(long individualId) {
        if(individualId > 0) {
            Member member = membersByIndividualId.get(individualId);
            return member != null ? member.getFormattedName() : null;
        } else {
            return  "";
        }
    }
}
