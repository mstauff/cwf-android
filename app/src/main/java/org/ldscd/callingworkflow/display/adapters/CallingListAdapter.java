package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.CallingDetailActivity;
import org.ldscd.callingworkflow.display.CallingDetailFragment;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.MemberData;

import java.util.List;

public class CallingListAdapter extends BaseExpandableListAdapter {
    private Context ctx;
    private Org org;
    private List<Org> subOrgs;
    private List<Calling> callings;
    private MemberData memberData;

    private boolean twoPane;
    private final FragmentManager fragmentManager;

    public CallingListAdapter(Org org, MemberData memberData, boolean twoPane, FragmentManager fragmentManager, Context ctx) {
        this.org = org;
        subOrgs = org.getChildren();
        callings = org.getCallings();
        this.memberData = memberData;
        this.ctx = ctx;
        this.twoPane = twoPane;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getGroupCount() {
        return subOrgs.size() + callings.size();
    }

    @Override
    public int getChildrenCount(int index) {
        if(index < subOrgs.size()) {
            Org subOrg = subOrgs.get(index);
            return subOrg.getChildren().size() + subOrg.getCallings().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int index) {
        if(index < subOrgs.size()) {
            return subOrgs.get(index);
        } else {
            return callings.get(index - subOrgs.size());
        }
    }

    @Override
    public Object getChild(int index, int subIndex) {
        Org subOrg = (Org) getGroup(index);
        if(subIndex < subOrg.getChildren().size()) {
            return subOrg.getChildren().get(subIndex);
        } else {
            return subOrg.getCallings().get(subIndex - subOrg.getChildren().size());
        }
    }

    @Override
    public long getGroupId(int index) {
        if(index < subOrgs.size()) {
            Org subOrg = (Org) getGroup(index);
            return subOrg.getId();
        } else {
            Calling calling = (Calling) getGroup(index);
            return calling.getPositionId();
        }
    }

    @Override
    public long getChildId(int index, int subIndex) {
        Org subOrg = (Org) getGroup(index);
        if(subIndex < subOrg.getChildren().size()) {
            return subOrg.getChildren().get(subIndex).getId();
        } else {
            return subOrg.getCallings().get(subIndex - subOrg.getChildren().size()).getPositionId();
        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int index, boolean isExpanded, View convertView, ViewGroup parentView) {
        if(index < subOrgs.size()) {
            Org org = (Org) getGroup(index);
            return getOrgView(org, isExpanded, convertView, parentView, false);
        } else {
            Calling calling = (Calling) getGroup(index);
            return getCallingView(calling, convertView, parentView, false);
        }
    }

    @Override
    public View getChildView(int index, int subIndex, boolean isLast, View convertView, ViewGroup parentView) {
        Org subOrg = (Org) getGroup(index);
        List<Org> subOrgChildren = subOrg.getChildren();
        if(subIndex < subOrgChildren.size()) {
            Org childSubOrg = subOrgChildren.get(subIndex);
            return getOrgView(childSubOrg, false, convertView, parentView, true);
        } else {
            Calling calling = subOrg.getCallings().get(subIndex - subOrgChildren.size());
            return getCallingView(calling, convertView, parentView, true);
        }
    }

    private View getOrgView(Org org, boolean isExpanded, View convertView, ViewGroup parentView, boolean childView) {
        if(convertView == null || convertView.getId() != R.id.calling_list_org) {
            convertView = LayoutInflater.from(parentView.getContext())
                    .inflate(R.layout.calling_list_org, parentView, false);
        }
        TextView orgName = (TextView) convertView.findViewById(R.id.org_list_name);
        orgName.setText(org.getOrgName());

        if(isExpanded) {
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.light_grey));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.white));
        }
        if(childView) {
            convertView.setPadding(50, 0, 0, 0);
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.light_grey));
        }

        return convertView;
    }

    private View getCallingView(Calling calling, View convertView, ViewGroup parentView, boolean childView) {
        if(convertView == null || convertView.getId() != R.id.calling_list_calling) {
            convertView = LayoutInflater.from(parentView.getContext())
                    .inflate(R.layout.calling_list_content, parentView, false);
        }

        TextView callingTitle = (TextView) convertView.findViewById(R.id.calling_list_title);
        TextView current = (TextView) convertView.findViewById(R.id.calling_list_current);
        TextView proposed = (TextView) convertView.findViewById(R.id.calling_list_proposed);

        callingTitle.setText(calling.getPosition());
        current.setText("(" + memberData.getMemberName(calling.getMemberId()) + ")");
        proposed.setText(memberData.getMemberName(calling.getProposedIndId()));

        if(childView) {
            convertView.setPadding(50, 0, 0, 0);
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.light_grey));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


}
