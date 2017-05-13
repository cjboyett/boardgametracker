package com.cjboyett.boardgamestats.data.games;

import android.content.ContentValues;
import android.database.Cursor;

import com.cjboyett.boardgamestats.data.TempContract;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import timber.log.Timber;

/**
 * Created by Casey on 4/13/2016.
 */
public class GamesDbUtility {
	public static List<String> getAllGamesSortedWithType(GamesDbHelper dbHelper) {
		List<String> boardGames = BoardGameDbUtility.getAllGames(dbHelper);
		List<String> rpgs = RPGDbUtility.getAllGames(dbHelper);
		List<String> videoGames = VideoGameDbUtility.getAllGames(dbHelper);
		List<String> gamesList = StringUtilities.combinesLists(boardGames, rpgs, videoGames);

		return gamesList;
	}

	public static List<String> getAllPlayedGamesSortedWithType(GamesDbHelper dbHelper) {
		List<String> boardGames = BoardGameDbUtility.getAllPlayedGames(dbHelper);
		List<String> rpgs = RPGDbUtility.getAllPlayedGames(dbHelper);
		List<String> videoGames = VideoGameDbUtility.getAllPlayedGames(dbHelper);
		List<String> gamesList = StringUtilities.combinesLists(boardGames, rpgs, videoGames);

		return gamesList;
	}

	public static List<String> getAllLocationsSorted(GamesDbHelper dbHelper) {
		List<String> boardGameLocations = BoardGameDbUtility.getAllLocations(dbHelper);
		List<String> rpgLocations = RPGDbUtility.getAllLocations(dbHelper);
		List<String> videoGameLocations = VideoGameDbUtility.getAllLocations(dbHelper);

		Set<String> locationSet = new TreeSet<>();
		locationSet.addAll(boardGameLocations);
		locationSet.addAll(rpgLocations);
		locationSet.addAll(videoGameLocations);
		List<String> locations = new ArrayList<>(locationSet);
		StringUtilities.sortList(locations);

		return locations;
	}

	public static List<String> getAllPlayersSorted(GamesDbHelper dbHelper) {
		List<String> boardGamePlayers = BoardGameDbUtility.getAllPlayers(dbHelper);
		List<String> rpgPlayers = RPGDbUtility.getAllPlayers(dbHelper);
		List<String> videoGamePlayers = VideoGameDbUtility.getAllPlayers(dbHelper);

		Set<String> playerSet = new TreeSet<>();
		playerSet.addAll(boardGamePlayers);
		playerSet.addAll(rpgPlayers);
		playerSet.addAll(videoGamePlayers);
		List<String> players = new ArrayList<>(playerSet);
		StringUtilities.sortList(players);

		return players;
	}

	public static List<String> getAllPlayersSorted(GamesDbHelper dbHelper, Date date) {
		List<String> boardGamePlayers = BoardGameDbUtility.getAllPlayers(dbHelper, date);
		List<String> rpgPlayers = RPGDbUtility.getAllPlayers(dbHelper, date);
		List<String> videoGamePlayers = VideoGameDbUtility.getAllPlayers(dbHelper, date);

		Set<String> playerSet = new TreeSet<>();
		playerSet.addAll(boardGamePlayers);
		playerSet.addAll(rpgPlayers);
		playerSet.addAll(videoGamePlayers);
		List<String> players = new ArrayList<>(playerSet);
		StringUtilities.sortList(players);

		return players;
	}

	// Temp data utilities

	public static void clearTempTables(GamesDbHelper dbHelper) {
		clearTempGamePlayTable(dbHelper);
		clearTempPlayersTable(dbHelper);
	}

	public static void clearTempGamePlayTable(GamesDbHelper dbHelper) {
		Timber.d("Play");
		dbHelper.getWritableDatabase().execSQL("DELETE FROM " + TempContract.GamePlayEntry.TABLE_NAME + ";");
	}

	public static void clearTempPlayersTable(GamesDbHelper dbHelper) {
		Timber.d("Players");
		dbHelper.getWritableDatabase().execSQL("DELETE FROM " + TempContract.PlayerEntry.TABLE_NAME + ";");
	}

	public static void addTempGamePlay(GamesDbHelper dbHelper, String gameName, String gameType, String timePlayed,
									   String date,
									   String location, String notes) {
		Timber.d("Play");
		ContentValues values = new ContentValues();
		values.put(TempContract.GamePlayEntry.GAME, gameName != null ? gameName : "");
		values.put(TempContract.GamePlayEntry.GAME_TYPE, gameType != null ? gameType : "");
		values.put(TempContract.GamePlayEntry.TIME_PLAYED, timePlayed != null ? timePlayed : "");
		values.put(TempContract.GamePlayEntry.DATE, date != null ? date : "");
		values.put(TempContract.GamePlayEntry.LOCATION, location != null ? location : "");
		values.put(TempContract.GamePlayEntry.NOTES, notes != null ? notes : "");
		dbHelper.getWritableDatabase().insertOrThrow(TempContract.GamePlayEntry.TABLE_NAME, null, values);
	}

	public static List<String> getTempGamePlay(GamesDbHelper dbHelper) {
		List<String> gamePlayData = new ArrayList<>();

		Cursor gameCursor = dbHelper.getReadableDatabase().query(TempContract.GamePlayEntry.TABLE_NAME,
																 new String[]{TempContract.GamePlayEntry.GAME,
																			  TempContract.GamePlayEntry.GAME_TYPE,
																			  TempContract.GamePlayEntry.TIME_PLAYED,
																			  TempContract.GamePlayEntry.DATE,
																			  TempContract.GamePlayEntry.LOCATION,
																			  TempContract.GamePlayEntry.NOTES},
																 null,
																 null,
																 null,
																 null,
																 null);

		if (gameCursor.moveToFirst()) {
			for (int i = 0; i < gameCursor.getColumnCount(); i++)
				gamePlayData.add(gameCursor.getString(i));
		}
		gameCursor.close();

		return gamePlayData;
	}

	public static void setTempTimer(GamesDbHelper dbHelper, long timerStart, long lastTimerStart, long lastTimerStop,
									long diff) {
		ContentValues values = new ContentValues();
		values.put(TempContract.GamePlayEntry.TIMER_START, timerStart);
		values.put(TempContract.GamePlayEntry.LAST_TIMER_START, lastTimerStart);
		values.put(TempContract.GamePlayEntry.LAST_TIMER_STOP, lastTimerStop);
		values.put(TempContract.GamePlayEntry.TIMER_DIFF, diff);
		dbHelper.getWritableDatabase().update(TempContract.GamePlayEntry.TABLE_NAME, values, null, null);
	}

	public static List<Long> getTempTimer(GamesDbHelper dbHelper) {
		List<Long> gamePlayData = new ArrayList<>();

		Cursor gameCursor = dbHelper.getReadableDatabase().query(TempContract.GamePlayEntry.TABLE_NAME,
																 new String[]{TempContract.GamePlayEntry.TIMER_START,
																			  TempContract.GamePlayEntry.LAST_TIMER_START,
																			  TempContract.GamePlayEntry.LAST_TIMER_STOP,
																			  TempContract.GamePlayEntry.TIMER_DIFF},
																 null,
																 null,
																 null,
																 null,
																 null);

		if (gameCursor.moveToFirst()) {
			Timber.d(gameCursor.getColumnCount() + "");
			for (int i = 0; i < gameCursor.getColumnCount(); i++) {
				gamePlayData.add(gameCursor.getLong(i));
				Timber.d(i + " " + gameCursor.getLong(i));
			}
		}
		gameCursor.close();

		return gamePlayData;
	}

	public static void addTempPlayer(GamesDbHelper dbHelper, GamePlayerData gamePlayerData) {
		addTempPlayer(dbHelper, gamePlayerData.getPlayerName(), gamePlayerData.getScore(), gamePlayerData.isWin());
	}

	public static void addTempPlayer(GamesDbHelper dbHelper, String name, double score, boolean win) {
		Timber.d("Player");
		ContentValues values = new ContentValues();
		if (name != null) values.put(TempContract.PlayerEntry.NAME, name);
		else values.put(TempContract.PlayerEntry.NAME, "");
		values.put(TempContract.PlayerEntry.SCORE, score);
		values.put(TempContract.PlayerEntry.WIN, win ? "y" : "n");

		dbHelper.getWritableDatabase().insertOrThrow(TempContract.PlayerEntry.TABLE_NAME, null, values);
	}

	public static List<GamePlayerData> getTempPlayers(GamesDbHelper dbHelper) {
		List<GamePlayerData> gamePlayerDataList = new ArrayList<>();

		Cursor playerCursor = dbHelper.getReadableDatabase().query(TempContract.PlayerEntry.TABLE_NAME,
																   new String[]{TempContract.PlayerEntry.NAME,
																				TempContract.PlayerEntry.SCORE,
																				TempContract.PlayerEntry.WIN},
																   null,
																   null,
																   null,
																   null,
																   null);

		while (playerCursor.moveToNext()) {
			gamePlayerDataList.add(new GamePlayerData(playerCursor.getString(0),
													  playerCursor.getDouble(1),
													  playerCursor.getString(2).equals("y")));
		}
		playerCursor.close();

		return gamePlayerDataList;
	}
}
