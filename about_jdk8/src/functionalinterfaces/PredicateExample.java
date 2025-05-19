package functionalinterfaces;
import java.util.function.Predicate;

public class PredicateExample {
    public static void main(String[] args) {
        Predicate<Integer> isPrime = (i) -> {
            if (i <= 1) return false;
            for (int j = 2; j < i; j++) if (i % j == 0) return false;
            return true;
        };
        System.out.println(isPrime.test(7)); // Output: true
        System.out.println(isPrime.test(8)); // Output: false

        Predicate<String> isPalindrome = (s) -> new StringBuilder(s).reverse().toString().equals(s);
        System.out.println(isPalindrome.test("racecar")); // Output: true
        System.out.println(isPalindrome.negate().test("racecar")); // Output: false

        Predicate<String> isJapaneseBike = (s) -> s.equals("Kawasaki") || s.equals("Honda") || s.equals("Suzuki");
        Predicate<String> isItalianBike = (s) -> s.equals("Ducati");
        Predicate<String> isAustrianBike = (s) -> s.equals("KTM");
        Predicate<String> isGermanBike = (s) -> s.equals("BMW");
        Predicate<String> isAmericanBike = (s) -> s.equals("Harley-Davidson");

        System.out.println(isGermanBike.or(isAustrianBike).test("KTM")); // Output: true
        System.out.println(isJapaneseBike.and(isItalianBike).test("Ducati")); // Output: false
        System.out.println(isJapaneseBike.and(isAmericanBike.negate()).test("Suzuki")); // Output: true

    }
}
