package streamsapi;

import java.util.Arrays;
import java.util.List;

public class StreamReduceExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        int sum = numbers.stream().reduce(0, (a, b) -> a + b);
        System.out.println(sum); // Output: 15

        List<String> words = Arrays.asList("Hello", "World", "Java", "Streams", "API");
        String longestWord = words.stream()
                .reduce("", (a, b) -> a.length() > b.length() ? a : b);
        System.out.println(longestWord); // Output: Streams

    }
}
