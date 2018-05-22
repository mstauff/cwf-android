package org.ldscd.callingworkflow.display;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.utils.SecurityUtil;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.ExceptionType;
import org.ldscd.callingworkflow.web.UI.Spinner;
import org.ldscd.callingworkflow.web.WebException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private AutoCompleteTextView userNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    @Inject
    DataManager dataManager;
    SharedPreferences sharedPreferences;

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

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        LdsUser user = dataManager.getCurrentUser();
        setRefreshButton(user != null);
    }

    private void setRefreshButton(boolean canUpdate) {
        Button refreshDataButton = findViewById(R.id.button_data_sync);
        if(canUpdate) {
            refreshDataButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = getAlertDialog();
                    dialog.show();
                }
            });
        } else {
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
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        /* Check for a valid email userName. */
        if (TextUtils.isEmpty(userName)) {
            userNameView.setError(getString(R.string.error_field_required));
            focusView = userNameView;
            cancel = true;
        } else if (!isUserNameValid(userName)) {
            userNameView.setError(getString(R.string.error_invalid_email));
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

    private boolean isUserNameValid(String userName) {
        return userName.length() > 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
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
            final List<Boolean> success = new ArrayList<>();
            success.add(0, false);
            boolean hasChanges = !Objects.equals(SecurityUtil.decrypt(getApplicationContext(), sharedPreferences.getString("username", null)), mUserName) ||
                    !Objects.equals(SecurityUtil.decrypt(getApplicationContext(), sharedPreferences.getString("password", null)), mPassword);
            dataManager.getUserInfo(mUserName, mPassword, hasChanges, new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    onPostExecute(ldsUser != null && ldsUser.getIndividualId() > 0);
                }
            }, loginErrorListener);
        }

        private void onPostExecute(final Boolean success) {
            mAuthTask = null;
            Spinner.showProgress(false, mLoginFormView, mProgressView, getResources());
            setRefreshButton(success);
            if (success) {
                if (isTaskRoot()) {
                /* Transition to splash screen or directory view depending on which view invoiced this page. */
                    Intent intent = new Intent(LDSAccountActivity.this, SplashActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                }
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        /* Implement this if a cancel button is provided in the future. */
        /*protected void onCancelled() {
            mAuthTask = null;
            Spinner.showProgress(false, mLoginFormView, mProgressView, getResources());
        }*/

        private Response.Listener<WebException> loginErrorListener = new Response.Listener<WebException>() {
            @Override
            public void onResponse(WebException error) {
                if(error.getExceptionType() == ExceptionType.LDS_AUTH_REQUIRED) {
                    onPostExecute(false);
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