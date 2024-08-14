package org.batch;

import org.batch.impl.MBJobResult;

import java.util.List;
import java.util.stream.Collectors;

public class UpperCaseBatchProcessor implements BatchProcessor<String, String> {
    @Override
    public List<JobResult<String>> process(List<Job<String>> jobs) {
        return jobs.stream()
                .map(job -> {
                    var input = job.getInput();
                    var output = input == null ? "" : input.toUpperCase();
                    MBJobResult<String> result = new MBJobResult<>();
                    result.setResult(output);
                    return result;
                }).collect(Collectors.toList());
    }
}
