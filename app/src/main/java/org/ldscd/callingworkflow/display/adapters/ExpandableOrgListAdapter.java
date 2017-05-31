package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.ExpandableOrgsListActivity;
import org.ldscd.callingworkflow.display.CreateCallingActivity;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

public class ExpandableOrgListAdapter extends BaseExpandableListAdapter {
    private final Context ctx;
    private Org org;
    private List<Org> subOrgs;
    private List<Calling> callings;
    private DataManager dataManager;

    private boolean twoPane;
    private final FragmentManager fragmentManager;

    public ExpandableOrgListAdapter(Org org, DataManager dataManager, boolean twoPane, FragmentManager fragmentManager, Context ctx) {
        this.org = org;
        subOrgs = org.getChildren();
        callings = org.getCallings();
        this.dataManager = dataManager;
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
            //Calling calling = (Calling) getGroup(index);
            return 0;
        }
    }

    @Override
    public long getChildId(int index, int subIndex) {
        return 0;
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

    private View getOrgView(final Org groupOrg, boolean isExpanded, View convertView, ViewGroup parentView, boolean childView) {
        if(convertView == null || convertView.getId() != R.id.list_org_item) {
            convertView = LayoutInflater.from(parentView.getContext())
                    .inflate(R.layout.list_org_item, parentView, false);
            ImageButton addButton = (ImageButton) convertView.findViewById(R.id.add_calling_button);
            addButton.setFocusable(false);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ctx.getApplicationContext(), CreateCallingActivity.class);
                    intent.putExtra(CreateCallingActivity.PARENT_ORG_ID, groupOrg.getId());
                    ctx.startActivity(intent);

                }
            });
        }
        TextView orgName = (TextView) convertView.findViewById(R.id.org_list_name);
        orgName.setText(groupOrg.getDefaultOrgName());

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
        if(convertView == null || convertView.getId() != R.id.calling_item) {
            convertView = LayoutInflater.from(parentView.getContext())
                    .inflate(R.layout.list_calling_item, parentView, false);
        }

        TextView callingTitle = (TextView) convertView.findViewById(R.id.calling_item_title);
        TextView current = (TextView) convertView.findViewById(R.id.calling_item_current);
        TextView proposed = (TextView) convertView.findViewById(R.id.calling_item_proposed);
        TextView proposedStatus = (TextView) convertView.findViewById(R.id.calling_item_proposed_status);

        //set calling name
        callingTitle.setText(calling.getPosition().getName());

        //set current called
        if(calling.getMemberId() != null && calling.getMemberId() > 0) {
            current.setText("(" + dataManager.getMemberName(calling.getMemberId()) + ")");
            final Long currentIndividualId = calling.getMemberId();
            current.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ctx instanceof ExpandableOrgsListActivity) {
                        ((ExpandableOrgsListActivity) ctx).wireUpIndividualInformationFragments(currentIndividualId);
                    }
                }
            });
        }
        //set proposed member
        if(calling.getProposedIndId() != null && calling.getProposedIndId() > 0) {
            proposed.setText(dataManager.getMemberName(calling.getProposedIndId()));
            final Long proposedIndividualId = calling.getProposedIndId();
            proposed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ctx instanceof ExpandableOrgsListActivity) {
                        ((ExpandableOrgsListActivity) ctx).wireUpIndividualInformationFragments(proposedIndividualId);
                    }
                }
            });
        }
        //set proposed status
        if(calling.getProposedStatus() != null) {
            proposedStatus.setText("(" + calling.getProposedStatus() + ")");
        }

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