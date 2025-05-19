# Variables

In this section, we will learn how to use manage variables in lambda expressions.

First rule, lambdas are allowed to use local variables, but they are not allowed to modify them. This introduces us the concept of **effectively final variables** even though they are not explicitly final. That's necessary in order to java be capable to perform concurrency operations effectively.

This rule has one exception: class level variables or instance variables are allowed for modification.

You can find an example of this rule [here](./src/VariablesInLambdasExample.java).
