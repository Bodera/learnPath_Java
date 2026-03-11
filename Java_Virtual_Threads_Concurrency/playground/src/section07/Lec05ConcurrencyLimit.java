package section07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import section07.externalservices.Client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lec05ConcurrencyLimit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec05ConcurrencyLimit.class);

    static void main() {
        execute(Executors.newCachedThreadPool(), 20);

        execute(Executors.newFixedThreadPool(3), 20);

        // temptating. but don't do this. VT should not be pooled
         var factory = Thread.ofVirtual().name("bodera-virtual", 1).factory();
         execute(Executors.newFixedThreadPool(3, factory), 20);
    }

    private static void printProductInfo(int id) {
        LOGGER.info("{} => {}", id, Client.getProduct(id));
    }

    private static void execute(ExecutorService executorService, int taskCount) {
        try (executorService) {
            for (int i = 1; i <= taskCount; i++) {
                int j = i;
                executorService.execute(() -> printProductInfo(j));
            }
            LOGGER.info("task submitted");
        }
    }
}
