package functionalinterfaces;
import java.util.function.Function;

public class FunctionExample {
    public static void main(String[] args) {
        Function<Integer, Integer> squareFunction = (i) -> i * i;
        int result = squareFunction.apply(2);
        System.out.println(result); // Output: 4

        Function<String, Integer> countCharFunction = (s) -> s.length();
        System.out.println(countCharFunction.apply("hello")); // Output: 5

        // andThen method
        System.out.println(countCharFunction.andThen(squareFunction).apply("hello")); // Output: 25
        // System.out.println(squareFunction.andThen(countCharFunction).apply("hello")); andThen is not commutative

        // compose method
        System.out.println(squareFunction.compose(countCharFunction).apply("hello")); // Output: 25
        // System.out.println(countCharFunction.compose(countCharFuncsquareFunctiontion).apply("hello")); compose is not commutative
    }
}
