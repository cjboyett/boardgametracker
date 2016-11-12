package com.cjboyett.boardgamestats.view.preferences;

import android.content.Context;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.view.adapter.ChooseThemeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/22/2016.
 */
public class ChooseThemeDialogPreference extends DialogPreference
{
	private List<Integer[]> palettes;
	private Spinner themeSpinner;
	private Integer[] currentPalette;

	public ChooseThemeDialogPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		int dark = context.getResources().getColor(R.color.colorMainDark);
		int light = context.getResources().getColor(R.color.colorMainLight);

		palettes = new ArrayList<>();
		palettes.add(new Integer[]{dark, light});
		palettes.add(new Integer[]{light, dark});

		palettes.add(new Integer[]{Color.parseColor("#9EA7C1"), Color.parseColor("#2F2D27")});
		palettes.add(new Integer[]{Color.parseColor("#2F2D27"), Color.parseColor("#9EA7C1")});

		palettes.add(new Integer[]{Color.parseColor("#6AA67A"), Color.parseColor("#033811")});
		palettes.add(new Integer[]{Color.parseColor("#033811"), Color.parseColor("#6AA67A")});

		palettes.add(new Integer[]{Color.parseColor("#3F0420"), Color.parseColor("#D6C761")});
		palettes.add(new Integer[]{Color.parseColor("#D6C761"), Color.parseColor("#3F0420")});

		palettes.add(new Integer[]{Color.parseColor("#310A60"), Color.parseColor("#D1C535")});
		palettes.add(new Integer[]{Color.parseColor("#D1C535"), Color.parseColor("#310A60")});
		currentPalette = palettes.get(0);

		setDialogLayoutResource(R.layout.dialog_choose_theme);
		setPositiveButtonText("Okay");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView()
	{
		View view = super.onCreateDialogView();
		themeSpinner = (Spinner)view.findViewById(R.id.spinner_choose_theme);

		ChooseThemeAdapter adapter = new ChooseThemeAdapter(palettes);
		themeSpinner.setAdapter(adapter);

		themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				currentPalette = palettes.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});
		return view;
	}



	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if (positiveResult)
		{
			if (callChangeListener(currentPalette))
			{
				Preferences.setThemeBackgroundPreference(getContext(), currentPalette[0]);
				Preferences.setThemeForegroundPreference(getContext(), currentPalette[1]);
				notifyChanged();
			}
		}
	}
}
