package webdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;


import org.junit.Test;
import org.junit.Assert;

public class ReviewSearchTest {
    
    final String DictionaryPath = "C:\\Users\\Eli\\Desktop\\university\\Web Information Retrival\\output";

    IndexReader ir = new IndexReader(DictionaryPath);
    ReviewSearch rs = new ReviewSearch(ir);


    private final String GetErrorMSG(List<String> productID, List<Integer> expected, List<Integer> actual, String name)
    {
        return name + " " + productID + " should return " + expected + " and not " + actual;
    }


    @Test
    public void getProductReviewsfShouldReturnEmpty() throws IOException
    {
        Enumeration<Integer> r = rs.vectorSpaceSearch(Collections.enumeration(Arrays.asList("blepp")), 10);
        assertTrue("blepp should not be found", !r.hasMoreElements());

        r = rs.languageModelSearch(Collections.enumeration(Arrays.asList("blepp")), 0.5, 5);
        assertTrue("blepp should not be found", Collections.list(r).size() == 0);
    }

    @Test
    public void TestCorrectCount() throws IOException
    {
        Enumeration<Integer> r = rs.vectorSpaceSearch(Collections.enumeration(Arrays.asList("they")), 5);
        assertTrue("they should return 5 elemnts", Collections.list(r).size() == 5);

        r = rs.vectorSpaceSearch(Collections.enumeration(Arrays.asList("quantity")), 1001);
        assertTrue("quantity should return 6 elemnts", Collections.list(r).size() == 6);

        r = rs.languageModelSearch(Collections.enumeration(Arrays.asList("they")), 0.5, 5);
        assertTrue("they should return 5 elemnts", Collections.list(r).size() == 5);

        r = rs.languageModelSearch(Collections.enumeration(Arrays.asList("quantity")), 0.5, 1001);
        assertTrue("quantity should return 6 elemnts", Collections.list(r).size() == 6);
    }

    @Test
    public void TestlanguageModelSearch() throws IOException
    {

        Map<List<String>, List<Integer>> map = Map.of(
            Arrays.asList("quantity", "error"), Arrays.asList(731, 2, 854, 642, 984, 1000, 532),
            Arrays.asList("error", "since"), Arrays.asList(2, 731, 47, 818, 96, 129, 807)
        );

        for(List<String> text: map.keySet())
        {
            Enumeration<Integer> r = rs.languageModelSearch(Collections.enumeration(text), 0.5, 7);
            List<Integer> actual = Collections.list(r);
            List<Integer> expected = map.get(text);
            Assert.assertTrue(GetErrorMSG(text, expected, actual, "#1"), expected.equals(actual));
        }

        map = Map.of(
            Arrays.asList("the", "peanuts"), Arrays.asList(2, 385, 367, 53, 390, 545, 647),
            Arrays.asList("error", "since"), Arrays.asList(2, 731, 47, 818, 96, 129, 807)
        );

        for(List<String> text: map.keySet())
        {
            Enumeration<Integer> r = rs.languageModelSearch(Collections.enumeration(text), 0.1, 7);
            List<Integer> actual = Collections.list(r);
            List<Integer> expected = map.get(text);
            Assert.assertTrue(GetErrorMSG(text, expected, actual, "#2"), expected.equals(actual));
        }
        
    }

    @Test
    public void TestvectorSpaceSearch() throws IOException
    {

        Map<List<String>, List<Integer>> map = Map.of(
            Arrays.asList("error", "quantity"), Arrays.asList(731, 2, 532, 642, 854, 984, 1000),
            Arrays.asList("error", "since"), Arrays.asList(2, 731, 47, 96, 97, 122, 140),
            Arrays.asList("the", "peanuts"), Arrays.asList(53, 2, 860, 647, 367, 390, 385)
        );

        for(List<String> text: map.keySet())
        {
            Enumeration<Integer> r = rs.vectorSpaceSearch(Collections.enumeration(text), 7);
            List<Integer> actual = Collections.list(r);
            List<Integer> expected = map.get(text);
            Assert.assertTrue(GetErrorMSG(text, expected, actual, "#1"), expected.equals(actual));
        }

        
        
    }
}
