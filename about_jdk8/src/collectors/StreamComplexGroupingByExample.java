package collectors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamComplexGroupingByExample {
    static Department mathDepartment = new Department("Mathematics", "Arts and Sciences");
    static Department scienceDepartment = new Department("Science", "Arts and Sciences");
    static Department historyDepartment = new Department("History", "Humanities");
    static Department englishDepartment = new Department("English", "Humanities");
    static Department csDepartment = new Department("Computer Science", "Engineering");

    static List<Course> courses = Arrays.asList(
            new Course("Math 101", mathDepartment),
            new Course("Math 202", mathDepartment),
            new Course("Science 101", scienceDepartment),
            new Course("Science 202", scienceDepartment),
            new Course("History 101", historyDepartment),
            new Course("English 101", englishDepartment),
            new Course("Computer Science 101", csDepartment),
            new Course("Computer Science 202", csDepartment));

    public static void main(String[] args) {
        Map<String, Map<String, List<Course>>> coursesByCollegeAndDepartment = courses.stream()
                .collect(Collectors.groupingBy(
                        course -> course.getDepartment().getCollege(),
                        Collectors.groupingBy( // nested grouping can be added as needed
                                course -> course.getDepartment().getName(),
                                Collectors.toList())));

        coursesByCollegeAndDepartment.forEach((college, departments) -> {
            System.out.println("College: " + college);
            departments.forEach((department, coursesInDepartment) -> {
                System.out.println("  Department: " + department);
                coursesInDepartment.forEach(course -> System.out.println("    " + course.getName()));
            });
        });

        /*
         * College: Engineering
         *   Department: Computer Science
         *      Computer Science 101
         *      Computer Science 202
         * College: Humanities
         *   Department: English
         *      English 101
         *   Department: History
         *      History 101
         * College: Arts and Sciences
         *   Department: Science
         *      Science 101
         *      Science 202
         *   Department: Mathematics
         *      Math 101
         *      Math 202
         */
    }

    static class Department {
        private String name;
        private String college;

        public Department(String name, String college) {
            this.name = name;
            this.college = college;
        }

        public String getName() {
            return name;
        }

        public String getCollege() {
            return college;
        }
    }

    static class Course {
        private String name;
        private Department department;

        public Course(String name, Department department) {
            this.name = name;
            this.department = department;
        }

        public String getName() {
            return name;
        }

        public Department getDepartment() {
            return department;
        }

        @Override
        public String toString() {
            return "Course{" +
                    "name='" + name + '\'' +
                    ", department='" + department + '\'' +
                    '}';
        }
    }
}
