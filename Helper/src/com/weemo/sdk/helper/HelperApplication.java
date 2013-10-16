package com.weemo.sdk.helper;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import android.app.Application;

import com.github.anrwatchdog.ANRWatchDog;
import com.github.nativehandler.NativeCrashHandler;
import com.weemo.sdk.Weemo;

/*
 * This is the ACRA configuration anotation.
 * Acra enables great crash reporting.
 * More intel at https://github.com/ACRA/acra
 */
@ReportsCrashes(
		formKey = "",
		mailTo = "coredumps@weemo.com",
		reportType = HttpSender.Type.JSON,
		forceCloseDialogAfterToast = false,
		mode = ReportingInteractionMode.DIALOG,
		resToastText = R.string.crash_toast_text,
		resDialogTitle = R.string.app_name,
		resDialogText = R.string.crash_dialog_text,
		logcatArguments = { "-t", "1000", "-v", "time" },
		resDialogOkToast = R.string.crash_dialog_ok_toast,
		customReportContent = {
				ReportField.REPORT_ID,
				ReportField.ANDROID_VERSION,
				ReportField.BUILD,
				ReportField.APP_VERSION_NAME,
				ReportField.APP_VERSION_CODE,
				ReportField.BRAND,
				ReportField.PRODUCT,
				ReportField.PHONE_MODEL,
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

	/*
	 * This is a Watchdog that will detect ANRs
	 * More intel in https://github.com/SalomonBrys/ANR-WatchDog
	 */
	private ANRWatchDog watchDog = new ANRWatchDog(20000);

	/*
	 * Because we are using NativeCrashHandler in onCreate (which uses native code),
	 * we need to make sure that the Weemo native library has correctly loaded.
	 * This is not necessary in a client application and only usefull in the context of Weemo.
	 */
	static {
		Weemo.ensureNativeLoad();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		// Please not that the three steps below are only useful for error reporting purposes.
		// It is not needed in a client application.
		
		// ACRA startup
		ACRA.init(this);
		ACRA.getErrorReporter().setReportSender(new AttachmentEmailSender(this));

		// Starts the ANR WatchDog
		watchDog.start();

		// Registers the Native Crash Handler to be activated in case of native crash.
		new NativeCrashHandler().registerForNativeCrash(this);
	}
}
