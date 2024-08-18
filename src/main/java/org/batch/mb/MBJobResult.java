package org.batch.mb;

import org.batch.JobResult;

import java.util.Optional;

/**
 * {@link JobResult} implementation that allows specifying
 * a result value, error status, and exception status.
 *
 * Setting an exception status will automatically set the
 * error message as the exception message.
 *
 * @param <T> Type parameter for job output.
 */
public class MBJobResult<T> implements JobResult<T> {

    private boolean success;
    private String errorMessage;
    private T result;
    private Exception exception;

    @Override
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public void setException(Exception e) {
        setSuccess(false);
        setErrorMessage(e.getMessage());
        this.exception = e;
    }
}
