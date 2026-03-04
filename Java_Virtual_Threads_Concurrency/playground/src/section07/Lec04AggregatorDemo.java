package section07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import section07.aggregator.AggregatorService;
import section07.aggregator.ProductDto;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class Lec04AggregatorDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec04AggregatorDemo.class);

    static void main(String[] args) throws InterruptedException, ExecutionException {
        //on real world these are more likely to be beans or singletons
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var aggregator = new AggregatorService(executor);

        LOGGER.info("product-42: {}", aggregator.getProduct(42));

        var futures = IntStream.rangeClosed(1, 50)
                .mapToObj(id -> executor.submit(() -> aggregator.getProduct(id)))
                .toList();

        List<ProductDto> products = futures.stream().map(Lec04AggregatorDemo::toProductDto).toList();
        LOGGER.info("list: {}", products);

    }

    private static ProductDto toProductDto(Future<ProductDto> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
