package interfacechanges;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComparatorExample {

    static Comparator<Person> nameComparator = Comparator.comparing(Person::getName);
    static Comparator<Person> ageComparator = Comparator.comparing(Person::getAge);

    public static void main(String[] args) {
        List<Person> people = new ArrayList<>();
        people.add(new Person("John", 30));
        people.add(new Person("Jane", 25));
        people.add(new Person("Bob", 35));

        System.out.println("Sorted by name:");
        sortByName(people);

        System.out.println("\nSorted by age:");
        sortByAge(people);

        System.out.println("\nSorted by age and name:");
        sortByAgeAndName(people);

        people.add(null);

        System.out.println("\nSorted by name with nulls first:");
        sortByNameWithNullsFirst(people);

        System.out.println("\nSorted by age with nulls last:");
        sortByAgeWithNullsLast(people);
    }

    static void sortByName(List<Person> people) {
        people.sort(nameComparator);
        people.forEach(person -> System.out.println(person));
    }

    static void sortByAge(List<Person> people) {
        people.sort(ageComparator);
        people.forEach(person -> System.out.println(person));
    }

    static void sortByAgeAndName(List<Person> people) {
        people.sort(Comparator.comparing(Person::getAge).thenComparing(Person::getName));
        people.forEach(person -> System.out.println(person));
    }

    static void sortByNameWithNullsFirst(List<Person> people) {
        people.sort(Comparator.nullsFirst(nameComparator));
        people.forEach(person -> System.out.println(person));
    }

    static void sortByAgeWithNullsLast(List<Person> people) {
        people.sort(Comparator.nullsLast(ageComparator));
        people.forEach(person -> System.out.println(person));
    }

    static class Person {
        String name;
        int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    
        @Override
        public String toString() {
            return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
        }
    }
}
