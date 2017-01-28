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
public class ImportCollectionDialogPreference extends DialogPreference {
	private Context context;
	private EditText usernameEditText;

	public ImportCollectionDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;

		setDialogLayoutResource(R.layout.dialog_import_collection);
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
			String username = usernameEditText.getText().toString();
			GameDownloadUtilities.downloadCollections(context, username);
		}
	}

}