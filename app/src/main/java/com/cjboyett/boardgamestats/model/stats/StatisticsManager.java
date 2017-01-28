package com.cjboyett.boardgamestats.model.stats;

import android.content.Context;
import android.database.Cursor;

import com.cjboyett.boardgamestats.data.games.GameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.utility.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Casey on 4/21/2016.
 */
public class StatisticsManager {
	private static StatisticsManager instance = null;
	private Context context;

	private int numberOfGames = -1;
	private int gamesTimesPlayed = -1;
	private int numberGamesPlayed = -1;
	private int totalGameTimePlayed = -1;
	private int numberGamePlaysPlayed = -1;
	private int numberPlayers = -1;
	private double averageGamesPerPlayer = -1;

	private List<String> allPlayers;

	private Map<Integer, List<String>> allGamesByTimesPlayed;
	private Map<Integer, List<String>> allGamesByTimePlayed;
	private Map<Integer, List<String>> allGamesByAverageTimePlayedWithType;
	private Map<Integer, List<String>> allPlayersByTimesPlayed;
	private Map<String, Integer> allPlayersWithTimesPlayed;
	private Map<Integer, List<String>> allPlayersByTimePlayed;

	private Map<String, Integer> allWonGames;
	private Map<String, Integer> allLostPlayers;

	private boolean useBoardGamesForStats, useRPGsForStats, useVideoGamesForStats;

	private StatisticsManager(Context context) {
		this.context = context;

		useBoardGamesForStats = Preferences.useBoardGamesForStats(context);
		useRPGsForStats = Preferences.useRPGsForStats(context);
		useVideoGamesForStats = Preferences.useVideoGamesForStats(context);
	}

	public static StatisticsManager getInstance(Context context) {
		if (instance == null) instance = new StatisticsManager(context);
		return instance;
	}

	public void initialize() {
		instance.getNumberOfGames();
		instance.getGamesTimesPlayed();
		instance.getNumberGamesPlayed();
		instance.getGameTimePlayed();
		instance.getNumberGamePlaysPlayed();
		instance.getAverageNumberGamesPerPlayer();
		instance.getAllGamesByTimesPlayed();
		instance.getAllGamesByTimePlayed();
		instance.getAllGamesByAverageTimePlayedWithType();
		instance.getAllPlayersByTimesPlayed();
		instance.getAllPlayersByTimePlayed();
		instance.getNumberWonGames();
		instance.getAllPlayersGamesLost();
	}

	public void reset() {
		useBoardGamesForStats = Preferences.useBoardGamesForStats(context);
		useRPGsForStats = Preferences.useRPGsForStats(context);
		useVideoGamesForStats = Preferences.useVideoGamesForStats(context);

		numberOfGames = -1;
		gamesTimesPlayed = -1;
		numberGamesPlayed = -1;
		totalGameTimePlayed = -1;
		numberGamePlaysPlayed = -1;
		numberPlayers = -1;
		averageGamesPerPlayer = -1;

		allPlayers = null;

		allGamesByTimesPlayed = null;
		allGamesByTimePlayed = null;
		allGamesByAverageTimePlayedWithType = null;
		allPlayersByTimesPlayed = null;
		allPlayersWithTimesPlayed = null;
		allPlayersByTimePlayed = null;

		allWonGames = null;
		allLostPlayers = null;
	}

	public int getGamesTimesPlayed() {
		if (gamesTimesPlayed == -1) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			gamesTimesPlayed = GameStatsDbUtility.timesPlayed(dbHelper,
															  useBoardGamesForStats,
															  useRPGsForStats,
															  useVideoGamesForStats);
			dbHelper.close();
		}
		return gamesTimesPlayed;
	}

	public int getNumberGamesPlayed() {
		if (numberGamesPlayed == -1) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			numberGamesPlayed = GameStatsDbUtility.getNumberGamesPlayed(dbHelper,
																		useBoardGamesForStats,
																		useRPGsForStats,
																		useVideoGamesForStats);
			dbHelper.close();
		}
		return numberGamesPlayed;
	}

	public int getGameTimePlayed() {
		if (totalGameTimePlayed == -1) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			totalGameTimePlayed = GameStatsDbUtility.totalTimePlayed(dbHelper,
																	 useBoardGamesForStats,
																	 useRPGsForStats,
																	 useVideoGamesForStats);
			dbHelper.close();
		}
		return totalGameTimePlayed;
	}

	public int getNumberGamePlaysPlayed() {
		if (numberGamePlaysPlayed == -1) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			numberGamePlaysPlayed = GameStatsDbUtility.timesPlayed(dbHelper,
																   useBoardGamesForStats,
																   useRPGsForStats,
																   useVideoGamesForStats);
			dbHelper.close();
		}
		return numberGamePlaysPlayed;
	}

	// TODO Fix to ignore skew from untracked games, such as RPGs
	public double getAverageNumberGamesPerPlayer() {
		if (averageGamesPerPlayer == -1) {
			Map<String, Integer> playersMap = getAllPlayersWithTimesPlayed();
			if (playersMap != null && playersMap.size() >= 0) {
				double totalTimesPlayed = 0;
				for (Integer i : playersMap.values()) totalTimesPlayed += i;
				averageGamesPerPlayer = totalTimesPlayed / playersMap.size();
			} else averageGamesPerPlayer = 0;
		}
		return averageGamesPerPlayer;
	}

	public Map<Integer, List<String>> getAllGamesByTimesPlayed() {
		if (allGamesByTimesPlayed == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allGamesByTimesPlayed = GameStatsDbUtility.getAllGamesByTimesPlayed(dbHelper,
																				useBoardGamesForStats,
																				useRPGsForStats,
																				useVideoGamesForStats);
			dbHelper.close();
		}
		return allGamesByTimesPlayed;
	}

	public Map<Integer, List<String>> getAllGamesByTimePlayed() {
		if (allGamesByTimePlayed == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allGamesByTimePlayed = GameStatsDbUtility.getAllGamesByTimePlayed(dbHelper,
																			  useBoardGamesForStats,
																			  useRPGsForStats,
																			  useVideoGamesForStats);
			dbHelper.close();
		}
		return allGamesByTimePlayed;
	}

	public Map<Integer, List<String>> getAllGamesByAverageTimePlayedWithType() {
		if (allGamesByAverageTimePlayedWithType == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allGamesByAverageTimePlayedWithType = GameStatsDbUtility.getAllGamesByTimePlayedWithType(dbHelper,
																									 useBoardGamesForStats,
																									 useRPGsForStats,
																									 useVideoGamesForStats);
			dbHelper.close();
		}
		return allGamesByAverageTimePlayedWithType;
	}

	public Map<Integer, List<String>> getAllPlayersByTimesPlayed() {
		if (allPlayersByTimesPlayed == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allPlayersByTimesPlayed = GameStatsDbUtility.getAllPlayersByTimesPlayed(dbHelper,
																					useBoardGamesForStats,
																					useRPGsForStats,
																					useVideoGamesForStats);
			dbHelper.close();
		}
		return allPlayersByTimesPlayed;
	}

	public Map<String, Integer> getAllPlayersWithTimesPlayed() {
		if (allPlayersWithTimesPlayed == null) {
			allPlayersWithTimesPlayed = new HashMap<>();
			for (Integer i : getAllPlayersByTimesPlayed().keySet())
				for (String player : getAllPlayersByTimesPlayed().get(i))
					allPlayersWithTimesPlayed.put(player, i);
		}
		return allPlayersWithTimesPlayed;
	}

	public Map<Integer, List<String>> getAllPlayersByTimePlayed() {
		if (allPlayersByTimePlayed == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allPlayersByTimePlayed = GameStatsDbUtility.getAllPlayersByTimePlayed(dbHelper,
																				  useBoardGamesForStats,
																				  useRPGsForStats,
																				  useVideoGamesForStats);
			dbHelper.close();
		}
		return allPlayersByTimePlayed;
	}

	public Map<String, Integer> getNumberWonGames() {
		if (allWonGames == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allWonGames = GameStatsDbUtility.getAllWonGames(dbHelper,
															useBoardGamesForStats,
															useRPGsForStats,
															useVideoGamesForStats);
			dbHelper.close();
		}
		return allWonGames;
	}

	public Map<String, Integer> getAllPlayersGamesLost() {
		if (allLostPlayers == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allLostPlayers = GameStatsDbUtility.getAllPlayersGamesLost(dbHelper,
																	   useBoardGamesForStats,
																	   useRPGsForStats,
																	   useVideoGamesForStats);
			dbHelper.close();
		}
		return allLostPlayers;
	}

	public int getNumberOfGames() {
		if (numberOfGames == -1) {
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			numberOfGames = GameStatsDbUtility.getNumberOfGamesInCollection(dbHelper,
																			useBoardGamesForStats,
																			useRPGsForStats,
																			useVideoGamesForStats);
			dbHelper.close();
		}
		return numberOfGames;
	}

	public int getNumberPlayers() {
		if (numberPlayers == -1) {
			for (List<String> players : getAllPlayersByTimesPlayed().values()) numberPlayers += players.size();
		}
		return numberPlayers;
	}

	public List<String> getAllPlayers() {
		if (allPlayers == null) {
			Set<String> players = new TreeSet<>();
			GamesDbHelper dbHelper = new GamesDbHelper(context);

			Cursor boardGamePlayers = dbHelper.getReadableDatabase()
											  .query(true,
													 BoardGameContract.PlayerEntry.TABLE_NAME,
													 new String[]{BoardGameContract.PlayerEntry.NAME},
													 null,
													 null,
													 null,
													 null,
													 null,
													 null);
			while (boardGamePlayers.moveToNext()) players.add(boardGamePlayers.getString(0));
			boardGamePlayers.close();

			Cursor rpgPlayers = dbHelper.getReadableDatabase()
										.query(true,
											   RPGContract.PlayerEntry.TABLE_NAME,
											   new String[]{RPGContract.PlayerEntry.NAME},
											   null,
											   null,
											   null,
											   null,
											   null,
											   null);
			while (rpgPlayers.moveToNext()) players.add(rpgPlayers.getString(0));
			rpgPlayers.close();

			Cursor videoGamePlayers = dbHelper.getReadableDatabase()
											  .query(true,
													 VideoGameContract.PlayerEntry.TABLE_NAME,
													 new String[]{VideoGameContract.PlayerEntry.NAME},
													 null,
													 null,
													 null,
													 null,
													 null,
													 null);
			while (videoGamePlayers.moveToNext()) players.add(videoGamePlayers.getString(0));
			videoGamePlayers.close();

			dbHelper.close();

			allPlayers = new ArrayList<>(players);
		}
		return allPlayers;
	}
}
