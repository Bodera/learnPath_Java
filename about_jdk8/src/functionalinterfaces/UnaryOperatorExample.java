package functionalinterfaces;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class UnaryOperatorExample {
    public static void main(String[] args) {
        UnaryOperator<String> reverseOperator = (s) -> new StringBuilder(s).reverse().toString();
        System.out.println(reverseOperator.apply("hello")); // Output: olleh

        UnaryOperator<Integer> incrementOperator = (i) -> i + 1;
        System.out.println(incrementOperator.apply(3)); // Output: 4

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        int[] i = {0};
        names.forEach(name -> {
            i[0] = incrementOperator.apply(i[0]);
            System.out.println(i[0] + ": " + reverseOperator.apply(name)); // Output: 1: eliA, 2: boB, 3: eilrahC
        });

        
    }
}
