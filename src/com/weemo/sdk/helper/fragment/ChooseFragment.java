package com.weemo.sdk.helper.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Selection;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.weemo.sdk.helper.DemoAccounts;
import com.weemo.sdk.helper.R;
import com.weemo.sdk.helper.util.UIUtils;

/**
 * This is a simple fragment that allows the user of the application to chose from a list
 * or to directly enter a value.
 * It is used  multiple times in this project
 * This is a simple util and does not contain Weemo SDK specific code
 */
public class ChooseFragment extends DialogFragment {

	/** Fragment TAG */
	private static final String TAG = "ChooseFragment";

	/** Fragment required string argument key: the text of the validation button */
	private static final String ARG_BUTTONTEXT = "buttonText";

	/** Fragment optional string argument key: the account to remove from choice */
	private static final String ARG_REMOVEID = "removeID";

	/** The input */
	protected @Nullable EditText input;

	/** The validation button */
	protected @Nullable Button goBtn;

	/** The contact list */
	protected @Nullable ListView listView;

	/** Proposed accounts */
	protected Map<String, String> accounts = new LinkedHashMap<String, String>(DemoAccounts.ACCOUNTS);

	/** The string that should be hidden*/
	private String removeID;

	/**
	 * Helper method to show a {@link ChooseFragment}, using
	 * newInstance method and remove any previous
	 * {@link ChooseFragment}.
	 * 
	 * @param buttonText The text of the validation button
	 * @param removeID The account to remove from choice
	 * @param fm The {@link ChooseFragment} to execute the transaction
	 */
	public static void show(final String buttonText, final @CheckForNull String removeID, FragmentManager fm) {
		hide(fm);
		ChooseFragment dialog = newInstance(buttonText, removeID);
		dialog.show(fm, TAG);
	}
	
	/**
	 * Helper method to show a {@link ChooseFragment}, using
	 * newInstance method and remove any previous
	 * {@link ChooseFragment}.
	 * 
	 * @param buttonText The text of the validation button
	 * @param fm The {@link ChooseFragment} to execute the transaction
	 */
	public static void show(final String buttonText, FragmentManager fm) {
		hide(fm);
		ChooseFragment dialog = newInstance(buttonText);
		dialog.show(fm, TAG);
	}
	
	/**
	 * Helper method to remove any previous {@link ChooseFragment}
	 * displayed.
	 * 
	 * @param fm The {@link FragmentManager} to execute the transaction
	 */
	public static void hide(FragmentManager fm) {
		ChooseFragment dialog = (ChooseFragment) fm.findFragmentByTag(TAG);
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	/**
	 * Factory (best practice for fragments)
	 *
	 * @param buttonText The text of the validation button
	 * @param removeID The account to remove from choice
	 * @return The created fragment
	 */
	public static ChooseFragment newInstance(final String buttonText, final @CheckForNull String removeID) {
		final ChooseFragment fragment = new ChooseFragment();
		final Bundle args = new Bundle();
		args.putString(ARG_BUTTONTEXT, buttonText);
		args.putString(ARG_REMOVEID, removeID);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Factory (best practice for fragments)
	 *
	 * @param buttonText The text of the validation button
	 * @return The created fragment
	 */
	public static ChooseFragment newInstance(final String buttonText) {
		return newInstance(buttonText, null);
	}

	/**
	 * Interface that activities using this fragment must implements
	 */
	public static interface ChooseListener {
		/**
		 * Used by this fragment to tell its activity that the user has chosen a user ID
		 *
		 * @param chose The chosen userID
		 */
		public void onChoose(String chose);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.removeID = getArguments().getString(ARG_REMOVEID);
		if (this.removeID != null) {
			this.accounts.remove(this.removeID);
		}
		
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	@Override
	public void onCancel(final DialogInterface dialog) {
		super.onCancel(dialog);
		getActivity().finish();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_choose, container, false);

		this.input = (EditText) root.findViewById(R.id.input);
		if (TextUtils.isEmpty(this.removeID)) {
			this.input.setText(DemoAccounts.getDeviceName(true));
		}

		this.goBtn = (Button) root.findViewById(R.id.go);

		this.goBtn.setText(getArguments().getString(ARG_BUTTONTEXT));

		final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		Set<Entry<String, String>> entrySet = ChooseFragment.this.accounts.entrySet();
		for (Entry<String, String> entry : entrySet) {
			HashMap<String,String> item = new HashMap<String, String>();
			item.put("id", entry.getKey());
			item.put("name", entry.getValue());
			list.add(item);
		}
		
		this.listView = (ListView) root.findViewById(R.id.list);
		this.listView.setAdapter(new SimpleAdapter(getActivity(), list, android.R.layout.simple_list_item_2, new String[] { "id","name" }, new int[] {android.R.id.text1, android.R.id.text2}));

		this.listView.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long itemId) {
				String id = list.get(position).get("id");
				ChooseFragment.this.input.setText(id);
			}
		});

		this.goBtn.setOnClickListener(new OnClickListener() {
			@Override public void onClick(final View arg0) {
				final String txt = ChooseFragment.this.input.getText().toString();

				if (txt.isEmpty()) {
					return ;
				}

				((ChooseListener) getActivity()).onChoose(txt);
			}
		});

		if (savedInstanceState != null) {
			boolean enabled = savedInstanceState.getBoolean("enabled", true);
			setEnabled(enabled);
		}

		this.input.post(new Runnable() {
			@Override
			public void run() {
				ChooseFragment.this.input.requestFocus();
				Selection.selectAll(ChooseFragment.this.input.getEditableText());
				UIUtils.showSoftkeyboard(getActivity(), ChooseFragment.this.input, null);
			}
		});
		
		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("enabled", this.goBtn.isEnabled());
		super.onSaveInstanceState(outState);
	}

	/**
	 * Make this fragment usable or not
	 *
	 * @param enabled Whether or not to enable interaction with this fragment
	 */
	public void setEnabled(final boolean enabled) {
		this.goBtn.setEnabled(enabled);

		this.input.setEnabled(enabled);
		this.input.setFocusable(enabled);
		this.listView.setEnabled(enabled);
		if (enabled) {
			this.input.setFocusableInTouchMode(enabled);
		}
	}

	/**
	 * Changes the text of the selection button
	 *
	 * @param text The new text of the button
	 */
	public void setButtonText(final String text) {
		this.goBtn.setText(text);
	}
}
