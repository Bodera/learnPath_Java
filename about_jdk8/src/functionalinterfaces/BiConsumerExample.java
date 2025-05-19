package functionalinterfaces;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BiConsumerExample {
    
    public static void main(String[] args) {
        Consumer<String> reverseConsumer = (s) -> {
            System.out.print(new StringBuilder(s).reverse().toString() + "\n");
        };

        BiConsumer<String, String> biConsumer = (s1, s2) -> reverseConsumer.accept(s1 + s2);
        biConsumer.accept("hello", " world");
        // Output: dlrow olleh

        BiConsumer<Integer, Integer> sumConsumer = (i1, i2) -> System.out.println(i1 + i2);
        sumConsumer.accept(2, 3);
        // Output: 5

        BiConsumer<Integer, Integer> productConsumer = (i1, i2) -> System.out.println(i1 * i2);
        productConsumer.accept(2, 3);
        // Output: 6

        sumConsumer.andThen(productConsumer).accept(4, 4);
    }
}
