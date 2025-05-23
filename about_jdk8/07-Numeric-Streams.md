# Numeric Streams

There are 3 numeric streams for primitive numeric types: `IntStream`, `LongStream` and `DoubleStream`.

## range() and rangeClosed()

The `range()` method creates an exclusive numeric stream from a start value to an end value.

```java
IntStream intStream = IntStream.range(1, 10); // [1, 2, 3, 4, 5, 6, 7, 8, 9]
DoubleStream doubleStream = DoubleStream.range(1.0, 10.0); // [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]
LongStream longStream = LongStream.range(1L, 10L); // [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

The `rangeClosed()` method creates an inclusive numeric stream from a start value to an end value.

```java
IntStream intStream = IntStream.rangeClosed(1, 10); // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
DoubleStream doubleStream = DoubleStream.rangeClosed(1.0, 10.0); // [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]
LongStream longStream = LongStream.rangeClosed(1L, 10L); // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
```

## Converting to numeric streams

The `asIntStream()`, `asDoubleStream()` and `asLongStream()` methods convert a numeric stream to another numeric stream.

```java
IntStream intStream = IntStream.of(1, 2, 3); // [1, 2, 3]
DoubleStream doubleStream = DoubleStream.of(1.0, 2.0, 3.0); // [1.0, 2.0, 3.0]
LongStream longStream = LongStream.of(1L, 2L, 3L); // [1, 2, 3]

IntStream newIntStream = doubleStream.asIntStream(); // [1, 2, 3]
DoubleStream newDoubleStream = intStream.asDoubleStream(); // [1.0, 2.0, 3.0]
LongStream newLongStream = intStream.asLongStream(); // [1, 2, 3]
```

## Aggregating numeric streams

We can use the `sum()`, `average()`, `max()`, `min()` and `count()` methods to aggregate numeric streams.

```java
IntStream intStream = IntStream.of(1, 2, 3); // [1, 2, 3]
DoubleStream doubleStream = DoubleStream.of(1.0, 2.0, 3.0); // [1.0, 2.0, 3.0]
LongStream longStream = LongStream.of(1L, 2L, 3L); // [1, 2, 3]

int sum = intStream.sum(); // 6
double average = doubleStream.average().getAsDouble(); // 2.0
int max = intStream.max().getAsInt(); // 3
int min = intStream.min().getAsInt(); // 1
long count = longStream.count(); // 3
```

## Boxing and unboxing

The `boxed()` method converts a numeric stream to a stream of objects converted to a wrapper class.

```java
IntStream intStream = IntStream.of(1, 2, 3); // [1, 2, 3]
DoubleStream doubleStream = DoubleStream.of(1.0, 2.0, 3.0); // [1.0, 2.0, 3.0]
LongStream longStream = LongStream.of(1L, 2L, 3L); // [1, 2, 3]

Stream<Integer> boxedIntStream = intStream.boxed(); // [1, 2, 3]
Stream<Double> boxedDoubleStream = doubleStream.boxed(); // [1.0, 2.0, 3.0]
Stream<Long> boxedLongStream = longStream.boxed(); // [1, 2, 3]
```

The unboxing is done by calling a `mapToInt()`, `mapToDouble()` or `mapToLong()` method.

```java
Stream<Integer> boxedIntStream = Stream.of(1, 2, 3); // [1, 2, 3]
Stream<Double> boxedDoubleStream = Stream.of(1.0, 2.0, 3.0); // [1.0, 2.0, 3.0]
Stream<Long> boxedLongStream = Stream.of(1L, 2L, 3L); // [1, 2, 3]

IntStream unboxedIntStream = boxedIntStream.mapToInt(Integer::intValue); // [1, 2, 3]
DoubleStream unboxedDoubleStream = boxedDoubleStream.mapToDouble(Double::doubleValue); // [1.0, 2.0, 3.0]
LongStream unboxedLongStream = boxedLongStream.mapToLong(Long::longValue); // [1, 2, 3]

// or

IntStream unboxedIntStream = boxedIntStream.mapToInt(i -> i); // [1, 2, 3]
DoubleStream unboxedDoubleStream = boxedDoubleStream.mapToDouble(d -> d); // [1.0, 2.0, 3.0]
LongStream unboxedLongStream = boxedLongStream.mapToLong(l -> l); // [1, 2, 3]

// or

IntStream unboxedIntStream = boxedIntStream.mapToObject(i -> { return new Integer(i); }); // [1, 2, 3]
DoubleStream unboxedDoubleStream = boxedDoubleStream.mapToObject(d -> { return new Double(d); }); // [1.0, 2.0, 3.0]
LongStream unboxedLongStream = boxedLongStream.mapToObject(l -> { return new Long(l); }); // [1, 2, 3]
```
