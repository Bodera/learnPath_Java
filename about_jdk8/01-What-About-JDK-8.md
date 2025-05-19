# Introduction

So here we are to explore what is about Java provides on the JDK 8 release. First thing to keep in mind when JDK 8 comes to table is **functional programming**. To summarize what is more relevant we can list:

- Lambdas
- Streams and Parallel Streams
- Optional
- Date and Time API

Is worth to mention that this release comes to help improve application performance without the need to write more code. As a matter of fact you're enabled to be more productive with less code, more readable and concise. Not to mention that developers can prevent likelihood NPEs issues by using the `Optional` class.

What kind of animal are that lambdas? How they look like? The basic syntax of a lambda is:

```java
(input) -> {body};
```

Lambdas are anonymous functions that can be used to replace method references. As a matter of fact, lambdas are functions that are defined without a name.

Let's explore a bit more about lambdas.

```java
// Before JDK 8
public static void main(String[] args) {
    
    int t1, t2;
    
    for (int i = 0; i <= 10; i++) {
        t1 += i;
    }

    for (int i = 0; i < 10; i++) {
        t2 += i;
    }
    
    System.out.println(t1);
    System.out.println(t2);
}
```

The code above can be rewritten in a functional way as follows:

```java
// After JDK 7
public static void main(String[] args) {

    int t1 = IntStream.rangeClosed(0, 10)
                          .reduce(0, Integer::sum);
    
    int t2 = IntStream.range(0, 10)
                      .reduce(0, Integer::sum);
    
    System.out.println(t1);
    System.out.println(t2);
}
```

Look these two APIs produce the same result: `IntStream.rangeClosed(0, 10)` and `IntStream.range(0, 11)`. The difference is that `rangeClosed` includes the end value, while `range` does not.

Aggregate operations are more common in functional programming, like `sum`, `average`, `min`, `max`, `count`, `distinct`. Let's check one of them:

```java
// Before JDK 8
public static void main(String[] args) {
    
    List<String> names = Arrays.asList("John", "Ringo", "George", "Ringo");
    
    List<String> uniqueNames = new ArrayList<>();
    
    for (String name : names) {
        if (!uniqueNames.contains(name)) {
            uniqueNames.add(name);
        }
    }
    
    System.out.println(uniqueNames);
}
```

The code above can be rewritten in a functional way as follows:

```java
// After JDK 7
public static void main(String[] args) {

    List<String> names = Arrays.asList("John", "Ringo", "George", "Ringo");
    
    List<String> uniqueNames = names.stream()
                                    .distinct()
                                    .collect(Collectors.toList());

    System.out.println(uniqueNames);
}
```

## A few examples on Runnables and Comparables

### Runnables using lambdas

```java
// Before JDK 8
public static void main(String[] args) {

    Runnable r = new Runnable() {
        @Override
        public void run() {
            System.out.println("Hello World!");
            System.out.println("Goodbye World!");
        }
    };
    
    new Thread(r).start();
}
```

The code above can be rewritten in a functional way as follows:

```java
// After JDK 7
public static void main(String[] args) {

    Runnable r = () -> {
        System.out.println("Hello!");
        System.out.println("Goodbye!");
    };
    new Thread(r).start();
}
```

### Comparables using lambdas

```java
// Before JDK 8
public static void main(String[] args) {
    
    List<String> names = Arrays.asList("John", "Ziggy", "George", "Ringo");
    
    Collections.sort(names, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    });
    
    System.out.println(names);
}
```

The code above can be rewritten in a functional way as follows:

```java
// After JDK 7
public static void main(String[] args) {

    List<String> names = Arrays.asList("John", "Ziggy", "George", "Ringo");
    
    Collections.sort(names, String::compareTo);
    
    System.out.println(names);
}
```
