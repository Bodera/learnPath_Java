package streamsapi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CollectionVsStreamsExample {
    public static void main(String[] args) {
        // Collection
        List<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");

        for (String name : names) {
            System.out.println(">>> First Iteration: " + name);
        }

        for (String name : names) {
            System.out.println(">>> Second Iteration: " + name);
        }

        Consumer<String> changeName = (name) -> {
            name = name.toLowerCase();
            System.out.println(name);
        };

        Stream<String> stream = Stream.of("David", "Eve", "Frank");
        stream
        .peek(changeName) // peek is a terminal operation that doesn't modify the stream
        .forEach(name -> System.out.println(">>> Third Iteration: " + name));

        // streams could not be reused
        // the line below will throw an exception because the stream is already processed
        // Exception in thread "main" java.lang.IllegalStateException: stream has already been operated upon or closed
        // stream.forEach(name -> System.out.println(">>> Fourth Iteration: " + name));
    }
}
