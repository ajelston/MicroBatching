package org.batch.mb;

import org.batch.JobResult;

import java.util.Optional;

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

    @Override
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public void setException(Exception e) {
        setSuccess(false);
        setErrorMessage(e.getMessage());
        this.exception = e;
    }

    public void setResult(T result) {
        this.result = result;
    }


}
