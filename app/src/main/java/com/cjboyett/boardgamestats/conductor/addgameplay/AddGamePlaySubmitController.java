package com.cjboyett.boardgamestats.conductor.addgameplay;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

public class AddGamePlaySubmitController extends BaseController {
	private View view;
	private boolean ignore;

	private AddGamePlayTabbedController parent;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.fragment_add_game_play_submit, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View v) {
		super.onAttach(v);
		view.findViewById(R.id.button_submit_gameplay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				parent.makeGamePlay(true, ((CheckBox) view.findViewById(R.id.checkbox_ignore)).isChecked());
			}
		});
		view.findViewById(R.id.button_submit_gameplay_and_share).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				parent.makeGamePlayAndShare(((CheckBox) view.findViewById(R.id.checkbox_ignore)).isChecked());
			}
		});
		((CheckBox) view.findViewById(R.id.checkbox_ignore)).setChecked(ignore);
		setColors();
		colorComponents();
	}

	public void setParent(AddGamePlayTabbedController parent) {
		this.parent = parent;
	}

	private void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.button_submit_gameplay),
										   buttonColor);
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.button_submit_gameplay_and_share),
										   buttonColor);

		((Button) view.findViewById(R.id.button_submit_gameplay)).setTextColor(foregroundColor);

		((Button) view.findViewById(R.id.button_submit_gameplay_and_share)).setTextColor(foregroundColor);

		view.findViewById(R.id.checkbox_ignore).setBackgroundColor(backgroundColor);
		((CheckBox) view.findViewById(R.id.checkbox_ignore)).setTextColor(foregroundColor);
		ViewUtilities.tintCheckBox(((AppCompatCheckBox) view.findViewById(R.id.checkbox_ignore)));

	}

	public void setIgnoreCheckBox(boolean ignore) {
		this.ignore = ignore;
	}
}
