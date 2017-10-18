package org.ldscd.callingworkflow.services;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.google.ConflictUtil;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.UnitSettings;
import org.ldscd.callingworkflow.model.serialize.PositionSerializer;
import org.ldscd.callingworkflow.utils.DataUtil;
import org.ldscd.callingworkflow.web.OrgCallingBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void getUnitSettings(final Response.Listener<UnitSettings> listener, Long unitNumber) {
        Metadata metadata = metaFileMap.get(DataUtil.getUnitFileName(unitNumber));
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
                            if(cwFileContent != null) {
                                InputStream inputStream = cwFileContent.getInputStream();
                                /* Convert the stream into a string for usability. */
                                String cwStr = ConflictUtil.getStringFromInputStream(inputStream);
                                /* Convert the json string into an UnitSettings Object.
                                    Then inject the UnitSettings object into the callback. */

                                UnitSettings unitSettings = new Gson().fromJson(cwStr, UnitSettings.class);
                                listener.onResponse(unitSettings);
                            }
                        }
                    });
            }
        } else {
            // invoke error listener
            Log.e(TAG, "Failed to retrieve unit settings data from Google Drive.");
        }
    }

    @Override
    public void saveUnitSettings(final Response.Listener<Boolean> listener, final UnitSettings unitSettings) {
        String fileName = DataUtil.getUnitFileName(unitSettings.getUnitNumber());
        if (metaFileMap != null) {
            final Metadata metadata = metaFileMap.get(fileName);
            if(metadata == null) {
                syncDriveIds(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        listener.onResponse(response);
                    }
                }, null);
            }
            if (metadata != null) {
                DriveId driveId = metadata.getDriveId();
                cwFile = driveId.asDriveFile();
                cwFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                            listener.onResponse(driveContentsResult.getStatus().isSuccess());
                            if (!driveContentsResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Unable to load unit settings data.");
                                return;
                            }
                            /* Create Gson object with custom formatting on the unit settings. */
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.serializeNulls();
                            String flattenedJson = gsonBuilder.create().toJson(unitSettings, UnitSettings.class);
                            if(flattenedJson != null && flattenedJson.length() > 0) {
                                try {
                                    /* Write Unit Settings content to DriveContents. */
                                    cwFileContent = driveContentsResult.getDriveContents();
                                    OutputStream outputStream = cwFileContent.getOutputStream();
                                    outputStream.write(flattenedJson.getBytes());
                                    outputStream.close();
                                    if(mGoogleApiClient.isConnected()) {
                                        cwFileContent.commit(mGoogleApiClient, null)
                                            .setResultCallback(new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(@NonNull Status status) {
                                                    listener.onResponse(status.getStatus().isSuccess());
                                                    if(status.getStatus().isSuccess()) {
                                                        Log.i("Update Method", status.getStatus().toString());
                                                    }
                                                }
                                            });
                                    } else {
                                        mGoogleApiClient.connect();
                                        cwFileContent.commit(mGoogleApiClient, null);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage());
                                    listener.onResponse(false);
                                }
                            }
                        }
                    });
            } else {
                createUnitSettingsFile(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        listener.onResponse(response);
                    }
                }, unitSettings);
            }
        }
    }

    private void createUnitSettingsFile(final Response.Listener<Boolean> listener, final UnitSettings unitSettings) {
        /* Meta data for the file to be saved. */
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(DataUtil.getUnitFileName(unitSettings.getUnitNumber()))
                .setMimeType("text/json")
                .build();
        /* Acquire the app folder to work with. */
        Drive.DriveApi.getAppFolder(mGoogleApiClient)
            /* Call the create file method. */
            .createFile(mGoogleApiClient, changeSet, null)
            /* Dive into the result callback method. */
            .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(final DriveFolder.DriveFileResult driveFileResult) {
                /* Get the content section of the file. */
                if (!driveFileResult.getStatus().isSuccess()) {
                    return;
                }
                /* Get the newly created file. */
                cwFile = driveFileResult.getDriveFile();
                /* Open connection to google services to prepare for writing. */
                cwFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                            if (!driveContentsResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Unable to load CW data.");
                                return;
                            }
                            /* When the connection is made add data to file content. */
                            final DriveContents driveContents = driveContentsResult.getDriveContents();
                            /* Create Gson object to flatten the Unit Settings object. */
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.serializeNulls();
                            String flattenedJson = gsonBuilder.create().toJson(unitSettings, UnitSettings.class);
                            if(flattenedJson != null && flattenedJson.length() > 0) {
                                try {
                                    /* Write Unit Settings content to DriveContents. */
                                    OutputStream outputStream = driveContents.getOutputStream();
                                    outputStream.write(flattenedJson.getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                /* If the update was successful, acquire the metadata of the newly updated file. */
                                driveContents.commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        Log.e("Save File", status.getStatusCode() == 0 ? "Successfully saved file" : "failed to save file");
                                        /* Add files meta data to metaFileMap. */
                                        driveFileResult.getDriveFile().getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                            @Override
                                            public void onResult(@NonNull DriveResource.MetadataResult metadataResult) {
                                                if(metadataResult != null && metadataResult.getMetadata() != null) {
                                                    /* Add meta data to quick lookup hashmap. */
                                                    metaFileMap.put(DataUtil.getUnitFileName(unitSettings.getUnitNumber()), metadataResult.getMetadata());
                                                    listener.onResponse(true);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            });
    }

    @Override
    public void getOrgData(final Response.Listener<Org> listener, final Response.ErrorListener errorListener, final Org org) {
        fileName = DataUtil.getOrgFileName(org);
        getOrgFromMeta(metaFileMap.get(fileName), listener, errorListener);

    }

    private void getOrgFromMeta(final Metadata metadata, final Response.Listener<Org> listener, final Response.ErrorListener errorListener) {
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
                                    if(cwFileContent != null) {
                                        InputStream inputStream = cwFileContent.getInputStream();
                                        /* Convert the stream into a string for usability. */
                                        String cwStr = ConflictUtil.getStringFromInputStream(inputStream);
                                        /* Convert the json string into an Org.  Then inject the org into the callback. */
                                    OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
                                        JSONArray jsonArray = null;
                                        JSONObject jsonObject = null;
                                        List<Org> orgs = null;
                                        try {
                                            jsonObject = new JSONObject(cwStr);
                                            jsonArray = new JSONArray();
                                            jsonArray.put(jsonObject);
                                            orgs = orgCallingBuilder.extractOrgs(jsonArray);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if(orgs != null && orgs.size() > 0) {
                                            listener.onResponse(orgs.get(0));
                                        } else {
                                            listener.onResponse(null);
                                        }

                                    } else {
                                        errorListener.onErrorResponse(new VolleyError("File not found: " + metadata.getOriginalFilename()));
                                    }
                                }
                            });
                }
            } else {
                // invoke error listener
                Log.e(TAG, "Initial startup failed to retrieve data from Google Drive.");
        }
    }

    @Override
    public void getOrgs(final Response.Listener<List<Org>> listener, Response.ErrorListener errorListener) {
        final int[] numOfOrgsReturned = {0}; //this must be an array to be able to increment it in the callback
        /* We have to break up the list of calls to Google drive because
           they only allow for 10 at a time.  This will need to be reworked to be more elegant.
         */
        final Set<Org> orgSet = new HashSet<>(metaFileMap.size());
        int size = Math.round(metaFileMap.size()/2);
        List<Metadata> metadataList = new ArrayList<Metadata>(metaFileMap.values());
        for(int i = 0; i <= size; i++) {
            Metadata metadata = metadataList.get(i);
            if(metadata.getFileExtension().equals("json")) {
                getOrgFromMeta(metadata, new Response.Listener<Org>() {
                    @Override
                    public void onResponse(Org response) {
                        // todo - potential threading issues if multiple threads handle responses the ++ may not be correct
                        if (response != null) {
                            orgSet.add(response);
                        }
                        numOfOrgsReturned[0]++;
                        if (numOfOrgsReturned[0] == metaFileMap.size()) {
                            listener.onResponse(new ArrayList<Org>(orgSet));
                        }
                    }
                }, errorListener);
            }
        }
        for(int i = metaFileMap.size()-1; i >= size; i--) {
            Metadata metadata = metadataList.get(i);
            if(metadata.getFileExtension().equals("json")) {
                getOrgFromMeta(metadata, new Response.Listener<Org>() {
                    @Override
                    public void onResponse(Org response) {
                        // todo - potential threading issues if multiple threads handle responses the ++ may not be correct
                        if (response != null) {
                            orgSet.add(response);
                        }
                        numOfOrgsReturned[0]++;
                        if (numOfOrgsReturned[0] == metaFileMap.size()) {
                            listener.onResponse(new ArrayList<Org>(orgSet));
                        }
                    }
                }, errorListener);
            }
        }
    }

    @Override
    public void saveOrgFile(final Response.Listener<Boolean> listener, final Org org) {
        fileName = DataUtil.getOrgFileName(org);
        if (metaFileMap != null) {
            final Metadata metadata = metaFileMap.get(fileName);
            if(metadata == null) {
                syncDriveIds(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        listener.onResponse(response);
                    }
                }, null);
            }
            if (metadata != null) {
                DriveId driveId = metadata.getDriveId();
                cwFile = driveId.asDriveFile();
                cwFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                        .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                listener.onResponse(driveContentsResult.getStatus().isSuccess());
                                if (!driveContentsResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Unable to load CW data.");
                                    return;
                                }
                                /* Create Gson object with custom formatting on the Calling to flatten the position object. */
                                GsonBuilder gsonBuilder = new GsonBuilder();
                                gsonBuilder.serializeNulls();
                                gsonBuilder.registerTypeAdapter(Calling.class, new PositionSerializer());
                                String flattenedJson = gsonBuilder.create().toJson(org, Org.class);
                                if(flattenedJson != null && flattenedJson.length() > 0) {
                                try {
                                    /* Write Org content to DriveContents. */
                                    cwFileContent = driveContentsResult.getDriveContents();
                                    OutputStream outputStream = cwFileContent.getOutputStream();
                                    outputStream.write(flattenedJson.getBytes());
                                    outputStream.close();
                                    if(mGoogleApiClient.isConnected()) {
                                        cwFileContent.commit(mGoogleApiClient, null)
                                                .setResultCallback(new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(@NonNull Status status) {
                                                listener.onResponse(status.getStatus().isSuccess());
                                                if(status.getStatus().isSuccess()) {
                                                    Log.i("Update Method", status.getStatus().toString());
                                                }
                                            }
                                        });
                                    } else {
                                        mGoogleApiClient.connect();
                                        cwFileContent.commit(mGoogleApiClient, null);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage());
                                    listener.onResponse(false);
                                }
                                }
                            }
                        });
            } else {
                createOrgFile(new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        listener.onResponse(response);
                    }
                }, org);
            }
        } else {
            //TODO: sync metaFileMap.
        }
    }

    @Override
    public void deleteFile(final Response.Listener<Boolean> listener, final Org org) {
        Metadata metadata = metaFileMap.get(DataUtil.getOrgFileName(org));
        if (metadata != null) {
            if (metadata.getDriveId() != null) {
                DriveId fileId = metadata.getDriveId();
                DriveFile orgFile = fileId.asDriveFile();
                /* Call to delete app data file. Unable to use trash because it's not a visible file. */
                com.google.android.gms.common.api.Status deleteStatus =
                        orgFile.delete(mGoogleApiClient).await();
                listener.onResponse(deleteStatus.isSuccess());
                if (!deleteStatus.isSuccess()) {
                    Log.e(TAG, "Unable to delete app data file.");
                }
                /* Remove stored MetaData. */
                metaFileMap.remove(DataUtil.getOrgFileName(org));
                Log.d(TAG, "Org file deleted.");
            }
        }
    }

    @Override
    public boolean isAuthenticated() {
        return mGoogleApiClient.isConnected();
    }

    @Override
    public void syncDriveIds(final Response.Listener<Boolean> listener, final List<Org> orgList) {
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
                                boolean hasUnitSettings = false;
                                for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                                    metaFileMap.put(metadata.getTitle(), metadata);
                                    /* This commented code is for removing a specified file and reloading it afresh. */
                                    /*if(metadata != null && metadata.getOriginalFilename().contains("RS")) {
                                        DriveId fileId = metadata.getDriveId();
                                        DriveFile orgFile = fileId.asDriveFile();
                                         //Call to delete app data file. Unable to use trash because it's not a visible file.
                                        orgFile.delete(mGoogleApiClient);
                                    }*/
                                    if(!hasUnitSettings && metadata.getFileExtension().equals("config")) {
                                        hasUnitSettings = true;
                                    }
                                }
                                if(orgList != null) {
                                    List<Org> itemsToCreate = new ArrayList<Org>();
                                    for (Org org : orgList) {
                                        if (org.getOrgTypeId() > 0 && !metaFileMap.containsKey(DataUtil.getOrgFileName(org))) {
                                            itemsToCreate.add(org);
                                        }
                                    }
                                    if(!itemsToCreate.isEmpty()) {
                                        createOrgFile(new Response.Listener<Boolean>() {
                                            @Override
                                            public void onResponse(Boolean response) {
                                                listener.onResponse(response);
                                            }
                                        }, itemsToCreate);
                                    }
                                }
                                if(!hasUnitSettings && orgList != null && orgList.size() > 0) {
                                    createUnitSettingsFile(new Response.Listener<Boolean>() {
                                        @Override
                                        public void onResponse(Boolean response) {
                                            listener.onResponse(response);
                                        }
                                    }, new UnitSettings(orgList.get(0).getUnitNumber(), new ArrayList<CallingStatus>()));
                                }
                            } else {
                                // If the file does not exist then create one.
                                createOrgFile(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        listener.onResponse(response);
                                    }
                                }, orgList);
                                createUnitSettingsFile(new Response.Listener<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean response) {
                                        listener.onResponse(response);
                                    }
                                }, new UnitSettings(orgList.get(0).getUnitNumber(), new ArrayList<CallingStatus>()));
                            }
                            // TODO: compare orglist with driveIDMap and create possible files that are not currently in google drive
                            listener.onResponse(true);
                        }
                    });
            }
        });
    }

    private void createOrgFile(Response.Listener<Boolean> listener, List<Org> orgs) {
        if(orgs != null) {
            for (Org org : orgs) {
                createOrgFile(listener, org);
            }
        }
    }

    private void createOrgFile(final Response.Listener<Boolean> listener, final Org org) {
        /* Meta data for the file to be saved. */
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(DataUtil.getOrgFileName(org))
                .setMimeType("text/json")
                .build();
        /* Acquire the app folder to work with. */
        Drive.DriveApi.getAppFolder(mGoogleApiClient)
            /* Call the create file method. */
            .createFile(mGoogleApiClient, changeSet, null)
            /* Dive into the result callback method. */
            .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(final DriveFolder.DriveFileResult driveFileResult) {
                /* Get the content section of the file. */
                    if (!driveFileResult.getStatus().isSuccess()) {
                        return;
                    }
                    /* Get the newly created file. */
                    cwFile = driveFileResult.getDriveFile();
                    /* Open connection to google services to prepare for writing. */
                    cwFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                        .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                if (!driveContentsResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Unable to load CW data.");
                                    return;
                                }
                                /* When the connection is made add data to file content. */
                                final DriveContents driveContents = driveContentsResult.getDriveContents();
                                /* Create Gson object with custom formatting on the Calling to flatten the position object. */
                                GsonBuilder gsonBuilder = new GsonBuilder();
                                gsonBuilder.serializeNulls();
                                gsonBuilder.registerTypeAdapter(Calling.class, new PositionSerializer());
                                String flattenedJson = gsonBuilder.create().toJson(org, Org.class);

                                if(flattenedJson != null && flattenedJson.length() > 0) {
                                    try {
                                        /* Write Org content to DriveContents. */
                                        OutputStream outputStream = driveContents.getOutputStream();
                                        outputStream.write(flattenedJson.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    /* If the update was successful, acquire the metadata of the newly updated file. */

                                    driveContents.commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(@NonNull Status status) {
                                            Log.e("Save File", status.getStatusCode() == 0 ? "Successfully saved file" : "failed to save file");
                                            /* Add files meta data to metaFileMap. */
                                            driveFileResult.getDriveFile().getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                                @Override
                                                public void onResult(@NonNull DriveResource.MetadataResult metadataResult) {
                                                    if(metadataResult != null && metadataResult.getMetadata() != null) {
                                                /* Add meta data to quick lookup hashmap. */
                                                        metaFileMap.put(DataUtil.getOrgFileName(org), metadataResult.getMetadata());
                                                        listener.onResponse(true);
                                                    } else {
                                                        listener.onResponse(false);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                }
            });
    }
}
