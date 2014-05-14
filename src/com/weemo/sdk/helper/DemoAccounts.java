package com.weemo.sdk.helper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.os.Build;

/**
 * Demo accounts (for fast testing)
 */
@SuppressWarnings("serial")
public final class DemoAccounts extends LinkedHashMap<String, String> {

	/**
	 * Associate UserID => DisplayName
	 */
	public static final Map<String, String> ACCOUNTS = Collections.unmodifiableMap(new DemoAccounts());

	/**
	 * Initializes the {@link #ACCOUNTS} map.
	 *
	 * Change this to set your own list of demo accounts
	 * These are the names the main characters of the manga "Monster" by Naoki Urasawa
	 * Every UID must comply to the Weemo naming rules:
	 * https://github.com/weemo/Release-4.x/wiki/WeemoDriver-Naming#token
	 */
	private DemoAccounts() {
		super();
		put("k.tenma", "Kenzo Tenma");
		put("fortner-n", "Nina Fortner");
		put("runge_h@bka.de", "Heinrich Runge");
		put("ev@heinman", "Eva Heinman");
		put("l j", "Johan Liebert"); // This UID does NOT comply to the Weemo naming rules.
		                                     // You can never find Johan, he finds you.
	}

	/**
	 * Generate a string corresponding to your device make and model.
	 *
	 * @param minify
	 *            if the space caracters should be stripped and replaced by -.
	 * @return The string generated.
	 */
	public static String getDeviceName(boolean minify) {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String res;
		if (model.startsWith(manufacturer)) {
			res = capitalize(model);
		} else {
			res = capitalize(manufacturer) + " " + model;
		}
		return minify ? res.replace(" ", "-") : res;
	}

	/**
	 * Uppercase only the first letter of each words
	 *
	 * @param s
	 *            the string to capitalize
	 * @return the string capitalized
	 */
	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		}
		return Character.toUpperCase(first) + s.substring(1);
	}
}
