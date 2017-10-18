package org.ldscd.callingworkflow.display;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.android.flexbox.FlexboxLayout;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.display.adapters.CallingFiltersAdapter;
import org.ldscd.callingworkflow.model.UnitSettings;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class StatusEditActivity extends AppCompatActivity {
    @Inject
    DataManager dataManager;
    List<CallingStatus> selectedStatus;
    final UnitSettings unitSettings = new UnitSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);

        setContentView(R.layout.activity_status_edit);
        wireUpToolbar();
        wireUpButtons();

    }

    private void wireUpButtons() {
        dataManager.getUnitSettings(new Response.Listener<UnitSettings>() {
            @Override
            public void onResponse(UnitSettings response) {
                if(response != null) {
                    unitSettings.setUnitNumber(response.getUnitNumber());
                    unitSettings.setDisabledStatuses(response.getDisabledStatuses());
                    wireUpStatus();
                }
            }
        });
        final TextView doneButton = (TextView) findViewById(R.id.edit_status_done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CallingStatus> statuses = new ArrayList<CallingStatus>();
                for(CallingStatus status : CallingStatus.values()) {
                    if(selectedStatus.contains(status) && !status.equals(CallingStatus.NONE)) {
                        statuses.add(status);
                    }
                }
                unitSettings.setDisabledStatuses(statuses);
                dataManager.saveUnitSettings(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        if(response) {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.items_saved), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.saving_error), Toast.LENGTH_LONG).show();
                        }
                    }
                }, unitSettings);
            }
        });
    }

    private void wireUpStatus() {
        FlexboxLayout statusLayout = (FlexboxLayout) findViewById(R.id.status_list);
        selectedStatus = unitSettings.getDisabledStatuses();
        for(final CallingStatus status: CallingStatus.values()) {
            if(!status.equals(CallingStatus.NONE)) {
                TextView statusView = new TextView(statusLayout.getContext());
                statusView.setText(status.toString());
                statusView.setPadding(16, 16, 16, 16);
                if (!selectedStatus.contains(status)) {
                    giveSelectedAppearance(statusView);
                } else {
                    removeSelectedAppearance(statusView);
                }

                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selectedStatus.contains(status)) {
                            selectedStatus.remove(status);
                            giveSelectedAppearance((TextView) view);
                        } else {
                            selectedStatus.add(status);
                            removeSelectedAppearance((TextView) view);
                        }
                    }
                });

                statusLayout.addView(statusView);
            }
        }
    }

    private void wireUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_status_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void giveSelectedAppearance(TextView view) {
        view.setTextColor(getResources().getColor(R.color.default_text_light));
        view.setBackground(getResources().getDrawable(R.drawable.selected_filter_background));
    }
    private void removeSelectedAppearance(TextView view) {
        view.setTextColor(getResources().getColor(R.color.ldstools_gray_dark));
        view.setBackground(null);
    }
}