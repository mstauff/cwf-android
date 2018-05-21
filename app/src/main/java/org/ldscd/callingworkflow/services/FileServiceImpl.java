package org.ldscd.callingworkflow.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;

import org.ldscd.callingworkflow.BuildConfig;
import org.ldscd.callingworkflow.utils.SecurityUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileServiceImpl implements FileService {

    /* Member Assignments */
    @Override
    public Task<String> GetCachedMemberAssignments(Context context) {
        int timer = BuildConfig.CacheTimer;
        return null;
    }

    @Override
    public Task<Boolean> SaveMemberAssignmentsToCache(Context context, String assignments) {
        int timer = BuildConfig.CacheTimer;
        return null;
    }

    /* File CRUD core methods. */
    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(SecurityUtil.encrypt(context, data));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {
        String value = "";
        try {
            InputStream inputStream = context.openFileInput("config.txt");
            File file = context.getFileStreamPath("config.txt");

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
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return SecurityUtil.decrypt(context, value);
    }
}