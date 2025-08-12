package section05;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class Lec03SynchronizationWithIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec01RaceCondition.class);

    public static void main(String[] args) {

        Runnable printTestMsg = () -> LOGGER.info("*** Test Message ***");

        //demo(Thread.ofPlatform());
        //Thread.ofPlatform().start(printTestMsg);
        demo(Thread.ofVirtual());
        Thread.ofVirtual().start(printTestMsg);

        ThreadUtils.sleep(Duration.ofSeconds(15));
    }

    private static void demo(Thread.Builder builder) {
        // create 50 threads
        for (int i = 0; i < 50; i++) {
            builder.start(() -> {
                LOGGER.info("Task started. {}", Thread.currentThread());
                ioTask();
                LOGGER.info("Task ended. {}", Thread.currentThread());
            });
        }
    }

    private static void ioTask() {
        // using sleep to simulate I/O intensive tasks or network calls
        ThreadUtils.sleep(Duration.ofSeconds(10));
    }
}
