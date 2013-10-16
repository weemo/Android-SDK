package com.weemo.sdk.helper;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/*
 * This is a very simple fragment that creates a loading Dialogfragment.
 * It is used  multiple times in this project
 * This is a simple util and does not contain Weemo SDK specific code
 */
public class LoadingDialogFragment extends DialogFragment {

	public static LoadingDialogFragment newFragmentInstance(String title, String text) {
		LoadingDialogFragment fragment = new LoadingDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("text", text);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(getActivity());
		
		dialog.setTitle(getArguments().getString("title"));
		dialog.setMessage(getArguments().getString("text"));
		
		dialog.setIndeterminate(true);
		
		return dialog;
	}
	
	
}
