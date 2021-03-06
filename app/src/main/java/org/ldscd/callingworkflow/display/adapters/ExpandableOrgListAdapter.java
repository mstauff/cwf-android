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
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.display.ConflictInfoFragment;
import org.ldscd.callingworkflow.display.CreateCallingActivity;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

public class ExpandableOrgListAdapter extends BaseExpandableListAdapter {
    private static final int GROUP_INDENT = 50;
    private static final int CHILD_INDENT = 100;
    private final Context ctx;
    private Org org;
    private List<Org> subOrgs;
    private List<Calling> callings;
    private DataManager dataManager;

    private final FragmentManager fragmentManager;

    public ExpandableOrgListAdapter(Org org, DataManager dataManager, FragmentManager fragmentManager, Context ctx) {
        this.org = org;
        subOrgs = org.getChildren();
        callings = org.getCallings();
        this.dataManager = dataManager;
        this.ctx = ctx;
        this.fragmentManager = fragmentManager;
    }

    public Org getOrg() {
        return org;
    }

    public void changeOrg(Org org) {
        this.org = org;
        subOrgs = org.getChildren();
        callings = org.getCallings();
        this.notifyDataSetChanged();
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
            return getOrgView(org, convertView, parentView, false);
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
            return getOrgView(childSubOrg, convertView, parentView, true);
        } else {
            Calling calling = subOrg.getCallings().get(subIndex - subOrgChildren.size());
            return getCallingView(calling, convertView, parentView, true);
        }
    }

    private View getOrgView(final Org groupOrg, View convertView, ViewGroup parentView, boolean childView) {
        //initialize
        if(convertView == null || convertView.getId() != R.id.list_org_item) {
            convertView = LayoutInflater.from(parentView.getContext())
                    .inflate(R.layout.list_org_item, parentView, false);
        }

        TextView orgName = convertView.findViewById(R.id.org_list_name);
        ImageButton addButton = convertView.findViewById(R.id.add_calling_button);
        ImageButton conflictIcon = convertView.findViewById(R.id.org_conflict_icon);

        orgName.setText(groupOrg.getDefaultOrgName());

        /*  If the calling section has the ability to add a calling and
            the user has permissions to add a calling to this org then show the
            addButton in the list.
         */
        if(groupOrg.potentialNewPositions().size() > 0 && groupOrg.getCanView()) {
            addButton.setVisibility(View.VISIBLE);
            addButton.setFocusable(false);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ctx.getApplicationContext(), CreateCallingActivity.class);
                    intent.putExtra(CreateCallingActivity.PARENT_ORG_ID, groupOrg.getId());
                    ctx.startActivity(intent);
                }
            });
        } else {
            addButton.setVisibility(View.GONE);
        }

         /* Show the conflict icon if the org in lcr was deleted but wasn't in google. */
        if(groupOrg.getConflictCause() != null) {
            addButton.setVisibility(View.GONE);
            conflictIcon.setFocusable(false);
            if(groupOrg.getConflictCause().equals(ConflictCause.LDS_EQUIVALENT_DELETED)) {
                conflictIcon.setVisibility(View.VISIBLE);
                conflictIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ConflictInfoFragment conflictInfo = new ConflictInfoFragment();
                        conflictInfo.setOrg(groupOrg);
                        conflictInfo.show(fragmentManager, "ConflictInfo");
                    }
                });
            } else {
                conflictIcon.setEnabled(false);
            }
        } else {
            conflictIcon.setVisibility(View.GONE);
        }

        if(childView) {
            convertView.setPadding(CHILD_INDENT, 0, 0, 0);
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.light_grey));
        } else {
            convertView.setPadding(GROUP_INDENT, 0, 0, 0);
        }

        return convertView;
    }

    private View getCallingView(final Calling calling, View convertView, ViewGroup parentView, boolean childView) {
        if(convertView == null || convertView.getId() != R.id.calling_item) {
            convertView = LayoutInflater.from(parentView.getContext())
                    .inflate(R.layout.list_calling_item, parentView, false);
        }

        TextView callingTitle = convertView.findViewById(R.id.calling_item_title);
        TextView current = convertView.findViewById(R.id.calling_item_current);
        TextView proposed = convertView.findViewById(R.id.calling_item_proposed);

        //set calling name
        PositionMetaData metaData = dataManager.getPositionMetadata(calling.getPosition().getPositionTypeId());
        String positionName = metaData != null ? metaData.getShortName() : null;
        if(positionName == null) {
            positionName = calling.getPosition().getName();
        }
        callingTitle.setText(positionName);

        //set proposed member and status
        String proposedName = "";
        if(calling.getProposedIndId() != null && calling.getProposedIndId() > 0) {
            proposedName = dataManager.getMemberName(calling.getProposedIndId());
        }
        CallingStatus status = calling.getProposedStatus();
        String proposedStatus = status==null || status.equals(CallingStatus.NONE) ? "" : status.toString();
        if(!proposedName.equals("") && !proposedStatus.equals("")) {
            proposed.setVisibility(View.VISIBLE);
            proposed.setText(proposedName + " - " + proposedStatus);
        } else if(proposedName.equals("") && proposedStatus.equals("")) {
            proposed.setVisibility(View.GONE);
        } else {
            proposed.setVisibility(View.VISIBLE);
            proposed.setText(proposedName + proposedStatus);
        }

        //set current called
        if(calling.getMemberId() != null && calling.getMemberId() > 0) {
            String currentName = dataManager.getMemberName(calling.getMemberId());
            int callingAge = DataUtil.getMonthsSinceActiveDate(calling.getActiveDate());
            String monthsLabel = convertView.getResources().getString(R.string.months);
            current.setText(currentName + " (" + callingAge + " " + monthsLabel + ")");
        } else {
            current.setText("--");
        }

        //indent if this is a child
        if(childView) {
            convertView.setPadding(CHILD_INDENT, 0, 0, 0);
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.light_grey));
        } else {
            convertView.setPadding(GROUP_INDENT, 0, 0, 0);
            convertView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.white_background));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}