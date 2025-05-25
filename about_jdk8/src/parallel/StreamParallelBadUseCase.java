package parallel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamParallelBadUseCase {

    public static void main(String[] args) {

        List<List<String>> data = Arrays.asList(
                Arrays.asList("a", "b", "c"),
                Arrays.asList("d", "e", "f"),
                Arrays.asList("g", "h", "i"));

        long sequentialTime = measurePerformance(
                () -> data.stream().flatMap(List::stream).count(),
                1000);

        long parallelTime = measurePerformance(
                () -> data.parallelStream().flatMap(List::stream).count(),
                1000);

        System.out.println("Sequential flatMap performance: " + sequentialTime + " ms");
        System.out.println("Parallel flatMap performance: " + parallelTime + " ms");

        /*
         * Output:
         * Sequential flatMap performance: 5 ms
         * Parallel flatMap performance: 12 ms
         */

        Calculation calc = new Calculation();
        List<Integer> range = IntStream.range(1, 1000000).boxed().collect(Collectors.toList());

        range.stream().forEach(calc::calculate);
        System.out.println("Result in sequential: " + calc.getTotal());

        calc.setTotal(0);

        range.parallelStream().forEach(calc::calculate);
        System.out.println("Result in parallel: " + calc.getTotal());

        /*
         * Output:
         * Result in sequential: 1783293664
         * Result in parallel: -1512215256
         */
    }

    private static long measurePerformance(Runnable task, int iterations) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            task.run();
        }
        return System.currentTimeMillis() - startTime;
    }
}

class Calculation {
    private int total;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void calculate(int i) {
        total += i;
    }
}
