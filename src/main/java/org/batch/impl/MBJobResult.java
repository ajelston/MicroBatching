package org.batch.impl;

import org.batch.JobResult;

public class MBJobResult<T> implements JobResult<T> {

    private T result;

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String getErrorMessage() {
        return "";
    }

    @Override
    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
