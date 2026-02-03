package section07;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ThreadUtils;

public class Lec01AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        Lec01AutoCloseable.class
    );

    public static void main(String[] args) {

        var executorService = Executors.newSingleThreadExecutor();
        executorService.submit(Lec01AutoCloseable::task);
        LOGGER.info("submitted");
        executorService.shutdownNow();
    }

    private static void task() {
        ThreadUtils.sleep(Duration.ofSeconds(1));
        LOGGER.info("task executed");
    }
}
