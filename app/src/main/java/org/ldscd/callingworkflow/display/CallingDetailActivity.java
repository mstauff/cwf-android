package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

/**
 * An activity representing a single Calling detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CallingListActivity}.
 */
public class CallingDetailActivity extends AppCompatActivity implements MemberLookupFragment.memberLookupFragmentInteractionListener, CallingDetailFragment.OnFragmentInteractionListener {
    @Inject
    DataManager dataManager;

    private long orgId;
    private Long individualId;
    private Calling calling;
    private Org org;
    private Member proposedMember;
    private String notes;
    private CallingStatus callingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Initialize Volley injection class */
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        /* Initialize UI */
        setContentView(R.layout.activity_calling_detail);

        orgId = getIntent().getLongExtra(CallingListActivity.ARG_ORG_ID, 0);
        org = dataManager.getOrg(orgId);
        String callingId = getIntent().getStringExtra(CallingDetailFragment.CALLING_ID);
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
            //submitOrgChanges();
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

    private void submitOrgChanges() {
        boolean hasChanges = false;
        if(!callingStatus.getStatus().equals(calling.getProposedStatus())) {
            calling.setProposedStatus(callingStatus.getStatus());
            hasChanges = true;
        }
        CharSequence extraNotes = notes;
        if(!extraNotes.equals(calling.getNotes())) {
            calling.setNotes(extraNotes.toString());
            hasChanges = true;
        }
        if(individualId.equals(calling.getProposedIndId())) {
            calling.setProposedIndId(individualId);
            hasChanges = true;
        }
        if(hasChanges) {
            dataManager.saveFile(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {

                }
            }, org);
        }
    }

    private void hydrateCalling(String callingId) {
        calling = dataManager.getCalling(callingId);
        wireUpToolbar();
    }

    private void wireUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(org.getDefaultOrgName() + " - " + calling.getPosition());
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
        CallingDetailFragment callingDetailFragment = new CallingDetailFragment();
        Bundle args = new Bundle();
        args.putLong(CallingDetailFragment.ORG_ID, orgId);
        args.putString(CallingDetailFragment.CALLING_ID, calling.getCallingId());
        args.putLong(CallingDetailFragment.INDIVIDUAL_ID, calling.getProposedIndId());
        callingDetailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.calling_detail_main_fragment_container, callingDetailFragment)
                .commit();
    }

    /***** LISTENERS ******/
    /* This method receives data from the fragment. */
    /* From MemberLookupFragment. */
    @Override
    public void onFragmentInteraction(Member member) {
        this.proposedMember = member;
        CallingDetailFragment callingDetailFragment = new CallingDetailFragment();
        Bundle args = new Bundle();
        args.putLong(CallingDetailFragment.ORG_ID, orgId);
        args.putString(CallingDetailFragment.CALLING_ID, calling.getCallingId());
        if(this.proposedMember != null && this.proposedMember.getFormattedName() != null) {
            args.putLong(CallingDetailFragment.INDIVIDUAL_ID, this.proposedMember.getIndividualId());
        }
        callingDetailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.calling_detail_main_fragment_container, callingDetailFragment)
            .commit();
    }

    /* From CallingDetailFragment. */
    @Override
    public void onFragmentInteraction(boolean search) {
        if(search) {
            MemberLookupFragment memberLookupFragment = new MemberLookupFragment();
            if(this.proposedMember != null && this.proposedMember.getIndividualId() > 0) {
                Bundle args = new Bundle();
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, this.proposedMember.getIndividualId());
                memberLookupFragment.setArguments(args);
            }
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.calling_detail_main_fragment_container, memberLookupFragment, null)
                .addToBackStack(null)
                .commit();
        }
    }
}