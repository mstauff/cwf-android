package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.android.volley.Response;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.web.CallingData;

import java.util.List;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    protected ProgressBar pb;

    @Inject
    GoogleDataService googleDataService;

    @Inject
    CallingData callingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_splash);
         pb = (ProgressBar) findViewById(R.id.progress_bar_splash);
        pb.setProgress(10);
        googleDataService.init(listener, this);
        pb.setProgress(40);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pb.setProgress(60);
        if (resultCode == RESULT_OK) {
            googleDataService.restartConnection(listener, this);
            pb.setProgress(90);
        }
    }

    protected Response.Listener<Boolean> listener = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if(response) {
                callingData.getOrgs(new Response.Listener<List<Org>>() {
                    @Override
                    public void onResponse(List<Org> orgs) {
                        googleDataService.syncDriveIds(new Response.Listener<Boolean>() {
                            @Override
                            public void onResponse(Boolean response) {
                                pb.setProgress(100);
                                Intent intent = new Intent(SplashActivity.this, OrgListActivity.class);
                                startActivity(intent);
                            }
                        }, orgs, getParent());

                    }
                });
            }
        }
    };
}
