package org.batch;

import java.util.concurrent.CompletableFuture;

public interface MicroBatcher<TInput, TOutput> {
    /**
     *
     * @param job
     * @return
     */
    CompletableFuture<JobResult<TOutput>> submit(Job<TInput> job);

    /**
     * Block until all previously submitted jobs have
     * been processed.
     */
    void shutdown();
}
