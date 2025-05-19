package functionalinterfaces;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class BiFunctionExample {

    public static void main(String[] args) {
        BiFunction<Integer, Integer, Integer> sumFunction = (i1, i2) -> i1 + i2;
        int result = sumFunction.apply(2, 3);
        System.out.println(result); // Output: 5

        BiFunction<List<Integer>, Predicate<Integer>, Map<String, Integer>> modFunction = (list, predicate) -> {
            Map<String, Integer> myMap = new HashMap<>();

            list.forEach(number -> {
                if (predicate.test(number)) {
                    myMap.put("Odd number: ", number);

                } else {
                    myMap.put("Even number: ", number);
                }
            });

            return myMap;
        };

        Predicate<Integer> modPredicate = (i) -> i % 2 == 0;

        List<Integer> numbers = Arrays.asList(1, 2);
        Map<String, Integer> resultMap = modFunction.apply(numbers, modPredicate);
        System.out.println(resultMap);
    }
}
