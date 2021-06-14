import java.io.*;
import java.util.*;

import webdata.*;


public class main {
    static float creationTime;
    static long createdIndexSize;
    static float queryTime;

    public static void main(String[] args) throws IOException {
        String inputFile = "C:\\Users\\Eli\\Desktop\\university\\Web Information Retrival\\1000.txt";
        String dir = "C:\\Users\\Eli\\Desktop\\university\\Web Information Retrival\\output";
        IndexWriter iw = new IndexWriter();
        iw.write(inputFile, dir);
        IndexReader ir = new IndexReader(dir);
        ReviewSearch rs = new ReviewSearch(ir);
        Enumeration<Integer> queryResults = rs.languageModelSearch(
                Collections.enumeration(List.of("Hello", "when")), 0.5, 3
        );
        while(queryResults.hasMoreElements()){
            System.out.println(queryResults.nextElement());
        }
    }
}
