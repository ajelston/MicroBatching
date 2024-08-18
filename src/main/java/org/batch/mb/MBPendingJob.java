package org.batch.mb;

import org.batch.Job;
import org.batch.JobResult;

import java.util.concurrent.CompletableFuture;

/**
 * Couples a {@link Job} with a CompletableFuture for the
 * {@link JobResult}.
 *
 * The CompletableFuture is returned to the client to signal
 * when the job has been processed. {@link MBBatcher} will
 * complete the future with the JobResult after the Job has
 * been submitted to the {@link org.batch.BatchProcessor}.
 *
 * @param <TInput> Type parameter for job input.
 * @param <TOutput> Type parameter for job output.
 */
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
