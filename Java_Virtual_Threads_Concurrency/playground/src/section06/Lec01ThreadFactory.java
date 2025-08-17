package section06;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class Lec01ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec01ThreadFactory.class);

    public static void main(String[] args) {

        demo(Thread.ofVirtual().name("bodera-virtual", 1).factory());

        ThreadUtils.sleep(Duration.ofSeconds(3));
    }

    /*
     * Creates a few threads.
     * Each thread creates one child thread.
     * Very basic demo. In real life, we use a ExecutorService.
     * Virtual threads are cheap to create.
     */
    private static void demo(ThreadFactory factory) {
        for (int i = 0; i < 3; i++) {
            Thread thread = factory.newThread(() -> {
                LOGGER.info("Task started. {}", Thread.currentThread());

                Thread childThread = factory.newThread(() -> {
                    LOGGER.info("Child task started. {}", Thread.currentThread());
                    ThreadUtils.sleep(Duration.ofSeconds(2));
                    LOGGER.info("Child task ended. {}", Thread.currentThread());
                });
                childThread.start();

                LOGGER.info("Task ended. {}", Thread.currentThread());
            });
            thread.start();
        }
    }
}
