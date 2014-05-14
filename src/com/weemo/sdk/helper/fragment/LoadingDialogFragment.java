package com.weemo.sdk.helper.fragment;

import javax.annotation.CheckForNull;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * This is a very simple fragment that creates a loading Dialogfragment.
 * It is used  multiple times in this project
 * This is a simple util and does not contain Weemo SDK specific code
 */
public class LoadingDialogFragment extends DialogFragment {
	
	/** Fragment TAG */
	private static final String TAG = "LoadingDialogFragment";

	/** Fragment required string argument key: the title of the dialog */
	private static final String ARG_TITLE = "title";

	/** Fragment required string argument key: the text of the dialog */
	private static final String ARG_TEXT = "text";

	/** Fragment optional string argument key: the text of the cancel button */
	private static final String ARG_CANCEL = "cancel";

	
	/**
	 * Helper method to show a {@link LoadingDialogFragment}, using
	 * newInstance method and remove any previous
	 * {@link LoadingDialogFragment}.
	 * 
	 * @param title The title of the dialog
	 * @param text The text of the dialog
	 * @param cancel Text for cancel button (can be null)
	 * @param fm The {@link FragmentManager} to execute the transaction
	 */
	public static void show(final String title, final String text, final @CheckForNull String cancel, FragmentManager fm) {
		hide(fm);
		LoadingDialogFragment dialog = newInstance(title, text, cancel);
		dialog.setCancelable(false);
		fm.beginTransaction().add(dialog, TAG).commitAllowingStateLoss();
		fm.executePendingTransactions();
	}
	
	/**
	 * Helper method to remove any previous {@link LoadingDialogFragment}
	 * displayed.
	 * 
	 * @param fm The {@link FragmentManager} to execute the transaction
	 */
	public static void hide(FragmentManager fm) {
		LoadingDialogFragment dialog = (LoadingDialogFragment) fm.findFragmentByTag(TAG);
		if (dialog != null) {
			dialog.dismissAllowingStateLoss();
			fm.executePendingTransactions();
		}
	}
	
	/**
	 * Factory (best practice for fragments)
	 *
	 * @param title The title of the dialog
	 * @param text The text of the dialog
	 * @param cancel Text for cancel button (can be null)
	 * @return The created fragment
	 */
	private static LoadingDialogFragment newInstance(final String title, final String text, final @CheckForNull String cancel) {
		final LoadingDialogFragment fragment = new LoadingDialogFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putString(ARG_TEXT, text);
		args.putString(ARG_CANCEL, cancel);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final ProgressDialog dialog = new ProgressDialog(getActivity());

		String title = getArguments().getString(ARG_TITLE);
		if(!TextUtils.isEmpty(title)) {
			dialog.setTitle(title);
		}
		dialog.setMessage(getArguments().getString(ARG_TEXT));

		String cancel = getArguments().getString(ARG_CANCEL);
		if (cancel != null) {
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, new OnClickListener() {
				@Override public void onClick(final DialogInterface dlg, int which) {
					Activity activity = getActivity();
					if (activity instanceof DialogInterface.OnCancelListener) {
						((DialogInterface.OnCancelListener) activity).onCancel(dialog);
					}
				}
			});
		}
		return dialog;
	}
}
