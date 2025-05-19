# Functional Interfaces

A functional interface is an interface that contains only one abstract method, thus they are dedicated to a single operation. They can also contain *default* and *static* methods.

Lambda expressions can be used to represent an instance of a functional interface, like `Runnable`, `Comparable`, `ActionListener` among others. You can find an example of a functional interface [here](./src/FunctionalInterfaceExample.java).

There are four pillar interfaces on JDK 8:

- `Consumer`
- `Predicate`
- `Function`
- `Supplier`

### Consumer

A consumer is a functional interface that accepts a single input argument and returns no result. It is defined like this:

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);

    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { 
            accept(t);
            after.accept(t); 
        };
    }
}
```

You can find an example of a how to use a consumer interface [here](./src/ConsumerExample.java).

### BiConsumer

A bi-consumer is a functional interface that accepts two input arguments and returns no result. It is defined like this:


```java
@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T t, U u);

    default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> { 
            accept(t, u);
            after.accept(t, u); 
        };
    }
}
```

You can find an example of a how to use a bi-consumer interface [here](./src/BiConsumerExample.java).

### Predicate

A predicate is a functional interface that accepts a single input argument and returns a boolean result. It is defined like this:

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);

    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default Predicate<T> negate() {
        return (t) -> !test(t);
    }

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    static <T> Predicate<T> isEqual(Object targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }
}
```

You can find an example of a how to use a predicate interface [here](./src/PredicateExample.java).

### BiPredicate

A bi-predicate is a functional interface that accepts two input arguments and returns a boolean result. It is defined like this:

```java
@FunctionalInterface
public interface BiPredicate<T, U> {
    boolean test(T t, U u);

    default BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (t, u) -> test(t, u) && other.test(t, u);
    }

    default BiPredicate<T, U> negate() {
        return (t, u) -> !test(t, u);
    }

    default BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (t, u) -> test(t, u) || other.test(t, u);
    }
}
```

You can find an example of a how to use a bi-predicate interface [here](./src/BiPredicateExample.java).

### Function

A function is a functional interface that accepts a single input type argument and returns a new value. It is defined like this:

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);

    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> Function<T, T> identity() {
        return t -> t;
    }
}
```

You can find an example of a how to use a function interface [here](./src/FunctionExample.java).

### BiFunction

A bi-function is a functional interface that accepts two input type arguments and returns a new value. It is defined like this:

```java
@FunctionalInterface
public interface BiFunction<T, U, R> {
    R apply(T t, U u);

    default <V> BiFunction<T, U, V> andThen(BiFunction<? super R, ? super U, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u), u);
    }
}
```

You can find an example of a how to use a bi-function interface [here](./src/BiFunctionExample.java).

### UnaryOperator

A unary operator is a functional interface that accepts a single input type argument and must return the same type as the argument. It is defined like this:

```java
@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {

    static <T> UnaryOperator<T> identity() {
        return t -> t;
    }
}
```

Since it extends the `Function` interface, it can be used in place of a function interface when both input and output types are the same.

You can find an example of a how to use a unary operator interface [here](./src/UnaryOperatorExample.java).

### BinaryOperator

A binary operator is a functional interface that accepts two arguments of same type, therefore must return the same type as the arguments. It is defined like this:

```java
@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {

    public static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    public static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }
}
```

Since it extends the `BiFunction` interface, it can be used in place of a bi-function interface when both input and output types are the same.

You can find an example of a how to use a binary operator interface [here](./src/BinaryOperatorExample.java).

### Supplier

A supplier is a functional interface that returns a result of some given type. It is defined like this:

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

You can find an example of a how to use a supplier interface [here](./src/SupplierExample.java).