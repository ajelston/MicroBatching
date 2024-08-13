package org.batch.impl;

import org.batch.JobResult;

public class MBJobResult<T> implements JobResult<T> {
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
        return null;
    }
}
