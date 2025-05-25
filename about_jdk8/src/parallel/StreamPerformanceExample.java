package parallel;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class StreamPerformanceExample {

    public static void main(String[] args) {
        System.out.println(
                "Sequential stream performance: " + checkPerformance(StreamPerformanceExample::sumUsingSequential, 50));
        System.out.println(
                "Parallel stream performance: " + checkPerformance(StreamPerformanceExample::sumUsingParallel, 50));

        /*
         * Output:
         * Sequential stream performance: 22
         * Parallel stream performance: 14
         */
    }

    static long checkPerformance(Supplier<Integer> sum, int numOfTimes) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < numOfTimes; i++) {
            sum.get();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    static int sumUsingSequential() {
        return IntStream.rangeClosed(0, 1000000).sum();
    }

    static int sumUsingParallel() {
        return IntStream.rangeClosed(0, 1000000).parallel().sum();
    }
}
