package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.display.ConflictInfoFragment;
import org.ldscd.callingworkflow.display.ExpandableOrgsListActivity;
import org.ldscd.callingworkflow.model.Org;

import java.util.List;

public class OrgListAdapter extends RecyclerView.Adapter<OrgListAdapter.ViewHolder> {

    private FragmentManager fragmentManager;
    private final List<Org> mValues;
    private boolean twoPane;

    public OrgListAdapter(List<Org> items, boolean twoPane, FragmentManager fragmentManager) {
        mValues = items;
        this.twoPane = twoPane;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.org_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.orgItem = mValues.get(position);
        holder.orgTitleView.setText(mValues.get(position).getDefaultOrgName());
        holder.orgTitleView.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(context, ExpandableOrgsListActivity.class);
                intent.putExtra(ExpandableOrgsListActivity.ARG_ORG_ID, holder.orgItem.getId());
                intent.putExtra(ExpandableOrgsListActivity.GET_DATA, true);
                context.startActivity(intent);
                //}
            }
        });

        if(holder.orgItem.getConflictCause() != null && holder.orgItem.getConflictCause().getConflictCause() != null && holder.orgItem.getConflictCause().getConflictCause().length() > 0 ) {
            holder.conflictContainer.setVisibility(View.VISIBLE);
            holder.conflictButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConflictInfoFragment conflictInfo = new ConflictInfoFragment();
                    conflictInfo.setOrg(holder.orgItem);
                    conflictInfo.setOperation(Operation.DELETE);
                    conflictInfo.show(fragmentManager, "ConflictInfo");
                }
            });
        } else {
            holder.conflictContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView orgTitleView;
        public Org orgItem;
        public ImageButton conflictButton;
        public View conflictContainer;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            orgTitleView = view.findViewById(R.id.org_list_name);
            conflictButton = view.findViewById(R.id.org_conflict_icon);
            conflictContainer = view.findViewById(R.id.org_list_conflict_container);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + orgTitleView.getText() + "'";
        }
    }
}
