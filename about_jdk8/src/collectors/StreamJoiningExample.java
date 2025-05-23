package collectors;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamJoiningExample {
    public static void main(String[] args) {
        String[] names = {"John", "Jane", "Bob", "Alice"};
        String joinedNames = Stream.of(names).collect(Collectors.joining(", ")); // Output: "John, Jane, Bob, Alice"
        System.out.println(joinedNames);

        String joinedNamesWithPrefix = Stream.of(names).collect(Collectors.joining(", ", "Names: ", "")); // Output: "Names: John, Jane, Bob, Alice"
        System.out.println(joinedNamesWithPrefix);
    }
}
