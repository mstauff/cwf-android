package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class CreateCallingActivity extends AppCompatActivity implements CallingDetailSearchFragment.OnFragmentInteractionListener {
    public static final String PARENT_ORG_ID = "parentOrgId";

    @Inject
    DataManager dataManager;

    private Org parentOrg;
    private long proposedIndividualId;

    Spinner positionDropdown;
    Spinner statusDropdown;
    EditText notesBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_create_calling);
        long parentOrgId = getIntent().getLongExtra(PARENT_ORG_ID, 0);
        parentOrg = dataManager.getOrg(parentOrgId);

        // Show the Up button in the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        actionBar.setTitle(R.string.title_create_calling);

        if(parentOrg != null) {
            //Position Dropdown
            List<Position> potentialPositions = parentOrg.potentialNewPositions();
            positionDropdown = (Spinner) findViewById(R.id.new_calling_position_dropdown);
            ArrayAdapter positionAdapter = new ArrayAdapter<Position>(this, android.R.layout.simple_list_item_1, potentialPositions);
            positionDropdown.setAdapter(positionAdapter);
        }

        //Status Dropdown
        List<CallingStatus> statusOptions = new ArrayList(Arrays.asList(CallingStatus.values()));
        statusDropdown = (Spinner) findViewById(R.id.new_calling_status_dropdown);
        ArrayAdapter statusAdapter = new ArrayAdapter<CallingStatus>(this, android.R.layout.simple_list_item_1, statusOptions);
        statusDropdown.setAdapter(statusAdapter);

        //notes editbox
        notesBox = (EditText) findViewById(R.id.new_calling_notes);

        //Member Lookup
        if (savedInstanceState == null) {
            CallingDetailSearchFragment searchFragment = new CallingDetailSearchFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.new_calling_search_container, searchFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // this adds the confirm button to the toolbar
        getMenuInflater().inflate(R.menu.confirm_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if(id == R.id.confirm_action) {
            Position position = (Position)positionDropdown.getSelectedItem();
            String status = ((CallingStatus)statusDropdown.getSelectedItem()).toString();
            String notes = notesBox.getText().toString();
            Calling calling = new Calling(null, null, null, proposedIndividualId, null, position, null, status, notes, parentOrg.getId());
            dataManager.addCalling(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean success) {
                    if(success) {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.new_calling_saved, Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.new_calling_failed, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }, calling, parentOrg);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Long individualId) {
        proposedIndividualId = individualId;
    }
}
