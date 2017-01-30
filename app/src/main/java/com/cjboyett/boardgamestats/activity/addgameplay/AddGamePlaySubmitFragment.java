package com.cjboyett.boardgamestats.activity.addgameplay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

public class AddGamePlaySubmitFragment extends Fragment {
	private View view;
	private int backgroundColor, foregroundColor;
	private boolean ignore;

	public AddGamePlaySubmitFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final AddGamePlayTabbedActivity activity = (AddGamePlayTabbedActivity) getActivity();
		view = inflater.inflate(R.layout.fragment_add_game_play_submit, container, false);
		view.findViewById(R.id.button_submit_gameplay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.makeGamePlay(true, ((CheckBox) view.findViewById(R.id.checkbox_ignore)).isChecked());
			}
		});
		view.findViewById(R.id.button_submit_gameplay_and_share).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.makeGamePlayAndShare(((CheckBox) view.findViewById(R.id.checkbox_ignore)).isChecked());
			}
		});
		((CheckBox) view.findViewById(R.id.checkbox_ignore)).setChecked(ignore);
		setColors();
		colorComponents();
		return view;
	}

	private void setColors() {
		backgroundColor = Preferences.getBackgroundColor(getActivity());
		foregroundColor = Preferences.getForegroundColor(getActivity());
	}

	private void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		if (Preferences.lightUI(getActivity())) {
			view.findViewById(R.id.button_submit_gameplay)
				.setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.button_submit_gameplay_and_share)
				.setBackgroundResource(R.drawable.main_button_background_dark);
		} else {
			view.findViewById(R.id.button_submit_gameplay)
				.setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.button_submit_gameplay_and_share)
				.setBackgroundResource(R.drawable.main_button_background_light);
		}

//		view.findViewById(R.id.button_submit_gameplay).setBackgroundColor(backgroundColor);
		((Button) view.findViewById(R.id.button_submit_gameplay)).setTextColor(foregroundColor);

//		view.findViewById(R.id.button_submit_gameplay_and_share).setBackgroundColor(backgroundColor);
		((Button) view.findViewById(R.id.button_submit_gameplay_and_share)).setTextColor(foregroundColor);

		view.findViewById(R.id.checkbox_ignore).setBackgroundColor(backgroundColor);
		((CheckBox) view.findViewById(R.id.checkbox_ignore)).setTextColor(foregroundColor);
		ViewUtilities.tintCheckBox(((AppCompatCheckBox) view.findViewById(R.id.checkbox_ignore)));

	}

	public void setIgnoreCheckBox(boolean ignore) {
		this.ignore = ignore;
	}
}
