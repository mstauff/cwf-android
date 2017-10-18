package org.ldscd.callingworkflow.display;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

/**
 * An activity representing a single Calling detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ExpandableOrgsListActivity}.
 */
public class CallingDetailActivity
        extends AppCompatActivity
        implements CallingDetailFragment.OnCallingDetailFragmentListener {
    @Inject
    DataManager dataManager;

    private static String CALLING_DETAIL_TAG = "CALLING_DETAIL_FRAGMENT";
    private long orgId;
    private Calling calling;
    private Org org;
    private boolean hasChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Initialize Volley injection class */
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        /* Initialize UI */
        setContentView(R.layout.activity_calling_detail);

        /* Capture passed in arguments. */
        orgId = getIntent().getLongExtra(ExpandableOrgsListActivity.ARG_ORG_ID, 0);
        org = dataManager.getOrg(orgId);
        String callingId = getIntent().getStringExtra(CallingDetailFragment.CALLING_ID);
        /* Commence the hydration of the UI and objects involved. */
        hydrateCalling(callingId);
        wireUpFragments(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.calling_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Initialize the top menu items. */
        int id = item.getItemId();
        boolean proceed = true;
        /* If the back button is pushed while in the member search screen fragment, close the fragment.
           and do not close out the Calling Detail activity.
         */
        for(Fragment fragment : getSupportFragmentManager().getFragments()) {
            if(fragment != null && fragment.getTag().equals(MemberLookupFragment.FRAG_NAME) && fragment.isVisible()) {
                getSupportFragmentManager().popBackStack();
                proceed = false;
            }
        }
        if (proceed && id == android.R.id.home) {
            /* This ID represents the Home or Up button. In the case of this
             activity, the Up button is shown. For
             more details, see the Navigation pattern on Android Design:*/
            /* Save changes if the back button is pushed. */
            submitOrgChanges();
            /* Go to the list of callings if the back button is pushed. */
            finish();
            return true;
        } else if(proceed && id == R.id.calling_detail_release_current_in_lcr) {
            // Send event to LCR
        } else if(proceed && id == R.id.calling_detail_delete_current_calling_in_lcr) {
            // Send event to LCR
        }
        return true;
    }

    private void submitOrgChanges() {
        /* If changes were made to the workflow, go ahead and save them. */
        if(hasChanges) {
            dataManager.updateCalling(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    String message = response ? getResources().getString(R.string.items_saved) : getResources().getString(R.string.new_calling_failed);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }, calling);
        }
    }

    private void hydrateCalling(String callingId) {
        /* Get the Calling object from the passed in Calling ID. */
        calling = dataManager.getCalling(callingId);
        /* Tool bar is wired up after the calling object is found so to get the name of the calling for display. */
        wireUpToolbar();
    }

    private void wireUpToolbar() {
        /* Once the calling is found we can then set the title of the Toolbar and initialize it. */
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(org.getDefaultOrgName() + " - " + calling.getPosition().getName());
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
        /* The main calling screen is sectioned out in fragments. */
        CallingDetailFragment callingDetailFragment = new CallingDetailFragment();
        /* Create a bundle object to pass arguments to the newly created fragment. */
        Bundle args = new Bundle();
        args.putSerializable(CallingDetailFragment.CALLING, calling);
        args.putLong(CallingDetailFragment.ORG_ID, orgId);
        args.putLong(CallingDetailFragment.INDIVIDUAL_ID, calling.getProposedIndId());
        callingDetailFragment.setArguments(args);
        /* Launch the fragment once the items have been created and initialized. */
        /* Be sure to pass in the TAG for quick lookup reference later. */
        FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction()
                .add(R.id.calling_detail_main_fragment_container, callingDetailFragment, CALLING_DETAIL_TAG)
                .commit();
    }

    /***** LISTENERS ******/
    /* This method receives data from the fragment. */
    /* From CallingDetailFragment. */
    @Override
    public void onFragmentInteraction(Calling newCalling, boolean newChanges) {
        /* Receive calling changes information from the calling detail fragment. */
        calling = newCalling;
        /* Sets if there were any changes so we know to call the save method eventually. */
        hasChanges = newChanges;
    }
}