## Method References

A method reference is a reference to a method that can be used to invoke that method without an instance of the class. Think of it as a shortcut to a lambda expression.

The basic syntax for method references is:

```java
ClassName::methodName
```

Where the `::` is our **token**. Keep in mind that the target type of the expression must be a functional interface.

You can find an example of a method reference [here](./src/referencemethods/ReferenceMethodExample.java).