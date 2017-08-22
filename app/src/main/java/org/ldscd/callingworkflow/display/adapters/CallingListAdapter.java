package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.display.CallingDetailActivity;
import org.ldscd.callingworkflow.display.CallingDetailFragment;
import org.ldscd.callingworkflow.display.ExpandableOrgsListActivity;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.List;

public class CallingListAdapter extends RecyclerView.Adapter<CallingListAdapter.ViewHolder>
        implements  CallingFiltersAdapter {

    private FragmentManager fragmentManager;
    private boolean twoPane;
    private DataManager dataManager;

    private List<Calling> allValues;
    private List<Calling> displayValues;
    private List<CallingStatus> statusFilters;
    private List<Org> orgFilters;

    public CallingListAdapter(List<Calling> items, DataManager dataManager, boolean twoPane, FragmentManager fragmentManager) {
        allValues = items;
        displayValues = items;
        this.dataManager = dataManager;
        this.twoPane = twoPane;
        this.fragmentManager = fragmentManager;
        statusFilters = new ArrayList<>();
        orgFilters = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_calling_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.callingItem = displayValues.get(position);

        //set calling name
        holder.callingTitleView.setText(holder.callingItem.getPosition().getName());

        //set proposed member and status
        String proposedName = "";
        Long proposedId = holder.callingItem.getProposedIndId();
        if(proposedId != null && proposedId > 0) {
            proposedName = dataManager.getMemberName(proposedId);
        }
        CallingStatus status = holder.callingItem.getProposedStatus();
        String proposedStatus = status == null || status.equals(CallingStatus.NONE) ? "" : status.toString();

        if(!proposedName.equals("") && !proposedStatus.equals("")) {
            holder.proposedMemberView.setVisibility(View.VISIBLE);
            holder.proposedMemberView.setText(proposedName + " - " + proposedStatus);
        } else if(proposedName.equals("") && proposedStatus.equals("")) {
            holder.proposedMemberView.setVisibility(View.GONE);
        } else {
            holder.proposedMemberView.setVisibility(View.VISIBLE);
            holder.proposedMemberView.setText(proposedName + proposedStatus);
        }

        //set current called
        Long currentId = holder.callingItem.getMemberId();
        if(currentId != null && currentId > 0) {
            String memberName = dataManager.getMemberName(currentId);
            int callingAge = DataUtil.getMonthsSinceActiveDate(holder.callingItem.getActiveDate());
            String monthsLabel = holder.view.getResources().getString(R.string.months);
            holder.currentCalledView.setText(memberName + " (" + callingAge + " " + monthsLabel + ")");
        } else {
            holder.currentCalledView.setText("--");
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (twoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(CallingDetailFragment.CALLING_ID, holder.orgItem.id);
                    CallingDetailFragment fragment = new CallingDetailFragment();
                    fragment.setArguments(arguments);
                    fragmentManager.beginTransaction()
                            .replace(R.id.calling_detail_container, fragment)
                            .commit();
                } else {*/
                Context context = v.getContext();
                Intent intent = new Intent(context, CallingDetailActivity.class);
                intent.putExtra(CallingDetailFragment.CALLING_ID, holder.callingItem.getCallingId());
                intent.putExtra(ExpandableOrgsListActivity.ARG_ORG_ID, holder.callingItem.getParentOrg());
                context.startActivity(intent);
                //}
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayValues == null ? 0 : displayValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView callingTitleView;
        public final TextView currentCalledView;
        public final TextView proposedMemberView;
        public Calling callingItem;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            callingTitleView = (TextView) view.findViewById(R.id.calling_item_title);
            currentCalledView = (TextView) view.findViewById(R.id.calling_item_current);
            proposedMemberView = (TextView) view.findViewById(R.id.calling_item_proposed);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + callingTitleView.getText() + "'";
        }
    }

    @Override
    public void setStatusList(List<CallingStatus> statusList) {
        statusFilters = statusList;
    }

    @Override
    public List<CallingStatus> getStatusList() {
        return statusFilters;
    }

    @Override
    public void setOrgList(List<Org> orgList) {
        orgFilters = orgList;
    }

    @Override
    public List<Org> getOrgList() {
        return orgFilters;
    }

    @Override
    public void filterCallings() {
        displayValues = new ArrayList<>();
        for(Calling calling: allValues) {
            boolean addCalling = true;

            //if there are values in statusFilters than only include callings with a matching proposedStatus
            if(statusFilters.size() > 0) {
                addCalling = false;
                for(CallingStatus callingStatus: statusFilters) {
                    if(calling.getProposedStatus().equals(callingStatus)) {
                        addCalling = true;
                        break;
                    }
                }
            }

            //if there are values in orgFilters and the calling hasn't already been removed by another filter
            //only include it if it resides in one of the filter orgs
            if(addCalling && orgFilters.size() > 0) {
                addCalling = false;
                for(Org org: orgFilters) {
                    List<String> orgCallings = org.allOrgCallingIds();
                    if(orgCallings.contains(calling.getCallingId())) {
                        addCalling = true;
                        break;
                    }
                }
            }

            if(addCalling) {
                displayValues.add(calling);
            }
        }
        notifyDataSetChanged();
    }
}
