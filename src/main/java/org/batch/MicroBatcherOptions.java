package org.batch;

import java.time.Duration;
import java.util.Optional;

public interface MicroBatcherOptions {
    int batchSize();
    Optional<Duration> timeout();
}
