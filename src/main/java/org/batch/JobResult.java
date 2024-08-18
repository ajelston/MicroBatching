package org.batch;

import java.util.Optional;

/**
 * Interface holding output values for jobs, including
 * whether the job was processed successfully, any error
 * message or exception object, and a result value if
 * available.
 *
 * Clients of this library are expected to implement
 * this interface according to their needs. A simple
 * implementation is provided in {@link org.batch.mb.MBJobResult}
 * if desired.
 *
 * @param <T> Type parameter for job output.
 */
public interface JobResult<T> {
    /**
     * Should return true if the job was successfully processed
     * and holds a result value.
     */
    boolean isSuccess();

    /**
     * Should return any appropriate error message.
     */
    String getErrorMessage();

    /**
     * Should return any appropriate output value for the job.
     */
    T getResult();

    /**
     * Should return any relevant exception object.
     *
     * If the {@link BatchProcessor} throws an exception, that
     * batch will return {@link org.batch.mb.MBJobResult} objects
     * instead with the thrown exception returned here.
     */
    Optional<Exception> getException();
}
