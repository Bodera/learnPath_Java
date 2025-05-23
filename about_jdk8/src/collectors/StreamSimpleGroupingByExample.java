package collectors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamSimpleGroupingByExample {

    static List<Course> courses = Arrays.asList(
            new Course("Math 101", "Mathematics"),
            new Course("Math 202", "Mathematics"),
            new Course("Science 101", "Science"),
            new Course("Science 202", "Science"),
            new Course("History 101", "Humanities"),
            new Course("English 101", "Humanities"),
            new Course("Computer Science 101", "Engineering"),
            new Course("Computer Science 202", "Engineering"));

    public static void main(String[] args) {
        Map<String, List<Course>> coursesByDiscipline = courses.stream()
                .collect(Collectors.groupingBy(Course::getDiscipline));

        coursesByDiscipline.forEach((discipline, coursesInDiscipline) -> {
            System.out.println("Discipline: " + discipline);
            coursesInDiscipline.forEach(course -> System.out.println("  " + course.getName()));
        });

        /*
         * Discipline: Engineering
         *   Computer Science 101
         *   Computer Science 202
         * Discipline: Humanities
         *   History 101
         *   English 101
         * Discipline: Science
         *   Science 101
         *   Science 202
         * Discipline: Mathematics
         *   Math 101
         *   Math 202
         */
    }

    static class Course {
        private String name;
        private String discipline;

        public Course(String name, String discipline) {
            this.name = name;
            this.discipline = discipline;
        }

        public String getName() {
            return name;
        }

        public String getDiscipline() {
            return discipline;
        }

        @Override
        public String toString() {
            return "Course{" +
                    "name='" + name + '\'' +
                    ", discipline='" + discipline + '\'' +
                    '}';
        }
    }
}
