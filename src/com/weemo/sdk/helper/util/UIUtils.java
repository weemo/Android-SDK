package com.weemo.sdk.helper.util;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.ResultReceiver;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

/**
 * Small repository of utility functions
 */
public final class UIUtils {

	/**
	 * This class is a function repository and cannot be instanciated
	 */
	private UIUtils() {}

	/**
	 * This is a horrible hack whose purpose is to force the display of the overflow...
	 *
	 * http://stackoverflow.com/a/11438245/1269640
	 * https://code.google.com/p/android/issues/detail?id=38013
	 *
	 * @param activity The activity to hack
	 */
	public static void forceOverflowMenu(final Activity activity) {
		try {
			final ViewConfiguration config = ViewConfiguration.get(activity);
			final Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch (Throwable exc) {
			exc.printStackTrace();
			// Ignore
		}
	}
	
	/**
	 * Hides the SoftKeyboard
	 * 
	 * @param context A valid context
	 * @param view The view associated with the keyboard
	 */
	public static void hideSoftKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * Shows the SoftKeyboard
	 * 
	 * @param context A valid context
	 * @param view The view requesting this action
	 * @param resultReceiver A callback
	 */
	public static void showSoftkeyboard(Context context, View view, ResultReceiver resultReceiver) {
		if (context == null) {
			return;
		}
		Resources resources = context.getResources();
		if (resources == null) {
			return;
		}
		Configuration config = context.getResources().getConfiguration();
		if (config == null) {
			return;
		}
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (resultReceiver != null) {
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT, resultReceiver);
			} else {
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}
	}

}
