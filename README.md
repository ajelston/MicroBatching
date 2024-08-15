# MicroBatching Library

## Usage 

```java
MBBatcherOptions options = new MBBatcherOptions.Builder()
        .withBatchSize(5)
        .withTimeout(Duration.ofMillis(100))
        .build();

BatchProcessor<TInput, TOutput> batchProcessor = new BatchProcessorImplementation();
MicroBatcher<TInput, TOutput> microBatcher = new MBBatcher<>(options, batchProcessor);

var job = new JobImplementation(...);
var jobResult = microBatcher.submit(job);

jobResult.thenAccept(jobResult -> {
    if (jobResult.isSuccess()) {
        System.out.println("Yay! Job successful");
        TOutput result = jobResult.getResult();
    } else {
        System.out.println("Job failed :(");
        System.out.println("Error: " + jobResult.getErrorMessage());
    }
});
```

## Thoughts

