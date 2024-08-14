package org.batch.mb;

import org.batch.MicroBatcherOptions;

import java.time.Duration;
import java.util.Optional;

public class MBBatcherOptions implements MicroBatcherOptions {
    private static final int DEFAULT_BATCH_SIZE = 5;

    private final int batchSize;
    private final Optional<Duration> timeout;

    public MBBatcherOptions(Builder builder) {
        this.batchSize = builder.getBatchSize();
        this.timeout = builder.getTimeout();
    }

    @Override
    public int batchSize() {
        return batchSize;
    }

    @Override
    public Optional<Duration> timeout() {
        return timeout;
    }

    public static class Builder {
        private int batchSize;
        private Optional<Duration> timeout;

        public Builder() {
            batchSize = DEFAULT_BATCH_SIZE;
            timeout = Optional.empty();
        }

        public int getBatchSize() {
            return batchSize;
        }

        public Optional<Duration> getTimeout() {
            return timeout;
        }

        public Builder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder withTimeout(Duration duration) {
            this.timeout = Optional.of(duration);
            return this;
        }

        public MBBatcherOptions build() {
            return new MBBatcherOptions(this);
        }
    }
}
