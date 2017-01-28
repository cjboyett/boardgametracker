package com.cjboyett.boardgamestats.view;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

/**
 * Created by Casey on 3/10/2016.
 */
public class AddPlayerView extends RelativeLayout {
	private TextView nameTextView, scoreTextView;
	private DatedTextView deleteTextView;
	private AutoCompleteTextView playerTextView;
	private EditText scoreEditText;

	public AddPlayerView(Context context) {
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.add_player_view, this);

		nameTextView = (TextView) findViewById(R.id.textview_name);
		scoreTextView = (TextView) findViewById(R.id.textview_score);
		deleteTextView = (DatedTextView) findViewById(R.id.button_remove_player);

		playerTextView = (AutoCompleteTextView) findViewById(R.id.edittext_other_player);
		scoreEditText = (EditText) findViewById(R.id.edittext_other_score);

		playerTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) nameTextView.setVisibility(VISIBLE);
				else nameTextView.setVisibility(INVISIBLE);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		scoreEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) scoreTextView.setVisibility(VISIBLE);
				else scoreTextView.setVisibility(INVISIBLE);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	public void colorComponents(int backgroundColor, int foregroundColor, int hintTextColor) {
		setBackgroundColor(backgroundColor);

		findViewById(R.id.relativelayout_add_player).setBackgroundColor(backgroundColor);

		nameTextView.setBackgroundColor(backgroundColor);
		nameTextView.setTextColor(foregroundColor);

		playerTextView.setTextColor(foregroundColor);
		playerTextView.setHintTextColor(hintTextColor);

		scoreTextView.setBackgroundColor(backgroundColor);
		scoreTextView.setTextColor(foregroundColor);

		scoreEditText.setTextColor(foregroundColor);
		scoreEditText.setHintTextColor(hintTextColor);

		deleteTextView.setBackgroundColor(backgroundColor);
		deleteTextView.setTextColor(foregroundColor);

		findViewById(R.id.checkbox_other_win).setBackgroundColor(backgroundColor);
		((CheckBox) findViewById(R.id.checkbox_other_win)).setTextColor(foregroundColor);
		ViewUtilities.tintCheckBox(((AppCompatCheckBox) findViewById(R.id.checkbox_other_win)));
	}
}
