package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.MemberLookupFragment;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberLookupAdapter extends ArrayAdapter<Member> {

    /* Fields */
    private Context mContext;
    private LayoutInflater inflater;
    private List<Member> members = null;
    private List<Member> wardMembers;
    private List<Member> suggestions;
    private final MemberLookupFragment fragment;

    /* Constructor */
    public MemberLookupAdapter(Context context, MemberLookupFragment fragment, int viewResourceId, ArrayList<Member> members) {
        super(context, viewResourceId);
        mContext = context;
        this.fragment = fragment;
        this.members = members;
        this.wardMembers = (ArrayList<Member>) members.clone();
        inflater = LayoutInflater.from(mContext);
        this.suggestions = new ArrayList<Member>();
    }

    /* Inner class referencing the xml layout */
    public class ViewHolder {
        TextView name;
        TextView calling;
        ImageView contact_info;
    }

    /* Methods */
    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Member getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final MemberLookupAdapter.ViewHolder holder;
        final Member member = members.get(position);
        if (view == null) {
            holder = new MemberLookupAdapter.ViewHolder();
            view = inflater.inflate(R.layout.fragment_member_lookup_list_layout, null);
            holder.name = (TextView) view.findViewById(R.id.member_lookup_list_layout_name);
            holder.calling = (TextView) view.findViewById(R.id.member_lookup_list_layout_current_calling);
            holder.contact_info = (ImageView) view.findViewById(R.id.member_lookup_list_layout_contact_info);
            view.setTag(holder);
        } else {
            holder = (MemberLookupAdapter.ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(member.getFormattedName());
        if(member.getCurrentCallings() != null && member.getCurrentCallings().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for(Calling calling : member.getCurrentCallings()) {
                sb.append(calling.getPosition().getName());
            }
            holder.calling.setText(sb.toString());
        } else {
            holder.calling.setText("");
        }
        holder.contact_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragment != null){
                    fragment.wireUpIndividualInformationFragments(member.getIndividualId());
                }
            }
        });
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        public String convertResultToString(Object resultValue) {
            return ((Member) (resultValue)).getFormattedName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (Member member : wardMembers) {
                    if (member.getFormattedName().toLowerCase()
                            .contains(constraint.toString().toLowerCase())) {
                        suggestions.add(member);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            ArrayList<Member> filteredList = (ArrayList<Member>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Member c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}