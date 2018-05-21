package org.ldscd.callingworkflow.services;

import android.content.Context;
import com.google.android.gms.tasks.Task;

public interface FileService {
    Task<String> GetCachedMemberAssignments(Context context);
    Task<Boolean> SaveMemberAssignmentsToCache(Context context, String assignments);
}