package org.batch;

// Should this be an interface or a class?
public interface JobResult<T> {
    boolean isSuccess();
    String getErrorMessage();
    T getResult();
}
