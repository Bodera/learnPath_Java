package streamsapi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamExample {
    public static void main(String[] args) {
        Stream<Integer> numbers = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        numbers.filter(i -> i % 2 == 0).forEach(System.out::println);

        System.out.println("===================");

        Stream<Integer> numbersFromArray = Stream.of(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        numbersFromArray.filter(i -> i % 2 == 0).forEach(System.out::println);

        System.out.println("===================");

        Stream<Integer> numbersFromList = Arrays.asList(null, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10).stream();
        numbersFromList.filter(i -> i % 2 == 0).forEach(System.out::println);

        System.out.println("===================");

        Predicate<Person> agePredicate = (p) -> p.age > 30;
        Map<String, List<String>> map = Person.getPersons()
            .stream() // Stream of Persons
            .filter(agePredicate) // Stream of Persons with age > 30
            .collect( // Final result
                Collectors.toMap(
                    p -> p.name,
                    p -> p.hobbies
                )
            );
        System.out.println(map);

        System.out.println("===================");

        List<String> hobbies = Person.getPersons()
            .stream() // Stream of Persons
            .map(p -> p.hobbies) // Stream of List<String>
            .flatMap(List::stream) // Stream<String>
            .distinct() // Stream<String>
            .collect(Collectors.toList()); // Collection<String>
        System.out.println(hobbies);
    }

    static class Person {
        String name;
        int age;
        List<String> hobbies;

        static public List<Person> getPersons() {
            Person p1 = new Person();
            p1.name = "John";
            p1.age = 30;
            p1.hobbies = Arrays.asList("reading", "hiking", "swimming");

            Person p2 = new Person();
            p2.name = "Jane";
            p2.age = 25;
            p2.hobbies = Arrays.asList("painting", "singing", "dancing");

            return Arrays.asList(p1, p2);
        }
    }
}
