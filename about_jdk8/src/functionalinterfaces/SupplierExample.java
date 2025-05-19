package functionalinterfaces;
import java.util.function.Supplier;

public class SupplierExample {
    public static void main(String[] args) {
        Supplier<Integer> randomSupplier = () -> (int) (Math.random() * 100);
        System.out.println(randomSupplier.get());

        Supplier<String> nameSupplier = () -> "Result of Supplier";
        System.out.println(nameSupplier.get());
    }
}
