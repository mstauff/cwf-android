package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.android.volley.Response;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

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
        dataManager.loadMembers(memberListener, pb);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            dataManager.loadOrgs(orgListener, pb, this);
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
                dataManager.loadOrgs(orgListener, pb, activity);
                if(orgDataFinished) {
                    startApplication();
                }
            }
        }
    };

    private void startApplication() {
        pb.setProgress(100);
        Intent intent = new Intent(SplashActivity.this, OrgListActivity.class);
        startActivity(intent);
    }
}
