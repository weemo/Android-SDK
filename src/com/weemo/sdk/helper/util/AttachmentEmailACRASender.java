package com.weemo.sdk.helper.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.impl.util.Util;

/**
 * This is used by ACRA to generate a (crash) report
 */
public class AttachmentEmailACRASender implements ReportSender {

	/** The application context */
	private final Application ctx;
	
	public enum Mode {
		CRASH("Crash"), REPORT("Report");
		public final String value;

		private Mode(String value) {
			this.value = value;
		}
	}
	/** This is used to distinguish a Crash from a simple Report */
	public static Mode MODE = Mode.CRASH;

	/**
	 * Constructor
	 *
	 * @param ctx The application context
	 */
	public AttachmentEmailACRASender(final Application ctx) {
		this.ctx = ctx;
	}

	@Override
	public void send(final CrashReportData errorContent) throws ReportSenderException {
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { ACRA.getConfig().mailTo() });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, buildSubject());
		emailIntent.putExtra(Intent.EXTRA_TEXT,"Please provide details about the issue (what happened and how it happened) to assist our team resolve your issue:\n");
		emailIntent.putExtra(Intent.EXTRA_STREAM, saveFile(errorContent));
		this.ctx.startActivity(emailIntent);
		MODE = Mode.CRASH;
	}
	
	
	/**
	 * Save the file with the provided error content which will be send by email
	 * @param errorContent The content 
	 * @return The uri of the created file
	 * @throws ReportSenderException if the the isn't successfuly saved
	 */
	@SuppressLint("DefaultLocale")
	private Uri saveFile(final CrashReportData errorContent) throws ReportSenderException {
		try {
			final File outputFile = new File(this.ctx.getExternalCacheDir(), MODE.value.toLowerCase() + "_" + errorContent.get(ReportField.REPORT_ID) + ".log");
			final FileOutputStream outputStream = new FileOutputStream(outputFile);
			try {
				outputStream.write(buildBody(errorContent).getBytes("UTF-8"));
				outputStream.flush();
			}
			finally {
				outputStream.close();
			}
			return Uri.fromFile(outputFile);
		}
		catch (IOException e) {
			throw new ReportSenderException(e.getMessage(), e);
		}
	}
	
	/**
	 * Build the subject of the email
	 * @return The subject of the email
	 */
	private String buildSubject() {
		if (MODE == null) {
			MODE = Mode.CRASH;
		}
		final String appName = this.ctx.getString(com.weemo.sdk.helper.R.string.app_name);
		return appName + " Android " + Weemo.getVersionFull(this.ctx) + " " + MODE.value + " " + Util.getDeviceName();
	}

	/**
	 * Build the body of the email containing the crash report
	 *
	 * @param errorContent The crash report data
	 * @return The body of the email
	 */
	private static String buildBody(final CrashReportData errorContent) {
		ReportField[] fields = ACRA.getConfig().customReportContent();
		if (fields.length == 0) {
			fields = ACRAConstants.DEFAULT_MAIL_REPORT_FIELDS;
		}

		final StringBuilder builder = new StringBuilder();
		for (final ReportField field : fields) {
			builder.append(field.toString()).append(":\n");
			builder.append(errorContent.get(field));
			builder.append("\n\n");
		}
		return builder.toString();
	}

}
