package com.weemo.sdk.helper;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import android.app.Application;

import com.github.nativehandler.NativeCrashHandler;
import com.weemo.sdk.Weemo;

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
//		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
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

//	private ANRWatchDog watchDog = new ANRWatchDog(10000);

	static {
		Weemo.ensureNativeLoad();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
		
		ACRA.getErrorReporter().setReportSender(new AttachmentEmailSender(this));
		
		// This disables ACRA during development
		//ACRA.getACRASharedPreferences().edit().putBoolean("acra.disable", true).apply();

		//watchDog.start();
		
		new NativeCrashHandler().registerForNativeCrash(this);
	}
}
