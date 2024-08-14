package org.batch.mb;

import org.batch.Job;
import org.batch.JobResult;

import java.util.concurrent.CompletableFuture;

public class MBPendingJob<TInput, TOutput> {
    private final Job<TInput> job;
    private final CompletableFuture<JobResult<TOutput>> jobResult;

    public MBPendingJob(Job<TInput> job) {
        this.job = job;
        this.jobResult = new CompletableFuture<>();
    }

    public Job<TInput> getJob() {
        return job;
    }

    public CompletableFuture<JobResult<TOutput>> getJobResult() {
        return jobResult;
    }
}
