package section05;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class Lec01RaceCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec01RaceCondition.class);
    private static final List<Integer> intList = new ArrayList<>();

    public static void main(String[] args) {
        
        LOGGER.info("starting demo");	
        //demo(Thread.ofPlatform());
        demo(Thread.ofVirtual());

        ThreadUtils.sleep(Duration.ofSeconds(1));

        LOGGER.info("intList size: {}", intList.size());
    }

    private static void demo(Thread.Builder builder) {
        // create 50 threads
        for (int i = 0; i < 50; i++) {
            builder.start(() -> {
                LOGGER.info("Task started. {}", Thread.currentThread());
                // do 200 in-memory tasks in each thread
                for (int j = 0; j < 200; j++) {
                    inMemoryTask();
                }
                LOGGER.info("Task ended. {}", Thread.currentThread());
            });
        }
        // by the end of the program we should have 50 * 200 = 10000 itens in the list
    }

    private static void inMemoryTask() {
        intList.add(1);
    }
}
