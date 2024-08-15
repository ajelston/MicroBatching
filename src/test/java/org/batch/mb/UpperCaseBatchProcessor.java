package org.batch.mb;

import org.batch.BatchProcessor;
import org.batch.Job;
import org.batch.JobResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Test BatchProcessor only, not intended for actual use.
 */
public class UpperCaseBatchProcessor implements BatchProcessor<String, String> {
    public static final String ERROR_MESSAGE = "Error message";
    public static final String EXCEPTION_MESSAGE = "Exception message";

    private static final String ERROR_INPUT = "error";
    private static final String EXCEPTION_INPUT = "exception";

    @Override
    public List<JobResult<String>> process(List<Job<String>> jobs) {
        return jobs.stream()
                .map(UpperCaseBatchProcessor::getJobResult)
                .collect(Collectors.toList());
    }

    private static MBJobResult<String> getJobResult(Job<String> job) {
        String input = job.getInput();

        if (ERROR_INPUT.equals(input)) {
            MBJobResult<String> result = new MBJobResult<>();
            result.setSuccess(false);
            result.setErrorMessage(ERROR_MESSAGE);
            return result;
        }

        if (EXCEPTION_INPUT.equals(input)) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }

        var output = input == null ? "" : input.toUpperCase();
        MBJobResult<String> result = new MBJobResult<>();
        result.setSuccess(true);
        result.setResult(output);
        return result;
    }
}
