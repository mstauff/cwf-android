package org.ldscd.callingworkflow.web;


import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberData {
    private IWebResources webResources;
    private LocalFileResources localFileResources;

    private List<Member> members;
    private Map<Long, Member> membersByIndividualId;

    public MemberData(IWebResources webResources, LocalFileResources localFileResources) {
        this.webResources = webResources;
        this.localFileResources = localFileResources;
    }

    private void loadMembers(final Response.Listener<List<Member>> membersCallback) {
        localFileResources.getWardList(new Response.Listener<List<Member>>() {
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

    public void getMemberName(final long individualId, final Response.Listener<String> nameCallback) {
        if(individualId > 0) {
            if (membersByIndividualId != null) {
                Member member = membersByIndividualId.get(individualId);
                String result = member != null ? member.getFormattedName() : null;
                nameCallback.onResponse(result);
            } else {
                loadMembers(new Response.Listener<List<Member>>() {
                    @Override
                    public void onResponse(List<Member> response) {
                        Member member = membersByIndividualId.get(individualId);
                        String result = member != null ? member.getFormattedName() : null;
                        nameCallback.onResponse(result);
                    }
                });
            }
        } else {
            nameCallback.onResponse("");
        }
    }
}
