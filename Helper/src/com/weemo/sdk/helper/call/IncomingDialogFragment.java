package com.weemo.sdk.helper.call;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import com.weemo.sdk.helper.R;

public class IncomingDialogFragment extends DialogFragment {
	
	public static IncomingDialogFragment newInstance(String displayName) {
		IncomingDialogFragment fragment = new IncomingDialogFragment();
		Bundle args = new Bundle();
		args.putString("displayName", displayName);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		
		dialog.setTitle("Machin");
		dialog.setMessage(R.string.is_calling_you);
		
		dialog.setPositiveButton(R.string.answer, new OnClickListener() {
			@Override public void onClick(DialogInterface paramDialogInterface, int paramInt) {
			}
		});
		
		dialog.setNegativeButton(R.string.decline, null);
		
		dialog.setCancelable(false);
		
		return dialog.create();
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		Log.i("IncomingDialog", "Dismiss");
		getActivity().finish();
	}
}