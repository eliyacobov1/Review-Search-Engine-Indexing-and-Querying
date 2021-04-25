package webdata;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class ReviewPreprocessor
{
    public static final int META_DATA_FIELDS = 4;
    private Scanner reader;
    // [0] - productId, [1] helpfulness numerator, [2] - helpfulness denominator, [3] - score
    private ArrayList<String> currentReviewMetaData;
    private String[] reviewText;

    /**
     * constructor for ReviewPreprocessor class
     * @param inputFile path to file with reviews to index.
     */
    public ReviewPreprocessor(String inputFile)
    {
        try
        {
            File rawData = new File(inputFile);
            reader = new Scanner(rawData);
            currentReviewMetaData = new ArrayList<>(META_DATA_FIELDS);
            for (int i = 0; i< META_DATA_FIELDS; i++)
            {
                currentReviewMetaData.add("");
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("error opening file");
        }
    }

    /***
     * returns the string that comes after the colon
     * @param line String with colon in it
     * @return String comming after te colon
     */
    private String getInfo(String line)
    {
        String[] strArr = line.split(": ");
        return strArr[1];
    }


    /***
     * Reads a single review from the input. Stores the meta data in the meta data list
     * and the the normalized text in the current reviewText.
     */
    private void readNextReview()
    {
        // old implementation - changed to use regex instead of just skipping lines blindly
//        String currLine;
//        currLine = reader.nextLine(); // productId
//
//        currentReviewMetaData.set(0, getInfo(currLine));
//        reader.nextLine();
//        reader.nextLine();
//
//        currLine = reader.nextLine(); // helpfulness
//        String helpfulness = getInfo(currLine);
//        String[] strArr = helpfulness.split("/");
//        String numerator = strArr[0];
//        String denominator = strArr[1];
//        currentReviewMetaData.set(1, numerator);
//        currentReviewMetaData.set(2, denominator);
//
//        currLine = reader.nextLine(); // score
//        currentReviewMetaData.set(3, getInfo(currLine));
//
//        reader.nextLine();
//        reader.nextLine();
//
//        currLine = reader.nextLine(); // text
//        currLine = getInfo(currLine);
//        reviewText = currLine.split("\\W+");
//        for (int i = 0; i < reviewText.length; i++)
//        {
//            reviewText[i] = reviewText[i].toLowerCase();
//        }
//
//        reader.nextLine();

        String currLine = reader.nextLine();
        while (!currLine.contains("product/productId:"))
            currLine = reader.nextLine();

        currentReviewMetaData.set(0, getInfo(currLine));

        while (!currLine.contains("review/helpfulness:"))
            currLine = reader.nextLine();

        String helpfulness = getInfo(currLine);
        String[] strArr = helpfulness.split("/");
        String numerator = strArr[0];
        String denominator = strArr[1];
        currentReviewMetaData.set(1, numerator);
        currentReviewMetaData.set(2, denominator);

        while (!currLine.contains("review/score:"))
            currLine = reader.nextLine();

        currentReviewMetaData.set(3, getInfo(currLine));

        while (!currLine.contains("review/text:"))
            currLine = reader.nextLine();

        currLine = getInfo(currLine);
//        reviewText = currLine.split("\\W+"); // take only alpha-numeric characters
        reviewText = currLine.split("[^A-Za-z0-9]++"); // take only alpha-numeric characters
        for (int i = 0; i < reviewText.length; i++)
        {
            reviewText[i] = reviewText[i].toLowerCase();
        }

    }

    /**
     * checks if there are more reviews to process.
     * @return True if there are more reviews to process. otherwise, false
     */
    public boolean hasMoreReviews()
    {
        return reader.hasNext("product/productId:");
    }

    /**
     * returns array list with the meta data of the last review the was read
     * @return array list of strings with the meta data of the last review the was read
     */
    public ArrayList<String> getNextReview()
    {
        readNextReview();
        return currentReviewMetaData;
    }

    /**
     * returns the text of the last review that was read. Text is normalized.
     * @return array of Strings of the text from the last review.
     */
    public String[] getReviewText()
    {
        return reviewText;
    }

}
