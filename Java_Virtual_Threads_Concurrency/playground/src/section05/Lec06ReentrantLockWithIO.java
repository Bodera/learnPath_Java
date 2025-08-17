package section05;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class Lec06ReentrantLockWithIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec06ReentrantLockWithIO.class);
    private static final Lock lock = new ReentrantLock(true);

    public static void main(String[] args) {

        Runnable printTestMsg = () -> LOGGER.info("*** Test Message ***");

        demo(Thread.ofVirtual());
        Thread.ofVirtual().start(printTestMsg);

        ThreadUtils.sleep(Duration.ofSeconds(2));
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
        try {
            lock.lock();
            // using sleep to simulate I/O intensive tasks or network calls
            ThreadUtils.sleep(Duration.ofSeconds(10));
        } catch (Exception e) {
            LOGGER.error("error", e);
        } finally {
            lock.unlock();
        }
    }
}
