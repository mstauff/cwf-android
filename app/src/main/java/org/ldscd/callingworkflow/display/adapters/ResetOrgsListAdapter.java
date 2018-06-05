package org.ldscd.callingworkflow.display.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Org;

import java.util.ArrayList;
import java.util.List;

public class ResetOrgsListAdapter extends ArrayAdapter<Org>{
    private List<Long> selectedOrgIds;

    public ResetOrgsListAdapter(Context context, List<Org> orgs) {
        super(context, R.layout.reset_org_list_content, orgs);
        selectedOrgIds = new ArrayList<>();
    }

    public List<Long> getSelectedOrgIds() {
        return selectedOrgIds;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reset_org_list_content, parent, false);
        }

        TextView nameView = convertView.findViewById(R.id.org_list_name);
        final ImageView checkboxView = convertView.findViewById(R.id.checkbox_icon);
        final Org org = getItem(position);
        nameView.setText(org.getDefaultOrgName());
        if(selectedOrgIds.contains(org)) {
            checkboxView.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            checkboxView.setImageResource(android.R.drawable.checkbox_off_background);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedOrgIds.contains(org.getId())) {
                    selectedOrgIds.remove(org.getId());
                    checkboxView.setImageResource(android.R.drawable.checkbox_off_background);
                } else {
                    selectedOrgIds.add(org.getId());
                    checkboxView.setImageResource(android.R.drawable.checkbox_on_background);
                }
            }
        });

        return convertView;
    }
}