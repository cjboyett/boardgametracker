package com.cjboyett.boardgamestats.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

/**
 * Created by Casey on 9/7/2016.
 */
public class AddGameForRecommendationView extends RelativeLayout
{
	private TextView gameTextView, weightTextView;
	private DatedTextView deleteTextView;
	private AutoCompleteTextView addGameEditTextView;
	private EditText weightEditText;

	public AddGameForRecommendationView(Context context)
	{
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.add_game_for_recommendation_view, this);

		gameTextView = (TextView) findViewById(R.id.textview_game);
		weightTextView = (TextView) findViewById(R.id.textview_weight);
		deleteTextView = (DatedTextView) findViewById(R.id.button_remove_game);

		addGameEditTextView = (AutoCompleteTextView) findViewById(R.id.edittext_add_game);
		weightEditText = (EditText) findViewById(R.id.edittext_weight);

		addGameEditTextView.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if (s.length() > 0) gameTextView.setVisibility(VISIBLE);
				else gameTextView.setVisibility(INVISIBLE);
			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});

		weightEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if (s.length() > 0) weightTextView.setVisibility(VISIBLE);
				else weightTextView.setVisibility(INVISIBLE);
			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});
	}

	public void colorComponents(int backgroundColor, int foregroundColor, int hintTextColor)
	{
		setBackgroundColor(backgroundColor);

		findViewById(R.id.relativelayout_add_game).setBackgroundColor(backgroundColor);

		gameTextView.setBackgroundColor(backgroundColor);
		gameTextView.setTextColor(foregroundColor);

		addGameEditTextView.setTextColor(foregroundColor);
		addGameEditTextView.setHintTextColor(hintTextColor);

		weightTextView.setBackgroundColor(backgroundColor);
		weightTextView.setTextColor(foregroundColor);

		weightEditText.setTextColor(foregroundColor);
		weightEditText.setHintTextColor(hintTextColor);

		deleteTextView.setBackgroundColor(backgroundColor);
		deleteTextView.setTextColor(foregroundColor);
	}

	public void setAdapter(ArrayAdapter adapter)
	{
		addGameEditTextView.setAdapter(adapter);
		addGameEditTextView.setThreshold(2);
	}

	public String getGame()
	{
		return addGameEditTextView.getText().toString();
	}

	public double getWeight()
	{
		String weight = weightEditText.getText().toString();
		if (TextUtils.isEmpty(weight)) return 1d;
		else return Double.parseDouble(weight);
	}
}
