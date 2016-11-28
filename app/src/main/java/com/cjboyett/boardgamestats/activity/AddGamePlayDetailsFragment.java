package com.cjboyett.boardgamestats.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.view.adapter.FilteredGameArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;

public class AddGamePlayDetailsFragment extends Fragment
{
	private TextView gameTextView, timePlayedTextView, dateTextView, locationTextView, notesTextView;
	private AddGamePlayTabbedActivity parent;
	private EditText timePlayedEditText, notesEditText;
	private AutoCompleteTextView gameEditText;
	private static AutoCompleteTextView locationEditText;
	private static EditText dateEditText;

	private TextView textViewStartTimer;
	private Chronometer timer;
	private boolean timerRunning;
	private long timerBase, lastStartTime, lastStopTime, diff;

	private ImageButton addPicturesButton;
	private List<String> picturePaths;

	private List<String> games;

	private String timePlayed = "", location = "", notes = "", gameType = "";
	private static String date;

	private View view;
	private int backgroundColor, foregroundColor, hintTextColor;

	private static final int REQUEST_IMAGE = 201;

	public AddGamePlayDetailsFragment()
	{
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{

		view = inflater.inflate(R.layout.fragment_add_game_play_details, container, false);

		gameTextView = (TextView) view.findViewById(R.id.textview_game);
		gameEditText = (AutoCompleteTextView) view.findViewById(R.id.edittext_game);
		timePlayedTextView = (TextView) view.findViewById(R.id.textview_time_played);
		timePlayedEditText = (EditText) view.findViewById(R.id.edittext_time_played);
		locationTextView = (TextView) view.findViewById(R.id.textview_location);
		locationEditText = (AutoCompleteTextView) view.findViewById(R.id.edittext_location);
		notesTextView = (TextView) view.findViewById(R.id.textview_notes);
		notesEditText = (EditText) view.findViewById(R.id.edittext_gameplay_notes);
		dateTextView = (TextView) view.findViewById(R.id.textview_date);
		dateEditText = (EditText) view.findViewById(R.id.edittext_date);

		addPicturesButton = (ImageButton) view.findViewById(R.id.imagebutton_add_pictures);
		picturePaths = new ArrayList<>();

		textViewStartTimer = (TextView) view.findViewById(R.id.textview_start_timer);
		timer = (Chronometer) view.findViewById(R.id.timer);

		View.OnClickListener timerListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				timer.setTextColor(Color.TRANSPARENT);

				if (timerRunning)
				{
					lastStopTime = SystemClock.elapsedRealtime();
					timer.stop();
					textViewStartTimer.setText("Start");
				}
				else
				{
					diff += (lastStopTime - lastStartTime);
					timer.setBase(SystemClock.elapsedRealtime() - diff);
					timerBase = timer.getBase();
					lastStartTime = SystemClock.elapsedRealtime();
					timer.start();
					textViewStartTimer.setText("Pause");
				}
				timerRunning = !timerRunning;
			}
		};

		timer.setOnClickListener(timerListener);
		textViewStartTimer.setOnClickListener(timerListener);

		timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
		{
			@Override
			public void onChronometerTick(Chronometer chronometer)
			{
				timePlayedEditText.setText(chronometer.getText());
			}
		});

		DataManager dataManager = DataManager.getInstance(getActivity().getApplication());
		games = dataManager.getAllGamesCombined();
		gameEditText.setAdapter(new FilteredGameArrayAdapter(parent, android.R.layout.simple_list_item_1, new ArrayList<>(games), true));
		gameEditText.setThreshold(2);
//		gameEditText.setDropDownHeight();

		if (parent.getGame() != null)
		{
			Game game = parent.getGame();
			gameEditText.setText(game.getName());
			if (game instanceof BoardGame) gameType = Game.GameType.BOARD.getType();
			else if (game instanceof RolePlayingGame) gameType = Game.GameType.RPG.getType();
			else if (game instanceof VideoGame) gameType = Game.GameType.VIDEO.getType();
		}

		final DialogFragment newFragment = new DatePickerFragment();

		final Calendar c = Calendar.getInstance();

		List<String> locations = dataManager.getAllLocations();
		locationEditText.setAdapter(new FilteredGameArrayAdapter(parent, android.R.layout.simple_list_item_1, locations, false));
		locationEditText.setThreshold(1);

		if (date == null || date.equals(""))
		{
			date = formatDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			dateEditText.setText(StringUtilities.dateToString(date));
		}
		dateEditText.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
			}
		});
		dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (v.equals(dateEditText) && hasFocus)
					newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
			}
		});

		gameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!hasFocus)
				{
					for (String game : games)
					{
						if (game.startsWith(gameEditText.getText().toString()))
						{
							String type = game.substring(game.length() - 1);
							if (type.equals("b")) gameType = Game.GameType.BOARD.getType();
							else if (type.equals("r")) gameType = Game.GameType.RPG.getType();
							else if (type.equals("v")) gameType = Game.GameType.VIDEO.getType();
							break;
						}
					}
				}
			}
		});

		timePlayedEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if (s.length() > 0) timer.setTextColor(Color.TRANSPARENT);
				else timer.setTextColor(hintTextColor);
			}

			@Override
			public void afterTextChanged(Editable s)
			{

			}
		});

		addPicturesButton.setVisibility(View.GONE);
		addPicturesButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				MultiImageSelector.create().start(parent, REQUEST_IMAGE);
			}
		});

		if (!timePlayed.equals("") && !timePlayed.contains(":"))
			timePlayedEditText.setText(timePlayed + "");
		if (!date.equals(""))
			dateEditText.setText(StringUtilities.dateToString(date));
		if (location != null && !location.equals(""))
			locationEditText.setText(location);
		if (notes != null && !notes.equals(""))
			notesEditText.setText(notes);

		setColors();
		colorComponents();

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		TempDataManager tempDataManager = TempDataManager.getInstance();
		List<String> gameData = tempDataManager.getTempGamePlayData();
		List<Long> timerData = tempDataManager.getTimer();

		try
		{
			Log.d("GAME", gameData.toString());
			Log.d("TIMER", timerData.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (gameData != null && gameData.size() >= 6)
		{
			if (!TextUtils.isEmpty(gameData.get(0))) gameEditText.setText(gameData.get(0));
			if (!TextUtils.isEmpty(gameData.get(1))) gameType = gameData.get(1);
			if (!TextUtils.isEmpty(gameData.get(2))) timePlayedEditText.setText(gameData.get(2));
			if (!TextUtils.isEmpty(gameData.get(3)))
				dateEditText.setText(StringUtilities.dateToString(gameData.get(3)));
			if (!TextUtils.isEmpty(gameData.get(4))) locationEditText.setText(gameData.get(4));
			if (!TextUtils.isEmpty(gameData.get(5))) notesEditText.setText(gameData.get(5));
		}

		if (timerData != null && timerData.size() >= 4)
		{
			timerBase = timerData.get(0);
			lastStartTime = timerData.get(1);
			lastStopTime = timerData.get(2);
			diff = timerData.get(3);
			timerRunning = lastStartTime > lastStopTime;
		}

		if (timerRunning)
		{
			timer.setTextColor(Color.TRANSPARENT);
			timer.stop();
			timer.setBase(timerBase);
			timer.start();
			textViewStartTimer.setText("Pause");
		}

		else if (timerBase != 0)
		{
			long tempTimerBase = SystemClock.elapsedRealtime() - (diff + (lastStopTime - lastStartTime));
			timer.setBase(tempTimerBase);
			timePlayedEditText.setText(timer.getText());
		}

		addPicturesButton.requestFocus();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		updateData();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		parent.onActivityResult(requestCode, resultCode, data);
	}

	public void setGame(String gameName, String gameType)
	{
		gameEditText.setText(gameName);
		this.gameType = gameType;
		updateData();
	}

	public void setData(int timePlayed, String date, String location, String notes)
	{
		if (timePlayed > 0) this.timePlayed = timePlayed + "";
		this.location = location;
		this.notes = notes;
		AddGamePlayDetailsFragment.date = date;
	}

	public void updateData()
	{
		try
		{
			if (parent != null)
			{
				parent.setData(gameEditText.getText().toString(),
				               gameType,
				               timePlayedEditText.getText().toString(),
				               date,
				               locationEditText.getText().toString(),
				               notesEditText.getText().toString());
			}

			TempDataManager tempDataManager = TempDataManager.getInstance();
			tempDataManager.clearTempGamePlayData();
			tempDataManager.setTempGamePlayData(gameEditText.getText().toString(),
			                                    gameType,
			                                    timePlayedEditText.getText().toString(),
			                                    date,
			                                    locationEditText.getText().toString(),
			                                    notesEditText.getText().toString());
			tempDataManager.saveTempGamePlayData();

			tempDataManager.setTimer(timerBase, lastStartTime, lastStopTime, diff);
			tempDataManager.saveTimer();
		}
		catch (Exception e)
		{

		}
	}

	private void setColors()
	{
		backgroundColor = Preferences.getBackgroundColor(parent);
		foregroundColor = Preferences.getForegroundColor(parent);
		hintTextColor = Preferences.getHintTextColor(parent);
	}

	private void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);
		//gameEditText.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
		gameEditText.setTextColor(foregroundColor);
		gameEditText.setHintTextColor(hintTextColor);
		gameTextView.setBackgroundColor(backgroundColor);
		gameTextView.setTextColor(hintTextColor);

		//timePlayedEditText.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
		timePlayedEditText.setTextColor(foregroundColor);
		timePlayedEditText.setHintTextColor(hintTextColor);
		timePlayedTextView.setBackgroundColor(backgroundColor);
		timePlayedTextView.setTextColor(hintTextColor);

		//locationEditText.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
		locationEditText.setTextColor(foregroundColor);
		locationEditText.setHintTextColor(hintTextColor);
		locationTextView.setBackgroundColor(backgroundColor);
		locationTextView.setTextColor(hintTextColor);

		//notesEditText.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
		notesEditText.setTextColor(foregroundColor);
		notesEditText.setHintTextColor(hintTextColor);
		notesTextView.setBackgroundColor(backgroundColor);
		notesTextView.setTextColor(hintTextColor);

		//dateEditText.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
		dateEditText.setTextColor(foregroundColor);
		dateTextView.setBackgroundColor(backgroundColor);
		dateTextView.setTextColor(hintTextColor);

		textViewStartTimer.setTextColor(hintTextColor);
		textViewStartTimer.setBackgroundColor(backgroundColor);
		timer.setTextColor(hintTextColor);
		timer.setBackgroundColor(backgroundColor);

		addPicturesButton.setBackgroundColor(backgroundColor);
	}

	@Override
	public void onAttach(Context context)
	{
		parent = (AddGamePlayTabbedActivity) context;
		super.onAttach(context);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
//		if (dbHelper != null) dbHelper.close();
		date = "";
	}

	private static String formatDate(int year, int month, int day)
	{
		String date = "";
		date += year;
		date += month < 10 ? 0 + "" + month : month;
		date += day < 10 ? 0 + "" + day : day;
		return date;
	}

	public boolean isTimerRunning()
	{
		return timerBase > 0;
	}

	public static class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener
	{

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			Dialog dateDialog;
			// Use the current date as the default date in the picker
			if (dateEditText.getText()
			                .toString()
			                .equals(""))
			{
				final Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				// Create a new instance of DatePickerDialog and return it
				dateDialog = new DatePickerDialog(getActivity(), this, year, month, day);
			}
			else
			{
				dateDialog = new DatePickerDialog(getActivity(), this,
				                                  Integer.parseInt(date.substring(0, 4)),
				                                  Integer.parseInt(date.substring(4, 6)),
				                                  Integer.parseInt(date.substring(6)));
			}
			return dateDialog;
		}

		@Override
		public void onDismiss(DialogInterface dialog)
		{
			super.onDismiss(dialog);
		}

		@Override
		public void onCancel(DialogInterface dialog)
		{
			super.onCancel(dialog);
			InputMethodManager inputManager = (InputMethodManager)
					dateEditText.getContext()
					            .getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(dateEditText.getWindowToken(),
			                                     InputMethodManager.HIDE_NOT_ALWAYS);
		}

		public void onDateSet(DatePicker view, int year, int month, int day)
		{
			date = formatDate(year, month, day);
			dateEditText.setText(StringUtilities.dateToString(date));

			locationEditText.requestFocus();
			InputMethodManager inputManager = (InputMethodManager)
					view.getContext()
					    .getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInputFromInputMethod(locationEditText.getWindowToken(),
			                                          InputMethodManager.SHOW_IMPLICIT);
		}
	}
}