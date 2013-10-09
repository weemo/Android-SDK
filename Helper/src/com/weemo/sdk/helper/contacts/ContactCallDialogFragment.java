package com.weemo.sdk.helper.contacts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent.Error;
import com.weemo.sdk.helper.R;

public class ContactCallDialogFragment extends DialogFragment {
	
	public static ContactCallDialogFragment newInstance(String contactId) {
		ContactCallDialogFragment fragment = new ContactCallDialogFragment();
		Bundle args = new Bundle();
		args.putString("contactId", contactId);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(getArguments().getString("contactId"));
		dialog.setPositiveButton(R.string.call, new OnClickListener() {
			@Override public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				((ContactsActivity) getActivity()).makeCall(getArguments().getString("contactId"));
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		return dialog.create();
	}

	@Override
	public void onStart() {
		super.onStart();

		// Register as event listener
		Weemo.getEventBus().register(this);
	}

	@Override
	public void onStop() {
		// Unregister as event listener
		Weemo.getEventBus().unregister(this);

		super.onStop();
	}

	@WeemoEventListener
	public void onCanCreateCallChanged(CanCreateCallChangedEvent e) {
		Error error = e.getError();
		if (error != null) {
			Toast.makeText(getActivity(), error.description(), Toast.LENGTH_SHORT).show();
			dismiss();
		}
	}

}
