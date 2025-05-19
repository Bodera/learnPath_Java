package referencemethods;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReferenceMethodExample {
    public static void main(String[] args) {
        Function<MyClass, String> function = (MyClass::sayHello);
        System.out.println(function.apply(new MyClass()));

        Comparator<Integer> comparator = MyClass::compare;
        System.out.println(comparator.compare(41, 42));

        Function<Integer, Boolean> isEven = MyClass::isEven;
        System.out.println(isEven.apply(42));

        BiFunction<Integer, Integer, Integer> sumFunction = MyClass::sum;
        System.out.println(sumFunction.apply(41, 42));
    }

    static class MyClass {
        public String sayHello() {
            return "Hello";
        }

        public static int compare(int a, int b) {
            return Integer.compare(a, b);
        }

        public static boolean isEven(int a) {
            return a % 2 == 0;
        }

        public static int sum(int a, int b) {
            return a + b;
        }
    }
}
