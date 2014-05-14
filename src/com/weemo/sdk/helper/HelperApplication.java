package com.weemo.sdk.helper;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;

import com.github.anrwatchdog.ANRWatchDog;
import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.helper.util.AttachmentEmailACRASender;
import com.weemo.sdk.helper.util.AttachmentEmailACRASender.Mode;
import com.weemo.sdk.helper.util.ReportException;

/**
 * Application object, mainly used for debugging purposes
 */
// This is the ACRA configuration anotation.
// Acra enables great crash reporting.
// More intel at https://github.com/ACRA/acra
@ReportsCrashes(
		formKey = "",
		mailTo = "mobilesdk@weemo.com",
		reportType = HttpSender.Type.JSON,
		forceCloseDialogAfterToast = false,
		mode = ReportingInteractionMode.DIALOG,
		resToastText = R.string.crash_toast_text,
		resDialogTitle = R.string.app_name,
		resDialogText = R.string.crash_dialog_text,
		logcatArguments = { "-t", "2000", "-v", "time" },
		resDialogOkToast = R.string.crash_dialog_ok_toast,
		customReportContent = {
				ReportField.REPORT_ID,
				ReportField.BRAND,
				ReportField.PRODUCT,
				ReportField.PHONE_MODEL,
				ReportField.ANDROID_VERSION,
				ReportField.APP_VERSION_NAME,
				ReportField.APP_VERSION_CODE,
				ReportField.CUSTOM_DATA,
				ReportField.BUILD,
				ReportField.STACK_TRACE,
				ReportField.INITIAL_CONFIGURATION,
				ReportField.CRASH_CONFIGURATION,
				ReportField.DISPLAY,
				ReportField.USER_APP_START_DATE,
				ReportField.USER_CRASH_DATE,
				ReportField.LOGCAT,
				ReportField.EVENTSLOG,
				ReportField.RADIOLOG,
				ReportField.INSTALLATION_ID,
				ReportField.DEVICE_FEATURES,
				ReportField.ENVIRONMENT,
				ReportField.SETTINGS_SYSTEM,
				ReportField.THREAD_DETAILS
		}
		)
public class HelperApplication extends Application {

	/**
	 * Because we are using NativeCrashHandler in onCreate (which uses native code),
	 * we need to make sure that the Weemo native library has correctly loaded.
	 * This is not necessary in a client application and only usefull in the context of Weemo.
	 */
	static {
		Weemo.ensureNativeLoad();
	}

	/** Key to override the AppID */
	public static final String KEY_APP_ID = "app_id";
	/** Key to override the UserID */
	public static final String KEY_UID = "user_id";
	/** Key to override the displayName */
	public static final String KEY_DISPLAY_NAME = "display_name";
	/** Key to override the callee UID */
	public static final String KEY_CALLEE = "callee";

	/** Overriden value of the AppID */
	public static String overrideAppID;
	/** Overriden value of the UserID */
	public static String overrideUID;
	/** Overriden value of the displayName */
	public static String overrideDisplayName;
	/** Overriden value of the callee UID */
	public static String overrideCallee;
	
	/** Acra key for APP_ID */
	public static final String ACRA_CUSTOM_APP_ID = "APP_ID";
	/** Acra key for USER_ID */
	public static final String ACRA_CUSTOM_USER_ID = "USER_ID";
	/** Acra key for USER_TYPE */
	public static final String ACRA_CUSTOM_USER_TYPE = "USER_TYPE";
	/** Acra key for DEVICE_TYPE */
	public static final String ACRA_CUSTOM_DEVICE_TYPE = "DEVICE_TYPE";
	/** Acra key for INSTALL_ID */
	public static final String ACRA_CUSTOM_INSTALL_ID = "INSTALL_ID";
	
	/** CountDown timer to put Weemo in background mode */
	private static CountDownTimer countDownTimer;
	
	@Override
	public void onCreate() {
		super.onCreate();

		// Please not that the three steps below are only useful for error reporting purposes.
		// It is not needed in a client application.

		// ACRA startup
		ACRA.init(this);
		ACRA.getErrorReporter().setReportSender(new AttachmentEmailACRASender(this));

		// Starts the ANR WatchDog
		// More intel in https://github.com/SalomonBrys/ANR-WatchDog
		new ANRWatchDog().start();
	}
	
	/**
	 * Override application data contained in the provided {@link Intent}.
	 * @param intent The {@link Intent} to crawl
	 */
	public static void detectOverrideData(Intent intent) {
		Uri data = intent.getData();
		if (data != null) {
			try {
				overrideAppID = data.getQueryParameter(KEY_APP_ID);
				overrideUID = data.getQueryParameter(KEY_UID);
				overrideDisplayName = data.getQueryParameter(KEY_DISPLAY_NAME);
				overrideCallee = data.getQueryParameter(KEY_CALLEE);
			} catch (UnsupportedOperationException ignore) {
				// NO-OP
			}
		}
	}

	/**
	 * Call this method to generate a report and send it.
	 */
	public static void sendReport() {
		AttachmentEmailACRASender.MODE = Mode.REPORT;
		ACRA.getErrorReporter().handleSilentException(new ReportException());
	}
	
	/**
	 * Start the count down timer to put Weemo in background mode if there is no call in progress.
	 */
	public static void startCountDown() {
		if (countDownTimer == null) {
			countDownTimer = new CountDownTimer(10000, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					Log.i(HelperApplication.class.getSimpleName(), millisUntilFinished + "ms until going to background mode");
				}

				@Override
				public void onFinish() {
					final WeemoEngine weemo = Weemo.instance();
					if (weemo != null && weemo.getCurrentCall() == null) {
						// If there is no call going on, then we go to
						// background which allows the Weemo engine to save
						// battery
						Log.i(HelperApplication.class.getSimpleName(), "Going to background mode");
						weemo.goToBackground();
					}
				}
			}.start();
		} else {
			cancelCountDown();
		}
		countDownTimer.start();
		Log.i(HelperApplication.class.getSimpleName(), "Starting countDown to background mode");
	}

	/**
	 * Cancel the count down timer.
	 */
	public static void cancelCountDown() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
		}
	}
}
