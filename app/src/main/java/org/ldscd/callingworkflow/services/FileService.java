package org.ldscd.callingworkflow.services;

import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.Map;

public interface FileService {
    /* Member Assignments */
    boolean isCachedMemberAssignmentsExpired();
    Map<Long, List<Long>> getCachedMemberAssignments();
    Task<Boolean> saveMemberAssignmentsToCache(Map<Long, List<Long>> memberAssignments);
}