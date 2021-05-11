package webdata;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;


public class IndexWriter
{

    public static final int AMOUNT_OF_DOCS_TO_READ = 100;
    private HashMap<String, Integer> termIdMapping;
    private String dirName;
    private String inputFileName;

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
     * @param reviewsMetaData list of lists with metadata of each review
     * @param numOfTotalTokens total amount of tokens in data
     * @param reviewId counter indicating review number
     * @param inputFile path to input file
     */
    private void processReviews(HashMap<String, Integer> wordCountTotal,
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
//                prevVal = wordInReviewsCount.putIfAbsent(word, 1);
//                if (prevVal != null) {
//                    wordInReviewsCount.put(word, ++prevVal);
//                }
                // update list of reviews this word appears in
//                updateArrayList(word, reviewsWordIsIn, reviewId[0]);
                // update amount of times this word appears in reviews
//                updateArrayList(word, countOfWordInReview, wordCountInThisReview.get(word));
            }
            reviewId[0]++;
        }
    }

    /**
     * Step 2 of index creation process. Parses all reviews in batches. For each batch of AMOUNT_OF_DOCS_TO_READ docs,
     * creates sequence of pairs (termId,docId). When finishes going over docs, sorts pairs and then writes sorted pairs
     * to file.
     * @return amount of files creates (==number of batches)
     */
    private int sortBatches()
    {
        ReviewPreprocessor rp = new ReviewPreprocessor(inputFileName);
        int docId = 1;
        int batchId = 0;
        String batchFileNameBase = "batch_";
        while (rp.hasMoreReviews())
        {
            ArrayList<IntPair> pairs = new ArrayList<>();
            for (int i=0; i < AMOUNT_OF_DOCS_TO_READ; i++)
            {
                rp.getNextReview();
                String[] text = rp.getReviewText();
                for (String word: text)
                {
                    IntPair pair = new IntPair(termIdMapping.get(word), docId);
                    pairs.add(pair);
                }
                docId++;
            }
            Collections.sort(pairs);
            String batchFileName = batchFileNameBase + Integer.toString(batchId);
            try
            {
                RandomAccessFile batchFile = new RandomAccessFile(Utils.getPath(dirName, batchFileName), "rw");
                for (IntPair pair: pairs)
                {
                    batchFile.writeInt(pair.termId);
                    batchFile.writeInt(pair.docId);
                }
                batchFile.close();
            }
            catch (IOException e) { Utils.handleException(e);}
            batchId++;
        }
        return batchId -1;
    }

    /**
     * Step 1 of index creation process. Parses input and creates dictionary and term-termId mapping
     * @param wordCountTotal how many times did word appear in total
     * @param numOfTotalTokens total amount of tokens in data
     * @param reviewId counter indicating review number
     * @return Dictionary object with all data except for 1) pointers to invertedIndex 2) lastWordEnding 3) numPaddedZeroes
     */
    private Dictionary createDictionaryAndTermIdMap(HashMap<String, Integer> wordCountTotal, int[] numOfTotalTokens,
                                                              int[] reviewId)
    {
        ArrayList<String> sortedVocabulary = new ArrayList<>(wordCountTotal.keySet());
        Collections.sort(sortedVocabulary); // sort vocabulary to insert into dictionary and index

        /*------------ <create dictionary> ------------*/
        Dictionary dict = new Dictionary(sortedVocabulary.size(), numOfTotalTokens[0]);
        termIdMapping = new HashMap<>();

        ListIterator<String> vocabIter = sortedVocabulary.listIterator();
        String prevWord = "";
        int index = 0;
        while (vocabIter.hasNext())
        {
            index = vocabIter.nextIndex();
            String word = vocabIter.next();
            termIdMapping.put(word, index);

            int postingPtr = -1;    //TODO - probably unneeded, left for now for place holding
            int freq = wordCountTotal.get(word);
            int prefixLen = commonPrefix(word, prevWord);

            if (index % Dictionary.K == 0)      // first word of block
            {
                dict.addFirstWordInBlock(word, freq, postingPtr);
            }
            else if ((index + 1) % Dictionary.K == 0) // last word of block
            {
                dict.addLastWordOfBlock(word, freq, postingPtr, prefixLen);
            }
            else // middle word of block
            {
                dict.addMiddleWordOfBlock(word, freq, postingPtr, prefixLen);
            }
            prevWord = word;
        }
        dict.sizeOfLastBlock = ((index) % Dictionary.K) +1;
        dict.amountOfReviews = reviewId[0] - 1;
        //TODO: next 2 line are here temporary just to remind us we need to update them after we create II
//        dict.lastWordEnding = pos;
//        dict.numPaddedZeroes = accumulatedString.length()%8 == 0 ?
//                0 : 8 - accumulatedString.length() % 8; // pad with zeroes in order to fit data into bytes

        return dict;
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void slowWrite(String inputFile, String dir)
    {
        /*
        1) first pass: create dictionary and mapping of term: termId
            1.1) go over all docs and create in memory data structures (word total count)
            1.2) sort vocabulary and create mapping of term: termId
            1.3) create dictionary without pointers to II
        2) second pass: for every X docs:
            2.1) create in memory pairs of (termId, docId)
            2.2) when finished parsing X docs, sort by termId (secondary by docId)
            2.3) write to disk
        3) merge sorted files to one big sorted file
        4) read big sorted file and create II (including all steps for it: compression, update posting list pointer...)
         */
        dirName = dir;
        inputFileName = inputFile;

        HashMap<String, Integer> wordCountTotal = new HashMap<>();
        ArrayList<ArrayList<String>> reviewsMetaData = new ArrayList<>();
        int[] numOfTotalTokens = {0};
        int[] reviewId = {1};
        processReviews(wordCountTotal, reviewsMetaData, numOfTotalTokens, reviewId, inputFile);

        Dictionary dict = createDictionaryAndTermIdMap(wordCountTotal, numOfTotalTokens, reviewId);
        /* step 1 done */

        int amountOfBatchFiles = sortBatches();

        /* step 2 done */


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
