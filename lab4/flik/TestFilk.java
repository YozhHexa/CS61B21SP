package flik;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestFilk {
    @Test
    public void testOne(){
        int a = 128;
        int b = 128;
        assertTrue(a == b);
        return;
    }

    @Test
    public void testTwo(){
        int a = 128;
        int b = 128;
        assertTrue(Flik.isSameNumber(a, b));
        return;
    }

    @Test
    public void testThree(){
        int a = 129;
        int b = 129;
        assertTrue(Flik.isSameNumber(a, b));
        return;
    }

    @Test
    public void testFour(){
        int a = 127;
        int b = 127;
        assertTrue(Flik.isSameNumber(a, b));
        return;
    }
}
