package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

/**
 * Represents a list of links for application setup and customization(s).
 */
public class SettingsActivity extends AppCompatActivity {

    @Inject
    DataManager dataManager;
    private LdsUser currentUser;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Initialize Volley injection class */
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_settings);
        /* Acquire the current user for security purposes */
        if(currentUser == null) {
            dataManager.getUserInfo(null, null, false, new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser user) {
                    currentUser = user;
                }
            });
        }
        /* Acquire Permission Manager for security checks */
        if(permissionManager == null) {
            permissionManager = dataManager.getPermissionManager();
        }
        wireUpToolbar();
        wireupLinks();
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
            navigateUpTo(new Intent(this, ExpandableOrgsListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void wireUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void wireupLinks() {
        TextView ldsCredentialsLink = (TextView) findViewById(R.id.settings_lds_credentials_link);
        ldsCredentialsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LDSAccountActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
        TextView syncDataLink = (TextView) findViewById(R.id.settings_sync_data_link);
        syncDataLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManageDataActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
        TextView editCallingStatusLink = (TextView) findViewById(R.id.settings_edit_calling_status_link);
        /* If the current user is not a Unit Admin they cannot edit the statuses */
        if(permissionManager.hasPermission(currentUser.getUnitRoles(), Permission.UNIT_GOOGLE_ACCOUNT_CREATE)) {
            editCallingStatusLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), StatusEditActivity.class);
                    getApplicationContext().startActivity(intent);
                }
            });
        } else {
            editCallingStatusLink.setVisibility(View.GONE);
        }
    }
}
