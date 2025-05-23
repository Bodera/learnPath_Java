# Terminal Operations

Terminal operations in the context of streams refer to the final actions that process data in a stream pipeline, resulting in a specific output. These operations, such as `collect`, `count`, and `forEach`, are essential for transforming and retrieving data from streams in programming.

## Collecting streams for terminal operations

The `collect()` method is used to collect a stream using the specified collector, resulting in a specific output. The collector specifies how the stream data should be processed and transformed into the desired output.

### Collectors.joining()

The `joining()` collector is used to join the elements of a stream into a single string using a specified delimiter.

You can find an example of the `joining()` collector [here](./src/collectors/StreamJoiningExample.java).

### Collectors.mapping()

You should know that we can recovery collections from stream by using the `collect()` method and a collector for the desired type of collection.

| Collection Type | Collector Method | Collector Class |
| --- | --- | --- |
| List | `toList()` | `Collectors.toList()` |
| Set | `toSet()` | `Collectors.toSet()` |
| Map | `toMap()`, `toConcurrentMap()` | `Collectors.toMap()`, `Collectors.toConcurrentMap()` |

The `mapping()` collector is used to transform the elements of a stream using a specified function and collect them into a new collection.

You can find an example of the `mapping()` collector [here](./src/collectors/StreamMappingExample.java).

### Using Collectors for aggregation

The available collectors for aggregation are:
| Collection Type | Collector Method | Collector Class |
| --- | --- | --- |
| Count | `count()` | `Collectors.counting()` |
| Sum | `summingInt()`, `summingLong()`, `summingDouble()` | `Collectors.summingInt()`, `Collectors.summingLong()`, `Collectors.summingDouble()` |
| Average | `averagingInt()`, `averagingLong()`, `averagingDouble()` | `Collectors.averagingInt()`, `Collectors.averagingLong()`, `Collectors.averagingDouble()` |
| Min/Max | `minBy()`, `maxBy()` | `Collectors.minBy()`, `Collectors.maxBy()` |

You can find an example of the collectors for aggregation [here](./src/collectors/StreamAggregationExample.java).

### Collectors.collectingAndThen()

The `collectingAndThen()` collector is used to collect a stream using a specified collector and then apply a function to the resulting collection.

You can find an example of the `collectingAndThen()` collector [here](./src/collectors/StreamCollectingAndThenExample.java).

### Collectors.groupingBy()

The `groupingBy()` collector is used to group the elements of a stream using a specified function and collect them into a new collection.

You can find an example of the `groupingBy()` collector [here](./src/collectors/StreamSimpleGroupingByExample.java).

You can add see how we can go further with the `groupingBy()` collector [here](./src/collectors/StreamComplexGroupingByExample.java).

### Collectors.partitioningBy()

The `partitioningBy()` collector is used to partition the elements of a stream using a specified function and collect them into a new collection.

You can find an example of the `partitioningBy()` collector [here](./src/collectors/StreamPartitioningByExample.java).
