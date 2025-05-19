package functionalinterfaces;
import java.util.function.Consumer;

public class ConsumerExample {

    public static void main(String[] args) {
        Consumer<Integer> powerConsumer = (i) -> System.out.println(Math.pow(i, 2));
        powerConsumer.accept(4);
        // Output: 16.0

        Consumer<String> spellingConsumer = (s) -> {
            s.chars().forEach(c -> {
                boolean isLastChar = c == s.charAt(s.length() - 1);
                System.out.print((char)c + (isLastChar ? "" : "-"));
            });
        };
        
        Consumer<String> reverseConsumer = (s) -> {
            System.out.print("\n" + new StringBuilder(s).reverse().toString());
        };

        spellingConsumer.andThen(reverseConsumer).accept("hello");
        // Output:h-e-l-l-o
        //        olleh
    }
    
}
