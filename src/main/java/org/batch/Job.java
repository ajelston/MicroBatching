package org.batch;

/**
 * Interface holding input values for jobs.
 *
 * Clients of the library are expected to implement
 * this interface according to their needs.
 *
 * A generic value-holder implementation is available
 * in {@link org.batch.mb.MBJob} if desired.
 *
 * @param <T> Type parameter for job input.
 */
public interface Job<T> {
    /**
     * @return the input object for the job.
     */
    T getInput();
}
