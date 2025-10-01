package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> rightList = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();
        for(int i = 0; i < 3; i++){
            rightList.addLast(i);
            buggyList.addLast(i);
        }
        assertEquals(rightList.removeLast(), buggyList.removeLast());
        assertEquals(rightList.removeLast(), buggyList.removeLast());
        assertEquals(rightList.removeLast(), buggyList.removeLast());
    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggyList.addLast(randVal);
                System.out.println("addLast(" + randVal + ") to both list");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int sizeBuggyList = buggyList.size();
                System.out.println("no resizing list size: " + size);
                System.out.println("buggy list size: " + sizeBuggyList);
                assertEquals(sizeBuggyList, size);
            } else if (operationNumber == 2) {
                // getLast
                if(L.size() <= 0 || buggyList.size() <= 0){
                    continue;
                }
                int lastNumber = L.getLast();
                int lastNumberBuggyList = buggyList.getLast();
                System.out.println("no resizing list last number: " + lastNumber);
                System.out.println("buggy list last number: " + lastNumberBuggyList);
                //assertEquals(lastNumberBuggyList, lastNumber);
            } else if (operationNumber == 3) {
                // removeLast
                if(L.size() <= 0){
                    continue;
                }
                int lastNumber = L.removeLast();
                int lastNumberBuggyList = buggyList.removeLast();
                System.out.println("no resizing list remove last number: " + lastNumber);
                System.out.println("buggy list remove last number: " + lastNumberBuggyList);
                assertEquals(lastNumberBuggyList, lastNumber);
            }
        }
    }

    @Test
    public void buggyAlistTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();

        int randVal = StdRandom.uniform(0, 100);
        L.addLast(randVal);
        buggyList.addLast(randVal);
        System.out.println("addLast(" + randVal + ") to both list");
    }
}
