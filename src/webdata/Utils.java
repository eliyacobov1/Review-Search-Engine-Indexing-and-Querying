package webdata;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Utils {
    static final int SECONDS = 0;
    static final int MINUTES = 1;
    static final int MILLISECONDS = 2;

    public static int AMOUNT_OF_DOCS_TO_PARSE = 1000;      // TODO: for testing only
    public static final String DICTIONARY_NAME = "dictionary";
    static final String MERGED_FILE_NAME = "mergedFile";
    public static final String INVERTED_INDEX_FILE_NAME = "inverted_index";
    public static final String INTERMEDIATE_INDEX_FILE_NAME = "intermediate_index";
    public static final String REVIEW_METADATA_FILE_NAME = "reviews_meta_data";
    static final String BATCH_FILE_NAME_BASE = "batch_";


    /**
     * reads an array of ints that was previously saved to disk. how much to read is stored before the data itself.
     * @param dis DataInputStream to read from
     * @return array of ints that was read
     */
    public static int[] readIntArray(DataInputStream dis) throws IOException {
        int arrayLen = dis.readInt();   // first read the length of the array
        int[] array = new int[arrayLen];
        for (int i = 0; i < arrayLen; i++)
        {
            array[i] = dis.readInt();
        }
        return array;
    }

    /**
     * reads an array of ints that was previously saved to disk. how much to read is stored before the data itself.
     * @param dis DataInputStream to read from
     * @return array of ints that was read
     */
    public static long[] readLongArray(DataInputStream dis) throws IOException {
        int arrayLen = dis.readInt();   // first read the length of the array
        long[] array = new long[arrayLen];
        for (int i = 0; i < arrayLen; i++)
        {
            array[i] = dis.readLong();
        }
        return array;
    }

    /**
     * this function writes the given pair to the given file
     */
    static void writePair(DataOutputStream file, IntPair pair) throws IOException {
        file.writeInt(pair.termId);
        file.writeInt(pair.docId);
    }


    /**
     * this function receives an array of RAF object and closes each of it's streams
     */
    static void closeRafStreams(OutputStream[] files) throws IOException {
        for(OutputStream file: files) file.close();
    }

    /**
     * this function receives an array of InputStream object and closes each of it's streams
     */
    static void closeRafStreams(InputStream[] files) throws IOException {
        for(InputStream file: files) file.close();
    }

    /**
     * this function converts the given file array into a DataInputStream array
     */
    static DataInputStream[] fileInputArrayToDataInputArray(FileInputStream[] arr) throws FileNotFoundException {
        DataInputStream[] files = new DataInputStream[arr.length];
        for(int i = 0; i < arr.length; i++) files[i] = new DataInputStream(new BufferedInputStream(arr[i]));
        return files;
    }

    /**
     * this function converts the given file array into a DataOutputStream array
     */
    static DataOutputStream[] fileOutputArrayToDataOutputArray(FileOutputStream[] arr) throws FileNotFoundException{
        DataOutputStream[] files = new DataOutputStream[arr.length];
        for(int i = 0; i < arr.length; i++) files[i] = new DataOutputStream(new BufferedOutputStream(arr[i]));
        return files;
    }


    /**
     * this function receives an array of files names and deletes them (meant for temporary files)
     */
    static void deleteFiles(String[] arr, String dirName){
        for(String fileName: arr){
//            new File(getPath(dirName, fileName)).deleteOnExit();
            File f = new File(getPath(dirName, fileName));
            if (!f.delete()){
                f.deleteOnExit();
            }
        }
    }

    /**
     * this function finds the index of the minimal argument in an array of IntPair objects
     */
    static int findMinArgIndex(IntPair[] arr){
        int min = 0;
        for(int i = 1; i < arr.length; i++){
            if(arr[i].compareTo(arr[min]) < 0) min = i;
        }
        return min;
    }

    /**
     * this function fills the given array with numbers from the given file starting
     * from the current marker location in the file
     */
    static void parseNextSequence(DataInputStream file, int[] arr) throws IOException {
        for(int i = 0; i < arr.length; i+=2){
            try {
                arr[i] = file.readInt();  // Token id
                arr[i+1] = file.readInt();  // Doc id
            } catch (EOFException e) {
                arr[i] = -1;
                return; // in case end of file is reached
            }
        }
    }

    /**
     * removes empty strings from array of strings
     * @param text array of strings maybe containing the empty string
     * @return array of strings now with out empty strings
     */
    static String[] removeEmptyString(String[] text)
    {
        String[] temp = new String[text.length];
        int j = 0;
        int i;
        for (i=0; i<text.length; i++){
            if (!text[i].equals("")){
                temp[j] = text[i];
                j++;
            }
        }
        String[] res = new String[text.length - (i-j)];
        System.arraycopy(temp, 0, res, 0, res.length);
        return res;
    }

    /**
     * safely closes the 2 given streams
     */
    static void safelyCloseStreams(Closeable f1, Closeable f2) {
        if (f1 != null) {
            try { f1.close(); }
            catch (IOException e) { handleException(e); }
        }
        if (f2 != null) {
            try { f2.close(); }
            catch (IOException e) { handleException(e); }
        }
    }

    /**
     * this function returns the number of bits in
     * the binary representation of the given number
     */
    private static int getBitCount(int n){
        return (int)((Math.log(n) / Math.log(2))+1);
    }

    /**
     * this function converts the given string representation of a binary number
     * into a byte array. If string_size % 8 != 0, additional zero bits will be
     * added to the left portion of the number in order to allow proper conversion
     * of bits into byte array (.e.g, 1010 -> 00001010)
     */
    static byte[] binaryStringToByte(String s) {
        if (s.length() % 8 != 0){ // pad with zeroes
            StringBuilder sBuilder = new StringBuilder(s);
            for(int i = 0; i < 8 - s.length() % 8; i++) sBuilder.insert(0, '0');
            s = sBuilder.toString();
        }
        byte[] data = new byte[s.length() / 8];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '1') {
                data[i >> 3] |= 0x80 >> (i & 0x7);
            }
        }
        return data;
    }

    /**
     * this function converts the given string representation
     * of a binary number into a base-10 integer
     */
    private static int binaryStringToInt(String s){
        return Integer.parseInt(s, 2);
    }

    /**
     * this function generates the gamma encoding of the given number
     * @param num the number that is to be encoded
     * @param delta if set to true, delta encoding is generated
     */
    static String gammaRepr(int num, boolean delta){
        if(num == 0) return "";
        int bitCount = getBitCount(num);
        String unaryRepr = delta ? gammaRepr(bitCount, false) :
                "1".repeat(Math.max(0, bitCount-1)) + '0';
        String binaryRepr = Integer.toBinaryString(num).substring(1);
        return unaryRepr + binaryRepr;
    }

    private static String intTo8bitBinaryString(int num){
        return String.format("%8s", Integer.toBinaryString(num & 0xff)).replace(' ', '0');
    }

    /**
     * This function receives a String of a number in binary base, which represents
     * a certain chain of integers in delta encoding. The function deciphers and
     * return a list of this chain of integers in decimal base.
     */
    // 41, 1, 159, 1, 746, 1, 863, 1
    static ArrayList<Integer> decodeDelta(String encodedData){
        int i = 0, numBitsCurLength, length;
        ArrayList<Integer> decodedVals = new ArrayList<>();
        while(i < encodedData.length()) {
            numBitsCurLength = 1;
            while (encodedData.charAt(i) != '0') {
                numBitsCurLength++;
                i++;
            }
            i++;
            length = binaryStringToInt( // decode number length in bits
                    '1' + encodedData.substring(i, i+numBitsCurLength-1)
            );
            i += numBitsCurLength-1;
            decodedVals.add(binaryStringToInt( // decode number and add it to the list
                    '1' + encodedData.substring(i, i+length-1)
            ));
            i += length-1;
        }
        return decodedVals;
    }

    static byte[] productIDToByteArray(String id){
        byte[] idAsBytes = new byte[id.length()];
        for(int i = 0; i < id.length(); i++){
            idAsBytes[i] = (byte)((int)id.charAt(i));
        }
        return idAsBytes;
    }

    /**
     * this function receives the meta data of a review in the form of a byte array
     * and returns an ArrayList which contains the parsed fields of data. (The fields
     * are: Product ID, Numerator, Denominator, Helpfulness, length)
     */
    static ArrayList<String> parseReviewMetaData(byte[] meta, int productIdLength){
        ArrayList<String> parsedMetaData = new ArrayList<>();
        StringBuilder productID = new StringBuilder();
        for(int i = 0; i < productIdLength; i++) productID.append((char) meta[i]);
        parsedMetaData.add(productID.toString()); // product id
        parsedMetaData.add(String.valueOf(meta[productIdLength])); // numerator
        parsedMetaData.add(String.valueOf(meta[productIdLength+1])); // denominator
        parsedMetaData.add(String.valueOf(meta[productIdLength+2])); // score
        String reviewLength = String.valueOf(binaryStringToInt(
                intTo8bitBinaryString(meta[productIdLength+4]) +
                        intTo8bitBinaryString(meta[productIdLength+3])
        ));
        parsedMetaData.add(reviewLength); // length
        return parsedMetaData;
    }

    /**
     * takes array list with posting list with gap encoding and decodes it the regular posting list
     * i.e 30,1,1,4 -> 30, 31, 32, 36
     */
    static void decompressPostingList(ArrayList<Integer> list) {
        int prev = 0;
        int temp;
        for (int i = 0; i < list.size(); i+=2) {
            temp = list.get(i) + prev;
            list.set(i, temp);
            prev = temp;
        }
    }

    /**
     * take array list with review ids and counts and returns only the review ids
     */
    static ArrayList<Integer> getOnlyReviewIds(ArrayList<Integer> list) {
        ArrayList<Integer> onlyReviewIds = new ArrayList<>();
        for (int i = 0; i < list.size(); i+=2) {
            onlyReviewIds.add(list.get(i));
        }
        return onlyReviewIds;
    }

    /**
     * combines dir name and file name
     */
    public static String getPath(String dir, String fileName) {
        Path path = Paths.get(dir).resolve(fileName);
        return path.toString();
    }

    static RandomAccessFile openIndexFile(String fileName, String dir)
    {
        try {
            return new RandomAccessFile(Utils.getPath(dir, fileName), "r");
        }
        catch (IOException e) { handleException(e); }
        return null;
    }

    static void safelyCloseFile(Closeable f1)
    {
        try
        {
            f1.close();
        }
        catch (IOException e) { handleException(e);}
    }

    public static void handleException(Exception e)
    {
        e.printStackTrace();
        System.exit(-1);
    }

    public static void printTime(String step, long timeInMs, int convertTo){
        System.out.println(step + " done.");
        float time = 0;
        String unit = "";
        switch (convertTo) {
            case MILLISECONDS:
                time = timeInMs;
                unit = " milliseconds\n";

            case SECONDS:
                time = (float) timeInMs / 1000f;
                unit = " seconds\n";

            case MINUTES:
                time = ((float) timeInMs / 1000f) / 60;
                unit = " minutes\n";

        }
        System.out.println("Time: " + time + unit);
    }
}