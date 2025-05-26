package interfacechanges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterfaceChangesExample {

    static List<Person> getPersons() {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("John", 1000, Arrays.asList(new Person("John Jr", 0, new ArrayList<>()))));
        persons.add(new Person("Jane", 2000, new ArrayList<>()));
        persons.add(new Person("Bob", 3000, Arrays.asList(null, new Person("Bob Jr", 0, new ArrayList<>()))));
        return persons;
    }

    public static void main(String[] args) {

        PersonDetails personDetails = new PersonImpl();
        System.out.println("Total Salary: " + personDetails.calculateTotalSalary(getPersons()));
        System.out.println("Total Kids: " + personDetails.totalKids(getPersons()));
        System.out.println("Names: " + PersonDetails.getNames(getPersons()));
    }

}

interface PersonDetails {

    // All subclasses must implement this
    double calculateTotalSalary(List<Person> persons);

    // All subclasses can override this
    default long totalKids(List<Person> persons) {
        return persons.stream().map(Person::getKids).flatMap(List::stream).count();
    }

    // None of the subclasses can override this
    static List<String> getNames(List<Person> persons) {
        return persons.stream().map(Person::getName).toList();
    }
}

class PersonImpl implements PersonDetails {

    @Override
    public double calculateTotalSalary(List<Person> persons) {
        return persons.stream().mapToDouble(Person::getSalary).sum();
    }
}

class Person {

    String name = "";
    double salary = 0;
    List<Person> kids = new ArrayList<>();

    public Person(String name, double salary, List<Person> kids) {
        this.name = name;
        this.salary = salary;
        this.kids = kids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public List<Person> getKids() {
        return kids;
    }

    public void setKids(List<Person> kids) {
        this.kids = kids;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", salary=" + salary + ", kids=" + kids + "]";
    }
}