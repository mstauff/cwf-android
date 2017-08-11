package org.ldscd.callingworkflow.display;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.ExpandableOrgListAdapter;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

/**
 * An activity representing a list of Callings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CallingDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ExpandableOrgsListActivity extends AppCompatActivity {
    public static final String ARG_ORG_ID = "orgId";
    public static final String ARG_EXPAND_ID = "expandOrgId";
    public static final String GET_DATA = "load";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;
    AppCompatActivity activity = this;
    long orgId;
    long expandId;
    ExpandableListView orgsListView;
    ExpandableOrgListAdapter adapter;
    boolean loadData;

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_expandable_org_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            orgId = getIntent().getLongExtra(ARG_ORG_ID, 0) == 0  ? savedInstanceState.getLong(ARG_ORG_ID) : getIntent().getLongExtra(ARG_ORG_ID, 0);
            Org org = dataManager.getOrg(orgId);
            expandId = getIntent().getLongExtra(ARG_EXPAND_ID, 0);
            loadData = orgId > 0 && getIntent().getBooleanExtra(GET_DATA, false);
            if(loadData) {
                dataManager.loadOrg(new Response.Listener<Org>() {
                    @Override
                    public void onResponse(Org response) {
                        orgsListView = (ExpandableListView) findViewById(R.id.expandable_org_list);
                        assert orgsListView != null;
                        setupListView(orgsListView, response);
                    }
                }, org);

                getSupportActionBar().setTitle(org.getDefaultOrgName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else if(org == null) {
                Log.e("CallingListActivity", "Org id list not found or empty");
                Context context = getApplicationContext();
                Intent intent = new Intent(context, OrgListActivity.class);
                context.startActivity(intent);
            } else {
                orgsListView = (ExpandableListView) findViewById(R.id.expandable_org_list);
                assert orgsListView != null;
                setupListView(orgsListView, org);
                getSupportActionBar().setTitle(org.getDefaultOrgName());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        if (findViewById(R.id.calling_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupListView(@NonNull final ExpandableListView callingListView, final Org org) {
        FragmentManager fragmentManager = twoPane ? getSupportFragmentManager() : null;
        adapter = new ExpandableOrgListAdapter(org, dataManager, twoPane, fragmentManager, activity);
        callingListView.setAdapter(adapter);

        //expand subOrg if expandId was provided
        if(expandId > 0) {
            for (int i = 0; i < org.getChildren().size(); i++) {
                if(org.getChildren().get(i).getId() == expandId) {
                    callingListView.expandGroup(i);
                    break;
                }
            }
        }

        callingListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View groupView, int index, long id) {
                if(index >= org.getChildren().size()) {
                    //it's a calling so open detail
                    String callingId = org.getCallings().get(index - org.getChildren().size()).getCallingId();
                    openCallingDetail(groupView.getContext(), callingId);
                    return true;
                }
                return false;
            }
        });
        callingListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int index, int subIndex, long id) {
                Org subOrg = org.getChildren().get(index);
                if(subIndex < subOrg.getChildren().size()) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ExpandableOrgsListActivity.class);
                    intent.putExtra(ExpandableOrgsListActivity.ARG_ORG_ID, subOrg.getId());
                    long selectedChildId = subOrg.getChildren().get(subIndex).getId();
                    intent.putExtra(ExpandableOrgsListActivity.ARG_EXPAND_ID, selectedChildId);
                    context.startActivity(intent);
                    return true;
                } else {
                    String callingId = subOrg.getCallings().get(subIndex).getCallingId();
                    openCallingDetail(view.getContext(), callingId);
                    return true;
                }
            }
        });
    }
    private void openCallingDetail(Context context, String callingId) {

        /*if (twoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(CallingDetailFragment.CALLING_ID, holder.orgItem.id);
            CallingDetailFragment fragment = new CallingDetailFragment();
            fragment.setArguments(arguments);
            fragmentManager.beginTransaction()
                    .replace(R.id.calling_detail_container, fragment)
                    .commit();
        } else {*/
            Intent intent = new Intent(context, CallingDetailActivity.class);
            /* Must use bundle rather than directly inserting the data into the intent extra. */
            Bundle bundle = new Bundle();
            bundle.putString(CallingDetailFragment.CALLING_ID, callingId);
            bundle.putLong(ExpandableOrgsListActivity.ARG_ORG_ID, orgId);
            intent.putExtras(bundle);
            context.startActivity(intent);
        //}
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

        if(id == android.R.id.home) {
            finish();
            return true;
        } else {
            //This section keeps the menu from closing when items are selected
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_ORG_ID, getIntent().getLongExtra(ARG_ORG_ID, 0));
        super.onSaveInstanceState(outState);
    }

    public void wireUpIndividualInformationFragments(Long individualId) {
        if (individualId != null) {
            IndividualInformationFragment member_information_fragment = new IndividualInformationFragment();
            Bundle args = new Bundle();
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, individualId);
            member_information_fragment.setArguments(args);
            member_information_fragment.show(getSupportFragmentManager(), null);
        }
    }
}