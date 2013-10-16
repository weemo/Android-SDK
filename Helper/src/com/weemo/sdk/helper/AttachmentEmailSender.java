package com.weemo.sdk.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

/*
 * This is used by ACRA to generate a (crash) report
 */
public class AttachmentEmailSender implements ReportSender {

	private final Application ctx;

	public AttachmentEmailSender(Application ctx) {
		this.ctx = ctx;
	}

	@Override
	public void send(CrashReportData errorContent) throws ReportSenderException {
		int labelRes = ctx.getApplicationInfo().labelRes;
		String appName = (String) (labelRes != 0 ? ctx.getString(labelRes) : "(Android app)");
		String versionName = "";
		int versionCode = -1;
		try {
			PackageInfo pkgInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			versionName = pkgInfo.versionName;
			versionCode = pkgInfo.versionCode;
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { ACRA.getConfig().mailTo() });
		emailIntent.putExtra(
				android.content.Intent.EXTRA_SUBJECT, appName
			+	" " + versionName + " (" + versionCode + ")"
			+	" Crash: " + android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE
		);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Please provide details about the issue (what happened and how it happened) to assist outr team resolve your issue:\n"
		);

		try {
			File outputFile = new File(ctx.getExternalCacheDir(), "crash_" + errorContent.get(ReportField.REPORT_ID) + ".log");
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(buildBody(errorContent).getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
			Uri uri = Uri.fromFile(outputFile);
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		catch (IOException e) {
			throw new ReportSenderException(e.getMessage(), e);
		}

		ctx.startActivity(emailIntent);
	}

	private String buildBody(CrashReportData errorContent) {
		ReportField[] fields = ACRA.getConfig().customReportContent();
		if (fields.length == 0) {
			fields = ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS;
		}

		final StringBuilder builder = new StringBuilder();
		for (ReportField field : fields) {
			builder.append(field.toString()).append(":\n");
			builder.append(errorContent.get(field));
			builder.append("\n\n");
		}
		return builder.toString();
	}

}
