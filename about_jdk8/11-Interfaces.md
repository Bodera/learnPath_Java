# Interface changes

Before JDK 8 an interface was just a class used to define a contract, we can't define any implementation at all, only the class which implements it. That makes the evolution of the interface impossible.

In JDK 8 interfaces are allowed to have _default_ and _static_ methods. 

| Default | Static |
| --- | --- |
| `default` | `static` |
| Method is allowed to create method with body | Traditional static method with a body |
| Method can be overridden | Method cannot be overridden |
| Method is accessible from the subclass | Method is accessible from the interface |
| Upgrade is possible | Limited access |

You can find an example of interfaces changes [here](./src/interfacechanges/InterfaceChangesExample.java).

## Collections.sort()

The `Collections.sort()` method was updated in JDK 8 to accept a _comparator_ parameter.

```java
List<String> list = new ArrayList<>();
list.add("a");
list.add("e");
list.add("d");
list.add("b");
list.add("c");

Collections.sort(list);
list.sort(Comparator.naturalOrder());
list.sort(Comparator.reverseOrder());
```

You can see an example of a custom `Comparator` [here](./src/interfacechanges/ComparatorExample.java).