package com.cjboyett.boardgamestats.conductor.addgameplay;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.ConductorActivity;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.Timer;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.view.adapter.FilteredGameArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;

public class AddGamePlayDetailsController extends BaseController implements AddGamePlayDetailsView {
	@BindView(R.id.textview_game)
	TextView gameTextView;

	@BindView(R.id.edittext_game)
	AutoCompleteTextView gameEditText;

	@BindView(R.id.textview_time_played)
	TextView timePlayedTextView;

	@BindView(R.id.edittext_time_played)
	EditText timePlayedEditText;

	@BindView(R.id.textview_start_timer)
	TextView textViewStartTimer;

	@BindView(R.id.timer)
	Chronometer chronometer;

	@BindView(R.id.textview_date)
	TextView dateTextView;

	@BindView(R.id.edittext_date)
	EditText dateEditText;

	@BindView(R.id.textview_location)
	TextView locationTextView;

	@BindView(R.id.edittext_location)
	AutoCompleteTextView locationEditText;

	@BindView(R.id.textview_notes)
	TextView notesTextView;

	@BindView(R.id.edittext_gameplay_notes)
	EditText notesEditText;

	private AddGamePlayTabbedController parent;

	private GamePlayDetails gamePlayDetails;
	private boolean timerRunning;
	private Timer timer;

	private List<String> games;

	private String timePlayed = "", location = "", notes = "", gameType = "";
	private static String date;

	private View view;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.fragment_add_game_play_details, container, false);
		ButterKnife.bind(this, view);
		gamePlayDetails = new GamePlayDetails();
		timer = new Timer();
		gamePlayDetails.setTimer(timer);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View v) {
		super.onAttach(v);

		chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
			@Override
			public void onChronometerTick(Chronometer chronometer) {
				timePlayedEditText.setText(chronometer.getText());
			}
		});

		DataManager dataManager = DataManager.getInstance(getActivity().getApplication());
		games = dataManager.getAllGamesCombined();
		gameEditText.setAdapter(new FilteredGameArrayAdapter(parent.getActivity(),
															 android.R.layout.simple_list_item_1,
															 new ArrayList<>(games),
															 true));
		gameEditText.setThreshold(2);

		if (parent.getGame() != null) {
			Game game = parent.getGame();
			gameEditText.setText(game.getName());
			if (game instanceof BoardGame) gameType = Game.GameType.BOARD.getType();
			else if (game instanceof RolePlayingGame) gameType = Game.GameType.RPG.getType();
			else if (game instanceof VideoGame) gameType = Game.GameType.VIDEO.getType();
		}

		final Calendar c = Calendar.getInstance();

		List<String> locations = dataManager.getAllLocations();
		locationEditText.setAdapter(new FilteredGameArrayAdapter(parent.getActivity(),
																 android.R.layout.simple_list_item_1,
																 locations,
																 false));
		locationEditText.setThreshold(2);

		if (date == null || date.equals("")) {
			date = formatDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			dateEditText.setText(StringUtilities.dateToString(date));
		}

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
	}

	@Override
	protected void onChangeStarted(@NonNull ControllerChangeHandler changeHandler,
								   @NonNull ControllerChangeType changeType) {
		super.onChangeStarted(changeHandler, changeType);
		Timber.d("Changing!");
	}

	@Override
	public void setGame(String gameName, String gameType) {
		gameEditText.setText(gameName);
		this.gameType = gameType;
		updateData();
	}

	@Override
	public void saveGamePlayDetails(String gameName, Timer timer, Date date, String location, String notes) {

	}

	@Override
	public void loadGamePlayDetails() {

	}

	@Override
	public void setData(int timePlayed, String date, String location, String notes) {
		if (timePlayed > 0) this.timePlayed = timePlayed + "";
		this.location = location;
		this.notes = notes;
		AddGamePlayDetailsController.date = date;
	}

	@OnFocusChange(R.id.edittext_game)
	protected void onGameFocus(boolean hasFocus) {
		if (!hasFocus) {
			for (String game : games) {
				if (game.startsWith(gameEditText.getText().toString())) {
					String type = game.substring(game.length() - 1);
					switch (type) {
						case "b":
							gameType = Game.GameType.BOARD.getType();
							break;
						case "r":
							gameType = Game.GameType.RPG.getType();
							break;
						case "v":
							gameType = Game.GameType.VIDEO.getType();
							break;
					}
					break;
				}
			}
		}
	}

	@OnTextChanged(R.id.edittext_time_played)
	protected void onTimeTextChanged(CharSequence s) {
		if (s.length() > 0) chronometer.setTextColor(Color.TRANSPARENT);
		else chronometer.setTextColor(hintTextColor);
	}

	@OnClick({R.id.timer, R.id.textview_start_timer})
	protected void onTimerClicked() {
		chronometer.setTextColor(Color.TRANSPARENT);

		if (timerRunning) {
			timer.setLastStopTime(SystemClock.elapsedRealtime());
			chronometer.stop();
			textViewStartTimer.setText("Start");
		} else {
			timer.adjustDiffFromStopAndStart();
			chronometer.setBase(SystemClock.elapsedRealtime() - timer.getDiff());
			timer.setTimerBase(chronometer.getBase());
			timer.setLastStartTime(SystemClock.elapsedRealtime());
			chronometer.start();
			textViewStartTimer.setText("Pause");
		}
		timerRunning = !timerRunning;
	}

	@OnClick(R.id.edittext_date)
	protected void onDateClicked() {
		final DialogFragment newFragment = new DatePickerFragment(dateEditText, locationEditText);
		newFragment.show(((ConductorActivity) getActivity()).getSupportFragmentManager(), "datePicker");
	}

	@OnEditorAction(R.id.edittext_time_played)
	protected boolean onTimePlayedAction(int actionId) {
		boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_NEXT) {
			onDateClicked();
			handled = true;
		}
		return handled;
	}

	public void updateData() {
		try {
			if (parent != null) {
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

			tempDataManager.setTimer(timer);
			tempDataManager.saveTimer();
		} catch (Exception e) {
			Timber.e(e);
		}
	}

	@Override
	public void setGamePlayDetails(GamePlayDetails gamePlayDetails) {
		this.gamePlayDetails = gamePlayDetails;
		if (timer != null) {
			timerRunning = timer.isTimerRunning();
		}

		if (timerRunning) {
			chronometer.setTextColor(Color.TRANSPARENT);
			chronometer.stop();
			chronometer.setBase(timer.getTimerBase());
			chronometer.start();
			textViewStartTimer.setText("Pause");
		} else if (timer.getTimerBase() != 0) {
			long tempTimerBase = SystemClock.elapsedRealtime() -
					(timer.getDiff() + (timer.getLastStopTime() - timer.getLastStartTime()));
			chronometer.setBase(tempTimerBase);
			timePlayedEditText.setText(chronometer.getText());
		}
	}

	public GamePlayDetails getGamePlayDetails() {
		gamePlayDetails.setGameName(gameEditText.getText().toString());
		gamePlayDetails.setGameType(gameType);
		gamePlayDetails.setTimePlayed(timePlayedEditText.getText().toString());
		gamePlayDetails.setDate(date);
		gamePlayDetails.setLocation(locationEditText.getText().toString());
		gamePlayDetails.setNotes(notesEditText.getText().toString());
		return gamePlayDetails;
	}

	private void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		gameEditText.setTextColor(foregroundColor);
		gameEditText.setHintTextColor(hintTextColor);
		gameTextView.setBackgroundColor(backgroundColor);
		gameTextView.setTextColor(hintTextColor);

		timePlayedEditText.setTextColor(foregroundColor);
		timePlayedEditText.setHintTextColor(hintTextColor);
		timePlayedTextView.setBackgroundColor(backgroundColor);
		timePlayedTextView.setTextColor(hintTextColor);

		locationEditText.setTextColor(foregroundColor);
		locationEditText.setHintTextColor(hintTextColor);
		locationTextView.setBackgroundColor(backgroundColor);
		locationTextView.setTextColor(hintTextColor);

		notesEditText.setTextColor(foregroundColor);
		notesEditText.setHintTextColor(hintTextColor);
		notesTextView.setBackgroundColor(backgroundColor);
		notesTextView.setTextColor(hintTextColor);

		dateEditText.setTextColor(foregroundColor);
		dateTextView.setBackgroundColor(backgroundColor);
		dateTextView.setTextColor(hintTextColor);

		textViewStartTimer.setTextColor(hintTextColor);

		textViewStartTimer.setBackgroundColor(backgroundColor);
		chronometer.setTextColor(hintTextColor);
		chronometer.setBackgroundColor(backgroundColor);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		date = "";
	}

	public void setParent(AddGamePlayTabbedController parent) {
		this.parent = parent;
	}

	private static String formatDate(int year, int month, int day) {
		String date = "";
		date += year;
		date += month < 10 ? 0 + "" + month : month;
		date += day < 10 ? 0 + "" + day : day;
		return date;
	}

	public boolean isTimerRunning() {
		return timer.getTimerBase() > 0;
	}

	@SuppressLint("ValidFragment")
	public static class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		private EditText dateEditText;
		private EditText locationEditText;

		public DatePickerFragment(EditText dateEditText, EditText locationEditText) {
			super();
			this.dateEditText = dateEditText;
			this.locationEditText = locationEditText;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dateDialog;
			// Use the current date as the default date in the picker
			if (dateEditText.getText()
							.toString()
							.equals("")) {
				final Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);

				// Create a new instance of DatePickerDialog and return it
				dateDialog = new DatePickerDialog(getActivity(), this, year, month, day);
			} else {
				dateDialog = new DatePickerDialog(getActivity(), this,
												  Integer.parseInt(date.substring(0, 4)),
												  Integer.parseInt(date.substring(4, 6)),
												  Integer.parseInt(date.substring(6)));
			}
			return dateDialog;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			super.onDismiss(dialog);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
			InputMethodManager inputManager = (InputMethodManager)
					dateEditText.getContext()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(dateEditText.getWindowToken(),
												 InputMethodManager.HIDE_NOT_ALWAYS);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
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