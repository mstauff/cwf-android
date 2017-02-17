package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.CallingDetailActivity;
import org.ldscd.callingworkflow.display.CallingDetailFragment;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.web.MemberData;

import java.util.List;

public class CallingListAdapter extends RecyclerView.Adapter<CallingListAdapter.ViewHolder> {
    private final List<Calling> callings;
    private final MemberData memberData;
    private boolean twoPane;
    private final FragmentManager fragmentManager;

    public CallingListAdapter(List<Calling> callings, MemberData memberData, boolean twoPane, FragmentManager fragmentManager) {
        this.callings = callings;
        this.memberData = memberData;
        this.twoPane = twoPane;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calling_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = callings.get(position);
        holder.titleView.setText(holder.item.getName());
        memberData.getMemberName(holder.item.getCurrentIndId(), new Response.Listener<String>() {
            @Override
            public void onResponse(String memberName) {
                if(memberName != null) {
                    holder.currentCalledView.setText(memberName);
                } else {
                    holder.currentCalledView.setText(R.string.not_found);
                }
            }
        });
        memberData.getMemberName(holder.item.getProposedIndId(), new Response.Listener<String>() {
            @Override
            public void onResponse(String memberName) {
                if(memberName != null) {
                    holder.proposedView.setText(memberName);
                } else {
                    holder.proposedView.setText(R.string.not_found);
                }
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (twoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(CallingDetailFragment.ARG_ITEM_ID, holder.orgItem.id);
                    CallingDetailFragment fragment = new CallingDetailFragment();
                    fragment.setArguments(arguments);
                    fragmentManager.beginTransaction()
                            .replace(R.id.calling_detail_container, fragment)
                            .commit();
                } else {*/
                Context context = v.getContext();
                Intent intent = new Intent(context, CallingDetailActivity.class);
                intent.putExtra(CallingDetailFragment.ARG_ITEM_ID, holder.item.getPositionId());

                context.startActivity(intent);
                //}
            }
        });
    }

    @Override
    public int getItemCount() {
        return callings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView titleView;
        public final TextView currentCalledView;
        public final TextView proposedView;
        public Calling item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.titleView = (TextView) view.findViewById(R.id.calling_list_title);
            this.currentCalledView = (TextView) view.findViewById(R.id.calling_list_current);
            this.proposedView = (TextView) view.findViewById(R.id.calling_list_proposed);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + titleView.getText() + "'";
        }
    }
}
