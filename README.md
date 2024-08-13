# MicroBatching Library

## Usage 

```java
var batchProcessor = new BatchProcessorImpl(...);
var microBatcher = new MicroBatcher<String>(batchProcessor);

var job = new Job<String>(...);
var jobResult = microBatcher.submit(job);

jobResult.thenAccept(jobResult -> {
    if (jobResult.isSuccess()) {
        System.out.println("Yay! Job successful");
        String result = jobResult.getResult();
    } else {
        System.out.println("Job failed :(");
        System.out.println("Error: " + jobResult.getErrorMessage());
    }
});
```

## Thoughts

* We submit a list of Jobs to BatchProcessor, and it
  can return a list of JobResults, fine. But we also
  need to return a JobResult straight away to the 
  caller, even though we haven't submitted anything to
  BatchProcessor yet. 
* Should we be using Java's CompletableFuture interface to represent
  delayed computation of Jobs?
* This is going to make it hard to e.g. distribute across
  ddb though, as CompletableFuture is going to limit to in-memory references. 
* Should we make JobResult generic? Might as well,
  but you'll have to create different microBatchers for each type. 