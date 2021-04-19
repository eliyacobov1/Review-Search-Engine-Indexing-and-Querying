package webdata;


public class main {

    public static void main(String[] args)
    {
        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\1000.txt", "");
        Dictionary d = sw.dict;
        IndexReader ir = new IndexReader("", d);
        String[] w = new String[]{"background", "backpack", "backpacking", "backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony", "bald", "ball", "ballet"};
//        String[] w = new String[]{"background", "backpack", "backpacking", "backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony"};
//        for (String s : w)
//        {
//            System.out.println(ir.findBlockOfToken(s));
//        }
//        System.out.println(ir.getLocationOfTokenInBlock(0, "backpacking"));
//        System.out.println(ir.getLocationOfTokenInBlock(1, "badd"));
//        System.out.println(ir.getLocationOfTokenInBlock(2, "baggage"));
//        System.out.println(ir.getLocationOfTokenInBlock(3, "balcony"));
//        System.out.println(ir.getLocationOfTokenInBlock(4, "ballet"));
//        System.out.println(ir.getTokenFrequency("backpack"));
//        System.out.println(ir.getTokenFrequency("badminton"));
//        System.out.println(ir.getTokenFrequency("ballet"));
        System.out.println(ir.getTokenCollectionFrequency("a"));
        System.out.println(ir.getTokenCollectionFrequency("and"));
        System.out.println(ir.getTokenCollectionFrequency("patience"));
        System.out.println(ir.getTokenCollectionFrequency("gallon"));
        System.out.println(ir.getTokenCollectionFrequency("gallonnnn"));
        System.out.println(ir.getNumberOfReviews());
        System.out.println(ir.getTokenSizeOfReviews());


    }
}
