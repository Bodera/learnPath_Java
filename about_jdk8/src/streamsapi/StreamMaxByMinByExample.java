package streamsapi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamMaxByMinByExample {
    public static void main(String[] args) {
        List<Double> percentages = Arrays.asList(12.65, 77.01, 26.80, 91.56);
        
        // stream.max() returns an Optional<T> and handles empty streams more safely.
        System.out.println(percentages.stream().max(Double::compare).get()); // Output: 91.56
        System.out.println(percentages.stream().min(Double::compare).get()); // Output: 12.65
        
        // stream.collect(Collectors.maxBy()) returns a T object and may return null for empty streams, requiring additional null checks.
        System.out.println(percentages.stream().collect(Collectors.maxBy(Double::compare)).get()); // Output: 91.56
        System.out.println(percentages.stream().collect(Collectors.minBy(Double::compare)).get()); // Output: 12.65
    }
}
