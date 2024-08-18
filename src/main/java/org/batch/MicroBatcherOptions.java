package org.batch;

import java.time.Duration;

/**
 * Options for the {@link MicroBatcher} implementation.
 */
public interface MicroBatcherOptions {
    /**
     * Number of jobs to submit to {@link BatchProcessor} at a time.
     */
    int batchSize();

    /**
     * Maximum time {@link MicroBatcher} should wait for new jobs
     * to arrive before submitting a partial batch.
     */
    Duration timeout();
}
