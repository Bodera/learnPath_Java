package section03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ThreadUtils;

public class Task {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    public static void cpuIntensive(int i) {
//        LOGGER.info("starting CPU task. Thread info: {}", Thread.currentThread());
        var timeTaken = ThreadUtils.measure(() -> findFibonacci(i));
//        LOGGER.info("ending CPU task. Time taken was: {} seconds.", timeTaken);
    }

    // 2 ^ N algorithm - intentionally built this way to simulate a CPU intensive task using tons of recursive calls
    public static long findFibonacci(long position) {
        if (position < 2) {
            return position;
        }

        return findFibonacci(position - 1) + findFibonacci(position - 2);
    }
}
