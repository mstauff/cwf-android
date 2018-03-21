package org.ldscd.callingworkflow.display;

import android.app.Activity;
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
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

import javax.inject.Inject;

import static android.view.View.GONE;

// Default startup view is a list of Org's
// Initial login is not yet designed.  Needs google id etc.
// Post initial launch allow them to startup where left off.(This would require having the org structure.)
// Need to create a splash screen as a startup.
// Need to determine location of retrieved and shared data.
// TODO: Start up activity data sync. all this should start up on a background thread.
// 1. App Config setup call.
// 2. Login Information
// 3. User Information(Permissions, callings to determine permissions)
// 4. Query church services for org's
// 5. WardMember List
// 6. Query google drive for syncing.  Read and cache DriveId's

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

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_org_list);
        fragmentManager = twoPane ? getSupportFragmentManager() : null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.navigation_drawer_orgs));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_orgs);

        recyclerView = (RecyclerView) findViewById(R.id.org_list);
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
        recyclerView.setAdapter(new OrgListAdapter(orgs, twoPane, fragmentManager));
        if(orgs.isEmpty()) {
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
                final ProgressBar pb = findViewById(R.id.org_list_reload_data_progress);
                pb.setVisibility(View.VISIBLE);
                if(dataManager.isGoogleDriveAuthenticated(getApplicationContext())) {
                    dataManager.loadOrgs(new Response.Listener<Boolean>() {
                        @Override
                        public void onResponse(Boolean response) {
                            layout.setVisibility(GONE);
                            reloadButton.setVisibility(GONE);
                            pb.setVisibility(GONE);
                        }
                    }, pb, activity);
                } else {
                    layout.setVisibility(GONE);
                    reloadButton.setVisibility(GONE);
                    pb.setVisibility(GONE);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}