# MicroBatching Library

## Usage 

build.gradle: 
```groovy
dependencies {
    ...
    implementation 'org.batch:micro-batch'
    ...
}
```

```java
MBBatcherOptions options = new MBBatcherOptions.Builder()
        .withBatchSize(5)
        .withTimeout(Duration.ofMillis(100))
        .build();

BatchProcessor<TInput, TOutput> batchProcessor = new BatchProcessorImplementation();
MicroBatcher<TInput, TOutput> microBatcher = new MBBatcher<>(options, batchProcessor);

var job = new JobImplementation(...);
var jobResultFuture = microBatcher.submit(job);

jobResultFuture.thenAccept(jobResult -> {
    if (jobResult.isSuccess()) {
        System.out.println("Yay! Job successful");
        TOutput result = jobResult.getResult();
    } else {
        System.out.println("Job failed :(");
        System.out.println("Error: " + jobResult.getErrorMessage());
    }
});
```

Client applications must provide an implementation of the `BatchProcessor` interface - 
this should be a class that takes a list of `Job`s and returns a corresponding list of 
`JobResult`s. The classes `org.batch.mb.MBJob` and `org.batch.mb.MBJobResult` can be 
used for job input and output, however, any classes that implement the interfaces will
work. 

The Micro-Batcher is implemented in the `org.batch.mb.MBBatcher` class. When the client
application submits a Job, it will be returned a CompletableFuture wrapping a JobResult. 
The CompletableFuture can be used with Java's standard asynchronous programming facilities,
such as executing code upon completion, chaining actions, waiting for a group of jobs to
be completed, etc. 

MBBatcher is thread-safe and jobs can be submitted from any thread. 

The shutdown method will block until all previously submitted jobs have been processed,
it will also prevent the submission of any new jobs. 

## Building / Development 

```bash
# List available build tasks 
./gradlew tasks 

# Assemble and test 
./gradlew build 

# Run test suite - coverage report available at build/reports/jacoco/test/html/index.html 
./gradlew test 

# Create JAR file for distribution 
./gradlew jar 
```

## Demo Application 

```bash
# Build jar file 
./gradlew jar

# Run demo application
java -cp build/libs/MicroBatching-1.0-SNAPSHOT.jar org.batch.demo.Demo
```

## Design 

### Philosophy / Rationale 

As this micro-batching package is intended for use as a library, it has
been developed without any dependencies, other than the facilities provided
by the JDK itself. 

Although this comes at a cost of more verbose code in places, it makes this
package a very lightweight dependency for client applications, with a jar file
weighing in at 17kB, and without concerns about transitive dependencies 
introducing version conflicts or an increased attack surface. 

The purpose of a micro-batching system is to accumulate individual jobs
for submission to a batch processor in groups. In order to minimise overhead
for threads submitting jobs, the amount of work related to submitting jobs
is kept as minimal as possible, and the batching and submission to the batch
processor happens in a background thread. 

Job submission threads and the background thread coordinate through a thread-safe 
Java data structure (LinkedBlockingQueue). This requires some synchronisation 
when jobs are submitted, but provides the benefit that the background thread 
polling the queue can block waiting for available jobs. An alternative would have
been using ConcurrentLinkedQueue - that would have been a non-blocking operation 
for inserting jobs to the queue, but the background processor would have either
had to use a busy-loop to poll the queue, pegging a core at 100% CPU, or introduced
sleeps with unnecessary delays. With the LinkedBlockingQueue implementation,
performance tests on a Ryzen 7 7840HS show 10 threads can achieve approx 40-50 million
job submissions per second per thread. Overall throughput is a little under 1 million
jobs per second because the batch processing happens in a single thread - a future 
extension would look at multi-threaded batch processing - but that would put some 
requirements on the clients implementing BatchProcessor. 

Once a job has been submitted, the caller needs to know when it has been processed
and results are available. The JobResult returned by the BatchProcessor is wrapped
in a CompletableFuture, an asynchronous feature introduced in Java 8 that allows 
composing, combining, and executing asynchronous computations. 

