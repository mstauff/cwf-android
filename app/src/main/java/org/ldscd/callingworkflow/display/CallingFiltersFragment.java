package org.ldscd.callingworkflow.display;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.display.adapters.CallingFiltersAdapter;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

import javax.inject.Inject;

public class CallingFiltersFragment extends DialogFragment {
    @Inject
    DataManager dataManager;
    CallingFiltersAdapter adapter;
    List<CallingStatus> selectedStatus;
    List<Org> selectedOrgs;

    public void setAdapter(CallingFiltersAdapter adapter) {
        this.adapter = adapter;
        selectedStatus = adapter.getStatusList();
        selectedOrgs = adapter.getOrgList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((CWFApplication)getActivity().getApplication()).getNetComponent().inject(this);

        getDialog().setTitle("test R.string.filters_heading");
        View callingFilters = inflater.inflate(R.layout.calling_filters, container, false);

        FlexboxLayout statusFilters = (FlexboxLayout) callingFilters.findViewById(R.id.status_filters);
        for(final CallingStatus status: CallingStatus.values()) {
            TextView statusView = new TextView(statusFilters.getContext());
            statusView.setText(status.getStatus());
            statusView.setPadding(16, 16, 16, 16);
            if(selectedStatus.contains(status)) {
                statusView.setBackgroundColor(getResources().getColor(R.color.ldstools_blue));
            }

            statusView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selectedStatus.contains(status)){
                        selectedStatus.remove(status);
                        view.setBackgroundColor(getResources().getColor(R.color.white_background));
                    } else {
                        selectedStatus.add(status);
                        view.setBackgroundColor(getResources().getColor(R.color.ldstools_blue));
                    }
                }
            });

            statusFilters.addView(statusView);
        }

        final FlexboxLayout orgFilters = (FlexboxLayout) callingFilters.findViewById(R.id.org_filters);
        List<Org> orgs = dataManager.getOrgs();
        for(final Org org: orgs) {
            final TextView orgView = new TextView(statusFilters.getContext());
            orgView.setText(org.getDefaultOrgName());
            orgView.setPadding(16, 16, 16, 16);
            if(selectedOrgs.contains(org)) {
                orgView.setBackgroundColor(getResources().getColor(R.color.ldstools_blue));
            }

            orgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selectedOrgs.contains(org)) {
                        selectedOrgs.remove(org);
                        view.setBackgroundColor(getResources().getColor(R.color.white));
                    } else {
                        selectedOrgs.add(org);
                        view.setBackgroundColor(getResources().getColor(R.color.ldstools_blue));
                    }
                }
            });

            orgFilters.addView(orgView);
        }

        TextView confirmButton = (TextView) callingFilters.findViewById(R.id.calling_filters_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return callingFilters;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        adapter.filterCallings();
        super.onDismiss(dialog);
    }
}
