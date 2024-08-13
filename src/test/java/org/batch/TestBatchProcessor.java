package org.batch;

import org.batch.impl.MBJobResult;

import java.util.List;
import java.util.stream.Collectors;

public class TestBatchProcessor<T> implements BatchProcessor<T> {
    @Override
    public List<JobResult<T>> process(List<Job<T>> jobs) {
        return jobs.stream()
                .map((job) -> new MBJobResult<T>())
                .collect(Collectors.toList());
    }
}
