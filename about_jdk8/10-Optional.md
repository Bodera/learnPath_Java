# Optional

The `Optional` class is a container object used to contain not-null objects. It's used to represent `null` as absence of a value, and also introduces utility methods to facilitate code maintainability to handle values as _available_ or _not available_ instead of checking for `null`.

The big promise of this class is that null checks are no longer required and thus no more NPEs are thrown at runtime. Reducing boilerplate code is another benefit.

| Method | Description |
| --- | --- |
| empty() | Returns an empty `Optional` instance |
| of(T value) | Returns an `Optional` with the specified presented not null value |
| ofNullable(T value) | Returns an `Optional` describing the specified value, an empty `Optional` if the value is null |

You can find an example of the `Optional` class [here](./src/optional/OptionalExample.java).

## Exceptions with Optional

Keep in mind that calling `Optional.get()` on an empty `Optional` will throw a `NoSuchElementException`. And calling `Optional.isPresent()` on an instance of `Optional.of(null)` will throw a `NullPointerException`.

Also, is still valid to call `Optional.orElseThrow()` on an empty `Optional`.

## Stream methods that return Optional

The `Stream` interface provides methods that return `Optional` instances. They are:

- `findFirst()`
- `findAny()`
- `min()`
- `max()`
- `reduce()`

These methods return an `Optional` because they may not always return a value (e.g., if the stream is empty). The `Optional` class provides a way to handle this possibility in a concise and expressive way.
