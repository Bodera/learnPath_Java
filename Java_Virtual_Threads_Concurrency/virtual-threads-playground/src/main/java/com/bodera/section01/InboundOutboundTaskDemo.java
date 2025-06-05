package com.bodera.section01;

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
