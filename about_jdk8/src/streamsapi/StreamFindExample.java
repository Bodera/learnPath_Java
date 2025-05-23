package streamsapi;

import java.util.Arrays;
import java.util.List;

public class StreamFindExample {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David", "Eve");

        String first = names.stream().filter(name -> name.contains("A")).findAny().get();
        System.out.println(first);

        String last = names.stream().filter(name -> name.contains("A")).findFirst().get();
        System.out.println(last);

    }
}
