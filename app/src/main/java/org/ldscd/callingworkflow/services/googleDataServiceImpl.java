package org.ldscd.callingworkflow.services;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.Response;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.gson.Gson;
import org.ldscd.callingworkflow.google.ConflictUtil;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.utils.DataUtil;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoogleDataServiceImpl implements GoogleDataService, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public GoogleDataServiceImpl() {
    }

    @Override
    public void getOrgData(final Response.Listener<Org> listener, Response.ErrorListener errorListener, final Org org) {
        fileName = DataUtil.getFileName(org);
        // TODO: make cwFile local.
        // TODO: get file name.
        // get file id
        // get file from id
        // open file
        // read file and create org then pass in org to listener.
        if(metaFileMap != null) {
            /* Get the driveId and the file from it. */
            DriveId driveId = metaFileMap.get(fileName).getDriveId();
            if(driveId != null) {
                cwFile = driveId.asDriveFile();
            }
            /* From the drive file do invoke the call to open a fresh copy of the file from google drive. */
            if(cwFile != null) {
                cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                        /* Read the results from the callback as content and turn it into a stream. */
                        cwFileContent = driveContentsResult.getDriveContents();
                        InputStream inputStream = cwFileContent.getInputStream();
                        /* Convert the stream into a json string for usability. */
                        String cwStr = ConflictUtil.getStringFromInputStream(inputStream);
                        /* Convert the json string into an Org.  Then inject the org into the callback. */
                        listener.onResponse(new Gson().fromJson(cwStr, Org.class));
                        }
                    });
            }
        } else {
            // invoke error listener
            Log.e(TAG, "Initial startup failed to retrieve data from Google Drive.");
        }
    }

    @Override
    public boolean saveFile(final Org org) {
        fileName = DataUtil.getFileName(org);
        if(metaFileMap != null) {
            Metadata metadata = metaFileMap.get(fileName);
            if(metadata != null) {
                DriveId driveId = metadata.getDriveId();
                cwFile = driveId.asDriveFile();
                cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                        .setResultCallback(driveContentsCallback);
            }
        } else {
            createOrgFile(org);
        }

        return false;
    }

    @Override
    public boolean deleteFile(final Org org) {
        Metadata metadata = metaFileMap.get(DataUtil.getFileName(org));
        if(metadata != null) {
            if (metadata.getDriveId() != null) {
                DriveId fileId = metadata.getDriveId();
                DriveFile orgFile = fileId.asDriveFile();
                /* Call to delete app data file. Unable to use trash because it's not a visible file. */
                com.google.android.gms.common.api.Status deleteStatus =
                        orgFile.delete(mGoogleApiClient).await();
                if (!deleteStatus.isSuccess()) {
                    Log.e(TAG, "Unable to delete app data file.");
                    return true;
                }
                /* Remove stored MetaData. */
                metaFileMap.remove(DataUtil.getFileName(org));
                Log.d(TAG, "Org file deleted.");
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public void syncDriveIds(final List<Org> orgList) {
        //TODO: add callbacks as parameter to this method.
        organizationList = orgList;
        Drive.DriveApi.requestSync(getGoogleApiClient())
            .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (!status.isSuccess()) {
                        Log.e(TAG, "Unable to sync.");
                    }
                    DriveFolder folder = Drive.DriveApi.getAppFolder(getGoogleApiClient());
                    folder.listChildren(getGoogleApiClient())
                    .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                        @Override
                        public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                            if (!metadataBufferResult.getStatus().isSuccess()) {
                                //showMessage("Problem while retrieving results.");
                                return;
                            }
                            int results = metadataBufferResult.getMetadataBuffer().getCount();
                            if (results > 0) {
                                // If the file exists then use it.
                                for(Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                                    metaFileMap.put(metadata.getTitle(), metadata);
                                }
                                List<Org> itemsToCreate = new ArrayList<Org>();
                                for(Org org : orgList) {
                                    if(!metaFileMap.containsKey(DataUtil.getFileName(org))) {
                                        itemsToCreate.add(org);
                                    }
                                }
                                createOrgFile(itemsToCreate);
                            } else {
                                // If the file does not exist then create one.
                                /*Drive.DriveApi.newDriveContents(getGoogleApiClient())
                                .setResultCallback(newContentsCallback);*/
                            }
                            // TODO: compare orglist with driveIDMap and create possible files that are not currently in google drive
                        }
                    });
                }
            });
        // Callback.response(statusList);
    }

    private void createOrgFile(List<Org> orgs) {
        for(Org org : orgs) {
            createOrgFile(org);
        }
    }

    private void createOrgFile(final Org org) {
        /* Meta data for the file to be saved. */
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
            .setTitle(DataUtil.getFileName(org))
            .setMimeType("text/json")
            .build();
        /* Acquire the app folder to work with. */
        Drive.DriveApi.getAppFolder(getGoogleApiClient())
            /* Call the create file method. */
            .createFile(getGoogleApiClient(), changeSet, null)
            /* Dive into the result callback method. */
            .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                    /* Get the content section of the file. */
                    new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult result) {
                            if (!result.getStatus().isSuccess()) {
                                //showMessage("Error while trying to create new file contents");
                                return;
                            }
                            final DriveContents driveContents = result.getDriveContents();
                            /* Write Org content to DriveContents. */
                            OutputStream outputStream = driveContents.getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);
                            try {
                                writer.write(new Gson().toJson(org, Org.class));
                                writer.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }

                            /* Save file to app folder. */
                            Drive.DriveApi.getAppFolder(getGoogleApiClient())
                                .createFile(getGoogleApiClient(), changeSet, driveContents)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                        if (!driveFileResult.getStatus().isSuccess()) {
                                            Log.e(TAG, "Error while trying to create the file");
                                            return;
                                        }
                                        Log.e(TAG, "Created a file with content: " + driveFileResult.getDriveFile().getDriveId());
                                        driveFileResult.getDriveFile().getMetadata(mGoogleApiClient)
                                                .setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                                    @Override
                                                    public void onResult(DriveResource.MetadataResult metadataResult) {
                                                        metaFileMap.put(changeSet.getTitle(), metadataResult.getMetadata());
                                                    }
                                                });
                                    }
                                });
                        }
                    };
                }
            });
    }

    //*****************************************************************//
    private List<Org> organizationList;
    /*
     * Object for creating an instance of an object of type Org Listener.
     * Serves to inject into a callback method from passed in parameters.
     */
    private  Response.Listener<Org> orgListener;
    /*
     * The name of the file to search for.
     */
    private String fileName;
    /*
     * List of DriveIDs
     */
    private Map<String, Metadata> metaFileMap;
    /*
     * Name of the service being used.
     */
    private String TAG = "GoogleDataService";
    /*
     * Instance variables used for DriveFile and DriveContents to help initiate file conflicts.
     */
    protected DriveFile cwFile;
    /*
     * The file's content in json format.
     */
    protected DriveContents cwFileContent;
    /*
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;
    /*
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(new Activity())
                    .addApi(Drive.API)
                            // Scopes Drive.SCOPE_FILE and Drive.SCOPE_APPFOLDER are added here to ensure
                            // we can resolve conflicts on any file touched by our app.
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                            // If supporting multiple accounts setAccountName could be used here to define
                            // which of the accounts the GoogleApiClient should use. CompletionEvents are
                            // tied to a specific account so when creating a GoogleApiClient to handle them
                            // (like in MyDriveEventService) use the account name from the CompletionEvent
                            // to set the account name of the GoogleApiClient.
                    .build();
        }
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }

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
                    /*Writer writer = new OutputStreamWriter(outputStream);
                    try {
                        writer.write(cwFileEditText.getText().toString());
                        writer.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }*/

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
                            //.setTitle(getResources().getString(R.string.CWFileName))
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
    public ChangeListener changeListener = new ChangeListener() {
        @Override
        public void onChange(ChangeEvent event) {
            // Resolve conflict.
            DriveId resolvedItems = (event.getDriveId());
            /*MetadataChangeSet modifiedMetadataChangeSet = mConflictedCompletionEvent.getModifiedMetadataChangeSet();
            DriveApi.DriveContentsResult driveContentsResult = currentDriveContents.reopenForWrite(mGoogleApiClient).await();
            // [END reopen_file]
            if (driveContentsResult.getStatus().isSuccess()) {
                DriveContents writingDriveContents = driveContentsResult.getDriveContents();

                // Modified MetadataChangeSet is supplied here to be reapplied.
                writeItems(writingDriveContents, resolvedItems, modifiedMetadataChangeSet);
            } else {
                // The contents cannot be reopened at this point, probably due to
                // connectivity, so by snoozing the event we will get it again later.
                Log.d(TAG, "Unable to write resolved content, snoozing completion event.");
                mConflictedCompletionEvent.snooze();
            }*/
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
                        cwFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).setResultCallback(driveContentsCallback);
                        return;
                    }
                    cwFileContent = driveContentsResult.getDriveContents();
                    InputStream inputStream = cwFileContent.getInputStream();
                    String cwStr = ConflictUtil.getStringFromInputStream(inputStream);

                    orgListener.onResponse(new Gson().fromJson(cwStr, Org.class));
                }
            };

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.

            return;
        }
        try {
            result.startResolutionForResult(null, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }
}