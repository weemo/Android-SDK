package com.weemo.sdk.helper.contacts;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.WeemoEngine;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.call.CallActivity;

/*
 * This fragments displays a loading dialog while calling a remote contact.
 * When the call is established, it starts the CallActivity.
 */
public class ContactCallingDialogFragment extends DialogFragment {
	
	public static ContactCallingDialogFragment newInstance(int callId) {
		ContactCallingDialogFragment fragment = new ContactCallingDialogFragment();
		Bundle args = new Bundle();
		args.putInt("callId", callId);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(getArguments().getString("contactId"));
		dialog.setMessage(getString(R.string.calling));
		dialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new OnClickListener() {
			@Override public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				WeemoEngine weemo = Weemo.instance();
				assert weemo != null;
				WeemoCall call = weemo.getCall(getArguments().getInt("callId"));
				if (call != null)
					call.hangup();
			}
		});
		return dialog;
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
	 * This listener catches CallStatusChangedEvent
	 * 1. It is annotated with @WeemoEventListener
	 * 2. It takes one argument which type is CallStatusChangedEvent
	 * 3. It's fragment object has been registered with Weemo.getEventBus().register(this) in onStart()
	 */
	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		// We check that the call affected is the one we are monitoring
		if (e.getCall().getCallId() != getArguments().getInt("callId"))
			return ;
		
		// If the call is now ENDED, it means that the remote user has either:
		// - disconnected
		// - refused the call
		if (e.getCallStatus() == CallStatus.ENDED)
			dismiss();

		// If the call is now ACTIVE, it is now taking place
		// we therefore start the CallActivity
		else if (e.getCallStatus() == CallStatus.ACTIVE) {
			startActivity(
				new Intent(getActivity(), CallActivity.class)
					.putExtra("callId", e.getCall().getCallId())
			);
			dismiss();
		}
	}
}
