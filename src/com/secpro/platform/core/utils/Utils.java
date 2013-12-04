package com.secpro.platform.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.log.utils.PlatformLogger;

/**
 * @author baiyanwei Jul 13, 2013 This is the platform general utility class.
 */
public class Utils {

	private static final DateFormat format = new SimpleDateFormat("MMdd HHmm sss");

	//
	// Logging Object
	//
	private static PlatformLogger theLogger = PlatformLogger.getLogger(Utils.class);

	/**
	 * Get the String type date
	 * 
	 * @param millis
	 * @return
	 */
	public static String getTime(long millis) {

		return format.format(new Date(millis));

	}

	/**
	 * This will turn an input stream into a String Buffer.
	 * 
	 * @param inputStream
	 * @return
	 */
	public static StringBuffer getInputStream2StringBuffer(InputStream inputStream) {
		StringBuffer stringBuffer = new StringBuffer();
		char[] cbuf = new char[1024];
		int len = 0;
		InputStreamReader utf8 = null;
		try {
			utf8 = new InputStreamReader(inputStream, "UTF8");
			while ((len = utf8.read(cbuf)) > 0) {
				stringBuffer.append(cbuf, 0, len);
			}
		} catch (UnsupportedEncodingException e) {
			theLogger.exception(e);
		} catch (IOException e) {
			theLogger.exception(e);
		} finally {
			if (utf8 != null) {
				try {
					utf8.close();
				} catch (IOException e) {
				}
			}
		}
		return stringBuffer;
	}

	/**
	 * Converts the input bytes to hex.
	 */
	public static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Use the standard java GZip functionality.
	 * 
	 * returns a byte array of the compress data.
	 * 
	 * @param inputBytes
	 * @return
	 */
	public static byte[] compress(byte[] inputBytes) {
		ByteArrayOutputStream bytesOutputStream = null;
		GZIPOutputStream gZIPOutputStream = null;
		try {
			bytesOutputStream = new ByteArrayOutputStream();
			gZIPOutputStream = new GZIPOutputStream(bytesOutputStream);

			gZIPOutputStream.write(inputBytes, 0, inputBytes.length);

			bytesOutputStream.close();
			gZIPOutputStream.close();

			return bytesOutputStream.toByteArray();
		} catch (IOException e) {
			theLogger.exception(e);
		}
		return null;
	}

	/**
	 * Uncompress a byte array of data
	 * 
	 * @param inputBytes
	 * @return an uncompressed byte array.
	 */
	public static byte[] uncompress(byte[] inputBytes) {
		try {
			ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(inputBytes);
			GZIPInputStream gzipInputStream = new GZIPInputStream(bytesInputStream);
			ByteArrayOutputStream bytesOutputStream = new ByteArrayOutputStream();

			byte[] bytes = new byte[1024];
			int len = 0;

			while ((len = gzipInputStream.read(bytes)) > 0) {
				bytesOutputStream.write(bytes, 0, len);
			}

			gzipInputStream.close();
			bytesInputStream.close();
			bytesOutputStream.close();
			return bytesOutputStream.toByteArray();
		} catch (IOException e) {
			theLogger.exception(e);
		}
		return null;
	}

	public static String md5String(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] md5hash = new byte[32];
		md.update(content.getBytes("iso-8859-1"), 0, content.length());
		md5hash = md.digest();
		return convertToHex(md5hash);
	}

	/**
	 * traverse each extension and handle it.
	 * 
	 * @param extPnt
	 *            extension point identifier
	 * @param handler
	 * @throws PlatformException
	 * 
	 *             CR - I like this lets move it to core
	 */
	public static void traverseExtension(final String extPnt, IExtensionHandler handler) throws PlatformException {

		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint point = registry.getExtensionPoint(extPnt);
		if (point == null) {
			throw new PlatformException("The extension POINT for " + extPnt + " could not be found!!");
		}

		IExtension[] extensions = point.getExtensions();
		if (extensions == null) {
			throw new PlatformException("No extensions for " + extPnt + " could not be found!!");
		}

		for (int index = 0; index < extensions.length; index++) {
			IConfigurationElement[] elements = extensions[index].getConfigurationElements();
			for (int elementIndex = 0; elementIndex < elements.length; elementIndex++) {
				IConfigurationElement configurationElement = elements[elementIndex];
				// This is where we create the server object that will be
				handler.extend(configurationElement);
			}
		}

	}

	/**
	 * Tests whether or not the url has a http or https prefix, if it does
	 * returns true else returns false.
	 * 
	 * @param url
	 * @return
	 */
	public static boolean testHttpPrefix(String url) {
		return url.toLowerCase().startsWith("http://") == true || url.toLowerCase().startsWith("https://") == true;
	}

	/**
	 * subtract the pat from the source
	 * 
	 * @param source
	 * @param pat
	 * @return
	 */
	public static String subtract(String source, String pat) {

		int index = source.indexOf(pat);
		if (index == -1) {
			// not found
			return source;
		}

		StringBuffer sb = new StringBuffer();
		if (index != 0) {
			sb.append(source.substring(0, index));
		}

		if (index + pat.length() != source.length()) {
			sb.append(source.substring(index + pat.length()));
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getShortClassName(Class<?> clazz) {
		return getShortClassName(clazz.getName());
	}

	/**
	 * Get the Short class name.
	 * 
	 * @param className
	 * @return
	 */
	public static String getShortClassName(String className) {
		int lastDotIndex = className.lastIndexOf(".");
		int nameEndIndex = className.length();
		String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
		return shortName;
	}

	/**
	 * get 32 bit UUID String
	 * 
	 * @return
	 */
	public static String getUUID32() {
		String sn = UUID.randomUUID().toString();
		// UUID(32);
		return sn.substring(0, 8) + sn.substring(9, 13) + sn.substring(14, 18) + sn.substring(19, 23) + sn.substring(24);
	}
}
