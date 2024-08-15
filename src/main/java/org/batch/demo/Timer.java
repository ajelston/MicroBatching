package org.batch.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * Convenient utility class for wrapping a block of code
 * and reporting on the total time taken, along with an
 * average per-item time.
 *
 * Intended to be used in a try-with-resources block to
 * automatically report when the block is exited.
 */
public class Timer implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(Timer.class.getName());

    private final String description;
    private final int numHandled;
    private final Instant start;

    public Timer(String description, int numHandled) {
        this.description = description;
        this.numHandled = numHandled;
        this.start = Instant.now();
    }

    @Override
    public void close() {
        var elapsed = Duration.between(start, Instant.now());
        var elapsedStr = elapsed.toMillis() > 0
                ? String.format("%d milliseconds", elapsed.toMillis())
                : String.format("%d nanoseconds", elapsed.toNanos());
        var nanosPerItem = ((double) elapsed.toNanos()) / numHandled;
        var message = String.format("%s: %s, %d items, %f nanoseconds/item (avg), %d items/s",
                description, elapsedStr, numHandled,
                nanosPerItem, Math.round(Duration.ofSeconds(1).toNanos() / nanosPerItem));
        LOGGER.info(message);
    }
}
