# Stream

Streams in simple terms are just a sequence of data elements from a source that supports aggregate operations on them that can be processed in both a sequential and parallel manner. It doesn't support indexed access to the elements but support lazily evaluation of the elements.

Streams were designed to work with lambdas and functional interfaces. Each intermediate operation is lazily executed and returns a new stream as a result.

```java
Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5);
System.out.println(stream.map(i -> i * 2).collect(Collectors.toList()));
```

Streams don't change the original data structure (list, array, etc), they only provide the result as per the pipelining methods.

You can find an example of a stream [here](./src/StreamExample.java).

## Collections vs Streams

|  | Collections | Streams |
| --- | --- | --- |
| **Storage** | Stores all elements in memory | Only stores intermediate results |
| **Evaluation** | Eager evaluation | Lazy evaluation |
| **Access** | All elements are accessible | Only the current element is accessible |
| **Order** | Maintains the order | Order is not guaranteed |
| **Performance** | Slower than streams | Faster than collections |
| **Memory** | Higher memory usage | Lower memory usage |
| **Iteration** | Multiple iterations (over the collection) | Single iteration (internally) |
| **Data manipulation** | Can read and write | Read only |
| **Usage** | Collection of the data itself | About computation on data |

You can find an example comparing collections and streams [here](./src/CollectionVsStreamsExample.java).

## Stream map()

The `map()` function is designed to transform the elements of a stream using a function. It returns a new stream that contains the transformed elements which may be of a different type from the original elements. It has no relation to the `Map` collection.

You can find an example of the `map()` function [here](./src/StreamMapExample.java).

## Stream flatMap()

The `flatMap()` function also transforms one type to another type. It is designed to flatten a stream of collection into a new stream by applying one-to-many transformation.

You can find an example of the `flatMap()` function [here](./src/StreamFlatMapExample.java).

## Stream filter()

The `filter()` function is used to filter the elements of a stream based on a predicate. It returns a new stream that contains only the elements that satisfy the predicate.

You can find an example of the `filter()` function [here](./src/StreamFilterExample.java).

## Stream reduce()

The `reduce()` function is used to reduce the elements of a stream to a single value using a function. It returns a single value that is the result of applying the function to the elements of the stream.

It takes two arguments: the initial value and the function to apply to the elements of the stream.

The initial value is used as the starting point of the reduction and the function is used to combine the elements of the stream.

```java
stream().reduce(initialValue, (value1, value2) -> function); // optimal case return a new value, worst case returns the initial value.
```

When used with a `BinaryOperator`, the `reduce()` function returns an `Optional` containing the reduced value if the stream is non-empty, or an empty `Optional` if the stream is empty.

```java
stream().reduce((value1, value2) -> function); // optimal case return a Optional containing the reduced value, worst case returns an empty Optional
```

You can find an example of the `reduce()` function [here](./src/StreamReduceExample.java).

## Comparator maxBy() and minBy()

The `maxBy()` and `minBy()` functions are used to find the maximum or minimum element of a stream based on a comparator.

You can find an example of the `maxBy()` and `minBy()` functions [here](./src/StreamMaxByMinByExample.java).

## Stream limit() and skip()

The `limit()` function is used to limit the number of elements in a stream. It returns a new stream that contains the first `n` elements of the original stream.

The `skip()` function is used to skip the first `n` elements of a stream and return a new stream that contains the remaining elements.

You can find an example of the `limit()` and `skip()` functions [here](./src/StreamLimitSkipExample.java).

## Stream anyMatch() and noneMatch() and allMatch()

The `anyMatch()`, `noneMatch()` and `allMatch()` functions are used to check if any, none or all elements of a stream satisfy a predicate, respectively.

You can find an example of the `anyMatch()`, `noneMatch()` and `allMatch()` functions [here](./src/StreamMatchesExample.java).

## Stream findFirst() and findAny()

The `findFirst()` and `findAny()` functions are used to find the first or any element of a stream, respectively.

You can find an example of the `findFirst()` and `findAny()` functions [here](./src/StreamFindExample.java).
