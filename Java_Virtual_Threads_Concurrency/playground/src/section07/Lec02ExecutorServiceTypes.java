package section07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ThreadUtils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Lec02ExecutorServiceTypes {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            Lec02ExecutorServiceTypes.class
    );

    public static void main(String[] args) {

    }

    // single thread executor - to execute tasks sequentially
    private static void single() {
        execute(Executors.newSingleThreadExecutor(), 3);
    }

    // fixed thread pool
    private static void fixed() {
        execute(Executors.newFixedThreadPool(2), 10);
    }

    // elastic thread pool
    private static void cached() {
        execute(Executors.newCachedThreadPool(), 100);
    }

    // ExecutorService which creates VirtualThread per task
    private static void virtual() {
        execute(Executors.newVirtualThreadPerTaskExecutor(), 1_000);
    }

    // To schedule tasks periodically
    private static void scheduled() {
        try (var executorService = Executors.newSingleThreadScheduledExecutor()) {
            executorService.scheduleWithFixedDelay(() -> {
                LOGGER.info("executing scheduled task");
            }, 0, 1, TimeUnit.SECONDS);
            ThreadUtils.sleep(Duration.ofSeconds(5));
        }
    }

    private static void execute(ExecutorService executorService, int taskCount) {
        try (executorService) {
            for (int i = 0; i < taskCount; i++) {
                int j = i;
                executorService.execute(() -> ioTask(j));
            }
            LOGGER.info("task submitted");
        }
    }

    private static void ioTask(int i) {
        LOGGER.info("Task started: {}, Thread info {}", i, Thread.currentThread());
        ThreadUtils.sleep(Duration.ofSeconds(4));
        LOGGER.info("Task finished: {}, Thread info {}", i, Thread.currentThread());
    }
}
