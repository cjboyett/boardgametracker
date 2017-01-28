package com.cjboyett.boardgamestats.view.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;

/**
 * Created by Casey on 4/18/2016.
 */
public class SyncBggDataDialogPreference extends DialogPreference {
	private Context context;
	private EditText usernameEditText;

	public SyncBggDataDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;

		setDialogLayoutResource(R.layout.dialog_sync_bgg_data);
		setPositiveButtonText("Import");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		usernameEditText = (EditText) view.findViewById(R.id.edittext_bgg_username);
		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			final String username = usernameEditText.getText().toString();
			GameDownloadUtilities.syncWithBgg(context, username);
		}
	}

}
