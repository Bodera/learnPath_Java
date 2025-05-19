package functionalinterfaces;
import java.util.function.BiPredicate;

public class BiPredicateExample {
    public static void main(String[] args) {
        BiPredicate<String, String> isSameText = (s1, s2) -> s1.equals(s2);
        System.out.println(isSameText.test("hello", "hello")); // Output: true
        System.out.println(isSameText.test("hello", "world")); // Output: false
    }
}
