package org.ldscd.callingworkflow.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.ldscd.callingworkflow.BuildConfig;
import org.ldscd.callingworkflow.model.serialize.LocalCache;
import org.ldscd.callingworkflow.utils.SecurityUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class FileServiceImpl implements FileService {
    private static final String CONFIG_TXT = "config.txt";
    private Context context;
    private Map<Long, List<Long>> cachedMemberAssignments;

    public FileServiceImpl(Context context) {
        this.context = context;
    }
    /* Member Assignments */
    @Override
    public boolean isCachedMemberAssignmentsExpired() {
        LocalCache cache = getFile();
        boolean expired;
        if(cache == null || cache.getMemberAssignmentExpirationDate() == null) {
            expired = true;
        }else {
            expired = cache.getMemberAssignmentExpirationDate() < DateTime.now().getMillis();
        }
        return expired;
    }
    @Override
    public Map<Long, List<Long>> getCachedMemberAssignments() {
        if(cachedMemberAssignments == null) {
            LocalCache cache = getFile();
            cachedMemberAssignments = cache != null ? cache.getMemberAssignmentMap() : null;
        }
        return cachedMemberAssignments;
    }

    @Override
    public Task<Boolean> saveMemberAssignmentsToCache(Map<Long, List<Long>> memberAssignments) {
        int timer = BuildConfig.CacheTimer;
        LocalCache cache = getFile();
        if(cache == null) {
            cache = new LocalCache();
        }
        cache.setMemberAssignmentExpirationDate(DateTime.now().plusMinutes(timer).getMillis());
        cache.setMemberAssignmentMap(memberAssignments);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        String flattenedJson = gsonBuilder.create().toJson(cache, LocalCache.class);
        return saveFile(context, flattenedJson);
    }

    /* File CRUD core methods. */
    private LocalCache getFile() {
        String value = "";
        try {
            File file = context.getFileStreamPath(CONFIG_TXT);
            if(file.exists()) {
                InputStream inputStream = context.openFileInput(CONFIG_TXT);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    value = stringBuilder.toString();
                }
            } else {
                FileOutputStream fos = context.openFileOutput(CONFIG_TXT, Context.MODE_PRIVATE);
                LocalCache cache = new LocalCache();
                value = new GsonBuilder().create().toJson(cache, LocalCache.class);
                fos.write(SecurityUtil.encrypt(context, value).getBytes());
                fos.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return new GsonBuilder().create().fromJson(SecurityUtil.decrypt(context, value), LocalCache.class);
    }

    private Task<Boolean> saveFile(Context context, String data) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(CONFIG_TXT, Context.MODE_PRIVATE));
            outputStreamWriter.write(SecurityUtil.encrypt(context, data));
            outputStreamWriter.close();
            taskCompletionSource.setResult(true);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            taskCompletionSource.setException(new Exception("Failed to save local cache file."));
        }
        return taskCompletionSource.getTask();
    }
}