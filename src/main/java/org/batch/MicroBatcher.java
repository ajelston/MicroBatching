package org.batch;

import java.util.concurrent.CompletableFuture;

/**
 * MicroBatch interface.
 *
 * Allows submission of jobs and provides a shutdown mechanism.
 *
 * @param <TInput> Type parameter for job inputs.
 * @param <TOutput> Type parameter for job outputs.
 */
public interface MicroBatcher<TInput, TOutput> {
    /**
     * Submit a job to be processed at a future time.
     *
     * @param job Input job to be processed.
     * @return A {@link CompletableFuture} that signals when the job
     *         has been processed.
     */
    CompletableFuture<JobResult<TOutput>> submit(Job<TInput> job);

    /**
     * Blocks until all previously submitted jobs have been processed.
     *
     * When called, will prevent submission of any other jobs.
     */
    void shutdown();
}
