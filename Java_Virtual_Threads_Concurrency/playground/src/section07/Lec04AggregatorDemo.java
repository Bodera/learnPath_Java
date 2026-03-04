package section07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import section07.aggregator.AggregatorService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Lec04AggregatorDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec04AggregatorDemo.class);

    static void main(String[] args) throws InterruptedException, ExecutionException {
        //on real world these are more likely to be beans or singletons
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var aggregator = new AggregatorService(executor);

        LOGGER.info("product-42: {}", aggregator.getProduct(42));
    }
}
