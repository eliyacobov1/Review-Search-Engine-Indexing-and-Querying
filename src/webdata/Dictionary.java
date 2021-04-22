package webdata;

import java.io.*;

public class Dictionary implements Serializable
{
    public String concatStr = "";
    public int[] blockArray;
    //    public int[][] dictionary;      // for 2d array implementation
    public int[] dictionary;      // for 1d array implementation
//    public int[] metaDataPtrArray; // no need for this since metadata entries have constant size
    public int tokenSizeOfReviews;
    public int amountOfReviews;
    public int numPaddedZeroes;

    int blockIndex = 0;     // for keeping track in block array
    int dictIndex = 0;      // for keeping track in dictionary array

    /* ------------------------ Class Constants ----------------------------------*/
    public static final int K = 8; // value for k -1 in k encoding of dictionary
    public static final int ENTRIES_FOR_FIRST_LAST = 3;
    public static final int ENTRIES_FOR_MIDDLE = 4;
    public static final int LINE_LENGTH = ENTRIES_FOR_FIRST_LAST*2 + ENTRIES_FOR_MIDDLE*(K-2);
//  3 for first word in block (freq, len, postingPrt) + 3 for last word in block (freq, postingPrt, prefix)
//  + 4 for words in middle of block (freq, len, postingPrt, prefix)
    public static final int PREFIX_INDEX_MIDDLE = 2;
    public static final int PREFIX_INDEX_LAST = 1;
    public static final int POSTING_INDEX_FIRST_OR_LAST = 2;
    public static final int POSTING_INDEX_MIDDLE = 3;
    public static final int LENGTH_INDEX = 1;

    /*
    Dictionary description:
    -----------------------
    4 components - String, block array, dictionary, metaDataPtrArray

    1) String concatStr:
    The string of concatenated k-1 in k front Encoding string.

    2) int[] blockArray:
    Array with 1 entry per block. The i'th entry in the array in the pointer to the beginning of the i'th block of the
    concatStr - i.e where the first word of the i'th block begins (and is fully written)

    3) int[]/int[][] dictionary:
    Array with the actual data needed for our dictionary -
    1. frequency        2.length        3.length of common prefix with previous word
    4. pointer to location in inverted index where the posting list begins
    in practice first word of each block doesn't need the prefix length, and the last word of the block doesn't need
    the the length of the word to be stored.

    if dictionary was 2d array (for each word an array of length 3/4) we have that:
    - first word of block - [freq, length, postingPtr]
    - words in middle of block - [freq, length, prefix, postingPtr]
    - last word of block - [freq, prefix, postingPrt]

    4) metaDataPtrArray:
    array of pointers to metadata index file.
    The i'th entry is a pointer to location in the metadata index file where the metadata about the i'th review can be
    found. i.e metaDataPtrArray[reviewId] = pointer to metadataIndexFile where metadata of reviewId is saved.
    --------------------------------------------------------------------
     */

    public Dictionary(int vocabularySize, int amountOfTokens)
    {
        int amountOfBlocks = (int) Math.ceil(((double) vocabularySize / K));
        blockArray = new int[amountOfBlocks];

        // code for initiating 2d dictionary array
//        dictionary = new int[vocabularySize][];
//        for (int i = 0; i < amountOfBlocks; i++)
//        {
//            if (i % K == 0 || (i + 1) % K == 0)
//            {
//                dictionary[i] = new int[ENTRIES_FOR_FIRST_LAST];
//            }
//            else
//            {
//                dictionary[i] = new int[ENTRIES_FOR_MIDDLE];
//            }
//        }

        // if we want 1d array
        int amountOfMiddleOfBlockWords = vocabularySize - 2*amountOfBlocks;
        int totalSizeOfDict = amountOfBlocks*ENTRIES_FOR_FIRST_LAST + amountOfBlocks*ENTRIES_FOR_FIRST_LAST +
                amountOfMiddleOfBlockWords *ENTRIES_FOR_MIDDLE;
        dictionary = new int[totalSizeOfDict+1];

//        metaDataPtrArray = new int[amountOfReviews];
        tokenSizeOfReviews = amountOfTokens;
    }

    public Dictionary(int amountOfTokens, String concatStr, int[] blockArray, int[] dictionary, int amountOfReviews, int numPaddedZeroes)
    {
        this.tokenSizeOfReviews = amountOfTokens;
        this.concatStr = concatStr;
        this.blockArray = blockArray;
        this.dictionary = dictionary;
        this.amountOfReviews = amountOfReviews;
        this.numPaddedZeroes = numPaddedZeroes;
//        this.metaDataPtrArray = metaDataPtrArray;
    }

    /**
     * writes this dictionary to a file in the given directory
     * @param dir path to directory where dictionary should be written to
     */
    public void writeDictToDisk(String dir)
    {
        /*
        1) open file and stream
        2) write concatStr
        3) write tokenSizeOfReviews
        4) write amountOfReviews
        4) for each (blockArr, dictionary):
            4.1) write length of array to disk (???) maybe just write the needed values
            4.2) write each int to file
         */
        //TODO: take care of path properly, not sure this will work properly on linux
        String path = dir.concat("\\myDict");

        /*-------------------- writing ints and string --------------------*/
        DataOutputStream dos = null;
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(path);
            dos  = new DataOutputStream(fos);

            dos.writeInt(concatStr.length());
            dos.writeChars(concatStr);

            dos.writeInt(tokenSizeOfReviews);

            dos.writeInt(amountOfReviews);

            dos.writeInt(blockArray.length);
            for (int i: blockArray)
            {
                dos.writeInt(i);
            }

            dos.writeInt(dictionary.length);
            for (int i: dictionary)
            {
                dos.writeInt(i);
            }

            dos.writeInt(numPaddedZeroes);
            dos.flush();
        }
        catch (IOException e) { e.printStackTrace(); }
        finally
        {
            Utils.safelyCloseStreams(fos, dos);
        }

        /*-------------------- writing object --------------------*/
//        FileOutputStream fos = null;
//        ObjectOutputStream oos = null;
//        try
//        {
//            fos = new FileOutputStream(path);
//            oos = new ObjectOutputStream(fos);
//
//            oos.writeObject(this);
//        }
//        catch (IOException e) { e.printStackTrace(); }
//        finally
//        {
//            Utils.safelyCloseStreams(fos, oos);
//        }
    }

    /**
     * adds the data to the dictionary
     * @param data int value to be added to dictionary
     */
    private void writeToDict(int data)
    {
        dictionary[dictIndex] = data;
        dictIndex++;
    }

    /**
     * Adds the whole word to the end of concatStr and updates blockArray and dictionary with the needed data
     * @param word String word to be added
     * @param freq frequency of the word in all of the reviews
     * @param postingPrt pointer to location in inverted index where we can find posting list of word
     */
    public void addFirstWordInBlock(String word, int freq, int postingPrt)
    {
        blockArray[blockIndex] = concatStr.length();
        concatStr += word;
        blockIndex++;

        writeToDict(freq);
        writeToDict(word.length());
        writeToDict(postingPrt);
    }

    /**
     * Adds the unshared suffix of word to concatStr and updates the dictionary. If is middle word, adds length of word,
     * if not (i.e last word of block) - doesn't add length of word to dictionary
     * @param word String word to be added
     * @param freq frequency of the word in all of the reviews
     * @param postingPtr pointer to location in inverted index where we can find posting list of word
     * @param prefixLen length of the shared prefix of word and the word that came before it
     * @param middle boolean indicator indicating if word is in the middle of block or at the end
     */
    private void addWordThatIsNotFirst(String word, int freq, int postingPtr, int prefixLen, boolean middle)
    {
        concatStr += word.substring(prefixLen); // add suffix of word

        writeToDict(freq);
        if (middle)
        {
            writeToDict(word.length());
        }
        writeToDict(prefixLen);
        writeToDict(postingPtr);
    }

    /**
     * Adds the unshared suffix of word to concatStr and updates the dictionary
     * @param word String word to be added
     * @param freq frequency of the word in all of the reviews
     * @param postingPtr pointer to location in inverted index where we can find posting list of word
     * @param prefixLen length of the shared prefix of word and the word that came before it
     */
    public void addMiddleWordOfBlock(String word, int freq, int postingPtr, int prefixLen)
    {
        addWordThatIsNotFirst(word, freq, postingPtr, prefixLen, true);
    }

    /**
     * Adds the unshared suffix of word to concatStr and updates the dictionary
     * @param word String word to be added
     * @param freq frequency of the word in all of the reviews
     * @param postingPtr pointer to location in inverted index where we can find posting list of word
     * @param prefixLen length of the shared prefix of word and the word that came before it
     */
    public void addLastWordOfBlock(String word, int freq, int postingPtr, int prefixLen)
    {
        addWordThatIsNotFirst(word, freq, postingPtr, prefixLen, false);
    }

    /**
     * returns pointer to the beginning of the "row" where word #wordOffset begins
     * @param blockIndex index of the wanted block
     * @param wordOffset offset of the word in the block
     * @return pointer to the beginning of the wanted "row"
     */
    private int getWordRow(int blockIndex, int wordOffset)
    {
        int beginningOfBlock = blockIndex * Dictionary.LINE_LENGTH;
        int offset = 0;
        if (wordOffset != 0)
        {
            offset = ENTRIES_FOR_FIRST_LAST + (wordOffset-1)*ENTRIES_FOR_MIDDLE;
        }
        return beginningOfBlock + offset;
    }

    /**
     * returns the length of the wanted word
     * @param blockIndex block where word is in
     * @param wordOffset offset of the word in the block
     * @return length of the wanted word
     */
    public int getWordLen(int blockIndex, int wordOffset)
    {
        return dictionary[getWordRow(blockIndex, wordOffset) + LENGTH_INDEX];
    }

    /**
     * returns the frequency of the wanted word
     * @param blockIndex block where word is in
     * @param wordOffset offset of the word in the block
     * @return frequency of the wanted word
     */
    public int getWordFreq(int blockIndex, int wordOffset)
    {
        return dictionary[getWordRow(blockIndex, wordOffset)];
    }

    /**
     * returns the posting list pointer of the wanted word
     * @param blockIndex block where word is in
     * @param wordOffset offset of the word in the block
     * @param firstOrLast boolean indicator for first or last word of block
     * @return posting list pointer of the wanted word
     */
    public int getPostingPtr(int blockIndex, int wordOffset, boolean firstOrLast)
    {
        if (firstOrLast)
        {
            return dictionary[getWordRow(blockIndex, wordOffset) + POSTING_INDEX_FIRST_OR_LAST];
        }
        else
        {
            return dictionary[getWordRow(blockIndex, wordOffset) + POSTING_INDEX_MIDDLE];
        }
    }

    /**
     * returns the length of the prefix of the wanted word
     * @param blockIndex block where word is in
     * @param wordOffset offset of the word in the block
     * @return length of the prefix of the wanted word
     */
    public int getWordPrefix(int blockIndex, int wordOffset)
    {
        return dictionary[getWordRow(blockIndex, wordOffset) + PREFIX_INDEX_MIDDLE];
    }

    /**
     * returns the length of the prefix of the wanted word
     * @param blockIndex block where word is in
     * @param wordOffset offset of the word in the block
     * @param last true if this is the last word of the block
     * @return length of the prefix of the wanted word
     */
    public int getWordPrefix(int blockIndex, int wordOffset, boolean last)
    {
        if (last)
        {
            return dictionary[getWordRow(blockIndex, wordOffset) + PREFIX_INDEX_LAST];
        }
        else
        {
            return dictionary[getWordRow(blockIndex, wordOffset) + PREFIX_INDEX_MIDDLE];
        }
    }

}
