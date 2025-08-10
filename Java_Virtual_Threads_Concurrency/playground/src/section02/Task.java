package section02;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

/**
 * Some chained method calls which could throw exceptions
 */
public class Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    public static void execute(int i) {
        LOGGER.info("stating task {}", i);

        try {
            method1(i);
        } catch (Exception e) {
            LOGGER.error("error for {}", i, e);
        }

        LOGGER.info("ending task {}", i);
    }

    public static void method1(int i) {
        ThreadUtils.sleep(Duration.ofMillis(300));

        try {
            method2(i);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void method2(int i) {
        ThreadUtils.sleep(Duration.ofMillis(100));
        method3(i);
    }

    public static void method3(int i) {
        ThreadUtils.sleep(Duration.ofMillis(500));

        if (i != 4) return;

        throw new IllegalArgumentException("i cannot be 4.");
    }
}
