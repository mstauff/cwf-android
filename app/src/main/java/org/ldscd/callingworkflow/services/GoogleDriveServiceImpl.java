package org.ldscd.callingworkflow.services;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDriveServiceImpl implements GoogleDriveService {
    /** Constructor */
    public GoogleDriveServiceImpl() { }

    /** Fields */
    protected Activity activity;
    private UnitSettings unitSettings;
    private Map<String, DriveId> metaDriveMap;
    /* Name of the service being used. */
    private String TAG = "GoogleDataService";
    /* Handles high-level drive functions like sync. */
    private DriveClient mDriveClient;
    /* Handle access to Drive resources/files. */
    private DriveResourceClient mDriveResourceClient;
    /* Sign-in account. */
    private GoogleSignInAccount signInAccount;

    /** Properties */
    private static GoogleSignInOptions googleSignInOptions;
    static {
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Drive.SCOPE_FILE)
            .requestScopes(Drive.SCOPE_APPFOLDER)
            .build();
    }

    /** Methods */
    /* Initialize the client to interact with Google Drive. */
    private DriveClient getDriveClient() {
        return mDriveClient;
    }
    private DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }
    /* Continues the sign-in process, initializing the Drive clients with the current user's account. */
    private void initializeDriveClient(Context context) {
        mDriveClient = Drive.getDriveClient(context, signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
    }

    @Override
    public boolean isAuthenticated(Context context) {
        signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        return signInAccount != null && GoogleSignIn.hasPermissions(signInAccount, googleSignInOptions.getScopeArray());
    }
    /** Initial Entry Point */
    @Override
    public Task<Boolean> init(Activity activity) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        this.activity = activity;
        metaDriveMap = new HashMap<>();
        this.unitSettings = null;

        if(isAuthenticated(activity.getApplicationContext())) {
            initializeDriveClient(activity.getApplicationContext());
            taskCompletionSource.setResult(true);
        } else {
            taskCompletionSource.setResult(false);
            taskCompletionSource.setException(new Exception("not connected to google drive"));
        }

        return taskCompletionSource.getTask();
    }

    /** CRUD Methods for Org(s) in Google Drive */
    @Override
    public Task<Org> getOrgData(final Org org) {
        final TaskCompletionSource<Org> taskCompletionSource = new TaskCompletionSource<>();
        if(org != null) {
            getFileContent(metaDriveMap.get(DataUtil.getOrgFileName(org)).asDriveFile())
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> orgContentTask) {
                        String orgStr = orgContentTask.getResult();
                         /* If Org exist in google drive return it. */
                        if(orgStr != null) {
                            /* If org does not exist create the org file */
                            OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
                            Org newOrg = null;
                            try {
                                JSONObject jsonObject = new JSONObject(orgStr);
                                newOrg = orgCallingBuilder.extractOrg(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            taskCompletionSource.setResult(newOrg);
                        } else {
                            final Org newOrg = new Org(org.getUnitNumber(), org.getId(), org.getDefaultOrgName(), org.getOrgTypeId(),
                                    org.getDisplayOrder(), org.getChildren(), org.getCallings());
                            createOrgFile(org).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull Task<Boolean> task) {
                                    if(task.getResult()) {
                                        taskCompletionSource.setResult(newOrg);
                                    } else {
                                        taskCompletionSource.setResult(null);
                                    }
                                }
                            });
                        }
                    }
                });
        } else {
            taskCompletionSource.setResult(null);
        }
        return taskCompletionSource.getTask();
    }
    @Override
    public Task<List<Org>> getOrgs() {
        final TaskCompletionSource<List<Org>> taskCompletionSource = new TaskCompletionSource<>();
        final List<Task<Metadata>> metadataTasks = new ArrayList<>(metaDriveMap.size());
        List<DriveId> driveIds = new ArrayList<>();
        for(String key : metaDriveMap.keySet()) {
            if(!key.toLowerCase().contains("config")) {
                driveIds.add(metaDriveMap.get(key));
            }
        }
        for(DriveId driveId : driveIds) {
            metadataTasks.add(getFileMetaData(driveId.asDriveFile()));
        }
        Tasks.whenAll(metadataTasks)
            .continueWithTask(new Continuation<Void, Task<List<String>>>() {
                @Override
                public Task<List<String>> then(@NonNull Task<Void> task) throws Exception {
                    return getContentForFiles(metadataTasks);
                }
            })
            /* When the metadata has been retrieved add it to the Map<Metadata> metaFileMap. */
           .continueWithTask(new Continuation<List<String>, Task<List<Org>>>() {
                @Override
                public Task<List<Org>> then(@NonNull Task<List<String>> task) throws Exception {
                    List<String> contentList = task.getResult();
                    /* Convert the json string into an Org.  Then inject the org into the callback. */
                    OrgCallingBuilder orgCallingBuilder = new OrgCallingBuilder();
                    JSONObject jsonObject;
                    List<Org> orgs = new ArrayList<>(task.getResult().size());
                    for(String cwStr : contentList) {
                        try {
                            jsonObject = new JSONObject(cwStr);
                            orgs.add(orgCallingBuilder.extractOrg(jsonObject));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    TaskCompletionSource<List<Org>> orgListTask = new TaskCompletionSource<>();
                    orgListTask.setResult(orgs);
                    return orgListTask.getTask();
                }
            })
            .addOnSuccessListener(new OnSuccessListener<List<Org>>() {
                @Override
                public void onSuccess(List<Org> orgs) {
                    taskCompletionSource.setResult(orgs);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskCompletionSource.setResult(null);
                    taskCompletionSource.setException(e);
                }
        });

        return taskCompletionSource.getTask();

    }
    /* Retrieves the file content based on the metadata passed in. */
    private Task<List<String>> getContentForFiles(List<Task<Metadata>> metadataTasks) {
        final TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();
        if(metadataTasks != null) {
            final List<Task<String>> fileContentTasks = new ArrayList<>(metadataTasks.size());
            for(Task<Metadata> metadataTask : metadataTasks) {
                fileContentTasks.add(getFileContent(metadataTask.getResult().getDriveId().asDriveFile()));
            }
            Tasks.whenAll(fileContentTasks)
                    .continueWithTask(new Continuation<Void, Task<List<String>>>() {
                        @Override
                        public Task<List<String>> then(@NonNull Task<Void> task) throws Exception {
                            List<String> orgListStrings = new ArrayList<>(fileContentTasks.size());
                            for(Task<String> contentTask : fileContentTasks) {
                                String content = contentTask.getResult();
                                if(content != null && content.length() > 0) {
                                    orgListStrings.add(contentTask.getResult());
                                }
                            }
                            TaskCompletionSource<List<String>> contentTask = new TaskCompletionSource<>();
                            contentTask.setResult(orgListStrings);
                            return contentTask.getTask();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<List<String>>() {
                        @Override
                        public void onSuccess(List<String> stringList) {
                            taskCompletionSource.setResult(stringList);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            taskCompletionSource.setResult(new ArrayList<String>());
                            taskCompletionSource.setException(e);
                        }
                    });
        }
        return taskCompletionSource.getTask();
    }
    @Override
    public Task<Boolean> saveOrgFile(Org org) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
         /* Create Gson object with custom formatting on the Calling to flatten the position object. */
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(Calling.class, new PositionSerializer());
        String flattenedJson = gsonBuilder.create().toJson(org, Org.class);
        if(flattenedJson != null && flattenedJson.length() > 0) {
            DriveId driveId = metaDriveMap.get(DataUtil.getOrgFileName(org));
            /* If the file already exists then update it in google drive. */
            if(driveId != null) {
                updateFileContent(driveId.asDriveFile(), flattenedJson)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                taskCompletionSource.setResult(aBoolean);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                taskCompletionSource.setResult(false);
                                taskCompletionSource.setException(e);
                            }
                        });
            } else {
                /* If the file doesn't exist then create it in google drive. */
                createOrgFile(org)
                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            taskCompletionSource.setResult(aBoolean);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            taskCompletionSource.setResult(false);
                            taskCompletionSource.setException(e);
                        }
                    });
            }
        }
        return taskCompletionSource.getTask();
    }
    @Override
    public Task<Boolean> deleteOrgs(List<Org> orgs) {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        Collection<Task<Boolean>> tasks = new ArrayList<>(orgs.size());
        for(Org org : orgs) {
            tasks.add(deleteOrg(org));
        }
        Tasks.whenAllComplete(tasks)
            .addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                @Override
                public void onSuccess(List<Task<?>> tasks) {
                    taskCompletionSource.setResult(true);
                    /* Get results individually.  If any failed get one result and exception and return. */
                    for(Task task : tasks) {
                        if(!task.isSuccessful()) {
                            taskCompletionSource.setResult(false);
                            taskCompletionSource.setException(new Exception(task.getException()));
                            break;
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskCompletionSource.setResult(false);
                    taskCompletionSource.setException(e);
                }
            });
        return taskCompletionSource.getTask();
    }
    /* Delete a single Org file in google drive.  This will remove the file completely. */
    private Task<Boolean> deleteOrg(Org org) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        DriveId driveId = metaDriveMap.get(DataUtil.getOrgFileName(org));
        if(driveId != null) {
            deleteDriveFile(driveId.asDriveFile())
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        taskCompletionSource.setResult(task.getResult());
                        if(task.getException() != null) {
                            taskCompletionSource.setException(task.getException());
                        }
                    }
                });
        }
        return taskCompletionSource.getTask();
    }
    /* A method that captures several Orgs and pushes them to Google Drive's AppFolder for creation. */
    private Task<Boolean> createOrgFiles(final List<Org> orgs) {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        final List<Task<Boolean>> tasks = new ArrayList<>(orgs.size());
        for(Org org : orgs) {
            tasks.add(createOrgFile(org));
        }
        Tasks.whenAll(tasks)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    taskCompletionSource.setResult(true);
                     /* Get results individually.  If any failed get one result and exception and return. */
                    for(Task task : tasks) {
                        if(!task.isSuccessful()) {
                            taskCompletionSource.setResult(false);
                            taskCompletionSource.setException(new Exception(task.getException()));
                            break;
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskCompletionSource.setResult(false);
                    taskCompletionSource.setException(e);
                }
            });
        return taskCompletionSource.getTask();
    }
    /* Create a single Org file in the AppData Folder. */
    private Task<Boolean> createOrgFile(final Org org) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        /* Meta data for the file to be saved. */
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(DataUtil.getOrgFileName(org))
                .setMimeType("application/json")
                .build();
        /* Create Gson object with custom formatting on the Calling to flatten the position object. */
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(Calling.class, new PositionSerializer());
        String flattenedJson = gsonBuilder.create().toJson(org, Org.class);
        if(flattenedJson != null && flattenedJson.length() > 0) {
            createFile(changeSet, flattenedJson)
                .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        getMetadataFromDriveFile(driveFile)
                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    taskCompletionSource.setResult(aBoolean);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    taskCompletionSource.setResult(false);
                                    taskCompletionSource.setException(e);
                                }
                            });

                }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        taskCompletionSource.setResult(false);
                        taskCompletionSource.setException(e);
                    }
                });
        } else {
            taskCompletionSource.setResult(false);
            taskCompletionSource.setException(new Exception("Org data was null or empty.  Data is required."));
        }
        return taskCompletionSource.getTask();
    }

    private Task<Boolean> getMetadataFromDriveFile(DriveFile driveFile) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        getFileMetaData(driveFile)
            .addOnSuccessListener(new OnSuccessListener<Metadata>() {
                @Override
                public void onSuccess(Metadata metadata) {
                    if (metadata != null) {
                        /* Add meta data to quick lookup hashmap. */
                        metaDriveMap.put(metadata.getTitle(), metadata.getDriveId());
                        taskCompletionSource.setResult(true);
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskCompletionSource.setResult(false);
                    taskCompletionSource.trySetException(e);
                }
            });
        return taskCompletionSource.getTask();
    }

    /** Hydrates all metaFile cache in local device from google drive's AppFolder. */
    @Override
    public Task<Boolean> syncDriveIds(final List<Org> orgList) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        getAppDataFiles()
            .addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                @Override
                public void onSuccess(MetadataBuffer metadataBuffer) {
                    if(metadataBuffer != null && metadataBuffer.getCount() > 0) {
                        boolean hasUnitSettings = false;
                        metaDriveMap = new HashMap<>();
                        Map<String, Org> lcrOrgNames = new HashMap<>();
                        for(Org org : orgList) {
                            lcrOrgNames.put(DataUtil.getOrgFileName(org), org);
                        }
                        for (Metadata metadata : metadataBuffer) {
                            if (metadata.getFileExtension().equals("json")) {
                                /* Check to see if there are duplicate files.  If so, remove
                                    the file that is newest and keep the oldest file.
                                */
                                if(lcrOrgNames.containsKey(metadata.getTitle())) {
                                    DriveId existingDriveId = metaDriveMap.get(metadata.getTitle());
                                    if (existingDriveId != null) {
                                        metaDriveMap.put(metadata.getTitle(), metadata.getDriveId());
                                        deleteDriveFile(existingDriveId.asDriveFile());

                                    } else {
                                        metaDriveMap.put(metadata.getTitle(), metadata.getDriveId());
                                    }
                                }
                            } else if(metadata.getFileExtension().equals("config")) {
                                if(!hasUnitSettings && metadata.getTitle().equals(DataUtil.getUnitFileName(orgList.get(0).getUnitNumber()))) {
                                    /* Save the UnitSettings file meta to the metaFileMap. */
                                    DriveId existingSettingsDriveId = metaDriveMap.get(metadata.getTitle());
                                    if (existingSettingsDriveId != null) {
                                        metaDriveMap.put(metadata.getTitle(), metadata.getDriveId());
                                        deleteDriveFile(metadata.getDriveId().asDriveFile());
                                    } else {
                                        metaDriveMap.put(DataUtil.getUnitFileName(orgList.get(0).getUnitNumber()), metadata.getDriveId());
                                    }
                                    hasUnitSettings = true;
                                }
                            }
                        }
                        metadataBuffer.release();
                        createOneOffFiles(orgList, hasUnitSettings)
                            .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull Task<Boolean> task) {
                                    taskCompletionSource.setResult(task.getResult());
                                }
                            });

                    } else if(orgList != null && orgList.size() > 0) {
                        /* Create Org files because none exist */
                        createOrgFiles(orgList)
                            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    taskCompletionSource.setResult(result);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    taskCompletionSource.setResult(false);
                                    taskCompletionSource.trySetException(e);
                                }
                            });

                    } else {
                        taskCompletionSource.setResult(false);
                        taskCompletionSource.trySetException(new Exception("There wasn't any meta data returned to sync."));
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    taskCompletionSource.setResult(false);
                    taskCompletionSource.setException(e);
                }
            });
        return taskCompletionSource.getTask();
    }

    private Task<Boolean> createOneOffFiles(List<Org> orgList, Boolean hasUnitSettings)
    {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        /* Possible orgs that need to be created */
        if (orgList.size() > 0 || !hasUnitSettings) {
            List<Org> itemsToCreate = new ArrayList<>();
            for (Org org : orgList) {
                if (org.getOrgTypeId() > 0 && !metaDriveMap.containsKey(DataUtil.getOrgFileName(org))) {
                    itemsToCreate.add(org);
                }
            }
            /* Create a Task for the Org creation.*/
            final TaskCompletionSource<Boolean> orgsTask = new TaskCompletionSource<>();
            if(!itemsToCreate.isEmpty()) {
                createOrgFiles(itemsToCreate)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                orgsTask.setResult(result);
                            }
                        });
            } else {
                orgsTask.setResult(true);
            }
            /* Create a Task for the UnitSettings creation. */
            final TaskCompletionSource<Boolean> unitSettingTask = new TaskCompletionSource();
            if(!hasUnitSettings) {
                createUnitSettingsFile(new UnitSettings(orgList.get(0).getUnitNumber(), new ArrayList<CallingStatus>()))
                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            unitSettingTask.setResult(task.getResult());
                        }
                    });
            } else {
                unitSettingTask.setResult(true);
            }

            /* Evaluate both tasks and set the taskCompletionSource. */
            Tasks.whenAll(unitSettingTask.getTask(), orgsTask.getTask())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        taskCompletionSource.setResult(unitSettingTask.getTask().getResult() && orgsTask.getTask().getResult());
                    }
                });
        } else {
            taskCompletionSource.setResult(true);
        }
        return taskCompletionSource.getTask();
    }

    /** Unit Settings */
    @Override
    public Task<UnitSettings> getUnitSettings(final Long unitNumber) {
        final TaskCompletionSource<UnitSettings> taskCompletionSource = new TaskCompletionSource();
        if(unitSettings == null) {
            DriveId driveId = metaDriveMap.get(DataUtil.getUnitFileName(unitNumber));
            if(driveId != null) {
                getFileContent(driveId.asDriveFile())
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            Gson gson = new GsonBuilder().create();
                            taskCompletionSource.setResult(gson.fromJson(task.getResult(), UnitSettings.class));
                        }
                    });
            } else {
                /* If unit settings does not exist create the unit settings file */
                final UnitSettings tempSettings = new UnitSettings(unitNumber, new ArrayList<CallingStatus>());
                Task<Boolean> createUnitSettingsFileTask = createUnitSettingsFile(tempSettings);
                createUnitSettingsFileTask.addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.getResult()) {
                            unitSettings = tempSettings;
                            taskCompletionSource.setResult(unitSettings);
                        } else {
                            taskCompletionSource.setResult(null);
                        }
                    }
                });
            }
        } else {
            taskCompletionSource.setResult(unitSettings);
        }
        return taskCompletionSource.getTask();
    }
    @Override
    public Task<Boolean> saveUnitSettings(UnitSettings unitSettings) {
        this.unitSettings = unitSettings;
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        /* Create Gson object to flatten the Unit Settings object. */
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        String flattenedJson = gsonBuilder.create().toJson(unitSettings, UnitSettings.class);
        if(flattenedJson != null && flattenedJson.length() > 0) {
            DriveId driveId = metaDriveMap.get(DataUtil.getUnitFileName(unitSettings.getUnitNumber()));
            updateFileContent(driveId.asDriveFile(), flattenedJson)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        taskCompletionSource.setResult(aBoolean);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        taskCompletionSource.setResult(false);
                        taskCompletionSource.setException(e);
                    }
                });
        }
        return taskCompletionSource.getTask();
    }
    /* Creates the Unit Settings Json file and saves it in goolge drive AppData. */
    private Task<Boolean> createUnitSettingsFile(final UnitSettings unitSettings) {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        this.unitSettings = unitSettings;
        /* Meta data for the file to be saved. */
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(DataUtil.getUnitFileName(unitSettings.getUnitNumber()))
                .setMimeType("text/json")
                .build();
        /* Create Gson object to flatten the Unit Settings object. */
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        String flattenedJson = gsonBuilder.create().toJson(unitSettings, UnitSettings.class);
        if(flattenedJson != null && flattenedJson.length() > 0) {
            /* Create the UnitSettings file. */
            createFile(changeSet, flattenedJson)
                /* Retrieve the Metadata for the the DriveFile newly created file. */
                .continueWithTask(new Continuation<DriveFile, Task<Metadata>>() {
                    @Override
                    public Task<Metadata> then(@NonNull Task<DriveFile> task) throws Exception {
                        return getFileMetaData(task.getResult());
                    }
                })
                 /* When the metadata has been retrieved add it to the Map<Metadata> metaFileMap. */
                .continueWithTask(new Continuation<Metadata, Task<Boolean>>() {
                    @Override
                    public Task<Boolean> then(@NonNull Task<Metadata> task) throws Exception {
                        Metadata metadata = task.getResult();
                        TaskCompletionSource metadataTask = new TaskCompletionSource<Boolean>();
                        if (metadata != null) {
                            /* Add meta data to quick lookup hashmap. */
                            metaDriveMap.put(DataUtil.getUnitFileName(unitSettings.getUnitNumber()), metadata.getDriveId());
                            metadataTask.setResult(true);
                        } else {
                            metadataTask.setResult(false);
                            metadataTask.trySetException(new Exception("No Meta data for unitSettings was returned."));
                        }
                        return metadataTask.getTask();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        taskCompletionSource.setResult(aBoolean);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        taskCompletionSource.setResult(false);
                        taskCompletionSource.setException(e);
                    }
                });
        } else {
            taskCompletionSource.setResult(false);
            taskCompletionSource.setException(new Exception("UnitSettings data was null or empty.  Data is required."));
        }
        return taskCompletionSource.getTask();
    }

    /** Google Drive DAL methods */
    /* Changes made to a file are saved to the specified file in google drive. */
    private Task<Boolean> updateFileContent(DriveFile file, final String content) {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        /* Open file to write to */
        Task<DriveContents> openTask = getDriveResourceClient().openFile(file, DriveFile.MODE_WRITE_ONLY);
       /* Re-write or over-write the content in the file */
        openTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents driveContents = task.getResult();
                if(content != null && content.length() > 0) {
                    try {
                        /* Write content to DriveContents. */
                        OutputStream outputStream = driveContents.getOutputStream();
                        outputStream.write(content.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                /* Commit content */
                return getDriveResourceClient().commitContents(driveContents, null);
            }
        })
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                taskCompletionSource.setResult(true);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to update contents", e);
                taskCompletionSource.setResult(false);
                taskCompletionSource.setException(e);
            }
        });
        return taskCompletionSource.getTask();
    }
    /* All files needing to be created will use this method. */
    private Task<DriveFile> createFile(final MetadataChangeSet changeSet, final String content) {
        final TaskCompletionSource<DriveFile> taskCompletionSource = new TaskCompletionSource<>();
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
            .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                @Override
                public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                    DriveFolder parent = appFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write(content);
                    }

                    return getDriveResourceClient().createFile(parent, changeSet, contents);
                }
            })
            .addOnSuccessListener(
                    new OnSuccessListener<DriveFile>() {
                        @Override
                        public void onSuccess(DriveFile driveFile) {
                            taskCompletionSource.setResult(driveFile);
                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Unable to create file: " + changeSet.getTitle(), e);
                    taskCompletionSource.setResult(null);
                    taskCompletionSource.setException(e);
                }
            });

        return taskCompletionSource.getTask();
    }
    /* All file content retrieved will go through this method. */
    private Task<String> getFileContent(final DriveFile file) {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        /* Open file */
        Task<DriveContents> openFileTask = getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
        /* Begin reading file */
        openFileTask
                .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                        DriveContents contents = task.getResult();
                        /* Start to read as string */
                        try  {
                            InputStream inputStream = contents.getInputStream();
                            /* Convert the stream into a string for usability. */
                            taskCompletionSource.setResult(ConflictUtil.getStringFromInputStream(inputStream));
                        } catch(Exception e) {
                            Log.e(TAG, "Unable to parse contents for: " + file.getDriveId(), e);
                            taskCompletionSource.setResult(null);
                            taskCompletionSource.setException(e);
                        }
                        /* End reading and begin discarding content.  Must discard or commit from the open call. */
                        Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
                        return discardTask;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to retrieve contents for: " + file.getDriveId(), e);
                        taskCompletionSource.setResult(null);
                        taskCompletionSource.setException(e);
                    }
                });
        return taskCompletionSource.getTask();
    }
    /* All retrieval of metaData for a specified file wil go through this method. */
    private Task<Metadata> getFileMetaData(final DriveFile file) {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        //TODO: Possibly add resync
        Task<Metadata> getMetadataTask = getDriveResourceClient().getMetadata(file);
        getMetadataTask
                .addOnSuccessListener(
                        new OnSuccessListener<Metadata>() {
                            @Override
                            public void onSuccess(Metadata metadata) {
                                taskCompletionSource.setResult(metadata);
                            }
                        })
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to retrieve metadata", e);
                        taskCompletionSource.setResult(null);
                        taskCompletionSource.trySetException(e);
                    }
                });
        return taskCompletionSource.getTask();
    }
    /* Retrieves the AppData File MetaData in the AppData folder. */
    private Task<MetadataBuffer> getAppDataFiles() {
        final TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        getDriveClient().requestSync()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getDriveResourceClient().getAppFolder()
                        .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                            @Override
                            public Task<DriveFolder> then(@NonNull Task<DriveFolder> task) throws Exception {
                                return task;
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder appFolder) {
                                Task<MetadataBuffer> queryTask = getDriveResourceClient().listChildren(appFolder);
                                queryTask.addOnSuccessListener(
                                        new OnSuccessListener<MetadataBuffer>() {
                                            @Override
                                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                                taskCompletionSource.setResult(metadataBuffer);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Unable to get files from the AppData folder.  " + e.toString());
                                                taskCompletionSource.setResult(null);
                                                taskCompletionSource.setException(e);
                                            }
                                        });
                            }
                        });
            }
        });

        return taskCompletionSource.getTask();
    }
    /* Deletes a specified File in Google Drive. */
    private Task<Boolean> deleteDriveFile(final DriveFile file) {
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        getDriveResourceClient().delete(file)
                .addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG, "File: " + file.getDriveId() + "was deleted");
                            taskCompletionSource.setResult(true);
                        }
                })
                .addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Unable to delete file", e);
                            taskCompletionSource.setResult(false);
                            taskCompletionSource.setException(e);
                        }
                });
        return taskCompletionSource.getTask();
    }

}