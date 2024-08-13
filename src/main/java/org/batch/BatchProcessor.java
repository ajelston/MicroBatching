package org.batch;

import java.util.List;

public interface BatchProcessor<T> {
    /**
     * We have two types of JobResult here. One is presumably
     * returned straightaway (or in a blocking fashion) after
     * we invoke BatchProcessor.
     *
     * The other is the type returned to the caller of the library,
     * which represents delayed computation.
     *
     * The challenge is in resolving them with the same type. Might
     * be able to get around this by returning CompletableFuture<JobResult>
     * to the caller of the library, and having it wrap the BatchProcessor
     * type.
     *
     * The problem with that is going to be distributed microbatcher using
     * a persistence backend.
     *
     * @param jobs
     * @return
     */
    List<JobResult<T>> process(List<Job<T>> jobs);
}
