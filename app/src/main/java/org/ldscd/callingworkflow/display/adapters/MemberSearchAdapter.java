package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MemberSearchAdapter extends ArrayAdapter<Member> {
    private Context mContext;
    private LayoutInflater inflater;
    private List<Member> members = null;
    private List<Member> wardMembers;
    private List<Member> suggestions;
    private int viewResourceId;

    public MemberSearchAdapter(Context context, int viewResourceId, ArrayList<Member> members) {
        super(context, viewResourceId, members);
        mContext = context;
        this.members = members;
        this.wardMembers = (ArrayList<Member>) members.clone();
        inflater = LayoutInflater.from(mContext);
        this.suggestions = new ArrayList<Member>();
        this.viewResourceId = viewResourceId;
    }

    public class ViewHolder {
        TextView name;
    }

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
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.fragment_member_search_list, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(members.get(position).getFormattedName());
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
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
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


    // Filter Class
    /*public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        members.clear();
        if (charText.length() == 0) {
            members.addAll(arraylist);
        } else {
            for (Member wp : arraylist) {
                if (wp.getFormattedName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    members.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }*/
}