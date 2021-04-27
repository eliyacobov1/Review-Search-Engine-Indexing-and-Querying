package webdata;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SlowIndexWriter
{
    private RandomAccessFile invertedIndexFile = null;
    private RandomAccessFile reviewDataFile = null;
    private StringBuilder accumulatedString = new StringBuilder();
    private int pos = 0;

    /**
     * Name of the inverted index output file
     */
    private static final String invertedIndexFileName = "inverted_index";

    /**
     * Name of the review meta-data output file
     */
    private static final String reviewDataFileName = "reviews meta data";


    /**
     * this function writes the accumulated string ,which represents the encoded
     * concatenation of the inverted index of the token collection, into the output-file
     * in the form of binary data
     */
    private void writeInvertedIndex(){
        try {
            invertedIndexFile.seek(0);
            invertedIndexFile.write(Utils.binaryStringToByte(accumulatedString.toString()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * creates and returns a list containing the meta data of a review
     * @param productId String representing the productId of the product reviewed
     * @param numerator String representing the numerator of the helpfulness of the review
     * @param denominator String representing the denominator of the helpfulness of the review
     * @param score String representing the score of the review
     * @param length String representing the length of the text of the review
     * @return ArrayList<String> with meta data of review
     */
    private ArrayList<String> getMetadata(String productId, String numerator, String denominator, String score, String length) {
        ArrayList<String> metaData = new ArrayList<>();
        metaData.add(productId);
        metaData.add(numerator);
        metaData.add(denominator);
        metaData.add(score);
        metaData.add(length);
        return metaData;
    }

    /**
     * for updating hash maps with String key and arrayList values. If key isn't in hashmap yet - creates new one.
     * adds the given value to the array list the is at map[word]
     * @param word String key of hashmap
     * @param mapToUpdate HashMap<String, ArrayList<Integer>> that is to be updated
     * @param toAdd Integer value to add to the array list of word
     */
    private void updateArrayList(String word, HashMap<String, ArrayList<Integer>> mapToUpdate, Integer toAdd) {
        ArrayList<Integer> reviewList = mapToUpdate.get(word);
        if (reviewList == null) {
            reviewList = new ArrayList<>();
        }
        reviewList.add(toAdd);
        mapToUpdate.put(word, reviewList);
    }

    /**
     * Iterates over input reviews given and collects all needed data and updates data structures used for creating
     * dictionary and inverted index
     * @param wordCountTotal how many times did word appear in total
     * @param wordInReviewsCount how many reviews did word appear in
     * @param reviewsWordIsIn list of reviewId's word appears in
     * @param countOfWordInReview list of how many times did word appear in a review
     * @param reviewsMetaData list of lists with metadata of each review
     * @param numOfTotalTokens total amount of tokens in data
     * @param reviewId counter indicating review number
     * @param inputFile path to input file
     */
    private void processReviews(HashMap<String, Integer> wordCountTotal, HashMap<String, Integer> wordInReviewsCount,
                                HashMap<String, ArrayList<Integer>> reviewsWordIsIn,
                                HashMap<String, ArrayList<Integer>> countOfWordInReview,
                                ArrayList<ArrayList<String>> reviewsMetaData,
                                int[] numOfTotalTokens, int[] reviewId, String inputFile)
    {
        ReviewPreprocessor rp = new ReviewPreprocessor(inputFile);
        while (rp.hasMoreReviews()) {
            ArrayList<String> reviewData = rp.getNextReview();
            String[] text = rp.getReviewText();
            reviewsMetaData.add(getMetadata(reviewData.get(0), reviewData.get(1), reviewData.get(2), reviewData.get(3),
                    Integer.toString(text.length)));

            HashMap<String, Integer> wordCountInThisReview = new HashMap<>();   // for counting how many time each word appeared in the text
            // iterate over text. count amount of tokens (with repetitions)
            // and count amount of times word appeared in text
            Integer prevVal;
            for (String word : text) {
                numOfTotalTokens[0]++;
                prevVal = wordCountInThisReview.putIfAbsent(word, 1);
                if (prevVal != null) {
                    wordCountInThisReview.put(word, ++prevVal);
                }
            }

            // enter/update "productId" into data structures
            String productId = reviewData.get(0);
            prevVal = wordCountInThisReview.putIfAbsent(productId, 1);
            if (prevVal != null) {
                wordCountInThisReview.put(productId, ++prevVal);
            }

            // enter/update each significant word into data structures
            for (String word : wordCountInThisReview.keySet()) {
                // update total count of this word
                prevVal = wordCountTotal.putIfAbsent(word, wordCountInThisReview.get(word));
                if (prevVal != null) {
                    wordCountTotal.put(word, prevVal + wordCountInThisReview.get(word));
                }

                // update amount of reviews this word appears in
                prevVal = wordInReviewsCount.putIfAbsent(word, 1);
                if (prevVal != null) {
                    wordInReviewsCount.put(word, ++prevVal);
                }
                // update list of reviews this word appears in
                updateArrayList(word, reviewsWordIsIn, reviewId[0]);
                // update amount of times this word appears in reviews
                updateArrayList(word, countOfWordInReview, wordCountInThisReview.get(word));
            }
            reviewId[0]++;
        }
    }

    /**
     * calculates the length of the common prefix of 2 Strings
     * @param s1 first String
     * @param s2 second String
     * @return length of the common prefix
     */
    private int commonPrefix(String s1, String s2)
    {
        int len = Math.min(s1.length(), s2.length());
        for (int i=0; i < len; i++)
        {
            if (s1.charAt(i) != s2.charAt(i))
            {
                return i;
            }
        }
        return len;
    }


    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir) {
        //TODO: if dir doesn't exist create it. open files.
        //TODO: close input file inputFile. check where this should be done.
        /*---------------------- <preprocess input> ----------------------*/
        HashMap<String, Integer> wordCountTotal = new HashMap<>();
        HashMap<String, Integer> wordInReviewsCount = new HashMap<>();
        HashMap<String, ArrayList<Integer>> reviewsWordIsIn = new HashMap<>();
        HashMap<String, ArrayList<Integer>> countOfWordInReview = new HashMap<>();
        ArrayList<ArrayList<String>> reviewsMetaData = new ArrayList<>();
        int[] numOfTotalTokens = {0};
        int[] reviewId = {1};
        processReviews(wordCountTotal, wordInReviewsCount, reviewsWordIsIn, countOfWordInReview, reviewsMetaData,
                numOfTotalTokens, reviewId, inputFile);

        ArrayList<String> sortedVocabulary = new ArrayList<>(reviewsWordIsIn.keySet());
        Collections.sort(sortedVocabulary); // sort vocabulary to insert into dictionary and index

        /*-----------------  insert data into dictionary, inverted index and metadata file -----------------*/
        /*------- <dictionary and inverted index> -------*/

        try {
            if (!Files.exists(Paths.get(dir)))
            {
                Files.createDirectory(Paths.get(dir));
            }
            invertedIndexFile = new RandomAccessFile(Utils.getPath(dir, invertedIndexFileName), "rw");
        }
        catch (IOException e) { e.printStackTrace(); }

        Dictionary dict = new Dictionary(sortedVocabulary.size(), numOfTotalTokens[0]);
        ListIterator<String> vocabIter = sortedVocabulary.listIterator();
        String prevWord = "";
        int index = 0;
        while (vocabIter.hasNext())
        {
            index = vocabIter.nextIndex();
            String word = vocabIter.next();
            // write to index and save pointer
            ArrayList<Integer> invertedIndex = reviewsWordIsIn.get(word);
            ArrayList<Integer> wordCount = countOfWordInReview.get(word);
            // write to index and save pointer
            int postingPrt = pos;
            for(int i = 0; i < invertedIndex.size(); i++){
                int diff = invertedIndex.get(i)- (i > 0 ? invertedIndex.get(i-1) : 0);
                String encodedIndex = Utils.gammaRepr(diff, true);
                String encodedCount = Utils.gammaRepr(wordCount.get(i), true);
                accumulatedString.append(encodedIndex); // write next index to file
                accumulatedString.append(encodedCount); // write token count to file
                pos += encodedIndex.length();
                pos += encodedCount.length();
            }
            int freq = wordCountTotal.get(word);
            int prefixLen = commonPrefix(word, prevWord);
            if (index % Dictionary.K == 0)      // first word of block
            {
                dict.addFirstWordInBlock(word, freq, postingPrt);
            }
            else if ((index + 1) % Dictionary.K == 0) // last word of block
            {
                dict.addLastWordOfBlock(word, freq, postingPrt, prefixLen);
            }
            else // middle word of block
            {
                dict.addMiddleWordOfBlock(word, freq, postingPrt, prefixLen);
            }
            prevWord = word;
        }
        dict.sizeOfLastBlock = ((index) % Dictionary.K) +1;
        dict.lastWordEnding = pos;
        dict.amountOfReviews = reviewId[0] - 1;
        dict.numPaddedZeroes = accumulatedString.length()%8 == 0 ?
                0 : 8 - accumulatedString.length() % 8; // pad with zeroes in order to fit data into bytes
        /*------- </dictionary and inverted index> -------*/

        /*------------------ <metadata> ------------------*/
        // write review meta data fields to the review data file
        ArrayList<String> meta;
        try {
            reviewDataFile = new RandomAccessFile(Utils.getPath(dir, reviewDataFileName), "rw");
            reviewDataFile.seek(0);
        }
        catch (IOException e) { e.printStackTrace(); }
        for (int i = 0; i < reviewId[0]-1; i++)
        {
            meta = reviewsMetaData.get(i);
            try {
                reviewDataFile.write(Utils.productIDToByteArray(meta.get(0))); // product id
                reviewDataFile.write((byte)Integer.parseInt(meta.get(1))); // numerator
                reviewDataFile.write((byte)Integer.parseInt(meta.get(2))); // denominator
                reviewDataFile.write((byte)(int)Double.parseDouble(meta.get(3))); // score
                int length = Integer.parseInt(meta.get(4));
                reviewDataFile.write(length & 0xff); // length- first byte
                reviewDataFile.write((length >> 8) & 0xff); // length- second byte
            } catch (IOException e) { e.printStackTrace(); }
        }
        try {
            reviewDataFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*----------------- <write dictionary and inverted index to disk> -----------------*/
        writeInvertedIndex();
        dict.writeDictToDisk(dir);
        Utils.safelyCloseStreams(invertedIndexFile, reviewDataFile);
    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File directory = new File(dir);
        File[] entries = directory.listFiles();
        if (entries != null)
        {
            for (File file: entries) {
                if (!file.delete()) {
                    System.out.println("fail to delete file");
                }
            }
        }
        if (!directory.delete()) {
            System.out.println("fail to delete directory");
        }
    }
}