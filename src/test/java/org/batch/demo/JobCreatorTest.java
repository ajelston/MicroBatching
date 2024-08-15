package org.batch.demo;

import org.batch.Job;
import org.batch.JobResult;
import org.batch.MicroBatcher;
import org.batch.mb.MBJobResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobCreatorTest {

    @Test
    public void call_submitsJobsToMicroBatcher() {
        int threadNumber = 3;
        int numJobs = 10;
        TestBatcher testBatcher = new TestBatcher();
        JobCreator jobCreator = new JobCreator(threadNumber, numJobs, testBatcher);
        int result = jobCreator.call();

        assertEquals(threadNumber, result);
        assertEquals(numJobs, testBatcher.submittedJobs.size());
    }

    public class TestBatcher implements MicroBatcher<Integer, Integer> {

        public boolean shutdownCalled = false;
        public List<Job<Integer>> submittedJobs = new ArrayList<>();

        @Override
        public CompletableFuture<JobResult<Integer>> submit(Job<Integer> job) {
            submittedJobs.add(job);
            MBJobResult<Integer> jobResult = new MBJobResult<>();
            jobResult.setResult(1);
            return CompletableFuture.completedFuture(jobResult);
        }

        @Override
        public void shutdown() {
            shutdownCalled = true;
        }
    }
}
