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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.gson.Gson;

import org.ldscd.callingworkflow.google.ConflictUtil;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.utils.DataUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDataServiceImpl implements GoogleDataService, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleDataServiceImpl() { }

    protected Activity activity;
    protected Response.Listener<Boolean> listener;
    private List<Org> organizationList;
    /*
     * Object for creating an instance of an object of type Org Listener.
     * Serves to inject into a callback method from passed in parameters.
     */
    private Response.Listener<Org> orgListener;
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

    private void initializeGoogleApiClient(Activity activity) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    // Scopes Drive.SCOPE_FILE and Drive.SCOPE_APPFOLDER are added here to ensure
                    // we can resolve conflicts on any file touched by our app.
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    // If supporting multiple accounts setAccountName could be used here to define
                    // which of the accounts the GoogleApiClient should use. CompletionEvents are
                    // tied to a specific account so when creating a GoogleApiClient to handle them
                    // (like in MyDriveEventService) use the account name from the CompletionEvent
                    // to set the account name of the GoogleApiClient.
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void stopConnection() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void restartConnection(Response.Listener<Boolean> listener, Activity activity) {
        init(listener, activity);
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        listener.onResponse(true);
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
            listener.onResponse(false);
            return;
        }
        try {
            result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void init(Response.Listener<Boolean> listener, Activity activity) {
        initializeGoogleApiClient(activity);
        this.listener = listener;
        this.activity = activity;
        if(metaFileMap == null) {
            metaFileMap = new HashMap<>();
        }
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
        if (metaFileMap != null) {
            /* Get the driveId and the file from it. */
            Metadata metadata = metaFileMap.get(fileName);
            if(metadata != null) {
                DriveId driveId = metadata.getDriveId();
                if (driveId != null) {
                    cwFile = driveId.asDriveFile();
                }
                /* From the drive file do invoke the call to open a fresh copy of the file from google drive. */
                if (cwFile != null) {
                    cwFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
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
            }
        } else {
            // invoke error listener
            Log.e(TAG, "Initial startup failed to retrieve data from Google Drive.");
        }
    }

    @Override
    public boolean saveFile(final Org org) {
        fileName = DataUtil.getFileName(org);
        if (metaFileMap != null) {
            Metadata metadata = metaFileMap.get(fileName);
            if (metadata != null) {
                DriveId driveId = metadata.getDriveId();
                cwFile = driveId.asDriveFile();
                cwFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
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
        if (metadata != null) {
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
    public void syncDriveIds(final Response.Listener<Boolean> listener, final List<Org> orgList, Activity activity) {
        organizationList = orgList;

        Drive.DriveApi.requestSync(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (!status.isSuccess()) {
                            Log.e(TAG, "Unable to sync.");
                        }
                        DriveFolder folder = Drive.DriveApi.getAppFolder(mGoogleApiClient);
                        folder.listChildren(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                                @Override
                                public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                                    if (!metadataBufferResult.getStatus().isSuccess()) {
                                        return;
                                    }
                                    int results = metadataBufferResult.getMetadataBuffer().getCount();
                                    if (results > 0) {
                                        // If the file exists then use it.
                                        for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                                            metaFileMap.put(metadata.getTitle(), metadata);
                                            /*if(metadata.getTitle().toLowerCase().contains("primary")) {
                                                DriveId fileId = metadata.getDriveId();
                                                DriveFile orgFile = fileId.asDriveFile();
                                                *//* Call to delete app data file. Unable to use trash because it's not a visible file. *//*
                                                orgFile.delete(mGoogleApiClient);
                                            }*/
                                        }
                                        List<Org> itemsToCreate = new ArrayList<Org>();
                                        for (Org org : orgList) {
                                            if (!metaFileMap.containsKey(DataUtil.getFileName(org))) {
                                                itemsToCreate.add(org);
                                            }
                                        }
                                        createOrgFile(itemsToCreate);
                                    } else {
                                            // If the file does not exist then create one.
                                        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                                        .setResultCallback(newContentsCallback);
                                    }
                                    // TODO: compare orglist with driveIDMap and create possible files that are not currently in google drive
                                    listener.onResponse(true);
                                }
                            });
                    }

            });
    }

    private void createOrgFile(List<Org> orgs) {
        for (Org org : orgs) {
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
        Drive.DriveApi.getAppFolder(mGoogleApiClient)
            /* Call the create file method. */
                .createFile(mGoogleApiClient, changeSet, null)
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
                                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                        .createFile(mGoogleApiClient, changeSet, driveContents)
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
                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, cwFileContent)
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
                cwFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
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
                    cwFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(driveContentsCallback);
                    return;
                }
                cwFileContent = driveContentsResult.getDriveContents();
                InputStream inputStream = cwFileContent.getInputStream();
                String cwStr = ConflictUtil.getStringFromInputStream(inputStream);

                orgListener.onResponse(new Gson().fromJson(cwStr, Org.class));
            }
        };
}
