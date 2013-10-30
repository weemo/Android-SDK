package com.weemo.sdk.helper.contacts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.Toast;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent;
import com.weemo.sdk.event.global.CanCreateCallChangedEvent.Error;
import com.weemo.sdk.helper.R;

/*
 * Displays a confirmation dialog asking if the user want to call a remote contact.
 * This DialogFragment should only be called after the remote contact availability has been checked.
 */
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
				// When the users clicks on the "OK" button,
				// We start the call
				WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				weemo.createCall(getArguments().getString("contactId"));
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		return dialog.create();
	}

	@Override
	public void onStart() {
		super.onStart();

		// Register as event listener
		Weemo.eventBus().register(this);
	}

	@Override
	public void onStop() {
		// Unregister as event listener
		Weemo.eventBus().unregister(this);

		super.onStop();
	}

	/*
	 * This listener catches CanCreateCallChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CanCreateCallChangedEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onStart()
	 */
	@WeemoEventListener
	public void onCanCreateCallChanged(CanCreateCallChangedEvent e) {
		Error error = e.getError();
		// If there is an error, it means the user can not create a call anymore.
		// This means that this popup should not be shown anymore.
		if (error != null) {
			Toast.makeText(getActivity(), error.description(), Toast.LENGTH_SHORT).show();
			dismiss();
		}
	}

}
