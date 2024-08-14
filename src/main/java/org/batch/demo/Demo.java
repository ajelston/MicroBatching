package org.batch.demo;

import org.batch.BatchProcessor;
import org.batch.MicroBatcher;
import org.batch.mb.MBBatcher;
import org.batch.mb.MBBatcherOptions;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A demonstration of {@link MicroBatcher}.
 *
 * Creates a number of threads, each thread creates a number of jobs and
 * submits them to the MicroBatcher.
 *
 * An example {@link BatchProcessor} is attached that simply returns the
 * job input + 1. This is intended to allow measuring the performance
 * of the MicroBatcher library itself without having the time dominated
 * by the work of the BatchProcessor.
 *
 * Override default values with command-line arguments:
 *  - numThreads=X  (spawn X threads)
 *  - numJobs=Y (spawn Y jobs per thread)
 *  - batchSize=Z (Set batch size = Z on MicroBatcher)
 *  - timeoutMillis=T (set T millisecond timeout on job batch wait time)
 */
public class Demo {
    private static final Logger LOGGER = Logger.getLogger(Demo.class.getName());

    public static void main(String[] args) {
        LOGGER.info("MicroBatcher demo starting.");

        int numCallerThreads = getArg(args, "numThreads", 10);
        int numJobsPerThread = getArg(args, "numJobs", 1_000_000);
        int batchSize = getArg(args, "batchSize", 5);
        int timeoutMs = getArg(args, "timeoutMillis", 100);

        MBBatcherOptions options = new MBBatcherOptions.Builder()
                .withBatchSize(batchSize)
                .withTimeout(Duration.ofMillis(timeoutMs))
                .build();

        BatchProcessor<Integer, Integer> batchProcessor = new PlusOneBatchProcessor();
        MicroBatcher<Integer, Integer> microBatcher = new MBBatcher<>(options, batchProcessor);
        ExecutorService executorService = Executors.newFixedThreadPool(numCallerThreads);

        var timerDesc = "Jobs submitted and processed";
        try (final Timer t = new Timer(timerDesc, numCallerThreads * numJobsPerThread)) {
            var threads = IntStream.range(0, numCallerThreads)
                    .mapToObj(threadNum -> new JobCreator(threadNum, numJobsPerThread, microBatcher))
                    .map(executorService::submit)
                    .toList();
            threads.forEach(Demo::waitForThreadCompletion);

            microBatcher.shutdown();
        }

        executorService.shutdown();
        LOGGER.info("MicroBatcher demo completed.");
    }

    private static void waitForThreadCompletion(Future<Integer> future) {
        try {
            var threadNum = future.get();
            LOGGER.info(String.format("Thread %d completed", threadNum));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a command-line argument value, or return a default value if not
     * found.
     *
     * Note: this is pretty rough-and-ready to avoid pulling in an argument-
     * parsing library dependency. It doesn't handle misformed arguments, invalid values,
     * unexpected arguments etc.
     */
    private static int getArg(String[] args, String argName, int defaultValue) {
        for (String arg : args) {
            if (!arg.startsWith(argName + "=")) {
                continue;
            }
            String[] parts = arg.split("=");
            return Integer.parseInt(parts[1]);
        }
        return defaultValue;
    }
}