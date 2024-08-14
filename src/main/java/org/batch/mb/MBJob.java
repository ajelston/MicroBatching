package org.batch.mb;

import org.batch.Job;

public class MBJob<T> implements Job<T> {
    private T input;

    public T getInput() {
        return input;
    }

    public void setInput(T input) {
        this.input = input;
    }
}
