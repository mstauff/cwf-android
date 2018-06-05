package org.ldscd.callingworkflow.web;

import android.content.Context;
import android.widget.ProgressBar;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberData {
    private Context context;
    private IWebResources webResources;

    private List<Member> members;
    private Map<Long, Member> membersByIndividualId;

    public MemberData(Context context, IWebResources webResources) {
        this.context = context;
        this.webResources = webResources;
        members = Collections.emptyList();
        membersByIndividualId = Collections.emptyMap();
    }

    public void loadMembers(final Response.Listener<Boolean> membersCallback, Response.Listener<WebException> errorCallback, final ProgressBar pb) {
        webResources.getWardList(new Response.Listener<List<Member>>() {
            @Override
            public void onResponse(List<Member> response) {
                members = response;
                Collections.sort(members);
                membersByIndividualId = new HashMap<Long, Member>();
                for(Member member: members) {
                    if(member.getIndividualId() > 0) {
                        membersByIndividualId.put(member.getIndividualId(), member);
                    }
                }
                pb.setProgress(pb.getProgress() + 20);
                membersCallback.onResponse(true);
            }
        }, errorCallback);
    }

    public String getMemberName(long individualId) {
        if(individualId > 0) {
            Member member = membersByIndividualId.get(individualId);
            return member != null ? member.getFormattedName() : context.getString(R.string.unknown_member);
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
        if(callings != null) {
            for (Calling calling : callings) {
                addCallingToMembers(calling);
            }
        }
    }
    public void addCallingToMembers(Calling calling) {
        if(calling.getMemberId() != null && calling.getMemberId() > 0) {
            Member currentMember = getMember(calling.getMemberId());
            if(currentMember != null) {
                currentMember.addCurrentCalling(calling);
            }
        }
        if(calling.getProposedIndId() != null && calling.getProposedIndId() > 0) {
            Member proposedMember = getMember(calling.getProposedIndId());
            if(proposedMember != null) {
                proposedMember.addProposedCalling(calling);
            }
        }
    }

    public void removeMemberCallings(List<Calling> callings) {
        if(callings != null) {
            for(Calling calling: callings) {
                removeCallingFromMembers(calling);
            }
        }
    }
    public void removeCallingFromMembers(Calling calling) {
        if(calling.getMemberId() != null && calling.getMemberId() > 0) {
            Member currentMember = getMember(calling.getMemberId());
            if(currentMember != null) {
                currentMember.removeCurrentCalling(calling);
            }
        }
        if(calling.getProposedIndId() != null && calling.getProposedIndId() > 0) {
            Member proposedMember = getMember(calling.getProposedIndId());
            if(proposedMember != null) {
                proposedMember.removeProposedCalling(calling);
            }
        }
    }
    public void removeAllMemberCallings() {
        for(Member member: members) {
            member.getCurrentCallings().clear();
            member.getProposedCallings().clear();
        }
    }
}