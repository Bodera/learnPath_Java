package streamsapi;

import java.util.Arrays;
import java.util.List;

public class StreamLimitSkipExample {
    
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");

        names.stream().limit(2).forEach(System.out::println); // Output: Alice, Bob
        names.stream().skip(2).forEach(System.out::println); // Output: Charlie, David, Eve
    }
    
}
