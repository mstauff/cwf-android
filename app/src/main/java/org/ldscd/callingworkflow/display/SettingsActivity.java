package org.ldscd.callingworkflow.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.google.ConflictResolver;
import org.ldscd.callingworkflow.google.ConflictUtil;
import org.ldscd.callingworkflow.web.IWebResources;

import javax.inject.Inject;
import java.io.*;

/* User Sign-in with LDS Credentials. */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    protected EditText usernameEditText;
    protected EditText passwordEditText;
    protected Button authConfirmButton;

    @Inject
    IWebResources webResources;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_settings);
        wireUpToolbar();
        usernameEditText = (EditText) findViewById(R.id.userText);
        passwordEditText = (EditText) findViewById(R.id.passwordText);
        authConfirmButton = (Button) findViewById(R.id.auth_confirm);
        authConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if(!user.equals("") && !password.equals("")) {
                    webResources.setCredentials(user, password);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
}