package streamsapi;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamMatchesExample {
    
    public static void main(String[] args) {
        Flower[] flowers = new Flower[] {
            new Flower("Rose", "Red"),
            new Flower("Tulip", "Red"),
            new Flower("Orchid", "Red")
        };

        Flower[] whiteFlowers = new Flower[] {
            new Flower("Rose", "White"),
            new Flower("Tulip", "White"),
            new Flower("Orchid", "White")
        };

        
        System.out.println("Are all flowers red? " + Stream.of(flowers).allMatch(isRed));
        System.out.println("Are all flowers white? " + Stream.of(whiteFlowers).allMatch(isWhite));
        System.out.println("Are all flowers orange? " + Stream.of(flowers).allMatch(isOrange));
        System.out.println("======================");
        System.out.println("Are any flowers red? " + Stream.of(flowers).anyMatch(isRed));
        System.out.println("Are any flowers white? " + Stream.of(whiteFlowers).anyMatch(isWhite));
        System.out.println("Are any flowers orange? " + Stream.of(flowers).anyMatch(isOrange));
        System.out.println("======================");
        System.out.println("Are none of the flowers red? " + Stream.of(flowers).noneMatch(isRed));
        System.out.println("Are none of the flowers white? " + Stream.of(whiteFlowers).noneMatch(isWhite));
        System.out.println("Are none of the flowers orange? " + Stream.of(flowers).noneMatch(isOrange));
    }

    private static Predicate<Flower> isRed = (f) -> f.color.equals("Red");
    private static Predicate<Flower> isWhite = (f) -> f.color.equals("White");
    private static Predicate<Flower> isOrange = (f) -> f.color.equals("Orange");


    static class Flower {
        String name;
        String color;

        public Flower(String name, String color) {
            this.name = name;
            this.color = color;
        }
    }
}
