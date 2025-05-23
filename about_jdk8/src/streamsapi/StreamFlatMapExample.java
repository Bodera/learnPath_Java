package streamsapi;

import java.util.Arrays;
import java.util.List;

public class StreamFlatMapExample {
    public static void main(String[] args) {
        List<String> cats = Arrays.asList("Persian", "Siamese");
        List<String> dogs = Arrays.asList("Labrador", "Dalmata");
        List<List<String>> pets = Arrays.asList(cats, dogs);

        System.out.println("Before flattening: " + pets); // Output: [[Persian, Siamese], [Labrador, Dalmata]]
        System.out.println("After flattening: " + pets.stream().flatMap(List::stream).sorted().toList()); // Output: [Dalmata, Labrador, Persian, Siamese]
    }
}
