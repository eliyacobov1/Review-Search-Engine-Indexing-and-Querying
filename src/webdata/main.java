package webdata;


public class main {

    public static void main(String[] args)
    {
        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\100.txt", "");
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
        System.out.println(ir.getTokenFrequency("backpack"));
        System.out.println(ir.getTokenFrequency("badminton"));
        System.out.println(ir.getTokenFrequency("ballet"));
        //        String token = "bag";
//        String word = "bake";
//        System.out.println(token.compareTo(word));

    }
}
