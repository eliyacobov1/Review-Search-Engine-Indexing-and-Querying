package webdata;


import java.io.*;
import java.util.Arrays;

public class main {


    public static void main(String[] args) throws IOException {

        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\1000.txt", "C:\\Users\\USER\\Desktop\\webdata_index");
//        sw.removeIndex("C:\\Users\\USER\\Desktop\\webdata_index_2");
        IndexReader ir = new IndexReader("C:\\Users\\USER\\Desktop\\webdata_index");

//        Enumeration<Integer> res = ir.getReviewsWithToken("0");
//        Enumeration<Integer> res = ir.getProductReviews("B006K2ZZ7K");
//        Enumeration<Integer> res = ir.getProductReviews("B000LQOCH0");
//        Enumeration<Integer> res = ir.getProductReviews("B0009XLVG0");
//        Enumeration<Integer> res = ir.getProductReviews("B003ZFRKGO");
//        System.out.println("enumeration is empty: " + !res.hasMoreElements());
//        while (res.hasMoreElements())
//        {
//            System.out.println(res.nextElement());
//        }
//        System.out.println(ir.getTokenFrequency("prepare"));
//        System.out.println(ir.getTokenFrequency("rides"));
//        System.out.println(ir.getTokenFrequency("ridessss"));
//
//        System.out.println();
//        System.out.println(ir.getTokenCollectionFrequency("prepare"));
//        System.out.println(ir.getTokenCollectionFrequency("rides"));
//        System.out.println(ir.getTokenCollectionFrequency("ridessss"));
//
//        System.out.println(ir.getProductId(1));
//        System.out.println(ir.getProductId(3));
//        System.out.println(ir.getProductId(100));
//        System.out.println(ir.getProductId(1000));
//
//        System.out.println();
//        System.out.println(ir.getReviewScore(1));
//        System.out.println(ir.getReviewScore(3));
//        System.out.println(ir.getReviewScore(100));
//        System.out.println(ir.getReviewScore(1000));
//
//        System.out.println();
//        System.out.println(ir.getReviewHelpfulnessNumerator(1));
//        System.out.println(ir.getReviewHelpfulnessNumerator(3));
//        System.out.println(ir.getReviewHelpfulnessNumerator(100));
//        System.out.println(ir.getReviewHelpfulnessNumerator(1000));
//
//        System.out.println();
//        System.out.println(ir.getReviewHelpfulnessDenominator(1));
//        System.out.println(ir.getReviewHelpfulnessDenominator(3));
//        System.out.println(ir.getReviewHelpfulnessDenominator(100));
//        System.out.println(ir.getReviewHelpfulnessDenominator(1000));
////
//        System.out.println();
//        System.out.println(ir.getReviewLength(19));
//        System.out.println(ir.getReviewLength(3));
//        System.out.println(ir.getReviewLength(100));
        System.out.println(ir.getReviewLength(349));


    }
}
