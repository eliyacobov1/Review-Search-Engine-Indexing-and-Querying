package webdata;
import java.io.*;
import java.util.ArrayList;


class ReviewPreprocessor
{
    private static final int META_DATA_FIELDS = 4;
    private BufferedReader reader;
    // [0] - productId, [1] helpfulness numerator, [2] - helpfulness denominator, [3] - score
    protected ArrayList<String> currentReviewMetaData;
    private String[] reviewText;

    /**
     * constructor for ReviewPreprocessor class
     * @param inputFile path to file with reviews to index.
     */
    ReviewPreprocessor(String inputFile)
    {
        try {
            FileReader rawData = new FileReader(inputFile);
            reader = new BufferedReader(rawData);
            currentReviewMetaData = new ArrayList<>(META_DATA_FIELDS);
            for (int i = 0; i< META_DATA_FIELDS; i++) {
                currentReviewMetaData.add("");
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("error opening file");
            System.exit(-1);
        }
    }

    /**
     * returns the string that comes after the first colon
     * @param text String with colon in it
     * @return String coming after te colon
     */
    private String getText(String text) {
        String[] strArr = text.split(": ", 2);
        return strArr[1];
    }

    /**
     * returns the string that comes after the colon
     * @param line String with colon in it
     * @return String coming after te colon
     */
    private String getInfo(String line) {
        String[] strArr = line.split(": ");
        return strArr[1];
    }


    /***
     * Reads a single review from the input. Stores the meta data in the meta data list
     * and the the normalized text in the current reviewText.
     */
    private void readNextReview() throws IOException{
        String currLine = reader.readLine();
        while (!currLine.contains("product/productId:"))
            currLine = reader.readLine();

        currentReviewMetaData.set(0, getInfo(currLine));

        while (!currLine.contains("review/helpfulness:"))
            currLine = reader.readLine();

        String helpfulness = getInfo(currLine);
        String[] strArr = helpfulness.split("/");
        String numerator = strArr[0];
        String denominator = strArr[1];
        currentReviewMetaData.set(1, numerator);
        currentReviewMetaData.set(2, denominator);

        while (!currLine.contains("review/score:"))
            currLine = reader.readLine();

        currentReviewMetaData.set(3, getInfo(currLine));

        while (!currLine.contains("review/text:"))
            currLine = reader.readLine();
        currLine = getText(currLine);
        reviewText = currLine.split("[^A-Za-z0-9]++"); // take only alpha-numeric characters
        reviewText = Utils.removeEmptyString(reviewText); // remove empty tokens
        for (int i = 0; i < reviewText.length; i++) {
            reviewText[i] = reviewText[i].toLowerCase();
        }
    }

    /**
     * checks if there are more reviews to process.
     * @return True if there are more reviews to process. otherwise, false
     */
    boolean hasMoreReviews() {
        try {

            reader.mark(1024);
            String line = reader.readLine();
            while(line != null){
                if(line.contains("product")){
                    reader.reset();
                    return true;
                }
                else{
                    line = reader.readLine();
                }
            }
            reader.reset();
            return false;
        }
        catch (IOException e){
            Utils.handleException(e);
            return false;
        }
    }

    /**
     * returns array list with the meta data of the last review the was read
     * @return array list of strings with the meta data of the last review the was read
     */
    ArrayList<String> getNextReview(){
        try {
            readNextReview();
        }
        catch (IOException e)
        {
            Utils.handleException(e);
        }
        return currentReviewMetaData;
    }

    /**
     * returns the text of the last review that was read. Text is normalized.
     * @return array of Strings of the text from the last review.
     */
    String[] getReviewText() {
        return reviewText;
    }
}
