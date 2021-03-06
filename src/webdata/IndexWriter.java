package webdata;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class IndexWriter {
    private FileOutputStream invertedIndexFile = null;
    private DataOutputStream invertedIndexWriter = null;
    private FileOutputStream intermediateIndexFile = null;
    private DataOutputStream intermediateIndexWriter = null;
    private FileOutputStream reviewDataFile = null;
    private static int NUM_OF_FILES_TO_MERGE = 4;
    public final int AMOUNT_OF_DOCS_TO_READ_PER_BATCH = 100000;
    public final int AMOUNT_OF_PAIRS_TO_READ_TO_MAIN_MEM = 35000;
    public final int AMOUNT_OF_WORDS_PER_FLUSH_TO_II = 25000;
    private HashMap<String, Integer> termIdMapping;
    private String dirName;
    private String inputFileName;
    private StringBuilder accumulatedString = new StringBuilder();
    private long pos = 0;
    private Dictionary dict;
    private int amountOfTokens;
    private int amountOfReviews;

    /**
     * this function initializes a file input stream array of the given size. These are the files created
     * in phase 2 of the writing process and in each merge occurring in phase 3.
     */
    private FileInputStream[] initializeFileInputArray(int numFiles, int phase) throws FileNotFoundException {
        FileInputStream[] fileArray = new FileInputStream[numFiles];
        for (int i = 0; i < numFiles; i++) {
            String batchFileName = numFiles == 1 ? Utils.MERGED_FILE_NAME : ((phase > 0 ? "phase" + phase + "_" : "")
                    + Utils.BATCH_FILE_NAME_BASE + i);
            fileArray[i] = new FileInputStream(Utils.getPath(dirName, batchFileName));
        }
        return fileArray;
    }

    /**
     * this function initializes a file output stream array of the given size. These are the files created
     * in phase 2 of the writing process and in each merge occurring in phase 3.
     */
    private FileOutputStream[] initializeFileOutputArray(int numFiles, int phase) throws FileNotFoundException {
        FileOutputStream[] fileArray = new FileOutputStream[numFiles];
        for (int i = 0; i < numFiles; i++) {
            String batchFileName = numFiles == 1 ? Utils.MERGED_FILE_NAME : ((phase > 0 ? "phase" + phase + "_" : "")
                    + Utils.BATCH_FILE_NAME_BASE + i);
            fileArray[i] = new FileOutputStream(Utils.getPath(dirName, batchFileName));
        }
        return fileArray;
    }

    /**
     * returns an array of names of files for merging phase
     */
    private String[] getMergeFileNames(int numFiles, int phase){
        String[] fileNames = new String[numFiles];
        for (int i = 0; i < numFiles; i++) {
            fileNames[i] = numFiles == 1 ? Utils.MERGED_FILE_NAME : ((phase > 0 ? "phase" + phase + "_" : "")
                    + Utils.BATCH_FILE_NAME_BASE + i);
        }
        return fileNames;
    }

    /**
     * takes array of file names and returns an array of data input streams with the file names as file input streams
     * for the data input streams
     */
    private DataInputStream[] outputToInputDataStream(String[] fileNames) throws IOException {
        DataInputStream[] dataInputStreams = new DataInputStream[fileNames.length];
        for (int i=0; i<fileNames.length; i++){
            dataInputStreams[i] = new DataInputStream(new BufferedInputStream(new FileInputStream(Utils.getPath(dirName, fileNames[i]))));
        }
        return dataInputStreams;
    }


    /**
     * this function, which is a part of phase 3 in the writing process, sorts and merges the mergedFile
     * contents that of the files that are between the begin and end indexes of the given mergedFile array
     * @param filesToMergeArray the array which contains the files that the function will merge
     * @param begin begin index of the slice
     * @param end end index of the slice
     * @param sequenceSize the size of the sequence of numbers that is inspected in each sorting phase
     * @param mergedFile the mergedFile into which all of the sorted and merged content will be written to
     */
    private void mergeFiles(DataInputStream[] filesToMergeArray, int begin, int end, int sequenceSize,
                            DataOutputStream mergedFile) throws IOException {
        int numOfFiles = Math.min(end, filesToMergeArray.length)-begin;  // the number fo files to merge
        int[] sequencePointers = new int[numOfFiles];  // this array stores the pointer values of each sequence
        IntPair[] pairs = new IntPair[numOfFiles]; // current pair of each of the sequences
        int[][] currFileContents = new int[numOfFiles][sequenceSize*2];

        for(int i = 0; i < numOfFiles; i++){ // read the initial sequences from each mergedFile
            Utils.parseNextSequence(filesToMergeArray[begin+i], currFileContents[i]);
            pairs[i] = new IntPair(currFileContents[i][0], currFileContents[i][1]);
        }
        int numUnfinishedFiles = numOfFiles;

        while(numUnfinishedFiles > 0){ // while didn't finish writing content of all files
            int minArg = Utils.findMinArgIndex(pairs);  // write the minimal pair to mergedFile
            Utils.writePair(mergedFile, pairs[minArg]);
            int currPointer = sequencePointers[minArg] += 2;

            // advance pointer of sequence or load next sequence or mark end of mergedFile
            if(currPointer >= sequenceSize * 2){ // reached the end of the current sequence
                sequencePointers[minArg] = 0;
                Utils.parseNextSequence(filesToMergeArray[begin+minArg], currFileContents[minArg]);
                pairs[minArg].setVals(currFileContents[minArg][0], currFileContents[minArg][1]);
                currPointer = 0;
            }
            if(currFileContents[minArg][currPointer] == -1){  // reached end of mergedFile
                numUnfinishedFiles -= 1;
                pairs[minArg].setVals(amountOfTokens, amountOfTokens);
            }
            else pairs[minArg].setVals(currFileContents[minArg][currPointer],
                    currFileContents[minArg][currPointer+1]);
        }
        mergedFile.flush();
    }


    /**
     * this function receives the files that were created during phase 2 of the index writing
     * process and uses external sort in order to merge and sort their contents into a single file
     */
    private void externalSortAndMergeInvertedIndex(int numFiles) throws IOException {
        NUM_OF_FILES_TO_MERGE = Math.min(NUM_OF_FILES_TO_MERGE, numFiles);
        int mergePhase = 0;
        int currNumFiles = numFiles;
        String[] inputFileNames = getMergeFileNames(numFiles, mergePhase);
        FileInputStream[] inputFileArray = initializeFileInputArray(numFiles, mergePhase);
        DataInputStream[] inputDataArray = Utils.fileInputArrayToDataInputArray(inputFileArray);

        while(currNumFiles > 1){

            int sequenceSize = (int)(AMOUNT_OF_PAIRS_TO_READ_TO_MAIN_MEM / (double)currNumFiles) + 1;
            int numFilesAfterMerge = (int)Math.ceil(currNumFiles / (double)NUM_OF_FILES_TO_MERGE);
            String[] outputFileNames = getMergeFileNames(numFilesAfterMerge, mergePhase+1);
            FileOutputStream[] mergedFileArray = initializeFileOutputArray(numFilesAfterMerge, mergePhase+1);
            DataOutputStream[] mergedDataArray = Utils.fileOutputArrayToDataOutputArray(mergedFileArray);
            for(int i = 0; i < currNumFiles; i += NUM_OF_FILES_TO_MERGE){
                mergeFiles(inputDataArray, i, i+NUM_OF_FILES_TO_MERGE, sequenceSize,
                        mergedDataArray[i / NUM_OF_FILES_TO_MERGE]);
            }

            Utils.closeRafStreams(inputDataArray);
            Utils.deleteFiles(inputFileNames, dirName); // delete the files from the last merge-phase
            inputFileNames = outputFileNames;
            inputDataArray = outputToInputDataStream(outputFileNames);
            currNumFiles = numFilesAfterMerge;
            mergePhase++;
        }
        inputDataArray[0].close(); // close the merged file stream
    }

    /**
     * this function writes the accumulated string ,which represents the encoded
     * concatenation of the inverted index of the token collection, into the output-file
     * in the form of binary data
     */
    private void writeInvertedIndex(){
        try {
            byte[] stringAsByteArr = Utils.binaryStringToByte(accumulatedString.toString());
            invertedIndexWriter.write(stringAsByteArr, 0, stringAsByteArr.length);
        }
        catch (IOException e) {
            Utils.handleException(e);
        }
    }

    /**
     * this function writes the accumulated string ,which represents the encoded
     * concatenation of the inverted index of the token collection, into the output-file
     * in the form of binary data
     */
    private void writeToIntermediateIndex(){
        try {
            intermediateIndexWriter.writeBytes(accumulatedString.toString());
        }
        catch (IOException e) {
            Utils.handleException(e);
        }
    }

    /**
     * calculates the length of the common prefix of 2 Strings
     * @param s1 first String
     * @param s2 second String
     * @return length of the common prefix
     */
    private int commonPrefix(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());
        for (int i=0; i < len; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
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
     * dictionary. In addition this method also writes to meta data file
     * @param wordCountTotal how many times did word appear in total
     * @param numOfTotalTokens total amount of tokens in data
     * @param reviewId counter indicating review number
     * @param inputFile path to input file
     */
    private void processReviews(HashMap<String, Integer> wordCountTotal,
                                int[] numOfTotalTokens, int[] reviewId, String inputFile) throws IOException
    {
        ReviewPreprocessor rp = new ReviewPreprocessor(inputFile);
        DataOutputStream metaDataWriter = new DataOutputStream(new BufferedOutputStream(reviewDataFile));
        while (rp.hasMoreReviews()) {
            ArrayList<String> reviewData = rp.getNextReview();
            String[] text = rp.getReviewText();
            // write meta data to metadata file

            byte[] prodIdByteArr = Utils.productIDToByteArray(reviewData.get(0));
            metaDataWriter.write(prodIdByteArr, 0, prodIdByteArr.length); // product id
            metaDataWriter.write((byte)Integer.parseInt(reviewData.get(1))); // numerator
            metaDataWriter.write((byte)Integer.parseInt(reviewData.get(2))); // denominator
            metaDataWriter.write((byte)(int)Double.parseDouble(reviewData.get(3))); // score
            int length = text.length;
            metaDataWriter.write(length & 0xff); // length- first byte
            metaDataWriter.write((length >> 8) & 0xff); // length- second byte

            // iterate over text. count amount of tokens (with repetitions)
            // and count amount of times word appeared in text
            Integer prevVal;
            for (String word : text)
            {
                numOfTotalTokens[0]++;
                prevVal = wordCountTotal.putIfAbsent(word, 1);
                if (prevVal != null) {
                    wordCountTotal.put(word, ++prevVal);
                }
            }
            // enter/update "productId" into data structures
            String productId = reviewData.get(0);
            prevVal = wordCountTotal.putIfAbsent(productId, 1);
            if (prevVal != null) {
                wordCountTotal.put(productId, ++prevVal);
            }
            reviewId[0]++;
//            if (reviewId[0] == Utils.AMOUNT_OF_DOCS_TO_PARSE) // TODO: for testing only
//                break;
        }
        metaDataWriter.flush();
    }

    /**
     * encodes posting list and frequencies with gap encoding and delta coding
     * @param postingList array list of ints that are the docIds a word appears in
     * @param wordCountInEachReview matching frequencies of the word in the docs it appears in
     */
    private void encodePostingListAndFrequencies(ArrayList<Integer> postingList, ArrayList<Integer> wordCountInEachReview) {
        for(int i = 0; i < postingList.size(); i++){
            int diff = postingList.get(i)- (i > 0 ? postingList.get(i-1) : 0); // compute gap from previous docId
            String encodedIndex = Utils.gammaRepr(diff, true);
            String encodedCount = Utils.gammaRepr(wordCountInEachReview.get(i), true);
            accumulatedString.append(encodedIndex); // write next index to file
            accumulatedString.append(encodedCount); // write token count to file
            pos += encodedIndex.length() + encodedCount.length();
        }
    }

    /**
     * Step 4 of index creation process. Read the merged sorted file from step 3. Create posting list and frequencies
     * for each term and write them to II
     */
    private void readMergedAndCreateInvertedIndex()
    {
        DataInputStream dis = null;
        FileInputStream fis = null;
        try {
            // set up
            fis = new FileInputStream(Utils.getPath(dirName, Utils.MERGED_FILE_NAME));
            dis = new DataInputStream(new BufferedInputStream(fis));
            int prevTermId = dis.readInt();
            int prevDocId = dis.readInt();
            int termId = prevTermId;
            int docId = prevDocId;
            ArrayList<Integer> postingList = new ArrayList<>();
            ArrayList<Integer> wordCountInEachReview = new ArrayList<>();
            int tokenIndex = 0;      // what token are we processing out of all tokens
            int postingListPointerIndexOfDictionary = Dictionary.POSTING_INDEX_FIRST_OR_LAST;    // where in dictionary array should we write the pointer to the II
            int inReviewCount = 0;      // counter of how many times did term appear in the same review (i.e doc frequency)
            int distinctTermId = 0;

            while (true)    //go over all text tokens. at end of loop there is a try/catch that breaks from the loop when reaching EOF
            {
                if (termId == prevTermId) {   // still same word
                    if (docId == prevDocId) inReviewCount++; // still same doc
                    else {    // different doc, write previous and restart count
                        postingList.add(prevDocId);
                        wordCountInEachReview.add(inReviewCount);
                        inReviewCount = 1;
                    }
                }
                else {  // different word:
                    // (1) write previous to lists
                    // (2) write to II and update ptr in dict
                    // (3) restart count and lists
                    // (1)
                    postingList.add(prevDocId);
                    wordCountInEachReview.add(inReviewCount);

                    // (2)
                    // update posting list pointer in dictionary
                    dict.setPostingPtr(postingListPointerIndexOfDictionary, pos);
                    if ((distinctTermId + 1) % Dictionary.K == 0 || (distinctTermId + 2) % Dictionary.K == 0) // if this was a last word of block or before last word of block we shift by 3
                    {
                        postingListPointerIndexOfDictionary += Dictionary.POSTING_INDEX_FIRST_OR_LAST + 1;
                    }
                    else {    // other wise shift by 4
                        postingListPointerIndexOfDictionary += Dictionary.POSTING_INDEX_MIDDLE + 1;
                    }
                    // encode posting list and word frequencies
                    encodePostingListAndFrequencies(postingList, wordCountInEachReview);
                    if (distinctTermId % AMOUNT_OF_WORDS_PER_FLUSH_TO_II == 0) {
                        writeToIntermediateIndex();
                        accumulatedString = new StringBuilder();
                    }

                    // (3)
                    distinctTermId++;
                    inReviewCount = 1;
                    postingList.clear();
                    wordCountInEachReview.clear();
                }
                // get next pair
                prevTermId = termId;
                prevDocId = docId;
                try { termId = dis.readInt(); }
                catch (EOFException e) { break; } // reached EOF of mergedFile - exit loop
                docId = dis.readInt();
                tokenIndex++;
            }
            Utils.safelyCloseStreams(fis, dis);
            new File(Utils.getPath(dirName, Utils.MERGED_FILE_NAME)).deleteOnExit();
//            mergedFile.delete();
            // add last term to II
            postingList.add(docId);
            wordCountInEachReview.add(inReviewCount);
            dict.setPostingPtr(postingListPointerIndexOfDictionary, pos);
            encodePostingListAndFrequencies(postingList, wordCountInEachReview);
            writeToIntermediateIndex();
            intermediateIndexWriter.flush();
            accumulatedString = new StringBuilder();
        }
        catch (IOException e) { Utils.handleException(e);}
    }

    /**
     * Step 2 of index creation process. Parses all reviews in batches. For each batch of AMOUNT_OF_DOCS_TO_READ_PER_BATCH docs,
     * creates sequence of pairs (termId,docId). When finishes going over docs, sorts pairs and then writes sorted pairs
     * to file.
     * @return amount of files creates (==number of batches)
     */
    private int[] sortBatches() {
        ReviewPreprocessor rp = new ReviewPreprocessor(inputFileName);
        int docId = 1;
        int batchId = 0;
        int totalAmount = 0;    //TODO maybe needs to be long
        String batchFileNameBase = "batch_";
        ArrayList<IntPair> pairs = new ArrayList<>();
        while (rp.hasMoreReviews()) {
            int amountOfDocsLeft = amountOfReviews - (docId - 1);
            int howManyToRead = Math.min(amountOfDocsLeft, AMOUNT_OF_DOCS_TO_READ_PER_BATCH);
            // read AMOUNT_OF_DOCS_TO_READ_PER_BATCH (or what is left from the reviews) reviews and create pairs of (termId, docId)
            for (int i = 0; i < howManyToRead; i++) {
                if (!rp.hasMoreReviews())
                    break;
                rp.getNextReview();
                String[] text = rp.getReviewText();
                for (String word: text) {
                    IntPair pair = new IntPair(termIdMapping.get(word), docId);
                    pairs.add(pair);
                    totalAmount++;
                }
                IntPair pair = new IntPair(termIdMapping.get(rp.currentReviewMetaData.get(0)), docId);  // add productId
                pairs.add(pair);
                totalAmount++;
                docId++;
            }
            // sort and write to file
            Collections.sort(pairs);

            String batchFileName = batchFileNameBase + batchId;
            DataOutputStream batchFile;
            try {
                batchFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Utils.getPath(dirName, batchFileName))));
                for (IntPair pair: pairs) {
                    batchFile.writeInt(pair.termId);
                    batchFile.writeInt(pair.docId);
                }
                batchFile.flush();
                batchFile.close();
            }
            catch (IOException e) { Utils.handleException(e);}
            batchId++;
            pairs.clear();
//            if (docId == Utils.AMOUNT_OF_DOCS_TO_PARSE) // TODO: for testing only
//                break;
        }
        return new int[]{batchId, totalAmount};
    }

    /**
     * Step 1 of index creation process. Parses input and creates dictionary and term-termId mapping
     * @param wordCountTotal how many times did word appear in total
     * @param numOfTotalTokens total amount of tokens in data
     * @param reviewId counter indicating review number
    //     * @return Dictionary object with all data except for 1) pointers to invertedIndex 2) lastWordEnding 3) numPaddedZeroes
     */
    private void createDictionaryAndTermIdMap(HashMap<String, Integer> wordCountTotal, int[] numOfTotalTokens,
                                              int[] reviewId) {
        ArrayList<String> sortedVocabulary = new ArrayList<>(wordCountTotal.keySet());
        Collections.sort(sortedVocabulary); // sort vocabulary to insert into dictionary and index
        Dictionary dict = new Dictionary(sortedVocabulary.size(), numOfTotalTokens[0]);  // local var for saving to disk and having more free memory
        termIdMapping = new HashMap<>();

        ListIterator<String> vocabIter = sortedVocabulary.listIterator();
        String prevWord = "";
        int index = 0;
        // iterate all word in sorted vocabulary
        while (vocabIter.hasNext()) {
            index = vocabIter.nextIndex();
            String word = vocabIter.next();
            termIdMapping.put(word, index);

            int postingPtr = -1;
            int freq = wordCountTotal.get(word);
            int prefixLen = commonPrefix(word, prevWord);

            if (index % Dictionary.K == 0) // first word of block
                dict.addFirstWordInBlock(word, freq, postingPtr);
            else if ((index + 1) % Dictionary.K == 0) // last word of block
                dict.addLastWordOfBlock(word, freq, postingPtr, prefixLen);
            else dict.addMiddleWordOfBlock(word, freq, postingPtr, prefixLen); // middle word of block

            prevWord = word;
        }
        dict.concatStr = dict.concatStrBuilder.toString();
        dict.sizeOfLastBlock = ((index) % Dictionary.K) +1;
        dict.amountOfReviews = reviewId[0] - 1;
        dict.writeDictToDisk(dirName); // cache dictionary to disk
        amountOfReviews = reviewId[0] - 1;
    }

    /**
     * first pass over reviews - create metadata file, dictionary and termId mapping
     */
    private void step1()
    {
        HashMap<String, Integer> wordCountTotal = new HashMap<>();      // mapping term: total frequency in whole corpus
        int[] numOfTotalTokens = {0}; //TODO: maybe need long, int can hold "only" ~2.14 billion
        int[] reviewId = {1};
        try {
            processReviews(wordCountTotal, numOfTotalTokens, reviewId, inputFileName);      // meta data file is created here
        }
        catch (IOException e) { Utils.handleException(e); }
        createDictionaryAndTermIdMap(wordCountTotal, numOfTotalTokens, reviewId);
    }

    private void encodeIntermediateIndex(String dir) throws IOException {
        FileInputStream intermediateIndexInputFile = new FileInputStream(Utils.getPath(dir, Utils.INTERMEDIATE_INDEX_FILE_NAME));
        DataInputStream intermediateIndexInputStream = new DataInputStream(new BufferedInputStream(intermediateIndexInputFile));

        byte[] buffer = new byte[(AMOUNT_OF_PAIRS_TO_READ_TO_MAIN_MEM * 2 * 8) - dict.numPaddedZeroes];
        while(true) {
            int res = intermediateIndexInputStream.read(buffer);
            if(res == -1) break;  // reached end of file
            accumulatedString.append(new String(buffer, StandardCharsets.UTF_8));
            writeInvertedIndex();
            accumulatedString = new StringBuilder();
            buffer = new byte[(AMOUNT_OF_PAIRS_TO_READ_TO_MAIN_MEM * 2 * 8)];
        }
        invertedIndexWriter.flush();
        new File(Utils.getPath(dir, Utils.INTERMEDIATE_INDEX_FILE_NAME)).deleteOnExit();
        Utils.safelyCloseStreams(intermediateIndexInputFile, invertedIndexFile);
    }

    /**
     * Given product review data, creates an on disk index
     * inputFile is the path to the file containing the review data
     * dir is the directory in which all index files will be created
     * if the directory does not exist, it should be created
     */
    public void write(String inputFile, String dir) {
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
        /* ------------------- open files ------------------- */
        dirName = dir;
        inputFileName = inputFile;
        try {
            if (!Files.exists(Paths.get(dir))) {
                Files.createDirectory(Paths.get(dir));
            }
            invertedIndexFile = new FileOutputStream(Utils.getPath(dir, Utils.INVERTED_INDEX_FILE_NAME));
            invertedIndexWriter = new DataOutputStream(new BufferedOutputStream(invertedIndexFile));
            intermediateIndexFile = new FileOutputStream(Utils.getPath(dir, Utils.INTERMEDIATE_INDEX_FILE_NAME));
            intermediateIndexWriter = new DataOutputStream(new BufferedOutputStream(intermediateIndexFile));
            reviewDataFile = new FileOutputStream(Utils.getPath(dir, Utils.REVIEW_METADATA_FILE_NAME));
        }
        catch (IOException e) { Utils.handleException(e); }

        /* ------------- preprocess reviews (metadata of reviews and counting of terms and tokens) ------------- */
        step1();
        /* step 1 done */

        int[] res = sortBatches();
        int amountOfBatchFiles = res[0];
        amountOfTokens = res[1];        // This is exactly how many pairs were written

        /* step 2 done */

        if (amountOfBatchFiles > 1)
        {
            /* merge batch files into one big file*/
            try {
                externalSortAndMergeInvertedIndex(amountOfBatchFiles);
            }
            catch (IOException e) { Utils.handleException(e);}
        }
        else
        {
            File batch0 = new File(Utils.getPath(dirName, Utils.BATCH_FILE_NAME_BASE+"0"));
            File mergedFile = new File(Utils.getPath(dirName, Utils.MERGED_FILE_NAME));
            boolean status = batch0.renameTo(mergedFile);
        }

        /* step 3 done*/

        dict = Dictionary.loadDictionary(dirName);
        readMergedAndCreateInvertedIndex();

        dict.lastWordEnding = pos;
        dict.numPaddedZeroes = pos % 8 == 0 ? 0 : (int) (8 - pos % 8); // pad with zeroes in order to fit data into bytes
        Utils.safelyCloseStreams(intermediateIndexFile, reviewDataFile);
        try { encodeIntermediateIndex(dir); } // inverted-index was written as a string, will now be converted to binary delta encoding
        catch (IOException e) { e.printStackTrace(); }
        dict.writeDictToDisk(dir);
        /* step 4 done. index created*/
    }

    /**
     * Delete all index files by removing the given directory
     */
    public void removeIndex(String dir) {
        File directory = new File(dir);
        File[] entries = directory.listFiles();
        if (entries != null) {
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