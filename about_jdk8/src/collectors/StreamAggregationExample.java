package collectors;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamAggregationExample {
    public static void main(String[] args) {
        List<Student> studentGrades = getStudents();

        int count = studentGrades.stream().collect(Collectors.counting()).intValue(); // Count of all student grades
        int sum = studentGrades.stream().collect(Collectors.summingInt(Student::getGrade)); // Sum of all student grades
        int average = studentGrades.stream().collect(Collectors.averagingInt(Student::getGrade)).intValue(); // Average of all student grades
        int max = studentGrades.stream().collect(Collectors.maxBy(Comparator.comparingInt(Student::getGrade))).get().getGrade(); // Maximum student grade
        int min = studentGrades.stream().collect(Collectors.minBy(Comparator.comparingInt(Student::getGrade))).get().getGrade(); // Minimum student grade
        
        System.out.println("Count: " + count); // Output: Count: 5
        System.out.println("Sum: " + sum); // Output: Sum: 438
        System.out.println("Average: " + average); // Output: Average: 87
        System.out.println("Max: " + max); // Output: Max: 95
        System.out.println("Min: " + min); // Output: Min: 78
    }

    private static List<Student> getStudents() {
        return Arrays.asList(
                new Student("Alice", 85),
                new Student("Bob", 92),
                new Student("Charlie", 78),
                new Student("David", 95),
                new Student("Eve", 88)
        );
    }

    static class Student {
        private String name;
        private int grade;
        
        public Student(String name, int grade) {
            this.name = name;
            this.grade = grade;
        }
        
        public String getName() {
            return name;
        }
        
        public int getGrade() {
            return grade;
        }
    }
}
