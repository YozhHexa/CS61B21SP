package tester;
import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

import java.util.Deque;

public class TestArrayDequeEC {

    @Test
    public void arrayTestOne() {
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();

        /**
         * @source StudentArrayDequeLauncher.java
         */
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        String operationMessage = "";
        for (int i = 0; i < 1000; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                sad1.addLast(i);
                ads.addLast(i);
                operationMessage += "addLast(" + i + ")" + "\n";
            } else if (numberBetweenZeroAndOne < 0.6) {
                sad1.addFirst(i);
                ads.addFirst(i);
                operationMessage += "addFirst(" + i + ")" + "\n";
            } else if (numberBetweenZeroAndOne < 0.7) {
                if (!sad1.isEmpty() && !ads.isEmpty()) {
                    operationMessage += "removeFirst()" + "\n";
                    assertEquals(operationMessage, ads.removeFirst(), sad1.removeFirst());
                }
            } else if (numberBetweenZeroAndOne < 0.8) {
                if (!sad1.isEmpty() && !ads.isEmpty()) {
                    operationMessage += "removeLast()" + "\n";
                    assertEquals(operationMessage, ads.removeLast(), sad1.removeLast());
                }
            } else if (numberBetweenZeroAndOne < 0.9) {
                int s1 = sad1.size() / 2;
                int s2 = ads.size() / 2;
                operationMessage += "size()" + "\n";
                assertEquals(operationMessage, s2, s1);
                if (s1 != 0 && s2 != 0) {
                    operationMessage += "get(" + s2 + ")" + "\n";
                    assertEquals(operationMessage, ads.get(s1), sad1.get(s2));
                }

            } else {
                operationMessage += "isEmpty()" + "\n";
                assertEquals(operationMessage, ads.isEmpty(), sad1.isEmpty());
            }
        }

    }
}
