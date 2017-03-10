package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateCallingActivity extends AppCompatActivity {
    public static final String PARENT_ORG_ID = "parentOrgId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_calling);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            actionBar.setTitle(R.string.title_create_calling);
        }

        //Position Dropdown
        List<Position> samplePositions = new ArrayList<>();
        samplePositions.add(new Position(1,"Primary Teacher",0));
        samplePositions.add(new Position(2,"Relief Society Pianist",0));
        samplePositions.add(new Position(3,"Ward Choir Director",0));
        Spinner positionDropdown = (Spinner) findViewById(R.id.new_calling_position_dropdown);
        ArrayAdapter positionAdapter = new ArrayAdapter<Position>(this, android.R.layout.simple_list_item_1, samplePositions);
        positionDropdown.setAdapter(positionAdapter);

        //Status Dropdown
        List<CallingStatus> status = new ArrayList(Arrays.asList(CallingStatus.values()));
        Spinner statusDropdown = (Spinner) findViewById(R.id.new_calling_status_dropdown);
        ArrayAdapter statusAdapter = new ArrayAdapter<CallingStatus>(this, android.R.layout.simple_list_item_1, status);
        statusDropdown.setAdapter(statusAdapter);

        //Member Lookup
        if(savedInstanceState == null) {
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
            Intent intent = new Intent(this, CallingListActivity.class);
            intent.putExtra(CallingListActivity.ARG_ORG_ID, getIntent().getLongExtra(CallingListActivity.ARG_ORG_ID, 0));
            navigateUpTo(intent);
            return true;
        } else if(id == R.id.confirm_action) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.new_calling_saved, Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(this, CallingListActivity.class);
            intent.putExtra(CallingListActivity.ARG_ORG_ID, getIntent().getLongExtra(CallingListActivity.ARG_ORG_ID, 0));
            navigateUpTo(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
