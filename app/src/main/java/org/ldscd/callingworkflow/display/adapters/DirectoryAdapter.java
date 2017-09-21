package org.ldscd.callingworkflow.display.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {
    private DataManager dataManager;

    private List<Member> values;

    public DirectoryAdapter(List<Member> items, DataManager dataManager) {
        values = items;
        this.dataManager = dataManager;
    }

    @Override
    public DirectoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_member_item, parent, false);
        return new DirectoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DirectoryAdapter.ViewHolder holder, int position) {
        holder.memberItem = values.get(position);

        //set member name
        holder.memberNameView.setText(holder.memberItem.getFormattedName());

        //set current callings
        List<Calling> currentCallings = holder.memberItem.getCurrentCallings();
        for(int i=0; i < holder.currentCallingViews.length; i++) {
            if (currentCallings.size() > i) {
                Calling calling = currentCallings.get(i);
                String title = calling.getPosition().getName();
                int callingAge = DataUtil.getMonthsSinceActiveDate(calling.getActiveDate());
                String callingDisplayText = holder.currentCallingViews[i].getResources().getString(R.string.current_calling_with_duration, title, callingAge);

                holder.currentCallingViews[i].setText(callingDisplayText);
                holder.currentCallingViews[i].setVisibility(View.VISIBLE);
            } else {
                holder.currentCallingViews[i].setVisibility(View.GONE);
            }
        }
        //if the number of callings > views to display them, use the last to display number of callings not shown
        if(currentCallings.size() > holder.currentCallingViews.length) {
            TextView lastView = holder.currentCallingViews[holder.currentCallingViews.length-1];
            int numberNotShown = currentCallings.size() - holder.currentCallingViews.length + 1;//plus one for the calling we're replacing with this message
            String displayText = lastView.getResources().getString(R.string.additional_lines_sum, numberNotShown);
            lastView.setText(displayText);
        }

        //set proposed callings
        List<Calling> proposedCallings = new ArrayList<>(holder.memberItem.getProposedCallings());
        for(int i=0; i < holder.proposedCallingViews.length; i++) {
            if (proposedCallings.size() > i) {
                Calling calling = proposedCallings.get(i);
                String title = calling.getPosition().getName();
                String status = calling.getProposedStatus().name();
                String callingDisplayText = holder.proposedCallingViews[i].getResources().getString(R.string.proposed_calling_with_status, title, status);

                holder.proposedCallingViews[i].setText(callingDisplayText);
                holder.proposedCallingViews[i].setVisibility(View.VISIBLE);
            } else {
                holder.proposedCallingViews[i].setVisibility(View.GONE);
            }
        }
        //if the number of callings > views to display them, use the last to display number of callings not shown
        if(proposedCallings.size() > holder.proposedCallingViews.length) {
            TextView lastView = holder.proposedCallingViews[holder.proposedCallingViews.length-1];
            int numberNotShown = proposedCallings.size() - holder.proposedCallingViews.length + 1; //plus one for the calling we're replacing with this message
            String displayText = lastView.getResources().getString(R.string.additional_lines_sum, numberNotShown);
            lastView.setText(displayText);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*Context context = v.getContext();
            Intent intent = new Intent(context, CallingDetailActivity.class);
            intent.putExtra(CallingDetailFragment.CALLING_ID, holder.memberItem.getCallingId());
            intent.putExtra(ExpandableOrgsListActivity.ARG_ORG_ID, holder.memberItem.getParentOrg());
            context.startActivity(intent);*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView memberNameView;
        public final TextView[] currentCallingViews;
        public final TextView[] proposedCallingViews;
        public Member memberItem;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            memberNameView = (TextView) view.findViewById(R.id.member_item_name);
            currentCallingViews = new TextView[3];
            currentCallingViews[0] = ((TextView) view.findViewById(R.id.member_item_current1));
            currentCallingViews[1] = ((TextView) view.findViewById(R.id.member_item_current2));
            currentCallingViews[2] = ((TextView) view.findViewById(R.id.member_item_current3));
            proposedCallingViews = new TextView[3];
            proposedCallingViews[0] = ((TextView) view.findViewById(R.id.member_item_proposed1));
            proposedCallingViews[1] = ((TextView) view.findViewById(R.id.member_item_proposed2));
            proposedCallingViews[2] = ((TextView) view.findViewById(R.id.member_item_proposed3));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + memberNameView.getText() + "'";
        }
    }

}
