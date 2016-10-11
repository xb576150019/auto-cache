package com.txm.topcodes.autocache.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileUtil {

	public FileUtil() {
		{
		/* cannot be instantiated */
			throw new UnsupportedOperationException("cannot be instantiated");
		}
	}


	public static byte[] getBytes(String url) throws Exception {
		InputStream is = null;
		byte[] bytes = new byte[0];
		if (null != url) {
			is = new URL(url).openStream();
			bytes = new byte[0];
			bytes = input2byte(is);
		}
		return bytes;
	}


	public static byte[] getBytes(String url, InputStream defaultStream) throws Exception {
		InputStream is = null;
		byte[] bytes;
		if (null != url) {
			is = new URL(url).openStream();
			bytes = input2byte(is);
		} else {
			bytes = input2byte(defaultStream);
		}
		return bytes;
	}


	public static final byte[] input2byte(InputStream inStream)
			throws IOException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = inStream.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		swapStream.close();
		buff = null;
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}
}
