package webdata;

public class Dictionary
{
    public String concatStr = "";
    public int[] blockArray;
//    public int[][] dictionary;      // for 2d array implementation
    public int[] dictionary;      // for 1d array implementation
    int blockIndex = 0;     // for keeping track in block array
    int dictIndex = 0;      // for keeping track in dictionary array

    public static final int K = 8; // value for k -1 in k encoding of dictionary
    public static final int ENTRIES_FOR_FIRST_LAST = 3;
    public static final int ENTRIES_FOR_MIDDLE = 4;
//  3 for first word in block (freq, len, postingPrt) + 3 for last word in block (freq, postingPrt, prefix) + 4 for words in middle of block (freq, len, postingPrt, prefix)

    /*
    Dictionary description:
    -----------------------
    3 components - String, block array, dictionary

    1) String concatStr:
    The string of concatenated k-1 in k front Encoding string.

    2) int[] blockArray:
    Array with 1 entry per block. The i'th entry in the array in the pointer to the beginning of the i'th block of the
    concatStr - i.e where the first word of the i'th block begins (and is fully written)

    3( int[]/int[][] dictionary:
    Array with the actual data needed for our dictionary -
    1. frequency        2.length        3.length of common prefix with previous word
    4. pointer to location in inverted index where the posting list begins
    in practice first word of each block doesn't need the prefix length, and the last word of the block doesn't need
    the the length of the word to be stored.

    if dictionary was 2d array (for each word an array of length 3/4) we have that:
    - first word of block - [freq, length, postingPtr]
    - words in middle of block - [freq, length, prefix, postingPtr]
    - last word of block - [freq, prefix, postingPrt]
    --------------------------------------------------------------------
     */
    public Dictionary(int vocabularySize)
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
        int middleOfBlockWords = vocabularySize - 2*amountOfBlocks;
        int totalSizeOfDict = amountOfBlocks*ENTRIES_FOR_FIRST_LAST + amountOfBlocks*ENTRIES_FOR_FIRST_LAST + middleOfBlockWords*ENTRIES_FOR_MIDDLE;
        dictionary = new int[totalSizeOfDict+1];
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

        // if word in the beginning of block i in blockArray, then word is at line i*k of dictionary
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

}
