package org.ldscd.callingworkflow.display;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * A login screen that offers login via email/password.
 */
public class LDSAccountActivity extends AppCompatActivity {
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
        userNameView = (AutoCompleteTextView) findViewById(R.id.userName);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
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
                    userNameView.setText(SecurityUtil.decrypt(getApplicationContext(), preferences.getString("username", null)));
                    mPasswordView.setText(SecurityUtil.decrypt(getApplicationContext(), preferences.getString("password", null)));
                }
            }
        });
        Button mLDSAccountSignInButton = (Button) findViewById(R.id.lds_credentials_sign_in_button);
        mLDSAccountSignInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Button refreshDataButton = (Button) findViewById(R.id.button_data_sync);
        refreshDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDataEventHandler();
            }
        });
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
            showProgress(true);
            mAuthTask = new UserLoginTask(userName, password);
            mAuthTask.doInBackground((Void) null);
        }
    }

    private boolean isUserNameValid(String userName) {
        return userName.length() > 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        /* On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         * for very easy animations. If available, use these APIs to fade-in
         * the progress spinner.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            /* The ViewPropertyAnimator APIs are not available, so simply show and hide the relevant UI components */
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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

        protected void doInBackground(Void... params) {
            final List<Boolean> success = new ArrayList<>();
            success.add(0, false);
            boolean hasChanges = SecurityUtil.decrypt(getApplicationContext(), sharedPreferences.getString("username", null)) != mUserName ||
                                 SecurityUtil.decrypt(getApplicationContext(), sharedPreferences.getString("password", null)) != mPassword;
            dataManager.getUserInfo(mUserName, mPassword, hasChanges, new Response.Listener<LdsUser>() {
                @Override
                public void onResponse(LdsUser ldsUser) {
                    onPostExecute(ldsUser != null && ldsUser.getIndividualId() > 0);
                }
            });
        }

        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                if(isTaskRoot()) {
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
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /* refresh Data Section */
    private void refreshDataEventHandler() {
        showProgress(true);
        dataManager.refreshLCROrgs(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to refresh data", Toast.LENGTH_SHORT);
                }
                showProgress(false);
            }
        });
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.lds_account_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}