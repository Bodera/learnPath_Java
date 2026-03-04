package section07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import section07.externalservices.Client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Lec03AccessResponseUsingFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec03AccessResponseUsingFuture.class);

    static void main(String[] args) throws InterruptedException, ExecutionException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Future<String> product41 = executor.submit(() -> Client.getProduct(41));
            Future<String> product42 = executor.submit(() -> Client.getProduct(42));
            Future<String> product43 = executor.submit(() -> Client.getProduct(43));

            LOGGER.info("product-41: {}", product41.get());
            LOGGER.info("product-42: {}", product42.get());
            LOGGER.info("product-43: {}", product43.get());
        }
    }
}
