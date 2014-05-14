package com.weemo.sdk.helper.contacts;

import javax.annotation.CheckForNull;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Selection;
import android.view.WindowManager;
import android.widget.EditText;

import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.util.UIUtils;

/**
 * This is a simple fragment that asks the user to chose a display name.
 * This is a simple util and does not contain Weemo SDK specific code
 */
public class AskDisplayNameDialogFragment extends DialogFragment {

	/** Fragment required string argument key: the default value in the input */
	private static final String ARG_DEFAULTNAME = "defaultName";

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param defaultName The default value in the input
	 * @return The created fragment
	 */
	public static AskDisplayNameDialogFragment newInstance(final String defaultName) {
		final AskDisplayNameDialogFragment fragment = new AskDisplayNameDialogFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_DEFAULTNAME, defaultName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(final @CheckForNull Bundle savedInstanceState) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(R.string.ask_title);
		alert.setMessage(R.string.ask_message);

		final EditText input = new EditText(getActivity());
		input.setId(R.id.input);

		if (savedInstanceState == null) {
			final String defaultName = getArguments().getString(ARG_DEFAULTNAME);
			input.setText(defaultName);
		}

		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override public void onClick(final DialogInterface dialog, final int whichButton) {
				final String value = input.getText().toString();
				((ContactsActivity) getActivity()).setDisplayName(value);
			}
		});

		setCancelable(false);

		AlertDialog dialog = alert.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		input.post(new Runnable() {
			@Override
			public void run() {
				input.requestFocus();
				Selection.selectAll(input.getEditableText());
				UIUtils.showSoftkeyboard(getActivity(), input, null);
			}
		});
		return dialog;
	}
}