package org.ldscd.callingworkflow.web;


import android.widget.ProgressBar;

import com.android.volley.Response;

import org.ldscd.callingworkflow.model.Calling;
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

    public void loadMembers(final Response.Listener<Boolean> membersCallback, final ProgressBar pb) {
        webResources.getWardList(new Response.Listener<List<Member>>() {
            @Override
            public void onResponse(List<Member> response) {
                members = response;
                membersByIndividualId = new HashMap<Long, Member>();
                for(Member member: members) {
                    if(member.getIndividualId() > 0) {
                        membersByIndividualId.put(member.getIndividualId(), member);
                    }
                }
                pb.setProgress(pb.getProgress() + 20);
                membersCallback.onResponse(true);
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

    public Member getMember(long individualId) {
        return membersByIndividualId.get(individualId);
    }

    public void getMembers(Response.Listener<List<Member>> listener) {
        listener.onResponse(this.members);
    }

    public void setMemberCallings(List<Calling> callings) {
        for(Calling calling : callings) {
            if(calling.getMemberId() != null && calling.getMemberId() > 0) {
                getMember(calling.getMemberId()).setCurrentCallings(calling);
            }
        }
    }
}