package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.ResetOrgsListAdapter;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDriveService;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.UI.Spinner;
import org.ldscd.callingworkflow.web.WebException;

import java.util.List;

import javax.inject.Inject;

public class ResetDataActivity extends AppCompatActivity {
    @Inject
    GoogleDriveService googleDataService;
    @Inject
    DataManager dataManager;

    Activity activity;
    List<Org> orgs;
    ResetOrgsListAdapter adapter;
    FrameLayout resetDataContainer;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        ((CWFApplication) getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_reset_data);

        // Show the Up button in the action bar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.reset_data);

        resetDataContainer = findViewById(R.id.reset_data_container);
        progressBar = findViewById(R.id.reset_data_progress);

        //create orgs list
        ListView orgsList = findViewById(R.id.org_list);
        orgs = dataManager.getOrgs();
        adapter = new ResetOrgsListAdapter(activity, orgs);
        orgsList.setAdapter(adapter);

        //wire up reset link
        TextView resetLink = findViewById(R.id.reset_data_link);
        resetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.getSelectedOrgIds().size() > 0) {
                    View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
                    TextView messageView = dialogView.findViewById(R.id.warning_message);
                    messageView.setText(R.string.confirm_reset_warning);
                    new AlertDialog.Builder(activity)
                        .setView(dialogView)
                        .setPositiveButton(R.string.reset_data, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Spinner.showProgress(true, resetDataContainer, progressBar, getResources());
                                List<Long> selectedOrgIds = adapter.getSelectedOrgIds();
                                dataManager.refreshGoogleDriveOrgs(selectedOrgIds, new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        Spinner.showProgress(false, resetDataContainer, progressBar, getResources());
                                        if(response)
                                            finish();
                                    }
                                }, webErrorListener);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                }
            }
        });
    }

    private Response.Listener<WebException> webErrorListener = new Response.Listener<WebException>() {
        @Override
        public void onResponse(WebException error) {
            View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
            TextView messageView = dialogView.findViewById(R.id.warning_message);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

            switch (error.getExceptionType()) {
                case NO_DATA_CONNECTION:
                    messageView.setText(R.string.error_no_data_connection);
                    break;
                case SERVER_UNAVAILABLE:
                    messageView.setText(R.string.error_lds_server_unavailable);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}