package com.cjboyett.boardgamestats.data;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;

import java.util.List;

/**
 * Created by Casey on 5/2/2016.
 */
public class TempDataManager
{
	private static TempDataManager instance = null;
	private Context context;

	private List<String> tempGamePlayData;
	private List<Long> timer;
	private List<GamePlayerData> tempPlayers;

	private TempDataManager(Context context)
	{
		this.context = context;
	}

	public static TempDataManager getInstance(Context context)
	{
		if (instance == null) instance = new TempDataManager(context);
		return instance;
	}

	public static TempDataManager getInstance()
	{
		return instance;
	}

	public void initialize()
	{
		Log.d("TEMPDATA", "Initializing");
		clearTempGamePlayData();
		clearTempPlayers();
		tempGamePlayData = null;
		timer = null;
		tempPlayers = null;
	}

	public List<String> getTempGamePlayData()
	{
		if (tempGamePlayData == null) loadTempGamePlayData();
		return tempGamePlayData;
	}

	public void setTempGamePlayData(String gameName, String gameType, String timePlayed, String date, String location, String notes)
	{
		getTempGamePlayData().clear();
		getTempGamePlayData().add(gameName);
		getTempGamePlayData().add(gameType);
		getTempGamePlayData().add(timePlayed);
		getTempGamePlayData().add(date);
		getTempGamePlayData().add(location);
		getTempGamePlayData().add(notes);
		Log.d("TEMPDATA", "Setting " + getTempGamePlayData().toString());
	}

	public void loadTempGamePlayData()
	{
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		tempGamePlayData = GamesDbUtility.getTempGamePlay(dbHelper);
		dbHelper.close();
		Log.d("TEMPDATA", "Loading temp game: " + tempGamePlayData.toString());
	}

	public void saveTempGamePlayData()
	{
		Log.d("TEMPDATA", "Saving temp game: " + getTempGamePlayData().toString());
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

	public void clearTempGamePlayData()
	{
		Log.d("TEMPDATA", "Clearing temp game");
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		GamesDbUtility.clearTempGamePlayTable(dbHelper);
		dbHelper.close();
	}

	public void setTimer(long timerBase, long lastStartTime, long lastStopTime, long diff)
	{
		getTimer().clear();
		getTimer().add(timerBase);
		getTimer().add(lastStartTime);
		getTimer().add(lastStopTime);
		getTimer().add(diff);
		Log.d("TEMPDATA", "Setting timer: " + SystemClock.elapsedRealtime() + " " + getTimer().toString());
	}

	public List<Long> getTimer()
	{
		if (timer == null) loadTimer();
		return timer;
	}

	public void loadTimer()
	{
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		timer = GamesDbUtility.getTempTimer(dbHelper);
		dbHelper.close();
		Log.d("TEMPDATA", "Loading timer: " + timer.toString());
	}

	public void saveTimer()
	{
		Log.d("TEMPDATA", "Saving timer: " + getTimer().toString());
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		GamesDbUtility.setTempTimer(dbHelper,
		                            getTimer().get(0),
		                            getTimer().get(1),
		                            getTimer().get(2),
		                            getTimer().get(3));
		dbHelper.close();
	}

	public void addTempPlayer(GamePlayerData gamePlayerData)
	{
		Log.d("TEMPDATA", "Adding " + gamePlayerData.toString());
		getTempPlayers().add(gamePlayerData);
	}

	public List<GamePlayerData> getTempPlayers()
	{
		if (tempPlayers == null) loadTempPlayers();
		return tempPlayers;
	}

	public void loadTempPlayers()
	{
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		tempPlayers = GamesDbUtility.getTempPlayers(dbHelper);
		dbHelper.close();
		Log.d("TEMPDATA", "Loading players: " + tempPlayers.toString());
	}

	public void saveTempPlayers()
	{
		Log.d("TEMPDATA", "Saving players: " + getTempPlayers().toString());
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		for (GamePlayerData gamePlayerData : getTempPlayers()) GamesDbUtility.addTempPlayer(dbHelper, gamePlayerData);
		dbHelper.close();
	}

	public void clearTempPlayers()
	{
		Log.d("TEMPDATA", "Clearing players");
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		GamesDbUtility.clearTempPlayersTable(dbHelper);
		dbHelper.close();
	}

}
