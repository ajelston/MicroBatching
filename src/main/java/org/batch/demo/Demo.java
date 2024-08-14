package org.batch.demo;

import org.batch.BatchProcessor;
import org.batch.MicroBatcher;
import org.batch.mb.MBBatcher;
import org.batch.mb.MBBatcherOptions;
import org.batch.mb.MBJob;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Demo {
    private static final Logger LOGGER = Logger.getLogger(Demo.class.getName());

    public static void main(String[] args) {
        LOGGER.info("MicroBatcher demo starting.");

        int numCallerThreads = 10;
        int numJobsPerThread = 1_000_000;

        // TODO: arguments for options/defaults, num threads
        // TODO: Refactor - create Callable/Runnable objects
        MBBatcherOptions options = new MBBatcherOptions.Builder()
                .withBatchSize(5)
                .withTimeout(Duration.ofMillis(100))
                .build();

        BatchProcessor<Integer, Integer> batchProcessor = new PlusOneBatchProcessor();
        MicroBatcher<Integer, Integer> microBatcher = new MBBatcher<>(options, batchProcessor);

        Instant start = Instant.now();
        try (ExecutorService executorService = Executors.newFixedThreadPool(numCallerThreads)) {
            IntStream.range(0, numCallerThreads)
                    .mapToObj(threadNum -> (Callable<Integer>) () -> {
                        Instant threadStart = Instant.now();
                        var jobFutures = IntStream.range(0, numJobsPerThread)
                                .mapToObj(jobNum -> {
                                    var job = new MBJob<Integer>();
                                    job.setInput(jobNum);
                                    LOGGER.fine(String.format("Thread %d submitting job %d", threadNum, jobNum));
                                    return microBatcher.submit(job);
                                });
                        Instant submitEnd = Instant.now();
                        var submitDuration = Duration.between(threadStart, submitEnd);
                        reportDuration(
                                String.format("Thread %d jobs submitted", threadNum),
                                submitDuration,
                                numJobsPerThread);
                        jobFutures.forEach(future ->
                                future.thenAccept(jobResult -> {
                                    var message = String.format("Thread %d job %d completed", threadNum, jobResult.getResult());
                                    LOGGER.fine(message);
                                }));

                        return threadNum;
                    }).map(executorService::submit)
                    .forEach(future -> {
                        try {
                            var threadNum = future.get();
                            LOGGER.info(String.format("Thread %d completed", threadNum));
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        LOGGER.info("Shutting down MicroBatcher and waiting for submitted jobs to process.");
        microBatcher.shutdown();

        Instant end = Instant.now();
        reportDuration(
                "All jobs submitted and processed",
                Duration.between(start, end),
                numCallerThreads * numJobsPerThread);

        LOGGER.info("MicroBatcher demo completed.");
    }

    private static void reportDuration(String description, Duration elapsed, int numItems) {
        var message = String.format("%s: %d milliseconds, %d items, %f nanoseconds/item",
                description, elapsed.toMillis(), numItems,
                ((double) elapsed.toNanos()) / numItems);
        LOGGER.info(message);
    }
}