package com.bodera.section01;

import java.util.concurrent.CountDownLatch;

public class InboundOutboundTaskDemo {

    private static final int TEN_PLATFORM = 10;
    private static final int TEN_THOUSAND_PLATFORM = 10_000;
    private static final int TEN_VIRTUAL = 10;
    private static final int ONE_MILLION_VIRTUAL = 1_000_000;

    public static void main(String[] args) {
        //smallPlatformThreadNonDaemon();
        //smallPlatformThreadDaemon();
        /*
            Output should look like the following (order of tasks will vary at each call)
            00:38:26.722 [bodera6] INFO com.bodera.section01.Task -- starting I/O task: 6
            00:38:26.722 [bodera7] INFO com.bodera.section01.Task -- starting I/O task: 7
            00:38:36.731 [bodera7] INFO com.bodera.section01.Task -- ending I/O task: 7
            00:38:36.731 [bodera6] INFO com.bodera.section01.Task -- ending I/O task: 6

            00:38:26.722 [bodera.daemon6] INFO com.bodera.section01.Task -- starting I/O task: 6
            00:38:26.722 [bodera.daemon7] INFO com.bodera.section01.Task -- starting I/O task: 7
            00:38:36.731 [bodera.daemon7] INFO com.bodera.section01.Task -- ending I/O task: 7
            00:38:36.731 [bodera.daemon6] INFO com.bodera.section01.Task -- ending I/O task: 6
            ... (other threads goes along)

            We can actually name our Java threads (aka OS Threads) and for our virtual threads
         */

        //hugePlatformThreadNonDaemon();
        // The line above is most probably expected to raise an OutOfMemoryException

        //smallPlatformThreadDaemon();
        // Method call above provides no output!

        //smallPlatformThreadDaemonSync();

        //smallVirtualThread();
        // Method call above provides no output!

        //smallVirtualThreadSync();

        //hugeVirtualThreadSync(); // Try increase to one million and set 1 minute on Thread.sleep at Task.java

        smallVirtualThreadSync();
    }

    private static void smallPlatformThreadNonDaemon() {
        threadStarter(TEN_PLATFORM);
    }

    private static void hugePlatformThreadNonDaemon() {
        threadStarter(TEN_THOUSAND_PLATFORM);
    }

    private static void threadStarter(int numberOfThreads) {
        Thread.Builder.OfPlatform threadBuilder = Thread.ofPlatform().name("bodera", 1);

        for (int i = 1; i <= numberOfThreads; i++) {
            int j = i;

            Thread thread = threadBuilder.unstarted(() -> Task.ioIntensiveOp(j));
            thread.start();

            // On production code we really don't want to create threads like this
            // Is most common to use a thread pool executor service or something related
            // We are just using it here for learning purposes.
        }
    }

    private static void smallPlatformThreadDaemon() {
        threadStarterDaemon(TEN_PLATFORM);
    }

    private static void hugePlatformThreadDaemon() {
        threadStarterDaemon(TEN_THOUSAND_PLATFORM);
    }

    private static void threadStarterDaemon(int numberOfThreads) {
        Thread.Builder.OfPlatform threadBuilder = Thread.ofPlatform().daemon().name("bodera.daemon", 1);

        for (int i = 1; i <= numberOfThreads; i++) {
            int j = i;

            Thread thread = threadBuilder.unstarted(() -> Task.ioIntensiveOp(j));
            thread.start();
        }
    }

    private static void smallPlatformThreadDaemonSync() {
        threadStarterDaemonSync(TEN_PLATFORM);
    }

    private static void threadStarterDaemonSync(int numberOfThreads) {
        try {
            CountDownLatch taskCountDown = new CountDownLatch(numberOfThreads);
            Thread.Builder.OfPlatform threadBuilder = Thread.ofPlatform().daemon().name("bodera.daemon", 1);

            for (int i = 0; i < numberOfThreads; i++) {
                int j = i;

                Thread thread = threadBuilder.unstarted(() -> {
                    Task.ioIntensiveOp(j);
                    taskCountDown.countDown();
                });
                thread.start();
            }

            taskCountDown.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void smallVirtualThread() {
        threadStarterDaemon(TEN_VIRTUAL);
    }

    private static void threadStarterVirtual(int numberOfThreads) {
        Thread.Builder.OfVirtual threadBuilder = Thread.ofVirtual().name("bodera.virtual", 1);

        for (int i = 0; i < numberOfThreads; i++) {
            int j = i;

            Thread thread = threadBuilder.unstarted(() -> Task.ioIntensiveOp(j));
            thread.start();
        }
    }

    private static void smallVirtualThreadSync() {
        threadStarterVirtualSync(TEN_VIRTUAL);
    }

    private static void hugeVirtualThreadSync() {
        threadStarterVirtualSync(ONE_MILLION_VIRTUAL);
    }

    private static void threadStarterVirtualSync(int numberOfThreads) {
        try {
            CountDownLatch taskCountDown = new CountDownLatch(numberOfThreads);
            Thread.Builder.OfVirtual threadBuilder = Thread.ofVirtual().name("bodera.virtual", 1);

            for (int i = 1; i <= numberOfThreads; i++) {
                int j = i;

                Thread thread = threadBuilder.unstarted(() -> {
                    Task.ioIntensiveOp(j);
                    taskCountDown.countDown();
                });
                thread.start();
            }

            taskCountDown.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
