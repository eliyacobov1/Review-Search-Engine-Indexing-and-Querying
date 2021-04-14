package webdata;

import java.util.*;

public class SlowIndexWriter
{

    /**
     * creates and returns a list containing the meta data of a review
     * @param productId String representing the productId of the product reviewed
     * @param numerator String representing the numerator of the helpfulness of the review
     * @param denominator String representing the denominator of the helpfulness of the review
     * @param score String representing the score of the review
     * @return ArrayList<String> with meta data of review
     */
    private ArrayList<String> getMetadata(String productId, String numerator, String denominator, String score)
    {
        ArrayList<String> metaData = new ArrayList<>();
        metaData.add(productId);
        metaData.add(numerator);
        metaData.add(denominator);
        metaData.add(score);
        return metaData;
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

            reviewsMetaData.add(getMetadata(reviewData.get(0), reviewData.get(1), reviewData.get(2), reviewData.get(3)));

            HashMap<String, Integer> wordCountInThisReview = new HashMap<>();

            for (String word : text)
            {
                numOfTotalTokens[0]++;
                Integer prevVal = wordCountInThisReview.putIfAbsent(word, 1);
                if (prevVal != null)
                {
                    wordCountInThisReview.put(word, ++prevVal);
                }
            }

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
                ArrayList<Integer> reviewList = reviewsWordIsIn.get(word);
                if (reviewList == null)
                {
                    reviewList = new ArrayList<>();
                }
                reviewList.add(reviewId[0]);
                reviewsWordIsIn.put(word, reviewList);

                // update amount of times this word appears in reviews
                ArrayList<Integer> wordCountList = countOfWordInReview.get(word);
                if (wordCountList == null)
                {
                    wordCountList = new ArrayList<>();
                }
                wordCountList.add(wordCountInThisReview.get(word));
                countOfWordInReview.put(word, wordCountList);
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
        int[] reviewId = {1};
        processReviews(wordCountTotal, wordInReviewsCount, reviewsWordIsIn, countOfWordInReview, reviewsMetaData,
                numOfTotalTokens, reviewId, inputFile);

        ArrayList<String> sortedVocabulary = new ArrayList<>(wordCountTotal.keySet());
        Collections.sort(sortedVocabulary);
        //--------------------------------------------------
        // sanity check for dictionary creation
        // backwards, bad, badly, badminton, bag, baggage, bake, baker, balcony

//        wordCountTotal.clear();
//        wordCountTotal.put("backwards", 1);
//        wordCountTotal.put("bad", 2);
//        wordCountTotal.put("badly", 3);
//        wordCountTotal.put("badminton", 4);
//        wordCountTotal.put("bag", 5);
//        wordCountTotal.put("baggage", 6);
//        wordCountTotal.put("bake", 7);
//        wordCountTotal.put("baker", 8);
//        wordCountTotal.put("balcony", 9);
//        String[] w = new String[]{"backwards", "bad", "badly", "badminton", "bag", "baggage", "bake", "baker", "balcony"};
//        ArrayList<String> sortedVocabulary = new ArrayList(Arrays.asList(w));
        //--------------------------------------------------
        System.out.println(sortedVocabulary);
        System.out.println(sortedVocabulary.size());

        Dictionary dict = new Dictionary(sortedVocabulary.size());
        ListIterator<String> vocabIter = sortedVocabulary.listIterator();
        String prevWord = "";


        while (vocabIter.hasNext())
        {
            int index = vocabIter.nextIndex();
            String word = vocabIter.next();
            int freq = wordCountTotal.get(word);
            int postingPrt = 0; //TODO: missing
            int prefixLen = commonPrefix(word, prevWord);
            if (index % Dictionary.K == 0)
            {
                // first word of block
                dict.addFirstWordInBlock(word, freq, postingPrt);
            }
            else if ((index + 1) % Dictionary.K == 0)
            {
                // last word of block
                dict.addLastWordOfBlock(word, freq, postingPrt, prefixLen);
            }
            else
            {
                // middle word of block
                dict.addMiddleWordOfBlock(word, freq, postingPrt, prefixLen);
            }
            prevWord = word;
        }

        System.out.println(dict.concatStr);
        System.out.println(Arrays.toString(dict.blockArray));
        System.out.println(Arrays.toString(dict.dictionary));
//        System.out.println(numOfTotalTokens[0]);
//        System.out.println(reviewId[0] - 1);
//        System.out.println(wordCountTotal);
//        System.out.println(wordInReviewsCount);
//        System.out.println(reviewsWordIsIn);
//        System.out.println(countOfWordInReview);
//        System.out.println(reviewsMetaData);
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
background|pack|ing || backwards|d|ly || badminton|g|gage || bake|r|lacony
0                      17                29                  43

blockArray:
holds "pointers" (indices in concatStr) to the beginning of blocks

index   | block ptr
--------|----------
0       |0
1       |17
2       |29
3       |43


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
