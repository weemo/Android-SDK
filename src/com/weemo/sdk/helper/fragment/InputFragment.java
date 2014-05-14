package com.weemo.sdk.helper.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.util.UIUtils;

/**
 * This is a very simple fragment that displays a message and an input
 */
public class InputFragment extends DialogFragment {

	/** Fragment TAG */
	private static final String TAG = "InputFragment";
	
	/** Fragment required string argument key: the text of the dialog */
	private static final String ARG_TEXT = "text";
	
	/**
	 * Helper method to show a {@link InputFragment}, using
	 * newInstance method and remove any previous
	 * {@link InputFragment}.
	 * 
	 * @param text The text of the dialog
	 * @param fm The {@link FragmentManager} to execute the transaction
	 */
	public static void show(final String text, FragmentManager fm) {
		hide(fm);
		InputFragment dialog = newInstance(text);
		dialog.show(fm, TAG);
	}
	
	/**
	 * Helper method to remove any previous {@link InputFragment}
	 * displayed.
	 * 
	 * @param fm The {@link FragmentManager} to execute the transaction
	 */
	public static void hide(FragmentManager fm) {
		InputFragment dialog = (InputFragment) fm.findFragmentByTag(TAG);
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param text The text of the dialog
	 * @return The created fragment
	 */
	private static InputFragment newInstance(final String text) {
		final InputFragment fragment = new InputFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_TEXT, text);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Interface that activities using this fragment must implements
	 */
	public static interface InputListener {
		/**
		 * Used by this fragment to tell its activity that the user has entered and validated an input
		 *
		 * @param text The entered text
		 */
		public void onInput(String text);
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.weemo_appid);
		final View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_input, null);
		final EditText input = (EditText) root.findViewById(R.id.input);
		input.setText(getArguments().getString(ARG_TEXT));
		builder.setView(root);
		builder.setPositiveButton(android.R.string.ok, null);

		final AlertDialog dialog = builder.create();

		// We use this instead of setPositiveButtonlistener to be able to prevent dismissal
		dialog.setOnShowListener(new OnShowListener() {
			@Override public void onShow(final DialogInterface dialogInterface) {
				final Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
				button.setOnClickListener(new OnClickListener() {
					@Override public void onClick(final View view) {
						((InputListener) getActivity()).onInput(input.getText().toString());
					}
				});
			}
		});

		dialog.setCanceledOnTouchOutside(false);
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
	
	@Override
	public void onCancel(final DialogInterface dialog) {
		super.onCancel(dialog);
		getActivity().onBackPressed();
	}
	
}
