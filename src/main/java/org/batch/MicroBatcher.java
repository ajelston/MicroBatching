package org.batch;

import java.util.concurrent.CompletableFuture;

public interface MicroBatcher<T> {
    /**
     *
     * @param job
     * @return
     */
    CompletableFuture<JobResult<T>> submit(Job<T> job);

    /**
     * Block until all previously submitted jobs have
     * been processed.
     */
    void shutdown();
}
