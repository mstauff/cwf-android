package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.CallingListAdapter;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.MemberData;

import java.util.List;

import javax.inject.Inject;

/**
 * An activity representing a list of Callings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CallingDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CallingListActivity extends AppCompatActivity {
    public static final String ARG_ORG_ID = "orgId";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;
    AppCompatActivity activity = this;

    @Inject
    CallingData callingData;
    @Inject
    MemberData memberData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_calling_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            callingData.getOrg(getIntent().getLongExtra(ARG_ORG_ID, 0), new Response.Listener<Org>() {
                @Override
                public void onResponse(Org org) {
                    getSupportActionBar().setTitle(org.getOrgName());
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, CreateCallingActivity.class);
                startActivity(intent);
            }
        });

        View recyclerView = findViewById(R.id.calling_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.calling_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        callingData.getCallings(getIntent().getLongExtra(ARG_ORG_ID, 0), new Response.Listener<List<Calling>>() {
            @Override
            public void onResponse(List<Calling> callings) {
                FragmentManager fragmentManager = twoPane ? getSupportFragmentManager() : null;
                recyclerView.setAdapter(new CallingListAdapter(callings, memberData, twoPane, fragmentManager));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_display_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.display_view_organization:
            case R.id.display_view_indvidual:
                item.setChecked(true);
                break;
            case R.id.display_filter_proposed:
            case R.id.display_filter_approved:
            case R.id.display_filter_extended:
                item.setChecked(!item.isChecked());
                break;
        }

        //This section keeps the menu from closing when items are selected
        if(id != android.R.id.home) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            item.setActionView(new View(activity.getApplicationContext()));
            MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return false;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return false;
                }
            });
        }

        return false;
    }
}