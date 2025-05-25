package parallel;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Stream;

public class StreamParallelExample {

    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());

        String[] positions = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };

        System.out.println("-- - -- Sequential run -- - --");
        printStream(Arrays.stream(positions).sequential());

        System.out.println("-- - -- Parallel run -- - --");
        printStream(Arrays.stream(positions).parallel());

        /*
         * Output:
         * -- - -- Sequential run -- - --
         * 01:59:30.485562300 - Value 1 - Thread main
         * 01:59:31.503948200 - Value 2 - Thread main
         * 01:59:32.512054800 - Value 3 - Thread main
         * 01:59:33.519353900 - Value 4 - Thread main
         * 01:59:34.530447700 - Value 5 - Thread main
         * 01:59:35.538518500 - Value 6 - Thread main
         * 01:59:36.546060100 - Value 7 - Thread main
         * 01:59:37.555779800 - Value 8 - Thread main
         * 01:59:38.567674200 - Value 9 - Thread main
         * 01:59:39.578548 - Value 10 - Thread main
         * -- - -- Parallel run -- - --
         * 01:59:40.586485400 - Value 7 - Thread main
         * 01:59:40.587485100 - Value 3 - Thread ForkJoinPool.commonPool-worker-1
         * 01:59:40.587485100 - Value 2 - Thread ForkJoinPool.commonPool-worker-2
         * 01:59:40.587485100 - Value 6 - Thread ForkJoinPool.commonPool-worker-4
         * 01:59:40.587485100 - Value 9 - Thread ForkJoinPool.commonPool-worker-3
         * 01:59:40.587485100 - Value 1 - Thread ForkJoinPool.commonPool-worker-7
         * 01:59:40.587485100 - Value 5 - Thread ForkJoinPool.commonPool-worker-5
         * 01:59:40.587485100 - Value 8 - Thread ForkJoinPool.commonPool-worker-6
         * 01:59:40.587485100 - Value 10 - Thread ForkJoinPool.commonPool-worker-9
         * 01:59:40.587485100 - Value 4 - Thread ForkJoinPool.commonPool-worker-8
         */
    }

    static void printStream(Stream<String> stream) {
        stream.forEach(s -> {
            String message = "%s - Value %s - Thread %s".formatted(LocalTime.now(), s,
                    Thread.currentThread().getName());
            System.out.println(message);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
