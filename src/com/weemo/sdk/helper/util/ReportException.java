package com.weemo.sdk.helper.util;

/**
 * Exception without stack trace, used for manual error reporting.
 */
public class ReportException extends Exception {

	/** Serial */
	private static final long serialVersionUID = 1L;

	/** Constructor */
	public ReportException() {
		super("NO EXCEPTION, this is a report");
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
