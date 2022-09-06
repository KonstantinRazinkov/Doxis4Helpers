package com.sersolutions.doxis4helpers.archiver;

import ru.ubmb.jstribog.StribogProvider;

import java.security.MessageDigest;
import java.security.Security;

/**
 * Class for counting specific HASH
 */
public class HashProducer {
    /**
     * Count GOST-R 34112012 Hash
     * @param input byte array
     * @return Hash
     * @throws Exception if something goes wrong
     */
    public static String CalculateGOSTR34112012(byte[] input) throws Exception {
        Security.addProvider(new StribogProvider());

        MessageDigest md = MessageDigest.getInstance("Stribog512", "JStribog");
        md.update(input);
        byte[] gost = md.digest();
        String hex = bytesToHex(gost);
        return hex;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Convert byte array to HEX
     * @param bytes byte array
     * @return HEX String
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
