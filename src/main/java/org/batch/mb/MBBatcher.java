package org.batch.mb;

import org.batch.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Override
    public CompletableFuture<JobResult<TOutput>> submit(Job<TInput> job) {
        if (isShutdown.get()) {
            throw new IllegalCallerException("Cannot submit jobs after shutdown!");
        }

        var pendingJob = new MBPendingJob<TInput, TOutput>(job);
        pending.add(pendingJob);
        return pendingJob.getJobResult();
    }

    private void processPendingJobs() {
        while (!isShutdown.get() || !pending.isEmpty()) {
            List<MBPendingJob<TInput, TOutput>> pendingJobs = getPendingJobs(options.batchSize());
            List<Job<TInput>> jobs = pendingJobs.stream()
                    .map(MBPendingJob::getJob)
                    .toList();
            var results = processor.process(jobs);
            for (int i = 0; i < results.size(); i++) {
                pendingJobs.get(i).getJobResult().complete(results.get(i));
            }
        }
    }

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
                break;
            }
        }
        return pendingJobs;
    }

    private MBPendingJob<TInput, TOutput> getPendingJob() throws InterruptedException {
        if (options.timeout().isEmpty()) {
            return pending.take();
        }
        Duration timeout = options.timeout().get();
        return pending.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

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
            // Do nothing, thread has been interrupted but our
            // only remaining task is issuing shutdown to the ExecutorService
            // anyway.
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }
}
