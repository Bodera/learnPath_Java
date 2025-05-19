package variables;

import java.util.function.Consumer;

public class VariablesInLambdasExample {

    static String name = "John"; // class variable

    public static void main(String[] args) {
        // we can't name our lambda variable the same as the local variable
        int i = 20;
        Consumer<Integer> c1 = (j) -> System.out.println(i);
        c1.accept(i);

        // 
        Consumer<String> c2 = (name) -> {
            //i++; // we can use the local variable since we don't update it
            name = "Jane";
            System.out.println("Name is now: " + name);
        };
        c2.accept(name);

        Consumer<Counter> c3 = (counter) -> {
            counter.increment();
            counter.increment();
            counter.count = 100;

            System.out.println("Count is now: " + counter.count);
        };
        Counter counter = new Counter();
        c3.accept(counter);
    }

    static class Counter {
        public int count = 0; // instance variable

        public void increment() {
            count++;
        }

        public void decrement() {
            count--;
        }
    }
    
}
