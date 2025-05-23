# Stream Factory Methods

## Stream.iterate()

The `Stream.iterate()` method creates a stream that produces a sequence of values by repeatedly applying a function to a seed value.

```java
UnaryOperator<Integer> f = n -> n * 2;
Stream<Integer> stream = Stream.iterate(1, f); // infinite stream
stream.limit(10).forEach(System.out::println);
```

## Stream.generate()

The `Stream.generate()` method creates a stream that produces a sequence of values by repeatedly applying a supplier function.

```java
Supplier<Integer> s = () -> (int) (Math.random() * 100);
Stream<Integer> stream = Stream.generate(s); // infinite stream
stream.limit(10).forEach(System.out::println);
```

## Stream.of()

The `Stream.of()` method creates an ordered stream from a variable number of arguments.

```java
Stream<Integer> stream = Stream.of(1); // [1]
stream.forEach(System.out::println);

// Using varargs
Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5); // [1, 2, 3, 4, 5]
stream.forEach(System.out::println);
```

## Stream.concat()

The `Stream.concat()` method creates a stream that concatenates two other streams.

```java
Stream<Integer> stream1 = Stream.of(1, 2, 3);
Stream<Integer> stream2 = Stream.of(4, 5, 6);
Stream<Integer> stream3 = Stream.concat(stream1, stream2); // [1, 2, 3, 4, 5, 6]
stream3.forEach(System.out::println);
```

## Stream.empty()

The `Stream.empty()` method creates an empty stream.

```java
Stream<Integer> stream = Stream.empty(); // []
stream.forEach(System.out::println);
```

## Stream.builder()

The `Stream.builder()` method creates a stream builder.

```java
Stream<Integer> stream = Stream.builder()
    .add(1)
    .add(2)
    .add(3)
    .build(); // [1, 2, 3]
stream.forEach(System.out::println);
```
