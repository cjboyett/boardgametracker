package com.cjboyett.boardgamestats.data.games;

import android.util.Log;

import com.cjboyett.boardgamestats.data.games.board.BoardGameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameStatsDbUtility;
import com.cjboyett.boardgamestats.model.games.GamePlayData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Casey on 4/13/2016.
 */
public class GameStatsDbUtility {
	public static int totalTimePlayed(GamesDbHelper dbHelper, boolean useBoardGamesForStats, boolean useRPGsForStats,
									  boolean useVideoGamesForStats) {
		int timePlayed = 0;
		if (useBoardGamesForStats) timePlayed += BoardGameStatsDbUtility.totalTimePlayed(dbHelper);
		if (useRPGsForStats) timePlayed += RPGStatsDbUtility.totalTimePlayed(dbHelper);
		if (useVideoGamesForStats) timePlayed += VideoGameStatsDbUtility.totalTimePlayed(dbHelper);
		return timePlayed;
	}

	public static int totalTimePlayedWithPlayer(GamesDbHelper dbHelper, String playerName,
												boolean useBoardGamesForStats, boolean useRPGsForStats,
												boolean useVideoGamesForStats) {
		int timePlayed = 0;
		if (useBoardGamesForStats)
			timePlayed += BoardGameStatsDbUtility.totalTimePlayedWithPlayer(dbHelper, playerName);
		if (useRPGsForStats) timePlayed += RPGStatsDbUtility.totalTimePlayedWithPlayer(dbHelper, playerName);
		if (useVideoGamesForStats)
			timePlayed += VideoGameStatsDbUtility.totalTimePlayedWithPlayer(dbHelper, playerName);
		return timePlayed;
	}

	public static int timesPlayed(GamesDbHelper dbHelper, boolean useBoardGamesForStats, boolean useRPGsForStats,
								  boolean useVideoGamesForStats) {
		int timesPlayed = 0;
		if (useBoardGamesForStats) timesPlayed += BoardGameStatsDbUtility.timesPlayed(dbHelper);
		if (useRPGsForStats) timesPlayed += RPGStatsDbUtility.timesPlayed(dbHelper);
		if (useVideoGamesForStats) timesPlayed += VideoGameStatsDbUtility.timesPlayed(dbHelper);
		return timesPlayed;
	}

	public static int timesPlayedWithPlayer(GamesDbHelper dbHelper, String playerName, boolean useBoardGamesForStats,
											boolean useRPGsForStats, boolean useVideoGamesForStats) {
		int timesPlayed = 0;
		if (useBoardGamesForStats) timesPlayed += BoardGameStatsDbUtility.timesPlayedWithPlayer(dbHelper, playerName);
		if (useRPGsForStats) timesPlayed += RPGStatsDbUtility.timesPlayedWithPlayer(dbHelper, playerName);
		if (useVideoGamesForStats) timesPlayed += VideoGameStatsDbUtility.timesPlayedWithPlayer(dbHelper, playerName);
		return timesPlayed;
	}

	public static int getNumberGamesPlayed(GamesDbHelper dbHelper, boolean useBoardGamesForStats,
										   boolean useRPGsForStats, boolean useVideoGamesForStats) {
		int timesPlayed = 0;
		if (useBoardGamesForStats) timesPlayed += BoardGameStatsDbUtility.getNumberGamesPlayed(dbHelper);
		if (useRPGsForStats) timesPlayed += RPGStatsDbUtility.getNumberGamesPlayed(dbHelper);
		if (useVideoGamesForStats) timesPlayed += VideoGameStatsDbUtility.getNumberGamesPlayed(dbHelper);
		return timesPlayed;
	}

	public static List<GamePlayData> getGamePlaysWithPlayer(GamesDbHelper dbHelper, String playerName,
															boolean useBoardGamesForStats, boolean useRPGsForStats,
															boolean useVideoGamesForStats) {
		List<GamePlayData> gamePlayDataList = new ArrayList<>();

		if (useBoardGamesForStats) {
			List<GamePlayData> boardGamePlayDataList =
					BoardGameStatsDbUtility.getGamePlaysWithPlayer(dbHelper, playerName);
			gamePlayDataList.addAll(boardGamePlayDataList);
		}

		if (useRPGsForStats) {
			List<GamePlayData> rpgPlayDataList = RPGStatsDbUtility.getGamePlaysWithPlayer(dbHelper, playerName);
			gamePlayDataList.addAll(rpgPlayDataList);
		}

		if (useVideoGamesForStats) {
			List<GamePlayData> videoGamePlayDataList =
					VideoGameStatsDbUtility.getGamePlaysWithPlayer(dbHelper, playerName);
			gamePlayDataList.addAll(videoGamePlayDataList);
		}

		Collections.sort(gamePlayDataList, new Comparator<GamePlayData>() {
			@Override
			public int compare(GamePlayData lhs, GamePlayData rhs) {
				return -lhs.getDate().rawDate().compareTo(rhs.getDate().rawDate());
			}
		});
		return gamePlayDataList;
	}

	public static Map<Integer, List<String>> getAllGamesByTimesPlayed(GamesDbHelper dbHelper,
																	  boolean useBoardGamesForStats,
																	  boolean useRPGsForStats,
																	  boolean useVideoGamesForStats) {
		Log.d("Getting games",
			  "BG " + useBoardGamesForStats + ", RPG " + useRPGsForStats + ", VG " + useVideoGamesForStats);
		Map<Integer, List<String>> allGames = new TreeMap<>();

		if (useBoardGamesForStats) {
			Map<Integer, List<String>> boardGames = BoardGameStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

			for (Integer i : boardGames.keySet()) {
				if (allGames.containsKey(i))
					allGames.get(i)
							.addAll(boardGames.get(i));
				else
					allGames.put(i, boardGames.get(i));
			}
		}

		if (useRPGsForStats) {
			Map<Integer, List<String>> rpgs = RPGStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

			for (Integer i : rpgs.keySet()) {
				if (allGames.containsKey(i))
					allGames.get(i)
							.addAll(rpgs.get(i));
				else
					allGames.put(i, rpgs.get(i));
			}
		}

		if (useVideoGamesForStats) {
			Map<Integer, List<String>> videoGames = VideoGameStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

			for (Integer i : videoGames.keySet()) {
				if (allGames.containsKey(i))
					allGames.get(i)
							.addAll(videoGames.get(i));
				else
					allGames.put(i, videoGames.get(i));
			}
		}

		return allGames;
	}

	public static Map<Integer, List<String>> getAllGamesByTimePlayed(GamesDbHelper dbHelper,
																	 boolean useBoardGamesForStats,
																	 boolean useRPGsForStats,
																	 boolean useVideoGamesForStats) {
		Map<Integer, List<String>> allGames = new TreeMap<>();

		if (useBoardGamesForStats) {
			Map<Integer, List<String>> boardGames = BoardGameStatsDbUtility.getAllGamesByTimePlayed(dbHelper);

			for (Integer i : boardGames.keySet()) {
				if (allGames.containsKey(i))
					allGames.get(i)
							.addAll(boardGames.get(i));
				else
					allGames.put(i, boardGames.get(i));
			}
		}

		if (useRPGsForStats) {
			Map<Integer, List<String>> rpgs = RPGStatsDbUtility.getAllGamesByTimePlayed(dbHelper);

			for (Integer i : rpgs.keySet()) {
				if (allGames.containsKey(i))
					allGames.get(i)
							.addAll(rpgs.get(i));
				else
					allGames.put(i, rpgs.get(i));
			}
		}

		if (useVideoGamesForStats) {
			Map<Integer, List<String>> videoGames = VideoGameStatsDbUtility.getAllGamesByTimePlayed(dbHelper);

			for (Integer i : videoGames.keySet()) {
				if (allGames.containsKey(i))
					allGames.get(i)
							.addAll(videoGames.get(i));
				else
					allGames.put(i, videoGames.get(i));
			}
		}

		return allGames;
	}

	public static Map<Integer, List<String>> getAllGamesByTimePlayedWithType(GamesDbHelper dbHelper,
																			 boolean useBoardGamesForStats,
																			 boolean useRPGsForStats,
																			 boolean useVideoGamesForStats) {
		Map<Integer, List<String>> allGames = new TreeMap<>();

		Map<String, Integer> allGamesByTimesPlayed = new HashMap<>();

		if (useBoardGamesForStats) {
			Map<Integer, List<String>> boardGamesTimePlayed = BoardGameStatsDbUtility.getAllGamesByTimePlayed(dbHelper);
			Map<Integer, List<String>> boardGamesTimesPlayed =
					BoardGameStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

			for (Integer i : boardGamesTimePlayed.keySet()) {
				List<String> games = boardGamesTimePlayed.get(i);
				for (int j = 0; j < games.size(); j++)
					games.set(j, games.get(j) + ":b");
			}

			for (Integer i : boardGamesTimesPlayed.keySet()) {
				List<String> games = boardGamesTimesPlayed.get(i);
				for (int j = 0; j < games.size(); j++)
					allGamesByTimesPlayed.put(games.get(j) + ":b", i);
			}

			for (Integer i : boardGamesTimePlayed.keySet()) {
				List<String> games = boardGamesTimePlayed.get(i);
				for (int j = 0; j < games.size(); j++) {
					int times = allGamesByTimesPlayed.get(games.get(j));
					int average = (int) (i / times * Math.pow(1.1, times));
					if (!allGames.containsKey(average))
						allGames.put(average, new ArrayList<String>());
					allGames.get(average)
							.add(games.get(j));
				}
			}
		}

		if (useRPGsForStats) {
			Map<Integer, List<String>> rpgsTimePlayed = RPGStatsDbUtility.getAllGamesByTimePlayed(dbHelper);
			Map<Integer, List<String>> rpgsTimesPlayed = RPGStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

			for (Integer i : rpgsTimePlayed.keySet()) {
				List<String> games = rpgsTimePlayed.get(i);
				for (int j = 0; j < games.size(); j++)
					games.set(j, games.get(j) + ":r");
			}

			for (Integer i : rpgsTimesPlayed.keySet()) {
				List<String> games = rpgsTimesPlayed.get(i);
				for (int j = 0; j < games.size(); j++)
					allGamesByTimesPlayed.put(games.get(j) + ":r", i);
			}

			for (Integer i : rpgsTimePlayed.keySet()) {
				List<String> games = rpgsTimePlayed.get(i);
				for (int j = 0; j < games.size(); j++) {
					int times = allGamesByTimesPlayed.get(games.get(j));
					int average = (int) (i / times * Math.pow(1.1, times));
					if (!allGames.containsKey(average))
						allGames.put(average, new ArrayList<String>());
					allGames.get(average)
							.add(games.get(j));
				}
			}
		}

		if (useVideoGamesForStats) {
			Map<Integer, List<String>> videoGamesTimePlayed = VideoGameStatsDbUtility.getAllGamesByTimePlayed(dbHelper);
			Map<Integer, List<String>> videoGamesTimesPlayed =
					VideoGameStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

			for (Integer i : videoGamesTimePlayed.keySet()) {
				List<String> games = videoGamesTimePlayed.get(i);
				for (int j = 0; j < games.size(); j++)
					games.set(j, games.get(j) + ":v");
			}

			for (Integer i : videoGamesTimesPlayed.keySet()) {
				List<String> games = videoGamesTimesPlayed.get(i);
				for (int j = 0; j < games.size(); j++)
					allGamesByTimesPlayed.put(games.get(j) + ":v", i);
			}

			for (Integer i : videoGamesTimePlayed.keySet()) {
				List<String> games = videoGamesTimePlayed.get(i);
				for (int j = 0; j < games.size(); j++) {
					int times = allGamesByTimesPlayed.get(games.get(j));
					int average = (int) (i / times * Math.pow(1.1, times));
					if (!allGames.containsKey(average))
						allGames.put(average, new ArrayList<String>());
					allGames.get(average)
							.add(games.get(j));
				}
			}
		}

		return allGames;
	}

	public static Map<Integer, List<String>> getAllPlayersByTimesPlayed(GamesDbHelper dbHelper,
																		boolean useBoardGamesForStats,
																		boolean useRPGsForStats,
																		boolean useVideoGamesForStats) {
		Map<Integer, List<String>> allPlayers = new TreeMap<>();
		Map<String, Integer> boardGamePlayers = null, rpgPlayers = null, videoGamePlayers = null;

		Set<String> players = new HashSet<>();

		if (useBoardGamesForStats) {
			boardGamePlayers = BoardGameStatsDbUtility.getAllPlayersByTimesPlayed(dbHelper);
			players.addAll(boardGamePlayers.keySet());
		}

		if (useRPGsForStats) {
			rpgPlayers = RPGStatsDbUtility.getAllPlayersByTimesPlayed(dbHelper);
			players.addAll(rpgPlayers.keySet());
		}

		if (useVideoGamesForStats) {
			videoGamePlayers = VideoGameStatsDbUtility.getAllPlayersByTimesPlayed(dbHelper);
			players.addAll(videoGamePlayers.keySet());
		}

		for (String player : players) {
			int count = 0;
			if (useBoardGamesForStats && boardGamePlayers.containsKey(player)) count += boardGamePlayers.get(player);
			if (useRPGsForStats && rpgPlayers.containsKey(player)) count += rpgPlayers.get(player);
			if (useVideoGamesForStats && videoGamePlayers.containsKey(player)) count += videoGamePlayers.get(player);
			if (!allPlayers.containsKey(count)) allPlayers.put(count, new ArrayList<String>());
			allPlayers.get(count).add(player);
		}
		return allPlayers;
	}

	public static Map<Integer, List<String>> getAllPlayersByTimePlayed(GamesDbHelper dbHelper,
																	   boolean useBoardGamesForStats,
																	   boolean useRPGsForStats,
																	   boolean useVideoGamesForStats) {
		Map<Integer, List<String>> allPlayers = new TreeMap<>();
		Map<String, Integer> boardGamePlayers = null, rpgPlayers = null, videoGamePlayers = null;

		Set<String> players = new HashSet<>();

		if (useBoardGamesForStats) {
			boardGamePlayers = BoardGameStatsDbUtility.getAllPlayersByTimePlayed(dbHelper);
			players.addAll(boardGamePlayers.keySet());
		}

		if (useRPGsForStats) {
			rpgPlayers = RPGStatsDbUtility.getAllPlayersByTimePlayed(dbHelper);
			players.addAll(rpgPlayers.keySet());
		}

		if (useVideoGamesForStats) {
			videoGamePlayers = VideoGameStatsDbUtility.getAllPlayersByTimePlayed(dbHelper);
			players.addAll(videoGamePlayers.keySet());
		}

		for (String player : players) {
			int count = 0;
			if (useBoardGamesForStats && boardGamePlayers.containsKey(player)) count += boardGamePlayers.get(player);
			if (useRPGsForStats && rpgPlayers.containsKey(player)) count += rpgPlayers.get(player);
			if (useVideoGamesForStats && videoGamePlayers.containsKey(player)) count += videoGamePlayers.get(player);
			if (!allPlayers.containsKey(count)) allPlayers.put(count, new ArrayList<String>());
			allPlayers.get(count).add(player);
		}
		return allPlayers;
	}

	public static Map<String, Integer> getAllWonGames(GamesDbHelper dbHelper, boolean useBoardGamesForStats,
													  boolean useRPGsForStats, boolean useVideoGamesForStats) {
		Map<String, Integer> allWonGames = new TreeMap<>();
//		Map<Integer, List<String>> rpgs = RPGStatsDbUtility.getAllGamesByTimesPlayed(dbHelper);

		if (useBoardGamesForStats) {
			Map<String, Integer> boardGames = BoardGameStatsDbUtility.getAllWonGames(dbHelper);
			allWonGames.putAll(boardGames);
		}

		if (useVideoGamesForStats) {
			Map<String, Integer> videoGames = VideoGameStatsDbUtility.getAllWonGames(dbHelper);
			allWonGames.putAll(videoGames);
		}

		return allWonGames;
	}

	public static Map<String, Integer> getAllPlayersGamesLost(GamesDbHelper dbHelper, boolean useBoardGamesForStats,
															  boolean useRPGsForStats, boolean useVideoGamesForStats) {
		Map<String, Integer> players = new TreeMap<>();

		if (useBoardGamesForStats) {
			Map<String, Integer> boardGamePlayers = BoardGameStatsDbUtility.getAllPlayersGamesLost(dbHelper);

			for (String player : boardGamePlayers.keySet()) {
				if (!players.containsKey(player)) players.put(player, boardGamePlayers.get(player));
				else players.put(player, boardGamePlayers.get(player) + players.get(player));
			}
		}

		if (useVideoGamesForStats) {
			Map<String, Integer> videoGamePlayers = VideoGameStatsDbUtility.getAllPlayersGamesLost(dbHelper);

			for (String player : videoGamePlayers.keySet()) {
				if (!players.containsKey(player)) players.put(player, videoGamePlayers.get(player));
				else players.put(player, videoGamePlayers.get(player) + players.get(player));
			}
		}

		return players;
	}

	public static int getNumberOfGamesInCollection(GamesDbHelper dbHelper, boolean useBoardGamesForStats,
												   boolean useRPGsForStats, boolean useVideoGamesForStats) {
		int count = 0;
		if (useBoardGamesForStats) count += BoardGameStatsDbUtility.getNumberOfGamesInCollection(dbHelper);
		if (useRPGsForStats) count += RPGStatsDbUtility.getNumberOfGamesInCollection(dbHelper);
		if (useVideoGamesForStats) count += VideoGameStatsDbUtility.getNumberOfGamesInCollection(dbHelper);
		return count;
	}
}
