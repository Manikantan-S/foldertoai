package com.foldertoai.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobManager {
    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }
    public static class JobInfo {
        public Status status;
        public String message;
        public String outputPath;
        public int filesProcessed;
        public String error;
    }

    private final Map<String, JobInfo> jobs = new ConcurrentHashMap<>();

    public String createJob() {
        String id = UUID.randomUUID().toString();
        JobInfo info = new JobInfo();
        info.status = Status.PENDING;
        info.message = "Job created";
        jobs.put(id, info);
        return id;
    }

    public JobInfo getJob(String id) {
        return jobs.get(id);
    }

    public void updateJob(String id, Status status, String message, String outputPath, int filesProcessed, String error) {
        JobInfo info = jobs.get(id);
        if (info != null) {
            info.status = status;
            info.message = message;
            info.outputPath = outputPath;
            info.filesProcessed = filesProcessed;
            info.error = error;
        }
    }
} 