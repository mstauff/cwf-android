package org.ldscd.callingworkflow.display;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.android.flexbox.FlexboxLayout;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.UnitSettings;
import org.ldscd.callingworkflow.services.GoogleDriveService;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.WebException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class StatusEditActivity extends AppCompatActivity {
    @Inject
    DataManager dataManager;
    List<CallingStatus> selectedStatus;
    final UnitSettings unitSettings = new UnitSettings();

    @Inject
    GoogleDriveService googleDataService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);

        setContentView(R.layout.activity_status_edit);
        wireUpToolbar();
        if(googleDataService.isAuthenticated(getApplicationContext())) {
            init();
        }
    }

    private void init() {
        dataManager.getUnitSettings(new Response.Listener<UnitSettings>() {
            @Override
            public void onResponse(UnitSettings response) {
                if(response != null) {
                    unitSettings.setUnitNumber(response.getUnitNumber());
                    unitSettings.setDisabledStatuses(response.getDisabledStatuses());
                    wireUpStatus();
                }
            }
        }, webErrorListener, false);
    }

    private void wireUpStatus() {
        FlexboxLayout statusLayout = findViewById(R.id.status_list);
        selectedStatus = unitSettings.getDisabledStatuses();
        for (final CallingStatus status : CallingStatus.values()) {
            if (!status.equals(CallingStatus.NONE)) {
                TextView statusView = new TextView(statusLayout.getContext());
                statusView.setText(status.toString());
                statusView.setPadding(36, 26, 36, 26);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Initialize the top menu items. */
        int id = item.getItemId();
        /* If the back button is pushed save the status changes. */
        if (id == android.R.id.home) {
            if(googleDataService.isAuthenticated(getApplicationContext())) {
            /* Save status items. */
                List<CallingStatus> statuses = new ArrayList<CallingStatus>();
                for (CallingStatus status : CallingStatus.values()) {
                    if (selectedStatus.contains(status) && !status.equals(CallingStatus.NONE)) {
                        statuses.add(status);
                    }
                }
                unitSettings.setDisabledStatuses(statuses);
                dataManager.saveUnitSettings(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        if (response) {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.items_saved), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.saving_error), Toast.LENGTH_LONG).show();
                        }
                    }
                }, webErrorListener, unitSettings);
            }
            finish();
            return true;
        }
        return true;
    }
    private void wireUpToolbar() {
        Toolbar toolbar = findViewById(R.id.edit_status_toolbar);
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
    private Response.Listener<WebException> webErrorListener = new Response.Listener<WebException>() {
        @Override
        public void onResponse(WebException error) {
            View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
            TextView messageView = dialogView.findViewById(R.id.warning_message);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StatusEditActivity.this);

            switch (error.getExceptionType()) {
                case NO_DATA_CONNECTION:
                    messageView.setText(R.string.error_no_data_connection);
                    break;
                case UNKOWN_GOOGLE_EXCEPTION:
                    messageView.setText(R.string.error_google_drive);
                    break;
                default:
                    messageView.setText(R.string.error_generic_web);
            }
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton(R.string.ok, null);
            dialogBuilder.show();
        }
    };
}