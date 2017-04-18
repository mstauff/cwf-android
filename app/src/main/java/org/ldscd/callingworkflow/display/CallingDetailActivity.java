package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.MemberData;

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
public class CallingDetailActivity extends AppCompatActivity implements CallingDetailSearchFragment.OnFragmentInteractionListener {
    @Inject
    CallingData callingData;
    @Inject
    MemberData memberData;
    @Inject
    GoogleDataService googleDataServices;

    Spinner statusDropdown;
    TextView notes;
    private long orgId;
    private Long individualId;
    private Calling calling;
    private Org org;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Initialize Volley injection class */
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        /* Initialize UI */
        setContentView(R.layout.activity_calling_detail);

        statusDropdown = (Spinner) findViewById(R.id.calling_detail_status_dropdown);
        notes = (TextView) findViewById(R.id.notes_calling_detail);

        orgId = getIntent().getLongExtra(CallingListActivity.ARG_ORG_ID, 0);
        String callingId = getIntent().getStringExtra(CallingDetailFragment.ARG_ITEM_ID);
        hydrateOrg(orgId);
        hydrateCalling(callingId);
        wireUpFragments(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calling_detail, menu);
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
            submitOrgChanges();
            Intent intent = new Intent(this, CallingListActivity.class);
            intent.putExtra(CallingListActivity.ARG_ORG_ID, orgId);
            navigateUpTo(intent);
            return true;
        } else if(id == R.id.calling_detail_release_current_in_lcr) {
            // Send event to LCR
        } else if(id == R.id.calling_detail_delete_current_calling_in_lcr) {
            // Send event to LCR
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Long individualId) {
        this.individualId = individualId;
    }

    private void submitOrgChanges() {
        boolean hasChanges = false;
        String status = ((CallingStatus)statusDropdown.getSelectedItem()).name();
        if(!status.equals(calling.getProposedStatus())) {
            calling.setProposedStatus(status);
            hasChanges = true;
        }
        CharSequence extraNotes = notes.getText();
        if(!extraNotes.equals(calling.getNotes())) {
            calling.setNotes(extraNotes.toString());
            hasChanges = true;
        }
        if(individualId.equals(calling.getProposedIndId())) {
            calling.setProposedIndId(individualId);
            hasChanges = true;
        }
        if(hasChanges) {
            googleDataServices.saveFile(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {

                }
            }, org);
        }
    }

    private void hydrateCalling(String callingId) {
        calling = callingData.getCalling(callingId);
        if(calling != null) {
            final TextView currentlyCalled = (TextView)findViewById(R.id.calling_detail_currently_called);
            String name = memberData.getMemberName(calling.getMemberId());
            currentlyCalled.setText(name);
            TextView notes = (TextView) findViewById(R.id.notes_calling_detail);
            if(calling.getNotes() != null && calling.getNotes().length() > 0) {
                notes.setText(calling.getNotes());
            }
            wireUpStatusDropdown();
            wireUpToolbar();
        }
    }

    private void hydrateOrg(long orgId) {
        final List<Org> orgs = new ArrayList<>();
        //TODO: get org from google drive until callingData is up to date.
        org = callingData.getOrg(orgId);
        wireUpFinalizeButton();
    }

    private void wireUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(org.getOrgName() + " - " + calling.getPosition());
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void wireUpStatusDropdown() {
        List<CallingStatus> status = new ArrayList(Arrays.asList(CallingStatus.values()));
        Spinner statusDropdown = (Spinner) findViewById(R.id.calling_detail_status_dropdown);
        ArrayAdapter adapter = new ArrayAdapter<CallingStatus>(this, android.R.layout.simple_list_item_1, status);
        statusDropdown.setSelection(adapter.getPosition(CallingStatus.get(calling.getProposedStatus())));
        statusDropdown.setAdapter(adapter);
    }

    private void wireUpFinalizeButton() {
        /* Finalize calling button setup */
        Button button = (Button) findViewById(R.id.button_finalize_calling);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrgChanges();
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
            CallingDetailSearchFragment searchFragment = new CallingDetailSearchFragment();
            if(calling != null && calling.getProposedIndId() > 0) {
                Bundle args = new Bundle();
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, calling.getProposedIndId());
                searchFragment.setArguments(args);
            }
            getSupportFragmentManager().beginTransaction()
                .add(R.id.calling_detail_search_container, searchFragment)
                .commit();
        }
    }
}