package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.services.GoogleDriveService;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

import static org.ldscd.callingworkflow.display.SplashActivity.SPLASH_ACTIVITY;

public class GoogleDriveOptionsActivity extends AppCompatActivity implements View.OnClickListener {
    /** Fields */
    private static final String TAG = "GoogleDriveOptions";
    private Activity activity;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private GoogleSignInOptions gso;
    /** Properties */
    @Inject
    GoogleDriveService googleDataService;

    @Inject
    DataManager dataManager;

    /** Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_settings);
        /* Wire up injected items. */
        ((CWFApplication)getApplication()).getNetComponent().inject(this);

        /* Show the Up button in the action bar. */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_google_settings);

        activity = this;

        /* Views */
        mStatusTextView = findViewById(R.id.status);

        /* Button listeners */
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);
        findViewById(R.id.google_sign_out_button).setOnClickListener(this);
        findViewById(R.id.google_disconnect_button).setOnClickListener(this);
        findViewById(R.id.reset_data_link).setOnClickListener(this);

        /* [START configure_signin]
         * Configure sign-in to request the user's ID, email address, and basic
         * profile. ID and basic profile are included in DEFAULT_SIGN_IN. */
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_FILE))
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .build();
        /* [END configure_signin] */

        /* [START build_client]
         * Build a GoogleSignInClient with access to the Google Sign-In API and the
         * options specified by gso. */
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        /* [END build_client] */

        /* [START customize_button]
         * Customize sign-in button. The sign-in button can be displayed in
         * multiple sizes. */
        SignInButton signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        /* [END customize_button] */
    }

    @Override
    public void onStart() {
        super.onStart();
        /* Check if the user is already signed in and all required scopes are granted. */
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, gso.getScopeArray())) {
            updateUI(account);
            /* Go back to the splash screen once login was successful */
            if(getIntent().getStringExtra("activity") != null && getIntent().getStringExtra("activity").equals(SPLASH_ACTIVITY)) {
                /* Transition to splash screen or directory view depending on which view invoiced this page. */
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
            }
        } else {
            updateUI(null);
        }
    }

    /* [START onActivityResult] */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    /* [END onActivityResult] */

    /* [START handleSignInResult] */
    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + (completedTask != null && completedTask.isSuccessful()));

        try {
            /* Signed in successfully, show authenticated User */
            GoogleSignInAccount account = completedTask != null ? completedTask.getResult(ApiException.class) : null;
            updateUI(account);
            Toast.makeText(activity, R.string.login_success, Toast.LENGTH_SHORT).show();
            /* Go back to the splash screen once login was successful */
            if(getIntent().getStringExtra("activity") != null && getIntent().getStringExtra("activity").equals(SPLASH_ACTIVITY)) {
                /* Transition to splash screen or directory view depending on which view invoiced this page. */
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
            }
        } catch (ApiException e) {
            /* Signed out, show unauthenticated UI. */
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
    }
    /* [END handleSignInResult] */

    /* [START signIn] */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    /* [END signIn] */

    /* [START signOut] */
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    /* [START_EXCLUDE] */
                    dataManager.clearLocalOrgData();
                    Toast.makeText(activity, R.string.logout_successful, Toast.LENGTH_SHORT).show();
                    updateUI(null);
                    /* [END_EXCLUDE] */
                } else {
                    Toast.makeText(activity, R.string.logout_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /* [END signOut] */

    /* [START revokeAccess] */
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
            new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    /* [START_EXCLUDE] */
                    updateUI(null);
                    Toast.makeText(activity, "Logged out and revoked rights to google drive.", Toast.LENGTH_SHORT).show();
                    /* [END_EXCLUDE] */
                }
            });
    }
    /* [END revokeAccess]

     * [START resetGoogleDriveData] */
    private void resetGoogleDriveData() {
        View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
        TextView messageView = (TextView) dialogView.findViewById(R.id.warning_message);
        messageView.setText(R.string.reset_usage_warning);
        new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(activity, ResetDataActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }
    /* [END resetGoogleDriveData] */

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.action_signed_in_format, account.getDisplayName()));

            findViewById(R.id.google_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.google_sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.action_signed_out);

            findViewById(R.id.google_sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.google_sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                signIn();
                break;
            case R.id.google_sign_out_button:
                signOut();
                break;
            case R.id.google_disconnect_button:
                revokeAccess();
                break;
            case R.id.reset_data_link:
                resetGoogleDriveData();
                break;
        }
    }
}