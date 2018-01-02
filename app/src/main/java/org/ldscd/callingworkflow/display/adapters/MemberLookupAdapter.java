package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.MemberLookupFragment;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.List;

public class MemberLookupAdapter extends ArrayAdapter<Member> implements Filterable {

    /* Fields */
    private Context mContext;
    private LayoutInflater inflater;
    private List<Member> filterMembersList;
    private List<Member> originalMembersList;
    private final MemberLookupFragment fragment;
    private MemberFilter mFilter = new MemberFilter();
    private DataManager dataManager;

    /* Constructor */
    public MemberLookupAdapter(Context context, MemberLookupFragment fragment, int viewResourceId,
                               List<Member> members, DataManager dataManager) {
        super(context, viewResourceId);
        mContext = context;
        this.fragment = fragment;
        this.filterMembersList = members;
        this.originalMembersList = members;
        inflater = LayoutInflater.from(mContext);
        this.dataManager = dataManager;
    }

    /* Inner class referencing the xml layout */
    private class ViewHolder {
        TextView name;
        TextView calling;
        TextView proposedCalling;
        ImageView contact_info;
    }

    /* Methods */
    @Override
    public int getCount() {
        return filterMembersList.size();
    }

    @Override
    public Member getItem(int position) {
        return filterMembersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final MemberLookupAdapter.ViewHolder holder;
        final Member member = filterMembersList.get(position);
            if (view == null) {
                holder = new MemberLookupAdapter.ViewHolder();
                view = inflater.inflate(R.layout.fragment_member_lookup_list_layout, null);
                holder.name = (TextView) view.findViewById(R.id.member_lookup_list_layout_name);
                holder.calling = (TextView) view.findViewById(R.id.member_lookup_list_layout_current_calling);
                holder.proposedCalling = (TextView) view.findViewById(R.id.member_lookup_list_layout_proposed_calling);
                holder.contact_info = (ImageView) view.findViewById(R.id.member_lookup_list_layout_contact_info);
                view.setTag(holder);
            } else {
                holder = (MemberLookupAdapter.ViewHolder) view.getTag();
            }

            holder.name.setText(member.getFormattedName());
            if (member.getCurrentCallings() != null && member.getCurrentCallings().size() > 0) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Calling calling : member.getCurrentCallings()) {
                    PositionMetaData metaData = dataManager.getPositionMetadata(calling.getPosition().getPositionTypeId());
                    String callingName = metaData != null && metaData.getMediumName() != null ? metaData.getMediumName() : calling.getPosition().getName();
                    sb.append(callingName + "[" + DataUtil.getMonthsSinceActiveDate(calling.getActiveDate()) + "M]");
                    if(i++ != member.getCurrentCallings().size() - 1){
                        sb.append(' ');
                        sb.append('-');
                        sb.append(' ');
                    }
                }
                holder.calling.setVisibility(View.VISIBLE);
                holder.calling.setText(sb.toString());
            } else {
                holder.calling.setVisibility(View.GONE);
            }
            if(member.getProposedCallings() != null && member.getProposedCallings().size() > 0) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Calling calling : member.getProposedCallings()) {
                    PositionMetaData metaData = dataManager.getPositionMetadata(calling.getPosition().getPositionTypeId());
                    String callingName = metaData != null && metaData.getMediumName() != null ? metaData.getMediumName() : calling.getPosition().getName();
                    sb.append(callingName + "[" + calling.getProposedStatus() + "]");
                    if(i++ != member.getProposedCallings().size() - 1){
                        sb.append(' ');
                        sb.append('-');
                        sb.append(' ');
                    }
                }
                holder.proposedCalling.setVisibility(View.VISIBLE);
                holder.proposedCalling.setText(sb.toString());
            } else {
                holder.proposedCalling.setVisibility(View.GONE);
            }

            holder.contact_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fragment != null) {
                        fragment.wireUpIndividualInformationFragments(member.getIndividualId());
                    }
                }
            });
        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /* Class(s). */
    private class MemberFilter extends Filter {
        /* Filter Section. */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = originalMembersList;
                results.count = originalMembersList.size();
            }
            else {
                List<Member> memberList = originalMembersList;
                int count = memberList.size();
                final List<Member> nMemberList = new ArrayList<Member>(count);

                for (Member member : memberList) {
                    if (member.getFormattedName().toUpperCase().contains(constraint.toString().toUpperCase())) {
                        nMemberList.add(member);
                    }
                }

                results.values = nMemberList;
                results.count = nMemberList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterMembersList = (ArrayList<Member>) results.values;
            notifyDataSetChanged();
        }
    }
}