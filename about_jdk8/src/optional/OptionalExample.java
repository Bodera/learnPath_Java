package optional;

import java.util.Optional;

public class OptionalExample {

    public static void main(String[] args) {
        String nullValue = sayMyName(null);
        String value = sayMyName("John");

        System.out.println("=======================");

        System.out.println("Null value: " + nullValue);
        System.out.println("Value: " + value);

        System.out.println("=======================");

        Optional<String> optOfNullValue = Optional.ofNullable(nullValue);
        Optional<String> optOfValue = Optional.of(value);

        System.out.println("=======================");

        System.out.println("Optional of null value: " + optOfNullValue);
        System.out.println("Optional of value: " + optOfValue);

        System.out.println("=======================");

        optOfNullValue.ifPresentOrElse(System.out::println, () -> System.out.println("Optional is empty"));
        optOfValue.ifPresent(System.out::println);

        System.out.println("=======================");

        System.out.println("Optional is empty: " + optOfNullValue.isEmpty());
        System.out.println("Optional is present: " + optOfValue.isPresent());

        System.out.println("=======================");

        System.out.println("Optional get: " + optOfValue.get());
        System.out.println("Optional orElse: " + optOfNullValue.orElse("Default value"));

        System.out.println("=======================");

        System.out.println("Optional orElseGet: " + optOfNullValue.orElseGet(() -> "Default value"));
        System.out.println("Optional orElseThrow: " + optOfNullValue.orElseThrow(IllegalArgumentException::new));
    }

    static String sayMyName(String name) {
        return name;
    }

    // No need to code this anymore
    // static String sayMyName(String name) {
    //     if (name == null) {
    //         return "Default name";
    //     } else {
    //         return name;
    //     }
    // }
}
