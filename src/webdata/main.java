package webdata;


import java.io.*;
import java.util.Arrays;

public class main {

    public static void main(String[] args) throws IOException {
        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\1000.txt", "C:\\Users\\USER\\Desktop\\webdata_index");
//        IndexReader ir = new IndexReader("C:\\Users\\USER\\Desktop\\webdata_index");
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
//        System.out.println(ir.dictionary.concatStr);
//        System.out.println(Arrays.toString(ir.dictionary.blockArray));
//        System.out.println(Arrays.toString(ir.dictionary.dictionary));
//        System.out.println(ir.dictionary.tokenSizeOfReviews);
//        System.out.println(ir.getTokenSizeOfReviews());
//        System.out.println(ir.getNumberOfReviews());


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
//        sw.removeIndex("C:\\Users\\USER\\Desktop\\webdata_index\\test");


        /* --------------------------- size comparison of files - object vs data ---------------------------*/
//        String path = "C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\testFile";
//        String path2 = "C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\testFileObject";
//        DataOutputStream dos = null;
//        FileOutputStream fos = null;
//        String concatStr = "00100lbs300watt61845998200x3307045506790abbydominalleoutsenceccordingaccountrosstivateuallyddedictaddingtionallyvesledvertisedingfteraftertastegainesoreelbanesecoholallergicesyowsmostonegalreadysoternativeurawaysmazingonamericaountsndyonethinganywherepartparentlyealinglesreciatesovalapprovedreamsomaundrhythmiaivearrivedtifialsksignedortmentasterisktehletevailablewayesomefulb001eo5qw8gvisjmabiesckdggiesbalanceloonsngriatricsenjitchesteriesbeachnsrsutycameusebecomesdefnrforehindbeinglieverriessttterweenveragewarebichongketterlackendwblissoatingndeodywnueodyboilingoktringstonthtlebottledomughtndwlsxboxesysrandseakfastingbreedsotherughtwnunchucksddiesbuggylldogmmedrnstyerbuyingycactusloriemelbakscandiesyesnedingotcansppuccinorbohydratesnationsriedsecastertsuseeliacntsurieshancechangedseaperrrywyickenldhoodchockolatesicesosespperristmaschubbyidernnamontrusylassosecoatedcoaffeeldleagueieormcombinationesmercialonlypanyetelaintsconcernedfectioneryirmednoisseursiderstencyumedconsumingptiontentsveniencetokingcookslrnstuldnntrycouplerseracksdlempingsnberrycravingzyeamersyispyingculpritpsrrentlytyclingdaddarkughtersysealcadesdecideepfinitelyliberatelycioussciousveredydesertpiteiabeticrrheadnetffercedifferencetgestibleonnnerrectionsdirectlytsappointedcomforteasesolvevletinctdistributedoctoringsesngsdolceheneorseubtwndrinkeringsoppedyuckduerationingeachrlierststheasilyytenrsingscstaticedmundffectivesitherlectrolytessewherememployeestyndeduranceolyteergeticglishenjoyableedoughsurevironmentalrrorscapingespeciallysentialtablishedtcvenlyeverybodyonethingxactlycellenterciseexpandectnsiveriencedmentingloredextractemelyyesfacestithfulkefamiliaryntasicakesrtlesssttfatherultvoriteededingtlidaefelltwiberguredlbertsnallydfindingeickyrmsstlyshvefizzlavoredfulingssurfloorolkslowedodsreverformsulatedundralingernkensteineefresheridgeendsgginomzefructoseuseullnrthermoregainedragesgastronomytoradeveelatinnerouststinggiantftsrlveningladglandsssueyoesingldengoneodiesnesseyturmetvernmentgprabinsyntedpefruitgrasseatengilloceryuaressguestosiltynnessmmiygustoyshabanerodirlfloweenhammerndleysppenierlyhappyrdlyymfulpsnhatedveningeadacheslthhealthieryrttedvenierlyheavylpedfulresheyighhigherlymntststingholdsmenestlyperseradishstingthotspotsursewarmingweverrefttpugehunchdredkssbandydrationiamsbsicedeasfmagineportantlyimpressedulsivenclanreaseddibledicatedindividuallygredientsstanteadulatingtedtactintakeendedrnetstinalorishonsisnsuestchingyemssjackmrswellyrryulyjumbopingstkateepsptkgibbleckedindsndskittiesleenexnownosherrogerlabellabeledradorcedmbncasterrgerstlatericeterbearnedstftlegmonssontterswisiberallylicoricefesaverghtkedsmeitlimitedneonquidyttlevedlyinglloadedcalklngerstlookedingsseingtvelovedrswfatyingmacciatomachinedegicinkesingltitolmangonerufacturedyplerketyssmassiveybeccannsealnmeasuredtdicinalumltedingntionedmessagethodxicanoicrowaveddleghtmileknutesssxingmmmolecularmneyreningstlythermotionuthfulviesuchltiplemshymusteryselfnateuraluseaearneededsighborhoodverwtonxtniceghtrvanaonermallynosetchhingiceablydwnowhereukemeroustmegritionsoakeroatmealssccasionsfferofferingicetenilskayldestolenceelineytopenedopeneringportunitytionrangederedorderinginaryganicthersuncertoutsideverlyseaswnerpackpackagesingedtsinfulpalatablentryrticularyssedtpastrydseanutslletnnsylvaniaopleppermintrperfectsistentonallypirationtcopetsicantekedyllowylaceplainnterstinumyeasedureplentyusomeranianodleppcornperpossibleyttatoundsrpowderedreparationedsscriptionervativesprettyventingiousicesmeobablylemproblemscessedorductsmptlyteinspuppiesyrchasedingtquakerqualityickteracingisinsmseynspberryratheriosweactionsdyrealizedlysonableccomendedeivedrecipeientsommendedrdsducingreesesfreshinggardingularlyithlatedmemberrememberedpresentquiredsingsearcholvedresultstrieverospecturnableviewersicerichdesiculousghtngsoastedrobitussinomttateunningssellyesalsatedwaterymenktisfiedsatisfyinguceveyscareiencescrapetchingeamingecondsreturelyducesseemslleringndsitivesentrvedsicengttlevenseveraleshakenperedingshepherdipmentpedingsoprtshorteruldickdegnificantlymilarplyncesinglekspsterststingxsizedkinnyliceghtlyothsmallerellsoothylienackingsnakesodaftggyldidifyingsolvedmeonethingimeswhatmersnsoonoooupyrcethpecificallyicesysportstsroutingquarestaffgeslestampndardpleraltedvingshtestatesyseelwickllockstomachppedrageeboughtsyvestovetoprawberryonguckffmbledubsidiarychsugarfreesygesteditedmmerpersuperblyfinemarketregeryprisespectsuzanneweetenerrnersssitchedswitchingymptomsruptabletsffyketakeslkedrgettsstedfulltastelesslysieryeallmperaturetendsquilarribleficstedtexasturehanksteirthemednreseyickenersthickerngskrdsoseughthoughtreeoatughimelysnytiredtleoastffeesgethernotoothptallyuchrailnsfereaterstrickedggerspyingubemmytvwentyinstszzlersotypesunablederstandigestiblefortunatelyiqueunitedlikenecessarypleasantsalteduspectingtilpusedsinguallyvanillarietiesvarietyouselvetyndorrmontsionyvetiolentsitingstalityoilawwantedrdrobemshntedwatchersingeryeantherwebsiteekightedllrestontwhatevereatgrassneverreastherwhichleolesomeseyidewifeldlntertchhoutonwonderfulingsrkedingsldwormsthuldnrappedongwwyyearstoungrrsumyummyzeroip";
//        int tokenSizeOfReviews = 71010;
//        int amountOfReviews = 1000;
//        int[] blockArray = new int[]{0, 16, 28, 38, 69, 98, 133, 165, 187, 220};
//        int[] dictionary = new int[]{1, 1, -1, 1, 2, 1, -1, 9, 1, 0, -1, 5, 2, 1, -1, 1, 3, 2, -1, 1, 5, 2, -1, 1, 8, 1, -1, 3, 1, -1, 1, 4, -1, 1, 2, 1, -1, 1, 4, 2, -1, 7, 1, 0, -1, 2, 2, 1, -1, 1, 3, 2, -1, 1, 2, 1, -1, 8, 0, -1, 2, 2, -1, 1, 3, 1, -1, 2, 1, 0, -1, 1, 2, 1, -1, 2, 1, 0, -1, 1, 2, 1, -1, 8, 1, 0, -1, 1, 0, -1, 1, 2, -1, 167, 1, 0, -1, 1, 4, 1, -1, 1, 9, 2, -1, 3, 4, 2, -1, 11, 5, 2, -1, 1, 7, 2, -1, 1, 1, -1, 1, 7, -1, 2, 6, 2, -1, 1, 8, 2, -1, 1, 6, 3, -1, 4, 8, 6, -1, 6, 3, 1, -1, 1, 5, 3, -1, 1, 3, -1, 1, 6, -1, 1, 8, 4, -1, 1, 12, 8, -1, 1, 9, 6, -1, 1, 6, 3, -1, 2, 10, 2, -1, 1, 11, 8, -1, 11, 1, -1, 2, 10, -1, 10, 5, 1, -1, 1, 3, 2, -1, 3, 4, 3, -1, 2, 3, 2, -1, 1, 5, 2, -1, 1, 8, 1, -1, 1, 2, -1, 31, 3, -1, 3, 8, 3, -1, 7, 9, 7, -1, 2, 7, 6, -1, 1, 6, 3, -1, 5, 6, 2, -1, 1, 5, 2, -1, 2, 4, -1, 1, 7, -1, 12, 4, 2, -1, 1, 11, 2, -1, 1, 6, 3, -1, 7, 6, 2, -1, 10, 2, 1, -1, 2, 7, 2, -1, 13, 4, -1, 1, 7, -1, 2, 6, 2, -1, 3, 7, 6, -1, 7, 2, 1, -1, 214, 3, 2, -1, 8, 3, 2, -1, 2, 6, 3, -1, 2, 3, -1, 3, 8, -1, 1, 5, 1, -1, 1, 10, 2, -1, 1, 9, 3, -1, 5, 5, 3, -1, 7, 6, 5, -1, 1, 11, 3, -1, 1, 4, -1, 1, 8, -1, 31, 3, 1, -1, 1, 4, 3, -1, 1, 4, 2, -1, 1, 5, 2, -1, 4, 6, 3, -1, 1, 10, 2, -1, 1, 3, -1};
//
//        try
//        {
//            fos = new FileOutputStream(path);
//            dos  = new DataOutputStream(fos);
//
//            dos.writeInt(concatStr.length());
//            dos.writeChars(concatStr);
////            for (int i = 0; i < concatStr.length(); i++)
////            {
////                dos.writeChar(concatStr.charAt(i));
////            }
//
//            dos.writeInt(tokenSizeOfReviews);
//            dos.writeInt(amountOfReviews);
//
//            dos.writeInt(blockArray.length);
//            for (int i: blockArray)
//            {
//                dos.writeInt(i);
//            }
//
//            dos.writeInt(dictionary.length);
//            for (int i: dictionary)
//            {
//                dos.writeInt(i);
//            }
//
//            dos.flush();
//        }
//        catch (IOException e) { e.printStackTrace(); }
//        finally
//        {
//            Utils.safelyCloseStreams(fos, dos);
//        }
//        Dictionary d = new Dictionary(tokenSizeOfReviews, concatStr, blockArray, dictionary, amountOfReviews);
//        FileOutputStream foos = null;
//        ObjectOutputStream oos = null;
//        try
//        {
//            foos = new FileOutputStream(path2);
//            oos = new ObjectOutputStream(foos);
//
//            oos.writeObject(d);
//        }
//        catch (IOException e) { e.printStackTrace(); }
//        finally
//        {
//            Utils.safelyCloseStreams(foos, oos);
//        }
    }
}
