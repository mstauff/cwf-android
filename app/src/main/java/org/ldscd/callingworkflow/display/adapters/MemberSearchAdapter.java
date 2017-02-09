package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MemberSearchAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private List<Member> members = null;
    private ArrayList<Member> arraylist;

    public MemberSearchAdapter(Context context, List<Member> members) {
        mContext = context;
        this.members = members;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Member>();
        this.arraylist.addAll(members);
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

    public View getView(final int position, View view, ViewGroup parent) {
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

    // Filter Class
    public void filter(String charText) {
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
    }
}