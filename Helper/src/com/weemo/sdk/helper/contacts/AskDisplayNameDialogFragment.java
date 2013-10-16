package com.weemo.sdk.helper.contacts;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.weemo.sdk.helper.R;

/*
 * This is a simple fragment that asks the user to chose a display name.
 * This is a simple util and does not contain Weemo SDK specific code
 */
public class AskDisplayNameDialogFragment extends DialogFragment {
	
	@Nullable EditText input = null;

	public static AskDisplayNameDialogFragment newInstance(String defaultName) {
		AskDisplayNameDialogFragment fragment = new AskDisplayNameDialogFragment();
		Bundle args = new Bundle();
		args.putString("defaultName", defaultName);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(@CheckForNull Bundle savedInstanceState) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(R.string.ask_title);
		alert.setMessage(R.string.ask_message);

		input = new EditText(getActivity());
		input.setId(R.id.input);
		
		if (savedInstanceState == null) {
			String defaultName = getArguments().getString("defaultName");
			input.setText(defaultName);
			input.setSelection(defaultName.length());
		}

		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			String value = input.getText().toString();
			((ContactsActivity) getActivity()).setDisplayName(value);
			}
		});

		alert.setNegativeButton(android.R.string.cancel, null);

		return alert.create();
	}
}