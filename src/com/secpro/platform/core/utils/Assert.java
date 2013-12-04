package com.secpro.platform.core.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author baiyanwei Jul 13, 2013 our own assertion class that is useful for
 *         failing fast and for eliminating
 */
public final class Assert {

	/**
	 * A private constructor to block instantiation.
	 */
	private Assert() {
	}

	/**
	 * check one object is null or not
	 * 
	 * @param o
	 * @return
	 */
	final public static boolean isNull(Object o) {
		if (null == o) {
			return true;
		}
		return false;
	}

	/**
	 * check one string is empty or not empty:null,empty String,no letter.
	 * 
	 * @param s
	 */
	final public static boolean isEmptyString(String s) {
		if (null == s || s.length() == 0 || s.trim().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * check one collection is empty or not
	 * 
	 * @param c
	 * @return
	 */
	final public static <T extends Object> boolean isEmptyCollection(Collection<?> c) {
		if (null == c || c.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * check one Map is empty or not
	 * 
	 * @param c
	 * @return
	 */
	final public static <T extends Object> boolean isEmptyMap(Map<?, ?> m) {
		if (null == m || m.isEmpty()) {
			return true;
		}
		return false;
	}
}
