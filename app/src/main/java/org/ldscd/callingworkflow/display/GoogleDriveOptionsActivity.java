package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

public class GoogleDriveOptionsActivity extends AppCompatActivity {
    protected static final int REQUEST_CODE_SIGN_IN = 1;

    @Inject
    GoogleDataService googleDataService;
    @Inject
    DataManager dataManager;

    private Activity activity;
    private SignInButton signInButton;
    private TextView signOutLink;
    private TextView resetDataLink;
    private boolean loadDataOnExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication) getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_google_settings);

        // Show the Up button in the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_google_settings);

        activity = this;
        signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        signOutLink = (TextView) findViewById(R.id.google_sign_out_link);
        resetDataLink = (TextView) findViewById(R.id.reset_data_link);

        final GoogleApiClient signInClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(signInClient);
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
            }
        });

        signOutLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.GoogleSignInApi.signOut(signInClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        googleDataService.stopConnection();
                        dataManager.clearLocalOrgData();
                        loadDataOnExit = false;
                        setUISignedOut();
                        Toast.makeText(activity, R.string.logout_successful, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        resetDataLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                //Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(signInClient);
                Auth.GoogleSignInApi.silentSignIn(signInClient).setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        if(googleSignInResult.isSuccess()) {
                            setUISignedIn();
                        } else {
                            setUISignedOut();
                        }
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        };
        signInClient.registerConnectionCallbacks(connectionCallbacks);
        signInClient.connect();
    }

    private void setUISignedIn() {
        signInButton.setVisibility(View.GONE);
        signOutLink.setVisibility(View.VISIBLE);
    }

    private void setUISignedOut() {
        signInButton.setVisibility(View.VISIBLE);
        signOutLink.setVisibility(View.GONE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(signInResult != null && signInResult.isSuccess()) {
                googleDataService.restartConnection(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        loadDataOnExit = true;
                        setUISignedIn();
                        Toast.makeText(activity, R.string.login_success, Toast.LENGTH_SHORT).show();
                    }
                }, activity);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(loadDataOnExit) {
            //todo: loadOrgs is a lengthy process, we should display a progress bar to keep them from navigating
            dataManager.loadOrgs(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
                }
            }, new ProgressBar(activity), activity);
        }
    }
}
