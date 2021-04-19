package webdata;

import java.util.*;

public class SlowIndexWriter
{
    public Dictionary dict;//TODO: delete
    /**
     * creates and returns a list containing the meta data of a review
     * @param productId String representing the productId of the product reviewed
     * @param numerator String representing the numerator of the helpfulness of the review
     * @param denominator String representing the denominator of the helpfulness of the review
     * @param score String representing the score of the review
     * @param length String representing the length of the text of the review
     * @return ArrayList<String> with meta data of review
     */
    private ArrayList<String> getMetadata(String productId, String numerator, String denominator, String score, String length)
    {
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
    private void updateArrayList(String word, HashMap<String, ArrayList<Integer>> mapToUpdate, Integer toAdd)
    {
        ArrayList<Integer> reviewList = mapToUpdate.get(word);
        if (reviewList == null)
        {
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
        while (rp.hasMoreReviews())
        {
            ArrayList<String> reviewData = rp.getNextReview();
            String[] text = rp.getReviewText();

            reviewsMetaData.add(getMetadata(reviewData.get(0), reviewData.get(1), reviewData.get(2), reviewData.get(3), Integer.toString(text.length)));

            HashMap<String, Integer> wordCountInThisReview = new HashMap<>();   // for counting how many time each word appeared in the text
            // iterate over text. count amount of tokens (with repetitions)
            // and count amount of times word appeared in text
            for (String word : text)
            {
                numOfTotalTokens[0]++;
                Integer prevVal = wordCountInThisReview.putIfAbsent(word, 1);
                if (prevVal != null)
                {
                    wordCountInThisReview.put(word, ++prevVal);
                }
            }

            // enter/update "productId" into data structures
            String productId = reviewData.get(0);
            updateArrayList(productId, reviewsWordIsIn, reviewId[0]);

            updateArrayList(productId, countOfWordInReview, 1);

            // enter/update each significant word into data structures
            for (String word : wordCountInThisReview.keySet())
            {
                // update total count of this word
                Integer prevVal = wordCountTotal.putIfAbsent(word, wordCountInThisReview.get(word));
                if (prevVal != null)
                {
                    wordCountTotal.put(word, prevVal + wordCountInThisReview.get(word));
                }

                // update amount of reviews this word appears in
                prevVal = wordInReviewsCount.putIfAbsent(word, 1);
                if (prevVal != null)
                {
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
    public void slowWrite(String inputFile, String dir)
    {
    /*
    data structures:
    hashmaps:

    wordCountTotal - how many times did word appear in total
    wordInReviews - how many reviews did word appear in (maybe not needed, this is the same as len(reviewsWordIsIn))
    reviewsWordIsIn - list of reviewId's word appears in
    countOfWordInReview - list of how many times did word appear in a review (matching indices to reviewsWordIsIn)
    *i.e reviewsWordIsIn["dog"] = (1,5,12),  countOfWordInReview["dog"] = (3,1,6)

    Lists:
    reviewMetaData - productId, score, numerator, denominator, length

    counters:
    numOfReviews - total amount of reviews processed
    numOfTotalTokens - total amount of tokens in data

    flow:
    1) read review from input File.
    2) preprocess text (break into tokens and normalize)
    3) update data structures and counters
    4) repeat until end of inputFile

    after processing all reviews - build dictionary and index.
     */
        HashMap<String, Integer> wordCountTotal = new HashMap<>();
        HashMap<String, Integer> wordInReviewsCount = new HashMap<>();
        HashMap<String, ArrayList<Integer>> reviewsWordIsIn = new HashMap<>();
        HashMap<String, ArrayList<Integer>> countOfWordInReview = new HashMap<>();
        ArrayList<ArrayList<String>> reviewsMetaData = new ArrayList<>();
        int[] numOfTotalTokens = {0};
        int[] reviewId = {1};      // TODO: do we start from 0 or 1? if from 1 - take in account in dictionary
        processReviews(wordCountTotal, wordInReviewsCount, reviewsWordIsIn, countOfWordInReview, reviewsMetaData,
                numOfTotalTokens, reviewId, inputFile);

        // sort vocabulary to insert into dictionary and index
        ArrayList<String> sortedVocabulary = new ArrayList<>(wordCountTotal.keySet());
        Collections.sort(sortedVocabulary);
        //--------------------------------------------------
        // sanity check for dictionary creation
        // background, backpack, backpacking, backwards, bad, badly, badminton, bag, baggage, bake, baker, balcony, bald, ball, ballet

//        wordCountTotal.clear();
//        wordCountTotal.put("background", 111);
//        wordCountTotal.put("backpack", 111);
//        wordCountTotal.put("backpacking", 1111);
//        wordCountTotal.put("backwards", 1);
//        wordCountTotal.put("bad", 2);
//        wordCountTotal.put("badly", 3);
//        wordCountTotal.put("badminton", 4);
//        wordCountTotal.put("bag", 5);
//        wordCountTotal.put("baggage", 6);
//        wordCountTotal.put("bake", 7);
//        wordCountTotal.put("baker", 8);
//        wordCountTotal.put("balcony", 9);
//        wordCountTotal.put("bald", 10);
//        wordCountTotal.put("ball", 11);
//        wordCountTotal.put("ballet", 12);
//        String[] w = new String[]{"background", "backpack", "backpacking", "backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony", "bald", "ball", "ballet"};
//        String[] w = new String[]{"background", "backpack", "backpacking", "backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony"};
//        ArrayList<String> sortedVocabulary = new ArrayList(Arrays.asList(w));
        //--------------------------------------------------
//        System.out.println(sortedVocabulary);
//        System.out.println(sortedVocabulary.size());

        dict = new Dictionary(sortedVocabulary.size(), reviewId[0] -1, numOfTotalTokens[0]); //TODO: delete
//        Dictionary dict = new Dictionary(sortedVocabulary.size(), reviewId[0] -1, numOfTotalTokens[0]);
        ListIterator<String> vocabIter = sortedVocabulary.listIterator();
        String prevWord = "";

        while (vocabIter.hasNext())
        {
            int index = vocabIter.nextIndex();
            String word = vocabIter.next();
            int freq = wordCountTotal.get(word);
            // write to index and save pointer
            int postingPrt = -1; //TODO: missing
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

        // save metadata of reviews. write to file and save pointer in dictionary
        ArrayList<String> meta;
        for (int i = 0; i < reviewId[0]-1; i++) //TODO: check boundary - maybe reviewId[0] and not -1
        {
            meta = reviewsMetaData.get(i);
            int metaPtr = -11;      // TODO: write meta to file, get pointer
            dict.metaDataPtrArray[i] = metaPtr;
        }

        System.out.println(dict.concatStr);
        System.out.println(Arrays.toString(dict.blockArray));
        System.out.println(Arrays.toString(dict.dictionary));
        System.out.println();
        System.out.println(numOfTotalTokens[0]);
        System.out.println(reviewId[0] - 1);
        System.out.println(wordCountTotal);
//        System.out.println(wordInReviewsCount);
        System.out.println(reviewsWordIsIn);
//        System.out.println(countOfWordInReview);
//        System.out.println(reviewsMetaData);
//        System.out.println(sortedVocabulary);
    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {}

}

/*
1) Dictionary

concatStr:
concatenated string of words with k-1 in k coding.

for example: 2 in 3
background|pack|ing || backwards|d|ly || badminton|g|gage || bake|r|lacony || bald|l|et
0                      17                29                  43               53

blockArray:
holds "pointers" (indices in concatStr) to the beginning of blocks

index   | block ptr
--------|----------
0       |0
1       |17
2       |29
3       |43
4       |53


fullArray:
hold rest of data: length, prefix, termPtr. (Also included Freq (#appearances in all reviews) and postingPtr)

Len     | prefix | termPtr
--------|--------|--------
10      |        | 0
8       | 4      |
        | 8      |
9       |        | 17
3       | 2      |
        | 3      |
9       |        | 29
3       | 2      |
        | 3      |
4       |        | 43
5       | 4      |
        | 2      |

Binary search is done on blockArray to find matching block.
i.e: find("badly") -
does binary search on blockArray to find that "badly" is in the second block (the one the starts at concatStr[17]).
To do this we compare "badly" with the word that starts at concatStr[blockArr[(start+end)//2]] and possibly also compare
with the word that starts at concatStr[blockArr[(start+end)//2 + 1]]
for example: start = 0, end = 3. mid = (start+end)//2. compare("badly", concatStr[blockArr[mid]]).
if "badly" > concatStr[blockArr[mid]]: compare("badly", concatStr[blockArr[mid] + 1]). (*if < continue search with end = mid)
if "badly" < concatStr[blockArr[mid] + 1]: "badly" is in this block. (* if > continue search with start = mid)

then needs to find "badly" in block. TBD...


2) Inverted index

for each word in dictionary we need an entry in inverted index holding:
sequence of (reviewId, #times word is in this review), in ascending order of reviewId
for example:
the word "dog" appears in review #1 3 times, in review #5 1 time, and in review #12 6 times.
"dogs" entry in inverted index will be ((1,3), (5,1), (12,6))
? should/can these be separate? i.e entry with review Ids and entry with freq?
* */