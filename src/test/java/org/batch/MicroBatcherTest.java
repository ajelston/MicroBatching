package org.batch;

import org.batch.impl.MBBatcherOptions;
import org.batch.impl.MBJob;
import org.batch.impl.MBBatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class MicroBatcherTest {

    private static final int BATCH_SIZE = 5;
    private static final int TIMEOUT_MS = 50;

    private BatchProcessor<String> processor;
    private MicroBatcherOptions options;
    private MicroBatcher<String> batcher;

    @BeforeEach
    public void setUp() {
        processor = new TestBatchProcessor<>();
        options = new MBBatcherOptions.Builder()
                .withBatchSize(BATCH_SIZE)
                .withTimeout(Duration.ofMillis(TIMEOUT_MS))
                .build();
        batcher = new MBBatcher<>(options, processor);
    }

    @Test
    public void submit_returnsJobResultFuture() {
        var job = new MBJob<String>();

        var jobResult = batcher.submit(job);

        assertNotNull(jobResult);
        assertFalse(jobResult.isDone());
    }

    @Test
    public void submit_batchSizeSubmitted_batchProcessorInvoked() {
        List<Job<String>> jobs = makeJobs(BATCH_SIZE);

        List<CompletableFuture<JobResult<String>>> jobResults = jobs.stream()
                .map(job -> batcher.submit(job))
                .toList();

        assertEquals(BATCH_SIZE, jobResults.size());
        jobResults.forEach((jobResultFuture) -> assertTrue(jobResultFuture.isDone()));
    }

    @Test
    public void submit_timeoutReached_batchProcessorInvoked() {
        List<Job<String>> jobs = makeJobs(BATCH_SIZE - 1);

        List<CompletableFuture<JobResult<String>>> jobResults = jobs.stream()
                .map(job -> batcher.submit(job))
                .toList();

        sleep(TIMEOUT_MS + 10);

        assertEquals(BATCH_SIZE - 1, jobResults.size());
        jobResults.forEach((jobResultFuture) -> assertTrue(jobResultFuture.isDone()));
    }

    private List<Job<String>> makeJobs(int numJobs) {
        List<Job<String>> jobs = new ArrayList<>();
        for (int i = 0; i < numJobs; i++) {
            Job<String> job = new MBJob<>();
            jobs.add(job);
        }
        return jobs;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
