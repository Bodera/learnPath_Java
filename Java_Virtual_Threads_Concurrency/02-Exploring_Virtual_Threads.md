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



