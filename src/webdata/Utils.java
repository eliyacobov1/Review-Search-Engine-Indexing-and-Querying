package webdata;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public class Utils
{
    /**
     * safely closes the 2 given streams
     */
    public static void safelyCloseStreams(Closeable f1, Closeable f2)
    {
        if (f1 != null)
        {
            try { f1.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }

        if (f2 != null)
        {
            try { f2.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    /**
     * this function returns the number of bits in
     * the binary representation of the given number
     */
    public static int getBitCount(int n){
        return (int)((Math.log(n) / Math.log(2))+1);
    }

    /**
     * this function converts the given string representation of a binary number
     * into a byte array. If string_size % 8 != 0, additional zero bits will be
     * added to the left portion of the number in order to allow proper conversion
     * of bits into byte array (.e.g, 1010 -> 00001010)
     */
    public static byte[] binaryStringToByte(String s) {
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
    public static int binaryStringToInt(String s){
        return Integer.parseInt(s, 2);
    }

    /**
     * this function generates the gamma encoding of the given number
     * @param num the number that is to be encoded
     * @param delta if set to true, delta encoding is generated
     */
    public static String gammaRepr(int num, boolean delta){
        if(num == 0) return "";
        int bitCount = getBitCount(num);
        String unaryRepr = delta ? gammaRepr(bitCount, false) :
                "1".repeat(Math.max(0, bitCount-1)) + '0';
        String binaryRepr = Integer.toBinaryString(num).substring(1);
        return unaryRepr + binaryRepr;
    }

    /**
     * This function receives a String of a number in binary base, which represents
     * a certain chain of integers in delta encoding. The function deciphers and
     * return a list of this chain of integers in decimal base.
     */
    public static ArrayList<Integer> decodeDelta(String encodedData){
        int i = 0, numBitsCurLength, length;
        ArrayList<Integer> decodedVals = new ArrayList<Integer>();
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
            decodedVals.add(binaryStringToInt( // decode number and it to the list
                    '1' + encodedData.substring(i, i+length-1)
            ));
            i += length-1;
        }
        return decodedVals;
    }
}
