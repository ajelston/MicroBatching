package org.batch.mb;

import org.batch.Job;

/**
 * {@link Job} implementation that allows providing an input value.
 * @param <T> Type parameter for job inputs.
 */
public class MBJob<T> implements Job<T> {
    private T input;

    public T getInput() {
        return input;
    }

    public void setInput(T input) {
        this.input = input;
    }
}
