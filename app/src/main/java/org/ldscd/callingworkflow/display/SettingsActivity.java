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

import org.ldscd.callingworkflow.R;

/**
 * Represents a list of links for application setup and customization(s).
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
        editCallingStatusLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StatusEditActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
    }
}
