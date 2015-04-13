package com.secdec.codedx.util;

/**************************************************************************
 * Copyright (c) 2014 Applied Visions, Inc. All Rights Reserved.
 * Author: Applied Visions, Inc. - Chris Ellsworth
 * Project: Code Dx
 * SubSystem: com.secdec.codedx.util
 * FileName: HashUtil.java
 *************************************************************************/

        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.Formatter;

/**
 * @author Chris Ellsworth
 *
 */
public class HashUtil {
//    public static String toMd5(String input) {
//        String toReturn = null;
//        try {
//            byte[] digest = MessageDigest.getInstance("MD5").digest(input.getBytes());
//            StringBuilder hashBuilder = new StringBuilder();
//            for (byte b : digest) {
//                hashBuilder.append(String.format("%x", b));
//            }
//            toReturn = hashBuilder.toString();
//        } catch (NoSuchAlgorithmException exc) {
//            throw new CodeDxException(exc);
//        }
//        return toReturn;
//    }

    public static byte[] getSHA1(byte[] input) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, "");
    }

    public static String toHexString(byte[] bytes, String sep) {
        Formatter f = new Formatter();
        for (int i = 0; i < bytes.length; i++) {
            f.format("%02x", bytes[i]);
            if (i < bytes.length - 1) {
                f.format(sep);
            }
        }
        String result = f.toString();
        f.close();
        return result;
    }
}