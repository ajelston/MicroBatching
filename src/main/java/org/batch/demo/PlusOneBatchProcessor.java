package org.batch.demo;

import org.batch.BatchProcessor;
import org.batch.Job;
import org.batch.JobResult;
import org.batch.mb.MBJobResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A demonstration BatchProcessor that adds one to Job input.
 */
public class PlusOneBatchProcessor implements BatchProcessor<Integer, Integer> {
    @Override
    public List<JobResult<Integer>> process(List<Job<Integer>> jobs) {
        return jobs.stream()
                .map(job -> {
                    MBJobResult<Integer> result = new MBJobResult<>();
                    result.setResult(job.getInput() + 1);
                    return result;
                }).collect(Collectors.toList());
    }
}
