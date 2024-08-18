package org.batch;

import java.util.List;

/**
 * Process a list of jobs and return job results for each one.
 *
 * Clients of the library are required to provide an implementation
 * of this interface according to their needs.
 *
 * @param <TInput> Type parameter for job input.
 * @param <TOutput> Type parameter for job output.
 */
public interface BatchProcessor<TInput, TOutput> {
    /**
     * Process a list of {@link Job}s and return a list of {@link JobResult}s.
     *
     * The output list of JobResults MUST match the input list of jobs in size
     * and order.
     */
    List<JobResult<TOutput>> process(List<Job<TInput>> jobs);
}
