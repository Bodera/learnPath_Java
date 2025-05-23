package streamsapi;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class StreamFilterExample {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Anthony", "Beatrice");

        Predicate<String> namesStartingWithA = (name) -> name.startsWith("A");
        names.stream()
        .filter(namesStartingWithA) // Stream<String>
        .forEach(System.out::println);
    }
    
}
