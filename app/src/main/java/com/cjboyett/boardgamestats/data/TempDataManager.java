package com.cjboyett.boardgamestats.data;

import android.content.Context;

import com.cjboyett.boardgamestats.conductor.addgameplay.GamePlayDetails;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.model.Timer;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;

import java.util.List;

import timber.log.Timber;

public class TempDataManager {
	private static TempDataManager instance = null;
	private Context context;

	private List<String> tempGamePlayData;
	//	private List<Long> timer;
	private Timer timer;
	private List<GamePlayerData> tempPlayers;

	private TempDataManager(Context context) {
		this.context = context;
	}

	public static TempDataManager getInstance(Context context) {
		if (instance == null) instance = new TempDataManager(context);
		return instance;
	}

	public static TempDataManager getInstance() {
		return instance;
	}

	public void initialize() {
		clearTempGamePlayData();
		clearTempPlayers();
		tempGamePlayData = null;
		timer = null;
		tempPlayers = null;
	}

	public List<String> getTempGamePlayData() {
		if (tempGamePlayData == null) loadTempGamePlayData();
		return tempGamePlayData;
	}

	public void setTempGamePlayData(GamePlayDetails gamePlayDetails) {
		setTempGamePlayData(gamePlayDetails.getGameName(),
							gamePlayDetails.getGameType(),
							gamePlayDetails.getTimePlayed(),
							gamePlayDetails.getDate(),
							gamePlayDetails.getLocation(),
							gamePlayDetails.getNotes());
	}

	public void setTempGamePlayData(String gameName, String gameType, String timePlayed, String date, String location,
									String notes) {
		getTempGamePlayData().clear();
		getTempGamePlayData().add(gameName);
		getTempGamePlayData().add(gameType);
		getTempGamePlayData().add(timePlayed);
		getTempGamePlayData().add(date);
		getTempGamePlayData().add(location);
		getTempGamePlayData().add(notes);
		Timber.d("Setting " + getTempGamePlayData().toString());
	}

	public void loadTempGamePlayData() {
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		tempGamePlayData = GamesDbUtility.getTempGamePlay(dbHelper);
		dbHelper.close();
		Timber.d("Loading temp game: " + tempGamePlayData.toString());
	}

	public void saveTempGamePlayData() {
		Timber.d("Saving temp game: " + getTempGamePlayData().toString());
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		if (getTempGamePlayData().size() >= 6)
			GamesDbUtility.addTempGamePlay(dbHelper,
										   getTempGamePlayData().get(0),
										   getTempGamePlayData().get(1),
										   getTempGamePlayData().get(2),
										   getTempGamePlayData().get(3),
										   getTempGamePlayData().get(4),
										   getTempGamePlayData().get(5));
		dbHelper.close();
	}

	public void clearTempGamePlayData() {
		Timber.d("Clearing temp game");
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		GamesDbUtility.clearTempGamePlayTable(dbHelper);
		dbHelper.close();
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

/*	public void setTimer(long timerBase, long lastStartTime, long lastStopTime, long diff) {
		getTimer().clear();
		getTimer().add(timerBase);
		getTimer().add(lastStartTime);
		getTimer().add(lastStopTime);
		getTimer().add(diff);
		Timber.d("Setting timer: " + SystemClock.elapsedRealtime() + " " + getTimer().toString());
	}*/

	public Timer getTimer() {
		if (timer == null) loadTimer();
		return timer;
	}

	public void loadTimer() {
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		timer = new Timer(GamesDbUtility.getTempTimer(dbHelper));
		dbHelper.close();
		Timber.d("Loading timer: " + timer.toString());
	}

	public void saveTimer() {
		Timber.d("Saving timer: " + getTimer().toString());
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		GamesDbUtility.setTempTimer(dbHelper,
									getTimer().getTimerBase(),
									getTimer().getLastStartTime(),
									getTimer().getLastStopTime(),
									getTimer().getDiff());
		dbHelper.close();
	}

	public void addTempPlayer(GamePlayerData gamePlayerData) {
		Timber.d("Adding " + gamePlayerData.toString());
		getTempPlayers().add(gamePlayerData);
	}

	public List<GamePlayerData> getTempPlayers() {
		if (tempPlayers == null) loadTempPlayers();
		return tempPlayers;
	}

	public void loadTempPlayers() {
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		tempPlayers = GamesDbUtility.getTempPlayers(dbHelper);
		dbHelper.close();
		Timber.d("Loading players: " + tempPlayers.toString());
	}

	public void saveTempPlayers() {
		Timber.d("Saving players: " + getTempPlayers().toString());
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		for (GamePlayerData gamePlayerData : getTempPlayers()) GamesDbUtility.addTempPlayer(dbHelper, gamePlayerData);
		dbHelper.close();
	}

	public void clearTempPlayers() {
		Timber.d("Clearing players");
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		GamesDbUtility.clearTempPlayersTable(dbHelper);
		dbHelper.close();
	}

}
