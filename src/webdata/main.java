package webdata;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class main {

    public static void main(String[] args) throws IOException {
        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\1000.txt", "C:\\Users\\USER\\Desktop\\webdata_index");
////        Dictionary d = sw.dict;
        IndexReader ir = new IndexReader("C:\\Users\\USER\\Desktop\\webdata_index");
////        String[] w = new String[]{"background", "backpack", "backpacking", "backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony", "bald", "ball", "ballet"};
////        String[] w = new String[]{"background", "backpack", "backpacking", "backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony"};
////        for (String s : w)
////        {
////            System.out.println(ir.findBlockOfToken(s));
////        }
////        System.out.println(ir.getLocationOfTokenInBlock(0, "backpacking"));
////        System.out.println(ir.getLocationOfTokenInBlock(1, "badd"));
////        System.out.println(ir.getLocationOfTokenInBlock(2, "baggage"));
////        System.out.println(ir.getLocationOfTokenInBlock(3, "balcony"));
////        System.out.println(ir.getLocationOfTokenInBlock(4, "ballet"));
////        System.out.println(ir.getTokenFrequency("backpack"));
////        System.out.println(ir.getTokenFrequency("badminton"));
////        System.out.println(ir.getTokenFrequency("ballet"));
////        System.out.println(ir.getTokenCollectionFrequency("a"));
////        System.out.println(ir.getTokenCollectionFrequency("and"));
////        System.out.println(ir.getTokenCollectionFrequency("patience"));
////        System.out.println(ir.getTokenCollectionFrequency("gallon"));
////        System.out.println(ir.getTokenCollectionFrequency("gallonnnn"));
////        System.out.println(ir.getNumberOfReviews());
        System.out.println(ir.dictionary.concatStr);
        System.out.println(Arrays.toString(ir.dictionary.blockArray));
        System.out.println(Arrays.toString(ir.dictionary.dictionary));
//        System.out.println(ir.dictionary.tokenSizeOfReviews);
//        System.out.println(ir.getTokenSizeOfReviews());


//        FileReader fr = new FileReader("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\100.txt");
//        BufferedReader reader = new BufferedReader(fr);
//        String line = reader.readLine();
//        String info;
//        while (!line.contains("product/productId:"))
//            line = reader.readLine();
//
//        info = line.split(": ")[1];
//        System.out.println(info);
//        while (!line.contains("review/helpfulness:"))
//        {
//            line = reader.readLine();
//        }
//        info = line.split(": ")[1];
//        System.out.println(info.split("/")[0]);
//        System.out.println(info.split("/")[1]);
//
//        while (!line.contains("review/score:"))
//        {
//            line = reader.readLine();
////            System.out.println(line);
//        }
//        info = line.split(": ")[1];
//        System.out.println(info);
//
//        while (!line.contains("review/text:"))
//        {
//            line = reader.readLine();
////            System.out.println(line);
//        }
//        info = line.split(": ")[1];
//        System.out.println(info);
//
//        reader.close();
//        fr.close();

    }
}
