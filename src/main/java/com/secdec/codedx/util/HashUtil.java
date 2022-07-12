/*
 *
 * Copyright 2022 Synopsys, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

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