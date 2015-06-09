package com.secdec.codedx.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Contains utilities for dealing with Hashes
 *
 * @author Samuel Johnson
 *
 */
public class HashUtil {

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