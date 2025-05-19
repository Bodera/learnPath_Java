package functionalinterfaces;
import java.util.Comparator;
import java.util.function.BinaryOperator;

public class BinaryOperatorExample {
    public static void main(String[] args) {
        BinaryOperator<Integer> sumOperator = (i1, i2) -> i1 + i2;
        System.out.println(sumOperator.apply(2, 3)); // Output: 5

        Comparator<Integer> compareOperator = (i1, i2) -> i1.compareTo(i2);
        BinaryOperator<Integer> maxOperator = BinaryOperator.maxBy(compareOperator);
        System.out.println(maxOperator.apply(2, 3)); // Output: 3

        BinaryOperator<Integer> minOperator = BinaryOperator.minBy(compareOperator);
        System.out.println(minOperator.apply(2, 3)); // Output: 2
    }
}
