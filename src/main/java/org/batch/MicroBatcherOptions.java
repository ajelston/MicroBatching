package org.batch;

import java.time.Duration;

public interface MicroBatcherOptions {
    int batchSize();
    Duration timeout();
}
