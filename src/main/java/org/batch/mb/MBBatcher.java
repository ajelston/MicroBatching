package org.batch.mb;

import org.batch.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MicroBatcher implementation.
 *
 * Accepts jobs and queues them until specified batch size is reached.
 * When a complete batch is received, or the specified timeout is reached,
 * the pending jobs are submitted to the BatchProcessor.
 *
 * When submitting a job, the caller receives a {@link CompletableFuture<JobResult>}.
 * This can be used with standard Java asynchronous features to indicate when the
 * job has been processed. JobResult holds the results from the BatchProcessor,
 * including success/error/exception status and any actual computed results.
 *
 * This class is built around a {@link LinkedBlockingQueue}, a native Java
 * concurrent data structure that is thread-safe. Submitted jobs are appended
 * to the end of the queue, and a background thread constantly polls from the
 * head of the queue until it has accumulated a complete batch. Once it has
 * a batch, it invokes the supplied {@link BatchProcessor} and marks all the
 * related CompletableFutures as completed.
 *
 * @param <TInput> Type parameter for job inputs.
 * @param <TOutput> Type parameter for job outputs.
 */
public class MBBatcher<TInput, TOutput> implements MicroBatcher<TInput, TOutput> {

    private final MicroBatcherOptions options;
    private final BatchProcessor<TInput, TOutput> processor;
    private final BlockingQueue<MBPendingJob<TInput, TOutput>> pending;
    private final ExecutorService executorService;
    private final Future<?> backgroundSubmitter;
    private final AtomicBoolean isShutdown;

    public MBBatcher(MicroBatcherOptions options, BatchProcessor<TInput, TOutput> processor) {
        this.options = options;
        this.processor = processor;
        this.pending = new LinkedBlockingQueue<>();
        this.isShutdown = new AtomicBoolean(false);
        this.executorService = Executors.newSingleThreadExecutor();
        this.backgroundSubmitter = executorService.submit(this::processPendingJobs);
    }

    /**
     * Submit a job to be processed once a complete batch is accumulated.
     *
     * Cannot be called after {@link #shutdown()} has been invoked.
     *
     * The {@link Job} is wrapped in {@link MBPendingJob} which constructs
     * the CompletableFuture and links it with the input job. The pending
     * job object is then placed onto the pending queue.
     *
     * @param job The job to be processed.
     * @return {@link CompletableFuture} when completed, will hold a
     *         {@link JobResult} with the results from the BatchProcessor.
     */
    @Override
    public CompletableFuture<JobResult<TOutput>> submit(Job<TInput> job) {
        if (isShutdown.get()) {
            throw new IllegalCallerException("Cannot submit jobs after shutdown!");
        }

        var pendingJob = new MBPendingJob<TInput, TOutput>(job);
        pending.add(pendingJob);
        return pendingJob.getJobResult();
    }

    /**
     * This method is invoked in the background thread to constantly
     * poll the pending queue for jobs and submit them as batches.
     *
     * Will exit after draining the queue after {@link #shutdown()} is
     * invoked.
     *
     * If the {@link BatchProcessor} throws an exception, the entire
     * batch is marked as a failure with details of the exception thrown.
     * Clients can choose the appropriate strategy accordingly - whether
     * to retry or fail affected jobs.
     */
    private void processPendingJobs() {
        while (!isShutdown.get() || !pending.isEmpty()) {
            List<MBPendingJob<TInput, TOutput>> pendingJobs = getPendingJobs(options.batchSize());
            List<Job<TInput>> jobs = pendingJobs.stream()
                    .map(MBPendingJob::getJob)
                    .toList();
            try {
                var results = processor.process(jobs);
                for (int i = 0; i < results.size(); i++) {
                    pendingJobs.get(i).getJobResult().complete(results.get(i));
                }
            } catch (Exception e) {
                // If BatchProcessor throws an exception, all jobs in the batch
                // report an exceptional error.
                pendingJobs.forEach(pendingJob -> {
                    MBJobResult<TOutput> jobResult = new MBJobResult<>();
                    jobResult.setException(e);
                    pendingJob.getJobResult().complete(jobResult);
                });
            }
        }
    }

    /**
     * Pull a batch of pending jobs off the queue, blocking until
     * jobs are available or until the specified timeout is reached.
     *
     * If the timeout is reached before a full batch is accumulated,
     * a partial or empty batch will be returned.
     *
     * @param batchSize Maximum number of jobs to fetch.
     * @return List of pending jobs, up to the specified batch size.
     */
    private List<MBPendingJob<TInput, TOutput>> getPendingJobs(int batchSize) {
        List<MBPendingJob<TInput, TOutput>> pendingJobs = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            try {
                var pendingJob = getPendingJob();
                if (pendingJob == null) {
                    break;
                }
                pendingJobs.add(pendingJob);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return pendingJobs;
    }

    /**
     * Get the next pending job, or null if the timeout is reached.
     */
    private MBPendingJob<TInput, TOutput> getPendingJob() throws InterruptedException {
        return pending.poll(options.timeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Shutdown the micro-batcher.
     *
     * Sets the shutdown flag to reject incoming job submissions,
     * and tells the background thread to exit after processing all
     * pending jobs.
     */
    @Override
    public void shutdown() {
        // Idempotence - check if batcher is already shutdown.
        if (isShutdown.get()) {
            return;
        }
        isShutdown.set(true);
        try {
            // Wait for the background submitter to finish processing
            // all outstanding jobs.
            backgroundSubmitter.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }
}
