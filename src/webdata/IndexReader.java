package webdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.io.*;


public class IndexReader
{
    public Dictionary dictionary; //TODO: make private
    private static final int METADATA_ENTRY_SIZE = 30; // size of metadata entry in bytes TODO: update according to actual size

    private final String inveretedIndexFileName = "Test";
    private RandomAccessFile invertedIndexFile = null;
    private int numPaddedZeroes; //TODO: read from dictionary file

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
    private String readInvertedIndex(int startPos, int endPos) throws IOException
    {
        startPos += numPaddedZeroes; // shift indexes in order to ignore redundant 0 bits
        endPos += numPaddedZeroes;
        invertedIndexFile.seek(startPos/8);
        byte[] bytes = new byte[(int)(Math.ceil((endPos-startPos)/8.0))];
        invertedIndexFile.read(bytes);
        StringBuilder resBuilder = new StringBuilder();
        for(byte n: bytes) resBuilder.append(String.format(
                "%8s", Integer.toBinaryString(n & 0xff)
        ));
        String res = resBuilder.toString().replace(' ', '0');
        res = res.substring(startPos % 8, (startPos % 8) + endPos-startPos+1);
        return res;
    }


    /**
     * reads the dictionary from the dist to memory
     * @param dir directory where dictionary is stored
     */
    private void loadDictionary(String dir)
    {
        //TODO: take care of path properly, not sure this will work properly on linux
        String path = dir.concat("\\myDict");

        /*-------------------- reading ints and string --------------------*/
        DataInputStream dis = null;
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(path);
            dis = new DataInputStream(fis);
            int concatStrLen = dis.readInt();
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i < concatStrLen; i++)
            {
                sb.append(dis.readChar());
            }
            String concatStr = sb.toString();

            int tokenSizeOfReviews = dis.readInt();

            int amountOfReviews = dis.readInt();

            int[] blockArray = readIntArray(dis);
            int[] dict = readIntArray(dis);
            dictionary = new Dictionary(tokenSizeOfReviews, concatStr, blockArray, dict, amountOfReviews);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Utils.safelyCloseStreams(fis, dis);
        }

        /*-------------------- reading object --------------------*/
//        InputStream fis = null;
//        ObjectInputStream ois = null;
//
//        try
//        {
//            fis = new FileInputStream(path);
//            ois = new ObjectInputStream(fis);
//            dictionary = (Dictionary) ois.readObject();
//        }
//        catch (IOException | ClassNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            Utils.safelyCloseStreams(fis, ois);
//        }

    }


    /**
     * Creates an IndexReader which will read from the given directory
     */
    public IndexReader(String dir)
    {
        loadDictionary(dir);
        //TODO add dir to inveretedIndexFileName
        try {
            invertedIndexFile = new RandomAccessFile(inveretedIndexFileName, "r");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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

    /**
     * returns the block index that token is in
     * @param token word to search for
     * @return int index of the block in the dictionary that token can be in. -1 if token isn't in dictionary
     */
    public int findBlockOfToken(String token) //TODO change to private
    {
        int start = 0;
        int end = dictionary.blockArray.length - 1;
        int mid, compareVal;
        String word;
        while (start <= end)
        {
            mid = (start + end) / 2;
            if (mid == 0 || mid == dictionary.blockArray.length - 1)
            {
                return mid;
            }
            word = getFirstWordOfBlock(mid);
            compareVal = token.compareTo(word);
            if (compareVal < 0)
            {
                // check if is in previous block: if token >  first word of previous block - token is in previous block

                word = getFirstWordOfBlock(mid-1);
                if (token.compareTo(word) >= 0)
                {
                    return mid - 1;
                }
                end = mid - 1;
            }

            else if (compareVal > 0)
            {
                // check if is in this block: if token <  first word of next block - token is in this block

                word = getFirstWordOfBlock(mid + 1);
                if (token.compareTo(word) < 0)
                {
                    return mid;
                }
                start = mid + 1;
            }
            else    // words are equal
            {
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
    private String getSuffix(int blockIndex, int inBlockPtr, int prefixLen, int wordOfBlock, int prevSuffixLen)
    {
        String suffix;
        int suffixLen;
        suffixLen = dictionary.getWordLen(blockIndex, wordOfBlock) - prefixLen;
        suffix = dictionary.concatStr.substring(inBlockPtr + prevSuffixLen, inBlockPtr + prevSuffixLen + suffixLen);
        return suffix;
    }

    /**
     * returns the offset of the given token in the block it is suspected to be in.
     * if token is not in this block - returns -1
     * @param blockIndex index of the block token is in (if indeed token is in dictionary)
     * @param token the token we are searching for
     * @return offset of the token in the block. -1 if token is not in the block
     */
    public int getLocationOfTokenInBlock(int blockIndex, String token) //TODO change to private
    {
        String word = getFirstWordOfBlock(blockIndex);
        if (token.equals(word))
        {
            return 0;
        }
        else
        {
            int inBlockPtr = dictionary.blockArray[blockIndex]; // pointer to concatStr where this block begins
            String prefix, suffix;
            int prefixLen, wordOfBlock;
            int prevSuffixLen = word.length();
            for (wordOfBlock = 1; wordOfBlock < Dictionary.K -1 ; ++wordOfBlock)
            {
                prefixLen = dictionary.getWordPrefix(blockIndex, wordOfBlock);
                prefix = word.substring(0, prefixLen);

                suffix = getSuffix(blockIndex, inBlockPtr, prefixLen, wordOfBlock, prevSuffixLen);
                inBlockPtr += prevSuffixLen;
                prevSuffixLen = suffix.length();

                word = prefix.concat(suffix);
                if (token.equals(word))
                {
                    return wordOfBlock;
                }
            }

            prefixLen = dictionary.getWordPrefix(blockIndex, wordOfBlock, true);
            prefix = word.substring(0, prefixLen);

            if (blockIndex < dictionary.blockArray.length - 1)  // not last block - use beginning of next block as indicator of end of suffix
            {
                int nextBlockBeginning = dictionary.blockArray[blockIndex+1];
                suffix = dictionary.concatStr.substring(inBlockPtr + prevSuffixLen, nextBlockBeginning);
            }
            else    // last block - use length of concatStr as indicator of end of suffix
            {
                suffix = dictionary.concatStr.substring(inBlockPtr + prevSuffixLen, dictionary.concatStr.length());
            }
            word = prefix.concat(suffix);
            if (token.equals(word))
            {
                return wordOfBlock;
            }
        }
        return -1;
    }

    /**
     * returns the pointer to the relevant location in metadata file.
     * returns -1 if there is no review with the given identifier
     * @param reviewId identifier of the wanted review
     * @return pointer to the relevant location in metadata file. -1 if there is no review with the given identifier
     */
    private int getMetaPtr(int reviewId)
    {
//        if (reviewId > dictionary.metaDataPtrArray.length)
        if (reviewId > dictionary.amountOfReviews)
        {
            return -1;
        }
//        return dictionary.metaDataPtrArray[reviewId];
        return METADATA_ENTRY_SIZE * reviewId;
    }


    // ---------------------------------- User API ----------------------------------

    /**
     * Returns the product identifier for the given review
     * Returns null if there is no review with the given identifier
     */
    public String getProductId(int reviewId)
    {
        int metaPtr = getMetaPtr(reviewId);
        if (metaPtr == -1) // no review with given identifier
        {
            return null;
        }
        else
        {
            return null; // TODO: read from metadata file the productId and return it
        }
    }

    /**
     * Returns the score for a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewScore(int reviewId)
    {
        int metaPtr = getMetaPtr(reviewId);
        if (metaPtr == -1) // no review with given identifier
        {
            return -1;
        }
        else
        {
            return -1; // TODO: read from metadata file the score and return it
        }
    }

    /**
     * Returns the numerator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessNumerator(int reviewId)
    {
        int metaPtr = getMetaPtr(reviewId);
        if (metaPtr == -1) // no review with given identifier
        {
            return -1;
        }
        else
        {
            return -1; // TODO: read from metadata file the HelpfulnessNumerator and return it
        }
    }

    /**
     * Returns the denominator for the helpfulness of a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewHelpfulnessDenominator(int reviewId)
    {
        int metaPtr = getMetaPtr(reviewId);
        if (metaPtr == -1) // no review with given identifier
        {
            return -1;
        }
        else
        {
            return -1; // TODO: read from metadata file the HelpfulnessDenominator and return it
        }
    }

    /**
     * Returns the number of tokens in a given review
     * Returns -1 if there is no review with the given identifier
     */
    public int getReviewLength(int reviewId)
    {
        int metaPtr = getMetaPtr(reviewId);
        if (metaPtr == -1) // no review with given identifier
        {
            return -1;
        }
        else
        {
            return -1; // TODO: read from metadata file the length and return it
        }
    }

    /**
     * returns the posting list pointer of given token. if token is not in dictionary - returns -1
     * @param token token to search for in dictionary
     * @return posting list pointer of given token. if token is not in dictionary - returns -1
     */
    private int getPostingListPrt(String token)
    {
        int blockIndex = findBlockOfToken(token);
        int wordOffset = getLocationOfTokenInBlock(blockIndex, token);
        if (wordOffset == -1)
            return -1;
        if (wordOffset == 0 || wordOffset == Dictionary.K - 1)
            return dictionary.getPostingPtr(blockIndex, wordOffset, true);
        return dictionary.getPostingPtr(blockIndex, wordOffset, false);
    }

    /**
     * Return the number of reviews containing a given token (i.e., word)
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenFrequency(String token)
    {
        int postingPtr = getPostingListPrt(token);
        if (postingPtr == -1)
        {
            return 0;
        }
        else
        {
            // TODO: read from invertedIndex and compute length of posting list
            return 0;
        }
    }

    /**
     * Return the number of times that a given token (i.e., word) appears in
     * the reviews indexed
     * Returns 0 if there are no reviews containing this token
     */
    public int getTokenCollectionFrequency(String token)
    {
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
     public Enumeration<Integer> getReviewsWithToken(String token)
     {
         int postingPtr = getPostingListPrt(token);
         int nextPostingPrt = -1; //TODO: get posting pointer of next token (take care of edge case - last word in dict)
         if (postingPtr == -1)
         {
             return Collections.emptyEnumeration();
         }
         else
         {
             //TODO: read from invertedIndex and create enumeration for output
             try
             {
                 String res = readInvertedIndex(postingPtr, nextPostingPrt);
                 ArrayList<Integer> tokenReviews = Utils.decodeDelta(res);
                 return (Enumeration<Integer>) tokenReviews;
             }
             catch (IOException e)
             {
                 e.printStackTrace();
                 //TODO: what happens here?
             }
             return null;
         }
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
    public Enumeration<Integer> getProductReviews(String productId)
    {
        int postingPtr = getPostingListPrt(productId);
        if (postingPtr == -1)
        {
            return Collections.emptyEnumeration();
        }
        else
        {
            //TODO: read from invertedIndex and create enumeration for output
            return null;
        }
    }
}
