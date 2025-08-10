package section04;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class CooperativeSchedulingDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(CooperativeSchedulingDemo.class);

    static {
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "1");
    }

    public static void main(String[] args) {
        
        var builder = Thread.ofVirtual();

        var t1 = builder.unstarted(() -> demo(1));
        var t2 = builder.unstarted(() -> demo(2));
        var t3 = builder.unstarted(() -> demo(3));

        t1.start();
        t2.start();
        t3.start();

        ThreadUtils.sleep(Duration.ofSeconds(2));
    }

    private static void demo(int threadNumber) {
        LOGGER.info("thread-{} started", threadNumber);
        for (int i = 0; i < 10; i++) {
            LOGGER.info("thread-{} is printing {}. Thread: {}", threadNumber, i, Thread.currentThread());
            if ((i % 2 == 0) && (threadNumber == 1 || threadNumber == 2)) {
                Thread.yield();
            }
        }
        LOGGER.info("thread-{} ended", threadNumber);
    }
}
