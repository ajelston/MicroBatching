package org.batch.demo;

import org.batch.JobResult;
import org.batch.MicroBatcher;
import org.batch.mb.MBJob;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Simple demo for a thread that creates a number of jobs and
 * submits them to a {@link MicroBatcher}.
 */
public class JobCreator implements Callable<Integer> {
    private static final Logger LOGGER = Logger.getLogger(JobCreator.class.getName());

    private final int threadNumber;
    private final int numJobs;
    private final MicroBatcher<Integer, Integer> microBatcher;

    public JobCreator(
            int threadNumber,
            int numJobs,
            MicroBatcher<Integer, Integer> microBatcher
    ) {
        this.threadNumber = threadNumber;
        this.numJobs = numJobs;
        this.microBatcher = microBatcher;
    }

    /**
     * Submit {@link #numJobs} jobs and wait for them to be processed.
     * @return the assigned thread number.
     */
    @Override
    public Integer call() {
        getJobFutures().forEach(future -> future.thenAccept(this::reportJobCompletion));
        return threadNumber;
    }

    /**
     * Generate and submit {@link #numJobs} to the {@link MicroBatcher}.
     *
     * For demonstration purposes, each Job's input is just an integer
     * that counts up as jobs are created.
     *
     * @return A stream of the {@link CompletableFuture}s returned from
     * the MicroBatcher. This is used later to wait for the jobs to be
     * processed.
     */
    private Stream<CompletableFuture<JobResult<Integer>>> getJobFutures() {
        var timerDesc = String.format("Thread %d job submission", threadNumber);
        try (final Timer t = new Timer(timerDesc, numJobs)) {
            return IntStream.range(0, numJobs)
                    .mapToObj(jobNum -> {
                        var job = new MBJob<Integer>();
                        job.setInput(jobNum);
                        LOGGER.fine(String.format("Thread %d submitting job %d", threadNumber, jobNum));
                        return microBatcher.submit(job);
                    });
        }
    }

    /**
     * Print a debug message that the job has been completed.
     */
    private void reportJobCompletion(JobResult<Integer> jobResult) {
        var message = String.format("Thread %d job %d completed",
                threadNumber,
                jobResult.getResult());
        LOGGER.fine(message);
    }
}
