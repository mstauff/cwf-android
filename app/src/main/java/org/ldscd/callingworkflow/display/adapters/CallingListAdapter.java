package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.CallingDetailActivity;
import org.ldscd.callingworkflow.display.CallingDetailFragment;
import org.ldscd.callingworkflow.display.ExpandableOrgsListActivity;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

public class CallingListAdapter extends RecyclerView.Adapter<CallingListAdapter.ViewHolder> {

    private FragmentManager fragmentManager;
    private final List<Calling> mValues;
    private boolean twoPane;
    private DataManager dataManager;

    public CallingListAdapter(List<Calling> items, DataManager dataManager, boolean twoPane, FragmentManager fragmentManager) {
        mValues = items;
        this.dataManager = dataManager;
        this.twoPane = twoPane;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_calling_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.callingItem = mValues.get(position);

        //set calling name
        holder.callingTitleView.setText(holder.callingItem.getPosition().getName());

        //set proposed member and status
        String proposedName = "";
        Long proposedId = holder.callingItem.getProposedIndId();
        if(proposedId != null && proposedId > 0) {
            proposedName = dataManager.getMemberName(proposedId);
        }
        String proposedStatus = holder.callingItem.getProposedStatus();
        if(proposedStatus == null) {
            proposedStatus = "";
        }
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
            holder.currentCalledView.setVisibility(View.VISIBLE);
            holder.currentCalledView.setText(memberName + " (" + callingAge + " " + monthsLabel + ")");
        } else {
            holder.currentCalledView.setVisibility(View.GONE);
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
        return mValues == null ? 0 : mValues.size();
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
}
