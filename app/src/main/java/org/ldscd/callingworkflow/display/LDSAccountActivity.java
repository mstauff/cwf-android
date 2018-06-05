package org.ldscd.callingworkflow.display;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.utils.SecurityUtil;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.ExceptionType;
import org.ldscd.callingworkflow.web.UI.Spinner;
import org.ldscd.callingworkflow.web.WebException;

import javax.inject.Inject;

/**
 * A login screen that offers login via email/password.
 */
public class LDSAccountActivity extends AppCompatActivity {
    public static final String LDS_ACCOUNT_ACTIVITY = "LDSAccountActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /* UI references. */
    private EditText userNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    @Inject
    DataManager dataManager;
    SharedPreferences sharedPreferences;
    private LdsUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_ldsaccount);
        wireUpToolbar();
        /* Set up the login form. */
        userNameView = findViewById(R.id.userName);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        dataManager.getSharedPreferences(new Response.Listener<SharedPreferences>() {
            @Override
            public void onResponse(SharedPreferences preferences) {
                sharedPreferences = preferences;
                if(preferences != null) {
                    String userName = preferences.getString("username", null);
                    String password = preferences.getString("password", null);
                    if(userName != null && password != null) {
                        userNameView.setText(SecurityUtil.decrypt(getApplicationContext(), userName));
                        mPasswordView.setText(SecurityUtil.decrypt(getApplicationContext(), password));
                    }
                }
            }
        });
        Button mLDSAccountSignInButton = findViewById(R.id.lds_credentials_sign_in_button);
        mLDSAccountSignInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                attemptLogin();
            }
        });

        findViewById(R.id.lds_credentials_sign_out_button)
            .setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    logOut();
                }
            });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        currentUser = dataManager.getCurrentUser();
        toggleView();
    }

    private void toggleView() {
        Button refreshDataButton = findViewById(R.id.button_data_sync);
        TextView loggedInLabel = findViewById(R.id.lds_credentials_logged_in_status_label);
        LinearLayout loggedInContainer = findViewById(R.id.lds_credentials_login_container);
        LinearLayout loggedOutContainer = findViewById(R.id.lds_credentials_logged_in_container);
        if(currentUser != null && currentUser.getIndividualId() > 0) {
            loggedInLabel.setText(getString(R.string.action_signed_in_format, userNameView.getText().toString()));
            loggedInContainer.setVisibility(View.GONE);
            loggedOutContainer.setVisibility(View.VISIBLE);
            if(new PermissionManager().hasPermission(currentUser.getUnitRoles(), Permission.ORG_INFO_READ)) {
                refreshDataButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog dialog = getAlertDialog();
                        dialog.show();
                    }
                });
            }
        } else {
            loggedInLabel.setText(getString(R.string.action_signed_in_format, ""));
            loggedInContainer.setVisibility(View.VISIBLE);
            loggedOutContainer.setVisibility(View.GONE);
            refreshDataButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), R.string.login_before_refresh, Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private AlertDialog getAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.lds_data_reset_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        refreshDataEventHandler();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        /* Reset errors. */
        userNameView.setError(null);
        mPasswordView.setError(null);

        /* Store values at the time of the login attempt. */
        String userName = userNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /* Check for a valid password, if the user entered one. */
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        /* Check for a valid email userName. */
        if (!isUserNameValid(userName)) {
            userNameView.setError(getString(R.string.error_field_required));
            focusView = userNameView;
            cancel = true;
        }

        if (cancel) {
            /* There was an error; don't attempt login and focus the first form field with an error. */
            focusView.requestFocus();
        } else {
            /* Show a progress spinner, and kick off a background task to perform the user login attempt. */
            Spinner.showProgress(true, mLoginFormView, mProgressView, getResources());
            mAuthTask = new UserLoginTask(userName, password);
            mAuthTask.doInBackground();
        }
    }

    private void logOut() {
        dataManager.signOut(
            new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    userNameView.setText("");
                    mPasswordView.setText("");
                    findViewById(R.id.lds_credentials_logged_in_container).setVisibility(View.GONE);
                    findViewById(R.id.lds_credentials_login_container).setVisibility(View.VISIBLE);
                    dataManager.clearLocalOrgData();
                }
            },
            new Response.Listener<WebException>() {
                @Override
                public void onResponse(WebException response) {
                    if (isTaskRoot()) {
                        /* Transition to splash screen or directory view depending on which view invoiced this page. */
                        Intent intent = new Intent(LDSAccountActivity.this, SplashActivity.class);
                        startActivity(intent);
                    } else {
                        finish();
                    }
                }
            });
    }

    private boolean isUserNameValid(String userName) {
        return userName != null && userName.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.length() > 0;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask {

        private final String mUserName;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUserName = username;
            mPassword = password;
        }

        private void doInBackground() {
            dataManager.getUserInfo(mUserName, mPassword, new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    currentUser = ldsUser;
                    onPostExecute();
                }
            }, loginErrorListener);
        }

        private void onPostExecute() {
            mAuthTask = null;
            Spinner.showProgress(false, mLoginFormView, mProgressView, getResources());
            toggleView();
            if (currentUser != null && currentUser.getIndividualId() > 0) {
                if (isTaskRoot()) {
                /* Transition to splash screen or directory view depending on which view invoiced this page. */
                    Intent intent = new Intent(LDSAccountActivity.this, SplashActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                }
            } else {
                userNameView.setError(getString(R.string.error_incorrect_username));
                mPasswordView.setError(getString(R.string.error_incorrect_password));
            }
        }

        private Response.Listener<WebException> loginErrorListener = new Response.Listener<WebException>() {
            @Override
            public void onResponse(WebException error) {
                if(error.getExceptionType() == ExceptionType.LDS_AUTH_REQUIRED) {
                    currentUser = null;
                    onPostExecute();
                } else {
                    View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
                    TextView messageView = dialogView.findViewById(R.id.warning_message);
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LDSAccountActivity.this);

                    switch (error.getExceptionType()) {
                        case NO_DATA_CONNECTION:
                            messageView.setText(R.string.error_no_data_connection);
                            break;
                        case SERVER_UNAVAILABLE:
                            messageView.setText(R.string.error_lds_server_unavailable);
                            break;
                        case UNAUTHORIZED:
                            messageView.setText(R.string.lds_account_failed_login);
                            currentUser = null;
                            mPasswordView.setText("");
                            onPostExecute();
                            break;
                        default:
                            messageView.setText(R.string.error_generic_web);
                    }
                    dialogBuilder.setView(dialogView);
                    dialogBuilder.setPositiveButton(R.string.ok, null);
                    dialogBuilder.show();
                    Spinner.showProgress(false, mLoginFormView, mProgressView, getResources());
                }
            }
        };
    }

    /* refresh Data Section */
    private void refreshDataEventHandler() {
        Spinner.showProgress(true, mLoginFormView, mProgressView, getResources());
        dataManager.refreshLCROrgs(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to refresh data", Toast.LENGTH_SHORT).show();
                }
                Spinner.showProgress(false, mLoginFormView, mProgressView, getResources());
            }
        }, refreshDataErrorListener);
    }

    private Response.Listener<WebException> refreshDataErrorListener = new Response.Listener<WebException>() {
        @Override
        public void onResponse(WebException error) {
            View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
            TextView messageView = dialogView.findViewById(R.id.warning_message);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LDSAccountActivity.this);

            switch (error.getExceptionType()) {
                case GOOGLE_AUTH_REQUIRED:
                    messageView.setText(R.string.error_google_auth_failed);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Intent intent = new Intent(LDSAccountActivity.this, GoogleDriveOptionsActivity.class);
                            intent.putExtra("activity", LDS_ACCOUNT_ACTIVITY);
                            startActivity(intent);
                        }
                    });
                    break;
                case NO_DATA_CONNECTION:
                    messageView.setText(R.string.error_no_data_connection);
                    break;
                case SERVER_UNAVAILABLE:
                    messageView.setText(R.string.error_lds_server_unavailable);
                    break;
                case UNAUTHORIZED:
                    messageView.setText(R.string.lds_account_failed_login);
                    currentUser = null;
                    mPasswordView.setText("");
                    break;
                default:
                    messageView.setText(R.string.error_generic_web);

            }
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton(R.string.ok, null);
            dialogBuilder.show();
            Spinner.showProgress(false, mLoginFormView, mProgressView, getResources());
        }
    };

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
        Toolbar toolbar = findViewById(R.id.lds_account_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}