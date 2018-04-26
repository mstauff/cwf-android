package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.WebException;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    public static String SPLASH_ACTIVITY = "SplashActivity";
    protected ProgressBar pb;
    private boolean orgDataFinished = false;
    private boolean memberDataFinished = false;
    private Activity activity;
    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_splash);
         pb = (ProgressBar) findViewById(R.id.progress_bar_splash);
        pb.setProgress(10);
        dataManager.getUserInfo(null, null, false, new Response.Listener<LdsUser>() {
            @Override
            public void onResponse(LdsUser ldsUser) {
                if(ldsUser != null && ldsUser.getIndividualId() > 0) {
                    dataManager.loadMembers(memberListener, webErrorListener, pb);
                } else {
                    Intent intent = new Intent(SplashActivity.this, LDSAccountActivity.class);
                    startActivity(intent);
                }
            }
        }, webErrorListener);
        dataManager.loadPositionMetadata();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(dataManager.isGoogleDriveAuthenticated(getApplicationContext())) {
                dataManager.loadOrgs(orgListener, webErrorListener, pb, this);
            } else {
                Intent intent = new Intent(SplashActivity.this, GoogleDriveOptionsActivity.class);
                intent.putExtra("activity", SPLASH_ACTIVITY);
                startActivity(intent);
            }
        }
    }

    protected Response.Listener<Boolean> orgListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                orgDataFinished = true;
                if(memberDataFinished) {
                    startApplication();
                }
            }
        }
    };
    protected Response.Listener<Boolean> memberListener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if (response) {
                memberDataFinished = true;
                if(dataManager.isGoogleDriveAuthenticated(getApplicationContext())) {
                    dataManager.loadOrgs(orgListener, webErrorListener, pb, activity);
                } else {
                    Intent intent = new Intent(SplashActivity.this, GoogleDriveOptionsActivity.class);
                    intent.putExtra("activity", SPLASH_ACTIVITY);
                    startActivity(intent);
                }
                if(orgDataFinished) {
                    startApplication();
                }
            }
        }
    };

    private Response.Listener<WebException> webErrorListener = new Response.Listener<WebException>() {
        @Override
        public void onResponse(WebException error) {
            View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
            TextView messageView = dialogView.findViewById(R.id.warning_message);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

            switch (error.getExceptionType()) {
                case LDS_AUTH_REQUIRED:
                    messageView.setText(R.string.error_lds_auth_failed);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Intent intent = new Intent(SplashActivity.this, LDSAccountActivity.class);
                            startActivity(intent);
                        }
                    });
                    break;
                case GOOGLE_AUTH_REQUIRED:
                    messageView.setText(R.string.error_google_auth_failed);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Intent intent = new Intent(SplashActivity.this, GoogleDriveOptionsActivity.class);
                            intent.putExtra("activity", SPLASH_ACTIVITY);
                            startActivity(intent);
                        }
                    });
                    break;
                case NO_DATA_CONNECTION:
                    messageView.setText(R.string.error_no_data_connection);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            startApplication();
                        }
                    });
                    break;
                case SERVER_UNAVAILABLE:
                    messageView.setText(R.string.error_lds_server_unavailable);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            startApplication();
                        }
                    });
                    break;
                case UNKOWN_GOOGLE_EXCEPTION:
                    messageView.setText(R.string.error_google_drive);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            startApplication();
                        }
                    });
                    break;
                default:
                    messageView.setText(R.string.error_generic_web);
                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            startApplication();
                        }
                    });

            }
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton(R.string.ok, null);
            dialogBuilder.create().show();
        }
    };

    private void startApplication() {
        pb.setProgress(100);
        Intent intent = new Intent(SplashActivity.this, OrgListActivity.class);
        startActivity(intent);
    }
}
