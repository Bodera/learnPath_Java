package section03;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class CPUTaskDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPUTaskDemo.class);
    private static final int TASK_COUNT = 3 * Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        LOGGER.info("Task count: {}", TASK_COUNT);

        for (int i = 0; i < 3; i++) {
            var totalTimeTaken = ThreadUtils.measure(() -> demo(Thread.ofVirtual()));
            LOGGER.info("Total time taken with virtual was {} seconds.", totalTimeTaken);
            totalTimeTaken = ThreadUtils.measure(() -> demo(Thread.ofPlatform()));
            LOGGER.info("Total time taken with platform was {} seconds.", totalTimeTaken);
        }

//        demo(Thread.ofVirtual());
    }

    private static void demo(Thread.Builder builder) {
        var latch = new CountDownLatch(TASK_COUNT);

        for (int i = 1; i <= TASK_COUNT ; i++) {
            builder.start(() -> {
                Task.cpuIntensive(45);
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
