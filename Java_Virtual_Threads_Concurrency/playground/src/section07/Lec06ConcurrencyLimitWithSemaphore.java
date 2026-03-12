package section07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import section07.concurrencylimit.ConcurrencyLimiter;
import section07.externalservices.Client;

import java.util.concurrent.Executors;

public class Lec06ConcurrencyLimitWithSemaphore {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec06ConcurrencyLimitWithSemaphore.class);

    static void main() {
        var factory = Thread.ofVirtual().name("bodera-virtual", 1).factory();
        var limiter = new ConcurrencyLimiter(Executors.newThreadPerTaskExecutor(factory), 3);
        execute(limiter, 20);
    }

    private static void execute(ConcurrencyLimiter concurrencyLimiter, int taskCount) {
        try (concurrencyLimiter) {
            for (int i = 1; i <= taskCount; i++) {
                int j = i;
                concurrencyLimiter.submit(() -> printProductInfo(j));
            }
            LOGGER.info("task submitted");
        }
    }

    private static String printProductInfo(int id) {
        var product = Client.getProduct(id);
        LOGGER.info("{} => {}", id, product);
        return product;
    }
}
