# Exploring Java Virtual Threads

Here is how we set up our project environment on IntelliJ IDEA:

![IntelliJ IDEA setup](./img/virtual-threads-playground-intellij-idea.png)

We also need a few dependencies:

- Logback
- Enable preview of Java 21

The code will be updated as we go in the **virtual-threads-playground** project directory.

## Our goal

We had discussed already that in the microservices architecture problem, we deal with tons of network calls, often times thread will be blocked, if we try to increase the number of threads then we have to allocate stack size for these threads which is expensive.

Let's verify if it's really expensive to create threads. Once we know the problem then we can understand the solution better.

To avoid confusions let's refer to Java `Thread` as platform thread. To simulate the slow network, I/O calls, we would be using `Thread.sleep()` in the first examples. Our examples on this section will be located on the package `section01`.

Let's create our first class named `Task` and there we will define a method to simulate the slow network call.

```java
public class Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    public static void ioIntensiveOp(int i) {

        try {
            LOGGER.info("starting I/O task: {}", i);

            Thread.sleep(Duration.ofSeconds(10));

            LOGGER.info("ending I/O task: {}", i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

Now let's create our second class named `InboundOutboundTaskDemo` and there we will define a method to simulate the network calls.

```java
public class InboundOutboundTaskDemo {

    private static final int TEN_PLATFORM = 10;
    private static final int TEN_MILLION_PLATFORM = 10000000;

    public static void main(String[] args) {
        smallPlatformThreadDemo();
        /*
            Output should look like the following (order of tasks will vary at each call)
            00:38:26.722 [Thread-6] INFO com.bodera.section01.Task -- starting I/O task: 6
            00:38:26.722 [Thread-7] INFO com.bodera.section01.Task -- starting I/O task: 7
            00:38:36.731 [Thread-7] INFO com.bodera.section01.Task -- ending I/O task: 7
            00:38:36.731 [Thread-6] INFO com.bodera.section01.Task -- ending I/O task: 6
            ... (other threads goes along)

            We can actually name our Java threads (aka OS Threads) and for our virtual threads
         */

        //hugePlatformThreadDemo();
        // The line above is most probably expected to raise an OutOfMemoryException
    }

    private static void smallPlatformThreadDemo() {
        threadStarter(TEN_PLATFORM);
    }

    private static void hugePlatformThreadDemo() {
        threadStarter(TEN_MILLION_PLATFORM);
    }

    private static void threadStarter(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            int j = i;

            Thread thread = new Thread(() -> Task.ioIntensiveOp(j));
            thread.start();

            // On production code we really don't want to create threads like this
            // Is most common to use a thread pool executor service or something related
            // We are just using it here for learning purposes.
        }
    }
}
```

Behind the curtains, whenever we call `Thread.start()` Java calls a native method to create the underlying platform thread. This is the `pthread_create` which comes from a C library to create POSIX threads, it's a standard for creating threads in the Unix-like machines (Linux, Mac), but Windows machines Java may call `winpthreads` instead for doing this job.

So when we called `hugePlatformThreadDemo()` we get the following:

```bash
Failed to start thread "Unknown thread" - pthread_create failed (EAGAIN) for attribute: stacksize=2048k, guardsize=16k, detachstate.
Exception in thread "main" java.lang.OutOfMemoryError: unable to create native thread: possible out of memory or process/resource limits reached
at java.base/java.lang.Thread.start0(Native Method)
```

Is not always because system is out of memory that this exception is thrown, sometimes it can be because we have too many threads, or we have too many threads with too much stack size. The underlying OS will not give us the flexibility to create millions of threads.

## Thread builder factory method

Before JDK 21 we used to create threads by calling `new Thread(Runnable)` and then call `Thread.start()` to start the thread.

```java
Thread thread = new Thread(() -> Task.ioIntensiveOp(i));
thread.start();
```

Now in JDK 21 we can use a `Thread.Builder.OfPlatform` to create our threads.

```java
Thread thread = Thread.ofPlatform().unstarted(() -> Task.ioIntensiveOp(i));
thread.start();
```

When we create threads like this we call them **non-daemon threads** or **foreground threads**, so they will not be terminated when the main thread is terminated just only when the JVM is terminated (our main application). So after `threadStarter()` is done executing our non-daemon threads will not be terminated until our `InboundOutboundTaskDemo.main()` is terminated (which is our main application).

Sometimes you might want to create threads to run in background mode, we call them **daemon threads**. To create a daemon thread we use the `Thread.Builder.OfPlatform.daemon()` method.

```java
Thread thread = Thread.ofPlatform().daemon().unstarted(() -> Task.ioIntensiveOp(i));
thread.start();
```

If we try to run the daemon thread our application will just exit immediately.

```bash
> Task :compileJava
> Task :processResources NO-SOURCE
> Task :classes
> Task :com.bodera.section01.InboundOutboundTaskDemo.main()

BUILD SUCCESSFUL in 233ms
```

That's because in our `threadStarterDaemon()` our main thread created ten background threads, and it exits immediately after without waiting for daemon threads to complete their tasks. That's how daemon threads works.

But how can we make our applications wait the background threads to finish their execution? And how we are talking about this without mentioning virtual threads? And if we have to wait for the application to do the job, why we are creating daemon threads in the first place? We will have a better insight on this on the next section.

But first let's see how can we make our applications wait the background threads to finish their execution. That's a job for `CountDownLatch`.

The `CountDownLatch` is nothing new, was present in Java since 1.5as part of the `java.util.concurrent package`. It's a synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads completes.

Common use cases for `CountDownLatch` include:

- Waiting for multiple threads to complete initialization before starting a service
- Coordinating the execution of multiple threads in a test scenario
- Implementing a "barrier" that allows threads to wait for each other to reach a certain point

So we can use it to wait for the background threads to complete their jobs.

```java
var latch = new CountDownLatch(numberOfThreads); //number of tasks

for (int i = 0; i < numberOfThreads; i++) {
    int j = i;

    Thread thread = Thread.ofPlatform().daemon().unstarted(() -> {
        Task.ioIntensiveOp(j);
        latch.countDown(); //decrement the count to indicate that the task is done
    }
    thread.start();
}
latch.await(); //wait until all tasks are done
```

In Java, a thread can be in a state where it has finished its task, but the thread itself is still alive. This is because the thread's task is typically executed in a `run()` method, and when that method completes, the thread's task is done. However, the thread itself may still be in a state where it's waiting for other threads to finish, or waiting for some other condition to be met, before it can actually exit.

Think of it like a worker who has finished their assigned task, but is still waiting for their manager to give them further instructions or to confirm that they can go home. The worker has finished their task, but they're still "on the clock" and haven't actually left the workplace yet.

In Java, when a thread finishes its task, it's said to be in a state of "completion", but it's not necessarily "terminated". A thread is only terminated when it's actually completed and is no longer running.

The `CountDownLatch` is typically used to wait for a thread to finish its task, whereas `Thread.join()` is used to wait for the thread to actually terminate.

Here's a code snippet combining `CountDownLatch` and `Thread.join()`:

```java
CountDownLatch taskCountDown = new CountDownLatch(numberOfThreads);
Thread.Builder.OfPlatform threadBuilder = Thread.ofPlatform().daemon().name("bodera.daemon", 1);

Thread[] threads = new Thread[numberOfThreads];

for (int i = 0; i < numberOfThreads; i++) {
    int j = i;

    threads[i] = threadBuilder.unstarted(() -> {
        Task.ioIntensiveOp(j);
        taskCountDown.countDown();
    });
    threads[i].start();
}

taskCountDown.await();

// Wait for each thread to finish
for (Thread thread : threads) {
    thread.join();
}
```



