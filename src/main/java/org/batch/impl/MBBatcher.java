package org.batch.impl;

import org.batch.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MBBatcher<T> implements MicroBatcher<T> {

    private final MicroBatcherOptions options;
    private final BatchProcessor<T> processor;
    private final List<MBPendingJob<T>> pending;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timeoutFuture;

    public MBBatcher(MicroBatcherOptions options, BatchProcessor<T> processor) {
        this.options = options;
        this.processor = processor;
        this.pending = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduleBackgroundTasks();
    }

    @Override
    public CompletableFuture<JobResult<T>> submit(Job<T> job) {
        var pendingJob = new MBPendingJob<>(job);
        pending.add(pendingJob);
        processPendingJobs(false);

        return pendingJob.getJobResult();
    }

    private void processPendingJobs(boolean force) {
        if (pending.size() < options.batchSize() && !force) {
            return;
        }
        List<Job<T>> pendingJobs = pending.stream()
                .map(MBPendingJob::getJob)
                .toList();
        var results = processor.process(pendingJobs);
        for (int i = 0; i < results.size(); i++) {
            pending.get(i).getJobResult().complete(results.get(i));
        }
        pending.clear();
    }

    @Override
    public void shutdown() {

    }

    private void scheduleBackgroundTasks() {
        if (options.timeout().isEmpty()) {
            return;
        }

        if (timeoutFuture != null) {
            throw new IllegalStateException("Background task already scheduled!");
        }

        long timeoutMs = options.timeout().get().toMillis();
        timeoutFuture = scheduler.scheduleAtFixedRate(
                () -> processPendingJobs(true),
                timeoutMs,
                timeoutMs,
                TimeUnit.MILLISECONDS);
    }
}
