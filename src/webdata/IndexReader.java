package webdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.io.*;


public class IndexReader {
    private static final int POINTER_TO_AFTER_DICT = -1;
    private Dictionary dictionary;
    private RandomAccessFile invertedIndexFile = null;
    private RandomAccessFile reviewDataFile = null;
    private static final int reviewSize = 15;
    private static final int productIdLength = 10; // the length of all product id's
    private int numPaddedZeroes;
    private final String dirName;

    /**
     * indexes for the fields of review meta-data
     */
    private static final int productIdIndex = 0, numeratorIndex = 1, denominatorIndex = 2,
            scoreIndex = 3, lengthIndex = 4;

    /**
     * Name of the inverted index output file
     */
    private static final String invertedIndexFileName = "inverted_index";

    /**
     * Name of the review meta-data output file
     */
    private static final String reviewDataFileName = "reviews meta data";

    /**
     * reads an array of ints that was previously saved to disk. how much to read is stored before the data itself.
     * @param dis DataInputStream to read from
     * @return array of ints that was read
     */
    private int[] readIntArray(DataInputStream dis) throws IOException {
        int arrayLen = dis.readInt();   // first read the length of the array
        int[] array = new int[arrayLen];
        for (int i = 0; i < arrayLen; i++)
        {
            array[i] = dis.readInt();
        }
        return array;
    }

    /**
     * this function returns a string which represents the bit data
     * stored in the output-file between the given start and end indexes
     */
    private String readInvertedIndex(int startPos, int endPos) throws IOException {
        invertedIndexFile = Utils.openIndexFile(invertedIndexFileName, dirName);
        startPos += numPaddedZeroes; // shift indexes in order to ignore redundant 0 bits
        endPos += numPaddedZeroes;
        invertedIndexFile.seek(startPos/8);
        byte[] bytes = new byte[(int)(Math.ceil((endPos-startPos)/8.0)) + 1];
        invertedIndexFile.read(bytes);
        StringBuilder resBuilder = new StringBuilder();
        for(byte n: bytes) resBuilder.append(String.format(
                "%8s", Integer.toBinaryString(n & 0xff)
        ));
        String res = resBuilder.toString().replace(' ', '0');
        res = res.substring(startPos % 8, (startPos % 8) + endPos-startPos);
        Utils.safelyCloseFile(invertedIndexFile);
        return res;
    }


    /**
     * reads the dictionary from the dist to memory
     * @param dir directory where dictionary is stored
     */
    private void loadDictionary(String dir) {

        /*-------------------- reading ints and string --------------------*/
        DataInputStream dis = null;
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(Utils.getPath(dir, Utils.DICTIONARY_NAME));
            dis = new DataInputStream(fis);
            int concatStrLen = dis.readInt();
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i < concatStrLen; i++) {
                sb.append(dis.readChar());
            }
            String concatStr = sb.toString();

            int tokenSizeOfReviews = dis.readInt();

            int amountOfReviews = dis.readInt();

            int[] blockArray = readIntArray(dis);
            int[] dict = readIntArray(dis);
            int amountOfPaddedZeros = dis.readInt();
            int lastWordEnding = dis.readInt();
            int sizeOfLastBlock = dis.readInt();
            dictionary = new Dictionary(tokenSizeOfReviews, concatStr, blockArray, dict, amountOfReviews, amountOfPaddedZeros, lastWordEnding, sizeOfLastBlock);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Utils.safelyCloseStreams(fis, dis);
        }
    }


    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir) {
        loadDictionary(dir);
        numPaddedZeroes = dictionary.numPaddedZeroes;
        dirName = dir;
    }


    /**
     * returns the first word of the block asked for
     * @param blockIndex index of the block
     * @return the first word of blockIndex
     */
    private String getFirstWordOfBlock(int blockIndex)
    {
        int blockPtr = dictionary.blockArray[blockIndex];
        int wordLength = dictionary.getWordLen(blockIndex, 0);
        return dictionary.concatStr.substring(blockPtr, blockPtr + wordLength);
    }

    private ArrayList<String> getReviewMetaData(int reviewId) throws IOException
    {
        reviewDataFile = Utils.openIndexFile(reviewDataFileName, dirName);
        reviewDataFile.seek(reviewSize*(reviewId-1));
        byte[] meta = new byte[reviewSize];
        reviewDataFile.read(meta);
        Utils.safelyCloseFile(reviewDataFile);
        return Utils.parseReviewMetaData(meta, productIdLength);
    }

    /**
     * returns the block index that token is in
     * @param token word to search for
     * @return int index of the block in the dictionary that token can be in. -1 if token isn't in dictionary
     */
    private int findBlockOfToken(String token) {
        int start = 0;
        int end = dictionary.blockArray.length - 1;
        int mid, compareVal;
        String word;
        while (start <= end) {
            mid = (start + end) / 2;
            if (mid == 0 || mid == dictionary.blockArray.length - 1) {
                return mid;
            }
            word = getFirstWordOfBlock(mid);
            compareVal = token.compareTo(word);
            if (compareVal < 0) {
                // check if is in previous block: if token >  first word of previous block - token is in previous block
                word = getFirstWordOfBlock(mid-1);
                if (token.compareTo(word) >= 0) {
                    return mid - 1;
                }
                end = mid - 1;
            }

            else if (compareVal > 0) {
                // check if is in this block: if token <  first word of next block - token is in this block

                word = getFirstWordOfBlock(mid + 1);
                if (token.compareTo(word) < 0) {
                    return mid;
                }
                start = mid + 1;
            }
            else{    // words are equal
                return mid;
            }
        }
        return -1;
    }

    /**
     * returns the suffix of the word
     * @param blockIndex the index of the block we are working in
     * @param inBlockPtr where in concatStr are we currently
     * @param prefixLen length of the prefix of the word
     * @param wordOfBlock witch word out of K words of the block are we reconstructing
     * @param prevSuffixLen the length of the suffix of the previous word we found
     * @return the suffix of the word we are reconstructing
     */
    private String getSuffix(int blockIndex, int inBlockPtr, int prefixLen, int wordOfBlock, int prevSuffixLen) {
        String suffix;
        int suffixLen;
        suffixLen = dictionary.getWordLen(blockIndex, wordOfBlock) - prefixLen;
        suffix = dictionary.concatStr.substring(inBlockPtr + prevSuffixLen, inBlockPtr + prevSuffixLen + suffixLen);
        return suffix;
    }

    private int getBlockSize(int blockIndex)
    {
        if (blockIndex == dictionary.blockArray.length - 1)
        {
            return dictionary.sizeOfLastBlock;
        }
        else
        {
            return Dictionary.K - 1;
        }
    }
    /**
     * returns the offset of the given token in the block it is suspected to be in.
     * if token is not in this block - returns -1
     * @param blockIndex index of the block token is in (if indeed token is in dictionary)
     * @param token the token we are searching for
     * @return offset of the token in the block. -1 if token is not in the block
     */
    private int getLocationOfTokenInBlock(int blockIndex, String token) {
        String word = getFirstWordOfBlock(blockIndex);
        if (token.equals(word)) {
            return 0;
        }
        else {
            int inBlockPtr = dictionary.blockArray[blockIndex]; // pointer to concatStr where this block begins
            String prefix, suffix;
            int prefixLen, wordOfBlock;
            int prevSuffixLen = word.length();
            int blockSize = getBlockSize(blockIndex);
            for (wordOfBlock = 1; wordOfBlock < blockSize; ++wordOfBlock) {
                prefixLen = dictionary.getWordPrefix(blockIndex, wordOfBlock);
                prefix = word.substring(0, prefixLen);

                suffix = getSuffix(blockIndex, inBlockPtr, prefixLen, wordOfBlock, prevSuffixLen);
                inBlockPtr += prevSuffixLen;
                prevSuffixLen = suffix.length();

                word = prefix.concat(suffix);
                if (token.equals(word)) {
                    return wordOfBlock;
                }
            }

            if (blockIndex < dictionary.blockArray.length - 1){  // not last block - use beginning of next block as indicator of end of suffix
                int nextBlockBeginning = dictionary.blockArray[blockIndex+1];
                suffix = dictionary.concatStr.substring(inBlockPtr + prevSuffixLen, nextBlockBeginning);
            }
            else{    // last block - use length of concatStr as indicator of end of suffix

                if (wordOfBlock < Dictionary.K) {
                    return -1;
                }
                suffix = dictionary.concatStr.substring(inBlockPtr + prevSuffixLen, dictionary.concatStr.length());
            }
            prefixLen = dictionary.getWordPrefix(blockIndex, wordOfBlock, true);
            prefix = word.substring(0, prefixLen);
            word = prefix.concat(suffix);
            if (token.equals(word)) {
                return wordOfBlock;
            }
        }
        return -1;
    }


    // ---------------------------------- User API ----------------------------------

    /**
     * Returns the product identifier for the given review
     * Returns null if there is no review with the given identifier
     */
    public String getProductId(int reviewId) {
        String retVal = null; // return value in case of invalid reviewId
        if (reviewId <= dictionary.amountOfReviews){
            try {
                retVal = getReviewMetaData(reviewId).get(productIdIndex);
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return retVal;
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId){
        int retVal = -1; // return value in case of invalid reviewId
        if (reviewId <= dictionary.amountOfReviews){
            try {
                retVal = Integer.parseInt(getReviewMetaData(reviewId).get(scoreIndex));
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return retVal;
    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId){
        int retVal = -1; // return value in case of invalid reviewId
        if (reviewId <= dictionary.amountOfReviews){
            try {
                retVal = Integer.parseInt(getReviewMetaData(reviewId).get(numeratorIndex));
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return retVal;
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId){
        int retVal = -1; // return value in case of invalid reviewId
        if (reviewId <= dictionary.amountOfReviews){
            try {
                retVal = Integer.parseInt(getReviewMetaData(reviewId).get(denominatorIndex));
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return retVal;
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId) {
        int retVal = -1; // return value in case of invalid reviewId
        if (reviewId <= dictionary.amountOfReviews){
            try {
                retVal = Integer.parseInt(getReviewMetaData(reviewId).get(lengthIndex));
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return retVal;
    }

    /**
     * returns the posting list pointer of given token and the one of the next token in dictionary.
     * if token is not in dictionary - returns null
     * @param token token to search for in dictionary
     * @return posting list pointers of given token and the following one.
     * if token is not in dictionary - returns null
     */
    private int[] getPostingListPtr(String token) {
        int[] pair = new int[2];
        int blockIndex = findBlockOfToken(token);
        int wordOffset = getLocationOfTokenInBlock(blockIndex, token);

        // if token is last word in dictionary - return postingPrt, -1
        if (blockIndex == dictionary.blockArray.length - 1 &&
                dictionary.getWordRow(blockIndex, wordOffset) + Dictionary.POSTING_INDEX_MIDDLE >= dictionary.dictionary.length - 1) {
            if (wordOffset == 0 || wordOffset == Dictionary.K - 1) // check if is first or last word of block
                pair[0] = dictionary.getPostingPtr(blockIndex, wordOffset, true);
            else
                pair[0] = dictionary.getPostingPtr(blockIndex, wordOffset, false);
            pair[1] = dictionary.lastWordEnding;
            return pair;
        }

        switch (wordOffset) {
            case -1: // token not found
                return null;
            case 0: // first word of block, next word is in same block
                pair[0] = dictionary.getPostingPtr(blockIndex, wordOffset, true);
                pair[1] = dictionary.getPostingPtr(blockIndex, wordOffset+1, false);
                break;
            case Dictionary.K - 1: // last word of block, next is first of next block
                pair[0] = dictionary.getPostingPtr(blockIndex, wordOffset, true);
                pair[1] = dictionary.getPostingPtr(blockIndex+1, 0, true);
                break;
            case Dictionary.K-2: // before last word of block, next is in same block but last
                pair[0] = dictionary.getPostingPtr(blockIndex, wordOffset, false);
                pair[1] = dictionary.getPostingPtr(blockIndex, wordOffset+1, true);
                break;
            default: // both are in same block
                pair[0] = dictionary.getPostingPtr(blockIndex, wordOffset, false);
                pair[1] = dictionary.getPostingPtr(blockIndex, wordOffset+1, false);
        }

        return pair;
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token) {
        int[] postingPtr = getPostingListPtr(token);
        if (postingPtr != null) {
            try {
                String binaryResult = readInvertedIndex(postingPtr[0], postingPtr[1]);
                return Utils.decodeDelta(binaryResult).size() / 2;
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return 0;
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token) {
        int blockIndex = findBlockOfToken(token);
        int wordOffset = getLocationOfTokenInBlock(blockIndex, token);
        if (wordOffset == -1)
            return 0;

        return dictionary.getWordFreq(blockIndex, wordOffset);
    }

    /**
     * Return a series of integers of the form id-1, freq-1, id-2, freq-2, ... such
     * that id-n is the n-th review containing the given token and freq-n is the
     * number of times that the token appears in review id-n
     * Only return ids of reviews that include the token
     * Note that the integers should be sorted by id
     *
     * Returns an empty Enumeration if there are no reviews containing this token
     */
    public Enumeration<Integer> getReviewsWithToken(String token) {
        int[] postingListPtrs = getPostingListPtr(token);
        if (postingListPtrs != null) {
            try {
                if (postingListPtrs[1] == POINTER_TO_AFTER_DICT) {
                    // token is the last word in the dictionary so we need don't have pointer to next word's
                    // posting list to use as border for reading
                    postingListPtrs[1] = (int)invertedIndexFile.length()*8;
                }
                String binaryResult = readInvertedIndex(postingListPtrs[0], postingListPtrs[1]);
                ArrayList<Integer> tokenReviews = Utils.decodeDelta(binaryResult);
                Utils.decompressPostingList(tokenReviews);
                return Collections.enumeration(tokenReviews);
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return Collections.emptyEnumeration();
    }

    /**
     * Return the number of product reviews available in the system
     */
    public int getNumberOfReviews()
    {
        return dictionary.amountOfReviews;
    }

    /**
     * Return the number of number of tokens in the system
     * (Tokens should be counted as many times as they appear)
     */
    public int getTokenSizeOfReviews()
    {
        return dictionary.tokenSizeOfReviews;
    }

    /**
     * Return the ids of the reviews for a given product identifier
     * Note that the integers returned should be sorted by id
     *
     * Returns an empty Enumeration if there are no reviews for this product
     */
    public Enumeration<Integer> getProductReviews(String productId) {
        int[] postingListPtrs = getPostingListPtr(productId);
        if (postingListPtrs != null) {
            try {
                String binaryResult = readInvertedIndex(postingListPtrs[0], postingListPtrs[1]);
                ArrayList<Integer> tokenReviews = Utils.decodeDelta(binaryResult);
                Utils.decompressPostingList(tokenReviews);
                ArrayList<Integer> onlyReviewIds = Utils.getOnlyReviewIds(tokenReviews);
                return Collections.enumeration(onlyReviewIds);
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return Collections.emptyEnumeration();
    }
}