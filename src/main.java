import java.io.*;
import java.util.HashMap;

import webdata.*;


public class main {
    static float creationTime;
    static long createdIndexSize;
    static float queryTime;
    static String[] wordsToQuery = {"achieve", "2011", "B000OVGBBW", "acharlie", "affection", "alternately", "annoyance", "arguably", "atypical", "balance", "begginers", "bison", "booklet", "broadway", "ca", "cartridge", "chanukah", "chrismas", "clients", "comedy", "concern", "contrived", "coyly", "culminate", "deals", "delusional", "dhoom", "discs", "donald", "drug", "eccentric", "empathy", "environments", "except", "eyed", "favors", "fireballing", "force", "frontal", "gems", "gnc", "grief", "handled", "hearing", "hoard", "humiliated", "impassioned", "informative", "interracial", "jail", "justified", "labor", "leeches", "linuxapachemysqlphp", "loveable", "managing", "mazurka", "michael", "mo", "ms", "nauseum", "nonsense", "off", "orginally", "pagaent", "pc", "philosophy", "plopped", "poverty", "pride", "prospective", "queens", "reaching", "reels", "reminisce", "responsibility", "ringo", "ruffled", "savor", "searches", "series", "shipped", "simpler", "sluttishly", "sono", "spoiler", "steal", "strip", "suggested", "sweetheart", "teared", "their", "tiresome", "tractor", "troubadour", "unconscious", "untreatable", "verse", "vulgar", "web", "window", "wouldn", "zzzzz"};

    public static void checkBatch(int batchNum) throws IOException
    {
        DataInputStream dis = null;
        FileInputStream fis = null;

        System.out.println("batch " + batchNum);
        fis = new FileInputStream("C:\\Users\\USER\\Desktop\\webdata_index_2\\batch_" + batchNum);
        dis = new DataInputStream(fis);
        for (int i=0; i<1000; i++)
        {
            System.out.println(dis.readInt() + ", " + dis.readInt());
        }
        System.out.println();
    }

    public static void check_step2() throws IOException{
        DataInputStream dis = null;
        FileInputStream fis = null;

        checkBatch(0);
//        checkBatch(1);
//        checkBatch(2);
//        checkBatch(0);
//        checkBatch(9);

    }

    public static void singleAnalysisRun(int amountOfReviews, String inputFile, String dir){
        // setup
        Utils.AMOUNT_OF_DOCS_TO_PARSE = amountOfReviews;
        IndexWriter iw = new IndexWriter();

        // creation time
        long startTime = System.currentTimeMillis();
        iw.write(inputFile, dir);
        long endTime = System.currentTimeMillis();
        creationTime = ((endTime - startTime)/ 1000f) / 60f;

        // index size
        long indexSize = 0;
        File f = new File(Utils.getPath(dir, Utils.INVERTED_INDEX_FILE_NAME));
        indexSize += f.length();
        f = new File(Utils.getPath(dir, Utils.DICTIONARY_NAME));
        indexSize += f.length();
        f = new File(Utils.getPath(dir, Utils.REVIEW_METADATA_FILE_NAME));
        indexSize += f.length();
        createdIndexSize = indexSize;

        // query words
        IndexReader ir = new IndexReader(dir);

        startTime = System.currentTimeMillis();
        for (String word: wordsToQuery){
            ir.getReviewsWithToken(word);
        }
        endTime = System.currentTimeMillis();
        queryTime = ((endTime - startTime)/ 1000f);;
    }

    private static void analysis(String inputFile, String dir) {
        int[] amountsOfReviews = {1000, 10000, 100000, 1000000, 10000000};

        for (int amount : amountsOfReviews){

            singleAnalysisRun(amount, inputFile, dir);
            System.out.println("\n####################################################");
            System.out.println("##################### Analysis #####################");
            System.out.println("####################################################");
            System.out.println("amount of reviews: " + amount);
            System.out.println("index created in: " + creationTime + " minutes");
            System.out.println("index size: " + createdIndexSize/1000000 + " mb");
            System.out.println("100 words queried in: " + queryTime + " seconds");
            System.out.println("####################################################");
            System.out.println("####################################################\n");
        }
    }

    public static void main(String[] args) throws IOException {
        String inputFile = "C:\\Users\\USER\\Downloads\\Movies__TV\\Movies_&_TV.txt";
        String dir = "C:\\Users\\USER\\Desktop\\webdata_index_2";
        analysis(inputFile, dir);

//
//        IndexWriter iw = new IndexWriter();
//        iw.write(inputFile, dir);

//        IndexReader ir = new IndexReader("C:\\Users\\USER\\Desktop\\webdata_index_3");
//        Enumeration<Integer> res = ir.getReviewsWithToken("zuccini");
//
//
////        Enumeration<Integer> res = ir.getReviewsWithToken("a");
////        Enumeration<Integer> res = ir.getProductReviews("B006K2ZZ7K");
////        Enumeration<Integer> res = ir.getProductReviews("B000LQOCH0");
////        System.out.println("enumeration is empty: " + !res.hasMoreElements());
//        while (res.hasMoreElements())
//        {
//            System.out.println(res.nextElement());
//        }

    }


}
