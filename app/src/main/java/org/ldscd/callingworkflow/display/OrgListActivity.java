package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.OrgListAdapter;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.WebException;

import java.util.List;

import javax.inject.Inject;

import static android.view.View.GONE;

public class OrgListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;
    AppCompatActivity activity = this;
    RecyclerView recyclerView;
    FragmentManager fragmentManager;
    ProgressBar progressBar;

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_org_list);
        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.navigation_drawer_orgs));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_orgs);

        recyclerView = findViewById(R.id.org_list);
        setupRecyclerView();

        if (findViewById(R.id.calling_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }
    }

    private void setupRecyclerView() {
        List<Org> orgs = dataManager.getOrgs();
        recyclerView.setAdapter(new OrgListAdapter(orgs, fragmentManager));
        if(orgs == null || orgs.isEmpty()) {
            reloadData();
        }
    }

    private void reloadData() {
        final Activity activity = this;
        final LinearLayout layout = findViewById(R.id.org_list_reload_data_container);
        layout.setVisibility(View.VISIBLE);
        final TextView reloadButton = findViewById(R.id.org_list_reload_data_progress_data_link);
        reloadButton.setVisibility(View.VISIBLE);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar = findViewById(R.id.org_list_reload_data_progress);
                progressBar.setVisibility(View.VISIBLE);
                if(dataManager.isGoogleDriveAuthenticated(getApplicationContext())) {
                    dataManager.getUserInfo(null, null, new Response.Listener<LdsUser>() {
                        @Override
                        public void onResponse(LdsUser response) {
                            dataManager.loadOrgs(new Response.Listener<Boolean>() {
                                @Override
                                public void onResponse(Boolean response) {
                                    layout.setVisibility(GONE);
                                    reloadButton.setVisibility(GONE);
                                    progressBar.setVisibility(GONE);
                                    setupRecyclerView();
                                }
                            }, webErrorListener, progressBar, activity);
                        }
                    }, webErrorListener);
                } else {
                    layout.setVisibility(GONE);
                    reloadButton.setVisibility(GONE);
                    progressBar.setVisibility(GONE);
                    Intent intent = new Intent(OrgListActivity.this, GoogleDriveOptionsActivity.class);
                    intent.putExtra("activity", "");
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_callings) {
            Intent intent = new Intent(this, CallingListActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_directory) {
            Intent intent = new Intent(this, DirectoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Response.Listener<WebException> webErrorListener = new Response.Listener<WebException>() {
        @Override
        public void onResponse(WebException error) {
            View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
            TextView messageView = dialogView.findViewById(R.id.warning_message);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

            switch (error.getExceptionType()) {
                case LDS_AUTH_REQUIRED:
                    messageView.setText(R.string.error_lds_auth_failed);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Intent intent = new Intent(OrgListActivity.this, LDSAccountActivity.class);
                            startActivity(intent);
                        }
                    });
                    break;
                case GOOGLE_AUTH_REQUIRED:
                    messageView.setText(R.string.error_google_auth_failed);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Intent intent = new Intent(OrgListActivity.this, GoogleDriveOptionsActivity.class);
                            intent.putExtra("activity", "");
                            startActivity(intent);
                        }
                    });
                    break;
                case NO_DATA_CONNECTION:
                    messageView.setText(R.string.error_no_data_connection);
                    break;
                case SERVER_UNAVAILABLE:
                    messageView.setText(R.string.error_lds_server_unavailable);
                    break;
                case UNKOWN_GOOGLE_EXCEPTION:
                    messageView.setText(R.string.error_google_drive);
                    break;
                case NO_PERMISSIONS:
                    messageView.setText("You do not have permissions to run this application");
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {

                        }
                    });
                    break;
                default:
                    messageView.setText(R.string.error_generic_web);
            }
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton(R.string.ok, null);
            dialogBuilder.show();
            progressBar.setVisibility(GONE);
        }
    };
}