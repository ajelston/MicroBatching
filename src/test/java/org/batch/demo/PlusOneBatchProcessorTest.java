package org.batch.demo;

import org.batch.Job;
import org.batch.JobResult;
import org.batch.mb.MBJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlusOneBatchProcessorTest {

    @Test
    public void process_addsOneToEachJobInput() {
        var processor = new PlusOneBatchProcessor();
        var jobs = makeJobs(1, 2, 3, 4, 5);

        Integer[] resultArray = new Integer[5];
        Integer[] expectedArray = new Integer[] {2, 3, 4, 5, 6};

        var results = processor.process(jobs).stream()
                .map(JobResult::getResult)
                .toList().toArray(resultArray);
        Assertions.assertArrayEquals(expectedArray, results);
    }

    private List<Job<Integer>> makeJobs(Integer... inputs) {
        return Arrays.stream(inputs).map(i -> {
            var job = new MBJob<Integer>();
            job.setInput(i);
            return job;
        }).collect(Collectors.toList());
    }
}
