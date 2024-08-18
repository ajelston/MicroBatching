package org.batch.mb;

import org.batch.MicroBatcherOptions;

import java.time.Duration;

public class MBBatcherOptions implements MicroBatcherOptions {
    private static final int DEFAULT_BATCH_SIZE = 5;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final int batchSize;
    private final Duration timeout;

    public MBBatcherOptions(Builder builder) {
        this.batchSize = builder.getBatchSize();
        this.timeout = builder.getTimeout();
    }

    @Override
    public int batchSize() {
        return batchSize;
    }

    @Override
    public Duration timeout() {
        return timeout;
    }

    public static class Builder {
        private int batchSize;
        private Duration timeout;

        public Builder() {
            batchSize = DEFAULT_BATCH_SIZE;
            timeout = DEFAULT_TIMEOUT;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public Builder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder withTimeout(Duration duration) {
            this.timeout = duration;
            return this;
        }

        public MBBatcherOptions build() {
            return new MBBatcherOptions(this);
        }
    }
}
