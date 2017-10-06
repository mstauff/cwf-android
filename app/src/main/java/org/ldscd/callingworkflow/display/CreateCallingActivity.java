package org.ldscd.callingworkflow.display;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class CreateCallingActivity extends AppCompatActivity implements CreateCallingFragment.SubFragmentOpenListener {
    public static final String PARENT_ORG_ID = "parentOrgId";

    @Inject
    DataManager dataManager;

    private boolean subFragmentOpen;
    private ActionBar actionBar;
    private Menu actionBarMenu;

    CreateCallingFragment createCallingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_create_calling);
        long parentOrgId = getIntent().getLongExtra(PARENT_ORG_ID, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_create_calling);

        createCallingFragment = new CreateCallingFragment();
        createCallingFragment.setSubFragmentOpenListener(this);
        Bundle args = new Bundle();
        args.putLong(CreateCallingFragment.PARENT_ORG_ID, parentOrgId);
        createCallingFragment.setArguments(args);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.create_calling_fragment_container, createCallingFragment, CreateCallingFragment.TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // this adds the confirm button to the toolbar
        getMenuInflater().inflate(R.menu.confirm_save, menu);
        actionBarMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if(subFragmentOpen) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
            return true;
        } else if(id == R.id.confirm_action) {
            Calling calling = createCallingFragment.getNewCalling();
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
            }, calling);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBaseFragmentStarted() {
        subFragmentOpen = false;
        // Show cancel and save icons in the action bar.
        actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        if(actionBarMenu != null) {
            actionBarMenu.setGroupVisible(0, true);
        }
    }

    @Override
    public void onSubFragmentStarted() {
        subFragmentOpen = true;
        // Show the Up button in the action bar.
        actionBar.setHomeAsUpIndicator(0);
        if(actionBarMenu != null) {
            actionBarMenu.setGroupVisible(0, false);
        }
    }
}
