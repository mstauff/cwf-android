package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.DirectoryAdapter;
import org.ldscd.callingworkflow.model.FilterOption;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

import javax.inject.Inject;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    AppCompatActivity activity = this;
    DirectoryAdapter adapter;

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_directory);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.navigation_drawer_directory));
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_directory);

        View recyclerView = findViewById(R.id.directory);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        dataManager.getWardList(new Response.Listener<List<Member>>() {
            @Override
            public void onResponse(List<Member> members) {
                adapter = new DirectoryAdapter(members, dataManager);
                recyclerView.setAdapter(adapter);
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.edit_filters) {
            FilterOption filterOption = adapter.getCurrentFilterOption();
            MemberLookupFilterFragment memberLookupFilterFragment = MemberLookupFilterFragment.newInstance(filterOption);
            memberLookupFilterFragment.setMemberLookupFilterListener(adapter);
            memberLookupFilterFragment.show(getSupportFragmentManager(), MemberLookupFragment.FRAG_NAME);
        }

        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_orgs) {
            Intent intent = new Intent(this, OrgListActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_callings) {
            Intent intent = new Intent(this, CallingListActivity.class);
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
