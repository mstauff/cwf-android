package org.ldscd.callingworkflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import org.ldscd.callingworkflow.google.ConflictResolver;
import org.ldscd.callingworkflow.google.ConflictUtil;
import org.ldscd.callingworkflow.web.WebResources;

import java.io.*;

import javax.inject.Inject;

/* Google drive api items. */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SignInActivity";
    protected EditText cwFileEditText;
    protected Button updateCWFileButton;

    protected EditText usernameEditText;
    protected EditText passwordEditText;
    protected Button authConfirmButton;

    // Instance variables used for DriveFile and DriveContents to help initiate file conflicts.
    protected DriveFile cwFile;
    protected DriveContents cwFileContent;

    // Receiver used to update the EditText once conflicts have been resolved.
    protected BroadcastReceiver broadcastReceiver;

    @Inject
    WebResources wr;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ((CWFApplication)getApplication()).getNetComponent().inject(this);
        setContentView(R.layout.activity_settings);

        cwFileEditText = (EditText) findViewById(R.id.editText);
        updateCWFileButton = (Button) findViewById(R.id.button);

        updateCWFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cwFileContent != null) {
                    cwFileContent.reopenForWrite(getGoogleApiClient())
                            .setResultCallback(updateDriveContentsCallback);
                    // Disable update button to prevent double taps.
                    updateCWFileButton.setEnabled(false);
                }
            }
        });

        // When conflicts are resolved, update the EditText with the resolved list
        // then open the contents so it contains the resolved list.
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConflictResolver.CONFLICT_RESOLVED)) {
                    Log.d(TAG, "Received intent to update edit text.");
                    String resolvedStr = intent.getStringExtra("conflictResolution");
                    cwFileEditText.setText(resolvedStr);
                    // Open {@code cwFile} in read only mode to update
                    // {@code cwFileContent} to current base state.
                    cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(driveContentsCallback);
                }
            }
        };

        usernameEditText = (EditText) findViewById(R.id.userText);
        passwordEditText = (EditText) findViewById(R.id.passwordText);
        authConfirmButton = (Button) findViewById(R.id.auth_confirm);
        authConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if(!user.equals("") && !password.equals("")) {
                    wr.setCredentials(user, password);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(ConflictResolver.CONFLICT_RESOLVED));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // Syncing to help devices use the same file.
        Drive.DriveApi.requestSync(getGoogleApiClient()).setResultCallback(syncCallback);
    }

    // Callback when requested sync returns.
    private ResultCallback<Status> syncCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (!status.isSuccess()) {
                Log.e(TAG, "Unable to sync.");
            }
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE,
                            getResources().getString(R.string.CWFileName)))
                    .build();
            //Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(metadataCallback);

            DriveFolder folder = Drive.DriveApi.getAppFolder(getGoogleApiClient());
            folder.listChildren(getGoogleApiClient()).setResultCallback(metadataCallback);

        }
    };

    // Callback when search for the cw file returns. It sets {@code cwFile} if
    // it exists or initiates the creation of a new file if no file is found.
    private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                    if (!metadataBufferResult.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving results.");
                        return;
                    }
                    int results = metadataBufferResult.getMetadataBuffer().getCount();
                    if (results > 0) {
                        // If the file exists then use it.
                        DriveId driveId = metadataBufferResult.getMetadataBuffer().get(0).getDriveId();
                        cwFile = driveId.asDriveFile();
                        cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                                .setResultCallback(driveContentsCallback);
                    } else {
                        // If the file does not exist then create one.
                        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                                .setResultCallback(newContentsCallback);
                    }
                }
            };

    // Callback when {@code cwFileContent} is reopened for writing.
    // [START update_drive_contents]
    private ResultCallback<DriveApi.DriveContentsResult> updateDriveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to updated file.");
                        return;
                    }
                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    OutputStream outputStream = driveContents.getOutputStream();
                    Writer writer = new OutputStreamWriter(outputStream);
                    try {
                        writer.write(cwFileEditText.getText().toString());
                        writer.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    // ExecutionOptions define the conflict strategy to be used.
                    // [START EXCLUDE]
                    // [START execution_options]
                    ExecutionOptions executionOptions = new ExecutionOptions.Builder()
                            .setNotifyOnCompletion(true)
                            .setConflictStrategy(ExecutionOptions.CONFLICT_STRATEGY_KEEP_REMOTE)
                            .build();
                            try {
                                driveContents.commit(getGoogleApiClient(), null, executionOptions)
                                        .setResultCallback(fileWrittenCallback);
                            }catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                    // [END execution_options]
                    // [END EXCLUDE]

                    Log.d(TAG, "Saving file.");
                }
            };
    // [END update_drive_contents]

    // Callback when file has been written locally.
    private ResultCallback<Status> fileWrittenCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (!status.isSuccess()) {
                Log.e(TAG, "Unable to write grocery list.");
            }
            Log.d(TAG, "File saved locally.");
            cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(driveContentsCallback);
        }
    };

    // Callback when {@code DriveApi.DriveContentsResult} for the creation of a new
    // {@code DriveContents} has been returned.
    private ResultCallback<DriveApi.DriveContentsResult> newContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to create CW file contents.");
                        return;
                    }
                    Log.d(TAG, "CW new file contents returned.");
                    cwFileContent = driveContentsResult.getDriveContents();

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(getResources().getString(R.string.CWFileName))
                            .setMimeType("text/json")
                            .build();
                    // create a file on root folder
                    Drive.DriveApi.getAppFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, cwFileContent)
                            .setResultCallback(CWFileCallback);
                }
            };

    // Callback when request to create CW file is returned.
    private ResultCallback<DriveFolder.DriveFileResult> CWFileCallback =
            new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                    if (!driveFileResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Unable to create CW file.");
                        return;
                    }
                    Log.d(TAG, "CW file returned.");
                    cwFile = driveFileResult.getDriveFile();
                    // Open {@code cwFile} in read only mode to update
                    // {@code cwFileContent} to current base state.
                    cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(driveContentsCallback);
                }
            };

    // Callback when request to open {@code cwFile} in read only mode is returned.
    private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
        new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Unable to load CW data.");

                    // Try to open {@code cwFile} again.
                    cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(driveContentsCallback);
                    return;
                }
                cwFileContent = driveContentsResult.getDriveContents();
                InputStream inputStream = cwFileContent.getInputStream();
                String cwStr = ConflictUtil.getStringFromInputStream(inputStream);

                // Only update {@code cwFileEditText} initially when {@code cwFile}
                // is opened.
                if (cwFileEditText.getText().toString().equals("Loading...")) {
                    cwFileEditText.setText(cwStr);
                }

                // The text in {@code cwFileEditText} should be the same as text in
                // {@code cwFileContent} to enable {@code updateCWFileButton}.
                if (cwFileEditText.getText().toString().trim().equals(cwStr.trim())) {
                    updateCWFileButton.setEnabled(true);
                }
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
            navigateUpTo(new Intent(this, CallingListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}