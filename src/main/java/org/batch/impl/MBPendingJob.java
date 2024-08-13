package org.batch.impl;

import org.batch.Job;
import org.batch.JobResult;

import java.util.concurrent.CompletableFuture;

public class MBPendingJob<T> {
    private final Job<T> job;
    private final CompletableFuture<JobResult<T>> jobResult;

    public MBPendingJob(Job<T> job) {
        this.job = job;
        this.jobResult = new CompletableFuture<>();
    }

    public Job<T> getJob() {
        return job;
    }

    public CompletableFuture<JobResult<T>> getJobResult() {
        return jobResult;
    }
}
