package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.LocalFileResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * An activity representing a single Calling detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CallingListActivity}.
 */
public class CallingDetailActivity extends AppCompatActivity {
    private Org organization;

    @Inject
    IWebResources webResources;

    @Inject
    GoogleDataService googleDataServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Initialize Volley injection class */
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        /* Initialize UI */
        setContentView(R.layout.activity_calling_detail);

        wireUpToolbar();
        wireUpFinalizeButton();
        wireUpStatusDropdown();
        wireUpFragments(savedInstanceState);
        final List<Org> orgs = new ArrayList<>();
        webResources.getOrgs(new Response.Listener<List<Org>>() {
            @Override
            public void onResponse(List<Org> response) {
                if(response != null && !response.isEmpty())
                orgs.addAll(response);
            }
        });

        //googleDataServices.syncDriveIds(orgs, this);

        googleDataServices.getOrgData(new Response.Listener<Org>() {
            @Override
            public void onResponse(Org response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }, orgs.get(0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_display_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            finish();
            return true;
        }
        return false;
    }

    private void submitOrgChanges(View v) {
        Spinner spinner = (Spinner) findViewById(R.id.calling_detail_status_dropdown);
        String selected = ((CallingStatus)spinner.getSelectedItem()).name();
    }

    private void wireUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle("Primary - CTR7");
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void wireUpStatusDropdown() {
        CallingStatus[] callingStatuses = new CallingStatus[CallingStatus.values().length];
        List<CallingStatus> status = new ArrayList(Arrays.asList(CallingStatus.values()));
        Spinner statusDropdown = (Spinner) findViewById(R.id.calling_detail_status_dropdown);
        ArrayAdapter adapter = new ArrayAdapter<CallingStatus>(this, android.R.layout.simple_list_item_1, status) {
           /* @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }*/
        };
        statusDropdown.setAdapter(adapter);
    }

    private void wireUpFinalizeButton() {
        /* Finalize calling button setup */
        Button button = (Button) findViewById(R.id.button_finalize_calling);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrgChanges(v);
            }
        });
    }

    private void wireUpFragments(Bundle savedInstanceState) {
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
//            Bundle arguments = new Bundle();
//            arguments.putString(CallingDetailFragment.ARG_ITEM_ID,
//                    getIntent().getStringExtra(CallingDetailFragment.ARG_ITEM_ID));
            CallingDetailFragment fragment = new CallingDetailFragment();
            CallingDetailSearchFragment searchFragment = new CallingDetailSearchFragment();
//            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    //.add(R.id.calling_detail_container, fragment)
                    .add(R.id.calling_detail_search_container, searchFragment)
                    .commit();
        }
    }
}