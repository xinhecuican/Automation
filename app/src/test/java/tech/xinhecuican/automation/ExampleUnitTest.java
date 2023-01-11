package tech.xinhecuican.automation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testClass(){
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = list1;
        list1.add(1);
        assertEquals(list1.size(), list2.size());
        list1.remove(0);
        assertEquals(list1.size(), list2.size());
    }
}