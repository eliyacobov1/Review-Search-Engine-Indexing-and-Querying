package webdata;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

class Utils {
    static final String DICTIONARY_NAME = "dictionary";

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
            catch (IOException e) { e.printStackTrace(); }
        }
        if (f2 != null) {
            try { f2.close(); }
            catch (IOException e) { e.printStackTrace(); }
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
    static String getPath(String dir, String fileName) {
        Path path = Paths.get(dir).resolve(fileName);
        return path.toString();
    }
}