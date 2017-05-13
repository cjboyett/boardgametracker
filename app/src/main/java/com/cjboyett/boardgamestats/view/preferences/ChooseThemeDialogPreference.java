package com.cjboyett.boardgamestats.view.preferences;

import android.content.Context;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.ChooseThemeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/22/2016.
 */
public class ChooseThemeDialogPreference extends DialogPreference {
	private List<Integer[]> palettes;
	private Spinner themeSpinner;
	private Integer[] currentPalette;
	private RelativeLayout layout;
	private TextView textView;
	private AppCompatButton button;
	private EditText editText;

	public ChooseThemeDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		int dark = context.getResources().getColor(R.color.colorMainDark);
		int light = context.getResources().getColor(R.color.colorMainLight);

		palettes = new ArrayList<>();
		palettes.add(new Integer[]{dark, light, dark});
		palettes.add(new Integer[]{light, dark, Color.parseColor("#A9A26B")});

		palettes.add(new Integer[]{Color.parseColor("#9EA7C1"),
								   Color.parseColor("#2F2D27"),
								   Color.parseColor("#A9A26B")});
		palettes.add(new Integer[]{Color.parseColor("#2F2D27"),
								   Color.parseColor("#9EA7C1"),
								   Color.parseColor("#A9A26B")});

		palettes.add(new Integer[]{Color.parseColor("#6AA67A"),
								   Color.parseColor("#033811"),
								   Color.parseColor("#A9A26B")});
		palettes.add(new Integer[]{Color.parseColor("#033811"),
								   Color.parseColor("#6AA67A"),
								   Color.parseColor("#A9A26B")});

		palettes.add(new Integer[]{Color.parseColor("#3F0420"),
								   Color.parseColor("#D6C761"),
								   Color.parseColor("#75355D")});
		palettes.add(new Integer[]{Color.parseColor("#D6C761"),
								   Color.parseColor("#3F0420"),
								   Color.parseColor("#A9A26B")});

		palettes.add(new Integer[]{Color.parseColor("#310A60"),
								   Color.parseColor("#D1C535"),
								   Color.parseColor("#A9A26B")});
		palettes.add(new Integer[]{Color.parseColor("#D1C535"),
								   Color.parseColor("#310A60"),
								   Color.parseColor("#A9A26B")});
		currentPalette = palettes.get(0);

		setDialogLayoutResource(R.layout.dialog_choose_theme);
		setPositiveButtonText("Okay");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		themeSpinner = (Spinner) view.findViewById(R.id.spinner_choose_theme);
		layout = (RelativeLayout) view.findViewById(R.id.layout_sample);
		textView = (TextView) view.findViewById(R.id.text_sample);
		button = (AppCompatButton) view.findViewById(R.id.button_sample);
		editText = (EditText) view.findViewById(R.id.edit_text_sample);

		colorComponents(currentPalette);

		ChooseThemeAdapter adapter = new ChooseThemeAdapter(palettes);
		themeSpinner.setAdapter(adapter);

		themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currentPalette = palettes.get(position);
				colorComponents(currentPalette);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		return view;
	}


	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			if (callChangeListener(currentPalette)) {
				Preferences.setThemeBackgroundPreference(getContext(), currentPalette[0]);
				Preferences.setThemeForegroundPreference(getContext(), currentPalette[1]);
				notifyChanged();
			}
		}
	}

	private void colorComponents(Integer[] palette) {
		int backgroundColor = palette[0];
		int foregroundColor = palette[1];
		int buttonColor = palette[2];
		int hintTextColor = ColorUtilities.hintTextColor(backgroundColor, foregroundColor);

		layout.setBackgroundColor(backgroundColor);
		textView.setTextColor(foregroundColor);
		ViewUtilities.tintButtonBackground(button, buttonColor);
		button.setTextColor(foregroundColor);
		editText.setHintTextColor(hintTextColor);
		editText.setTextColor(foregroundColor);
	}
}
