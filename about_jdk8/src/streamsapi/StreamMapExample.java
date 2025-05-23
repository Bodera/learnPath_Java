package streamsapi;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamMapExample {
    public static void main(String[] args) {
        Stream<String> fruits = Stream.of("Apple", "Banana", "Coconut", "Banana");
        fruits.map(String::length) // String Integer
        .collect(Collectors.toList()) // List<Integer>
        .forEach(System.out::println); // Output: 5, 6, 7

        Set<String> uniqueFruits = Stream.of("Apple", "Banana", "Coconut", "Banana")
        .map(String::toUpperCase) // String String
        .collect(Collectors.toSet()); // Set<String>
        System.out.println(uniqueFruits); // Output: [APPLE, BANANA, COCONUT] // order is not guaranteed
    }
}
