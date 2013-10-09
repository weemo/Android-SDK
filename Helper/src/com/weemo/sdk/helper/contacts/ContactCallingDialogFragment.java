package com.weemo.sdk.helper.contacts;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.weemo.sdk.Weemo;
import com.weemo.sdk.WeemoCall;
import com.weemo.sdk.WeemoCall.CallStatus;
import com.weemo.sdk.event.WeemoEventListener;
import com.weemo.sdk.event.call.CallStatusChangedEvent;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.call.CallActivity;
import com.weemo.sdk.helper.call.IncomingActivity;

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
				Weemo weemo = Weemo.instance();
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
		Weemo.getEventBus().register(this);
	}

	@Override
	public void onStop() {
		// Unregister as event listener
		Weemo.getEventBus().unregister(this);

		super.onStop();
	}
	
	@WeemoEventListener
	public void onCallStatusChanged(CallStatusChangedEvent e) {
		if (e.getCall().getCallId() != getArguments().getInt("callId"))
			return ;
		
		if (e.getCallStatus() == CallStatus.ENDED)
			dismiss();
		else if (e.getCallStatus() == CallStatus.ACTIVE) {
			Log.i("NPEAVOID", "Starting CallActivity from CallingDialog");
			startActivity(
				new Intent(getActivity(), CallActivity.class)
					.putExtra("callId", e.getCall().getCallId())
			);
			dismiss();
		}
	}
}
