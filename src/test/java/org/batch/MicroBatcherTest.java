package org.batch;

import org.batch.mb.MBBatcherOptions;
import org.batch.mb.MBJob;
import org.batch.mb.MBBatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class MicroBatcherTest {

    // TODO: Errored jobs
    // TODO: Exceptions thrown by BatchProcessor
    // TODO: Demo in Main - org.batch.demo package
    // TODO: Logging, Metrics?
    // TODO: Queue max capacity, time outs

    private static final int BATCH_SIZE = 5;
    private static final int TIMEOUT_MS = 10;

    private BatchProcessor<String, String> processor;
    private MicroBatcherOptions options;
    private MicroBatcher<String, String> batcher;

    @BeforeEach
    public void setUp() {
        processor = new UpperCaseBatchProcessor();
        options = new MBBatcherOptions.Builder()
                .withBatchSize(BATCH_SIZE)
                .withTimeout(Duration.ofMillis(TIMEOUT_MS))
                .build();
        batcher = new MBBatcher<>(options, processor);
    }

    @AfterEach
    public void tearDown() {
        batcher.shutdown();
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
        var jobs = makeJobs(BATCH_SIZE);
        var jobResults = submitJobs(jobs);

        assertEquals(BATCH_SIZE, jobResults.size());
        jobResults.forEach((jobResultFuture) -> assertTrue(jobResultFuture.isDone()));
    }

    @Test
    public void submit_timeoutReached_batchProcessorInvoked() {
        var jobs = makeJobs(BATCH_SIZE - 1);
        var jobResults = submitJobs(jobs);

        sleep(TIMEOUT_MS + 10);

        assertEquals(BATCH_SIZE - 1, jobResults.size());
        jobResults.forEach((jobResultFuture) -> assertTrue(jobResultFuture.isDone()));
    }

    @Test
    public void process_jobInputPassedToBatchProcessor_jobOutputInResult() throws Exception {
        var job = new MBJob<String>();
        job.setInput("hello");
        var jobResult = batcher.submit(job);

        // Force job submission
        batcher.shutdown();

        assertTrue(jobResult.isDone());
        assertEquals("HELLO", jobResult.get().getResult());
    }

    @Test
    public void shutdown_blocksUntilAllJobsProcessed() {
        var jobs = makeJobs(BATCH_SIZE - 2);
        var jobResults = submitJobs(jobs);

        batcher.shutdown();
        jobResults.forEach((jobResultFuture) -> assertTrue(jobResultFuture.isDone()));
    }

    @Test
    public void shutdown_submitAfterShutdownThrows() {
        batcher.shutdown();

        var job = new MBJob<String>();
        assertThrows(IllegalCallerException.class, () -> batcher.submit(job));
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

    private List<CompletableFuture<JobResult<String>>> submitJobs(List<Job<String>> jobs) {
        return jobs.stream()
                .map(job -> batcher.submit(job))
                .toList();
    }
}
