package section07.aggregator;

import section07.externalservices.Client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class AggregatorService {

    private final ExecutorService executor;

    public AggregatorService(ExecutorService executor) {
        this.executor = executor;
    }

    public ProductDto getProduct(int id) throws ExecutionException, InterruptedException {
        var product = executor.submit(() -> Client.getProduct(id));
        var rating = executor.submit(() -> Client.getRating(id));

        return new ProductDto(
                id, product.get(), rating.get()
        );
    }
}
