package com.cjboyett.boardgamestats.data.games.video;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract.VideoGameEntry;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.cjboyett.boardgamestats.data.games.video.VideoGameContract.GamePlayEntry;
import static com.cjboyett.boardgamestats.data.games.video.VideoGameContract.PlayerEntry;

/**
 * Created by Casey on 4/13/2016.
 */
public class VideoGameStatsDbUtility
{
	public static String getRandomBlurb(GamesDbHelper dbHelper, String game)
	{
		/* 0: Times played
		 * 1: Last time played
		 * 2: Most played with
		 * 3: Time played
		 * 4: Average game length
		 */
		Random r = new Random();
		int blurbCount = 5;
		if (averageTimePlayed(dbHelper, game) <= 2) blurbCount -= 2;
		int i = r.nextInt(blurbCount);
		String blurb = "";

		switch (i)
		{
			// Times played
			case 0:
				int timesPlayed = timesPlayed(dbHelper, game);
				blurb = "<b>Times played:</b><br/>" + timesPlayed + " play" + (timesPlayed != 1 ? "s" : "");
				break;
			// Last time played
			case 1:
				Date date = lastTimePlayed(dbHelper, game);
				if (date != null)
					blurb = "<b>Last played:</b><br/>" + date.toString();
				else
					blurb = "<b>You have never played.</b>";
				break;
			// Most played with
			case 2:
				String mostPlayedPlayer = getMostPlayedWithPlayer(dbHelper, game);
				if (mostPlayedPlayer != null)
					blurb = "<b>Most played with:</b><br/>" + mostPlayedPlayer;
				else
					blurb = "<b>You have only played solo.</b>";
				break;
			// Time played
			case 3:
				blurb = "<b>Total time played:</b><br/>" + StringUtilities.convertMinutes(totalTimePlayed(dbHelper, game));
				break;
			// Average game length
			case 4:
				blurb = "<b>Average game length:</b><br/>" + StringUtilities.convertMinutes(averageTimePlayed(dbHelper, game));
				break;
		}

		return blurb;
	}

/*
	public static int timePlayed(GamesDbHelper dbHelper, long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
		                            new String[]{GamePlayEntry.TIME_PLAYED},
		                            GamePlayEntry._ID + " = ?",
		                            new String[]{id + ""},
		                            null,
		                            null,
		                            null);
		int timePlayed = 1;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayed(GamesDbHelper dbHelper)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
				null,
				null,
				null,
				null,
				null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayed(GamesDbHelper dbHelper, String game)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
				GamePlayEntry.GAME + " = ?",
				new String[]{game},
				null,
				null,
				null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayedWithPlayer(GamesDbHelper dbHelper, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " +
						PlayerEntry.TABLE_NAME + " ON " +
						GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
						" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
				PlayerEntry.NAME + " = ?",
				new String[]{playerName},
				null,
				null,
				null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayedWithPlayer(GamesDbHelper dbHelper, String game, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " +
						PlayerEntry.TABLE_NAME + " ON " +
						GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
						" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME +
						" = ? AND " +
						GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.GAME + " = ?",
				new String[]{playerName, game},
				null,
				null,
				null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayed(GamesDbHelper dbHelper)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
				new String[]{"COUNT(" + GamePlayEntry.TIME_PLAYED + ")"},
				null,
				null,
				null,
				null,
				null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayed(GamesDbHelper dbHelper, String game)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
				new String[]{"COUNT(" + GamePlayEntry.TIME_PLAYED + ")"},
				GamePlayEntry.GAME + " = ?",
				new String[]{game},
				null,
				null,
				null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayedWithPlayer(GamesDbHelper dbHelper, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(PlayerEntry.TABLE_NAME,
				new String[]{"COUNT(*)"},
				PlayerEntry.NAME + " = ?",
				new String[]{playerName},
				null,
				null,
				null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayedWithPlayer(GamesDbHelper dbHelper, String game, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				PlayerEntry.TABLE_NAME + " INNER JOIN " +
						GamePlayEntry.TABLE_NAME + " ON " +
						GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
						" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"COUNT(*)"},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME +
						" = ? AND " +
						GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.GAME + " = ?",
				new String[]{playerName, game},
				null,
				null,
				null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int averageTimePlayed(GamesDbHelper dbHelper, String game)
	{
		return totalTimePlayed(dbHelper, game) / timesPlayed(dbHelper, game);
	}

	public static List<GamePlayData> getGamePlaysWithPlayer(GamesDbHelper dbHelper, String playerName)
	{
		List<Long> gamePlayIds = new ArrayList<>();
		List<GamePlayData> videoGamePlayDataList = new ArrayList<>();
		Cursor gamePlayIdCursor = dbHelper.getReadableDatabase()
				.query(true,
						PlayerEntry.TABLE_NAME + " INNER JOIN " + GamePlayEntry.TABLE_NAME,
						new String[]{PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID},
						PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME + " = ?",
						new String[]{playerName},
						null,
						null,
						null,
						null);
		while (gamePlayIdCursor.moveToNext()) gamePlayIds.add(gamePlayIdCursor.getLong(0));
		if (gamePlayIds.size() > 0)
		{
			videoGamePlayDataList = VideoGameDbUtility.getGamePlays(dbHelper, gamePlayIds);
			gamePlayIdCursor.close();
			Collections.sort(videoGamePlayDataList, new Comparator<GamePlayData>()
			{
				@Override
				public int compare(GamePlayData lhs, GamePlayData rhs)
				{
					return -lhs.getDate().rawDate().compareTo(rhs.getDate().rawDate());
				}
			});
		}
		return videoGamePlayDataList;
	}

	public static List<GamePlayData> getGamePlaysFromGame(GamesDbHelper dbHelper, String game)
	{
		List<Long> gamePlayIds = new ArrayList<>();
		List<GamePlayData> videoGamePlayDataList;
		Cursor gamePlayIdCursor = dbHelper.getReadableDatabase()
				.query(GamePlayEntry.TABLE_NAME,
						new String[]{GamePlayEntry._ID},
						GamePlayEntry.GAME + " = ?",
						new String[]{game},
						null,
						null,
						null,
						null);
		while (gamePlayIdCursor.moveToNext())
		{
			gamePlayIds.add(gamePlayIdCursor.getLong(0));
		}
		videoGamePlayDataList = VideoGameDbUtility.getGamePlays(dbHelper, gamePlayIds);
		gamePlayIdCursor.close();
		Collections.sort(videoGamePlayDataList, new Comparator<GamePlayData>()
		{
			@Override
			public int compare(GamePlayData lhs, GamePlayData rhs)
			{
				return -lhs.getDate().rawDate().compareTo(rhs.getDate().rawDate());
			}
		});
		return videoGamePlayDataList;
	}

	public static int getNumberGamesPlayed(GamesDbHelper dbHelper)
	{
		Cursor countCursor = dbHelper.getReadableDatabase().query(true,
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry.GAME},
				null,
				null,
				null,
				null,
				null,
				null);
		int count = countCursor.getCount();
		countCursor.close();
		return count;
	}

	public static Map<Integer, List<String>> getAllGamesByTimesPlayed(GamesDbHelper dbHelper)
	{
		Map<Integer, List<String>> games = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{"COUNT(" + GamePlayEntry.GAME + ")", GamePlayEntry.GAME},
				null,
				null,
				GamePlayEntry.GAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			int count = gamesCursor.getInt(0);
			if (!games.containsKey(count))
				games.put(count, new ArrayList<String>());
			games.get(count).add(gamesCursor.getString(1));
		}

		gamesCursor.close();

		return games;
	}

	public static Map<Integer, List<String>> getAllGamesByTimePlayed(GamesDbHelper dbHelper)
	{
		Map<Integer, List<String>> games = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")", GamePlayEntry.GAME},
				null,
				null,
				GamePlayEntry.GAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			int count = gamesCursor.getInt(0);
			count = Math.max(count, 1);
			if (!games.containsKey(count))
				games.put(count, new ArrayList<String>());
			games.get(count).add(gamesCursor.getString(1));
		}

		gamesCursor.close();

		return games;
	}

	public static Map<String, Integer> getAllPlayersByTimesPlayed(GamesDbHelper dbHelper)
	{
		Map<String, Integer> players = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				PlayerEntry.TABLE_NAME,
				new String[]{"COUNT(" + PlayerEntry.NAME + ")", PlayerEntry.NAME},
				null,
				null,
				PlayerEntry.NAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			String player = gamesCursor.getString(1);
			if (!player.equalsIgnoreCase("master_user") && !player.equalsIgnoreCase("Other"))
				players.put(gamesCursor.getString(1), gamesCursor.getInt(0));
		}

		gamesCursor.close();

		return players;
	}

	public static Map<String, Integer> getAllPlayersByTimePlayed(GamesDbHelper dbHelper)
	{
		Map<String, Integer> players = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
						" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
						PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")", PlayerEntry.NAME},
				null,
				null,
				PlayerEntry.NAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			String player = gamesCursor.getString(1);
			if (!player.equalsIgnoreCase("master_user") && !player.equalsIgnoreCase("Other"))
				players.put(gamesCursor.getString(1), gamesCursor.getInt(0));
		}

		gamesCursor.close();

		return players;
	}

	public static Map<String, Integer> getAllWonGames(GamesDbHelper dbHelper)
	{
		Map<String, Integer> games = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
						" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
						PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"COUNT(" + GamePlayEntry.GAME + ")", GamePlayEntry.GAME},
				GamePlayEntry.COUNT_FOR_STATS + " = ? AND " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.WIN + " = ? AND " + PlayerEntry.NAME + " = ?",
				new String[]{"y", "y", "master_user"},
				GamePlayEntry.GAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			games.put(gamesCursor.getString(1), gamesCursor.getInt(0));
		}

		gamesCursor.close();

		return games;
	}

	public static Map<String, Integer> getAllPlayersGamesLost(GamesDbHelper dbHelper)
	{
		List<Long> lostGamesIds = new ArrayList<>();
		Cursor lostGamesIdCursor = dbHelper.getReadableDatabase().query(
				PlayerEntry.TABLE_NAME,
				new String[]{PlayerEntry.GAME_PLAY_ID},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.WIN + " = ? AND " + PlayerEntry.NAME + " = ?",
				new String[]{"n", "master_user"},
				null,
				null,
				null);

		while(lostGamesIdCursor.moveToNext()) lostGamesIds.add(lostGamesIdCursor.getLong(0));
		lostGamesIdCursor.close();

		Map<String, Integer> players = new TreeMap<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		for (long id : lostGamesIds)
		{
			Cursor playersCursor = db.query(PlayerEntry.TABLE_NAME,
					new String[]{PlayerEntry.NAME},
					PlayerEntry.WIN + " = ? AND NOT " + PlayerEntry.NAME + " = ?",
					new String[]{"y", "OTHER"},
					null,
					null,
					null);
			while (playersCursor.moveToNext())
			{
				String player = playersCursor.getString(0);
				if (!players.containsKey(player)) players.put(player, 1);
				else players.put(player, players.get(player) + 1);
			}
		}
		db.close();

		return players;
	}

	public static String getMostPlayedWithPlayer(GamesDbHelper dbHelper, String game)
	{
		List<Long> gameIds = new ArrayList<>();
		Cursor gamePlayIdCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
				" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{PlayerEntry.GAME_PLAY_ID},
				GamePlayEntry.GAME + " = ?",
				new String[]{game},
				null,
				null,
				null);

		while(gamePlayIdCursor.moveToNext()) gameIds.add(gamePlayIdCursor.getLong(0));
		gamePlayIdCursor.close();

		Map<String, Integer> players = new TreeMap<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		for (long id : gameIds)
		{
			Cursor playersCursor = db.query(PlayerEntry.TABLE_NAME,
			                                new String[]{PlayerEntry.NAME},
			                                PlayerEntry.GAME_PLAY_ID + " = ? AND NOT " + PlayerEntry.NAME + " = ? AND NOT " + PlayerEntry.NAME + " = ?",
			                                new String[]{id + "", "OTHER", "master_user"},
			                                null,
			                                null,
			                                null);
			while (playersCursor.moveToNext())
			{
				String player = playersCursor.getString(0);
				if (!players.containsKey(player)) players.put(player, 1);
				else players.put(player, players.get(player) + 1);
			}
		}
		db.close();

		int mostPlays = 0;
		for (Integer i : players.values()) mostPlays = Math.max(mostPlays, i);
		List<String> mostPlayedPlayers = new ArrayList<>();
		for (String player : players.keySet())
			if (players.get(player) == mostPlays)
				mostPlayedPlayers.add(player);

		if (mostPlayedPlayers.isEmpty()) return null;
		else
		{
			Random r = new Random();
			String player = mostPlayedPlayers.get(r.nextInt(mostPlayedPlayers.size()));
			return player;
		}
	}

	public static Date lastTimePlayed(GamesDbHelper dbHelper, String game)
	{
		Cursor dateCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry.DATE},
				GamePlayEntry.GAME + " = ?",
				new String[]{game},
				null,
				null,
				GamePlayEntry.DATE + " DESC",
				"1");
		Date date;
		if (dateCursor.moveToFirst())
			date = new Date(dateCursor.getString(0));
		else
			date = null;
		dateCursor.close();

		return date;
	}

	public static int getNumberOfGamesInCollection(GamesDbHelper dbHelper)
	{
		int count = 0;
		Cursor cursor = dbHelper.getReadableDatabase()
		                        .query(VideoGameEntry.TABLE_NAME,
		                               new String[]{"COUNT (" + VideoGameEntry.NAME + ")"},
		                               null,
		                               null,
		                               null,
		                               null,
		                               null);
		if (cursor.moveToNext())
			count = cursor.getInt(0);
		return count;
	}

*/

	public static int timePlayed(GamesDbHelper dbHelper, long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
		                            new String[]{GamePlayEntry.TIME_PLAYED},
		                            GamePlayEntry._ID + " = ?",
		                            new String[]{id + ""},
		                            null,
		                            null,
		                            null);
		int timePlayed = 1;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayed(GamesDbHelper dbHelper)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
		                            new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
		                            null,
		                            null,
		                            null,
		                            null,
		                            null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayed(GamesDbHelper dbHelper, String game)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
		                            new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
		                            GamePlayEntry.GAME + " = ? AND " + GamePlayEntry.COUNT_FOR_STATS + " = ?",
		                            new String[]{game, "y"},
		                            null,
		                            null,
		                            null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayedWithPlayer(GamesDbHelper dbHelper, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " +
				PlayerEntry.TABLE_NAME + " ON " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
				" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
				PlayerEntry.NAME + " = ? AND " + GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{playerName, "y"},
				null,
				null,
				null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int totalTimePlayedWithPlayer(GamesDbHelper dbHelper, String game, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " +
				PlayerEntry.TABLE_NAME + " ON " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
				" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")"},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME +
				" = ? AND " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.GAME + " = ? AND " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{playerName, game, "y"},
				null,
				null,
				null);
		int timePlayed = 0;
		if (sumCursor.moveToNext()) timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayed(GamesDbHelper dbHelper)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
		                            new String[]{"COUNT(" + GamePlayEntry.TIME_PLAYED + ")"},
		                            GamePlayEntry.COUNT_FOR_STATS + " = ?",
		                            new String[]{"y"},
		                            null,
		                            null,
		                            null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayed(GamesDbHelper dbHelper, String game)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(GamePlayEntry.TABLE_NAME,
		                            new String[]{"COUNT(" + GamePlayEntry.TIME_PLAYED + ")"},
		                            GamePlayEntry.GAME + " = ? AND " + GamePlayEntry.COUNT_FOR_STATS + " = ?",
		                            new String[]{game, "y"},
		                            null,
		                            null,
		                            null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayedWithPlayer(GamesDbHelper dbHelper, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				PlayerEntry.TABLE_NAME + " INNER JOIN " +
				GamePlayEntry.TABLE_NAME + " ON " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
				" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"COUNT(*)"},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME +
				" = ? AND " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{playerName, "y"},
				null,
				null,
				null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int timesPlayedWithPlayer(GamesDbHelper dbHelper, String game, String playerName)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor sumCursor = db.query(
				PlayerEntry.TABLE_NAME + " INNER JOIN " +
				GamePlayEntry.TABLE_NAME + " ON " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
				" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"COUNT(*)"},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME +
				" = ? AND " +
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.GAME + " = ?",
				new String[]{playerName, game},
				null,
				null,
				null);
		sumCursor.moveToNext();
		int timePlayed = sumCursor.getInt(0);
		sumCursor.close();
		db.close();
		return timePlayed;
	}

	public static int averageTimePlayed(GamesDbHelper dbHelper, String game)
	{
		return totalTimePlayed(dbHelper, game) / timesPlayed(dbHelper, game);
	}

	public static List<GamePlayData> getGamePlaysWithPlayer(GamesDbHelper dbHelper, String playerName)
	{
		List<Long> gamePlayIds = new ArrayList<>();
		List<GamePlayData> boardGamePlayDataList = new ArrayList<>();
		Cursor gamePlayIdCursor = dbHelper.getReadableDatabase()
		                                  .query(true,
		                                         PlayerEntry.TABLE_NAME + " INNER JOIN " + GamePlayEntry.TABLE_NAME,
		                                         new String[]{PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID},
		                                         PlayerEntry.TABLE_NAME + "." + PlayerEntry.NAME + " = ?",
		                                         new String[]{playerName},
		                                         null,
		                                         null,
		                                         null,
		                                         null);
		while (gamePlayIdCursor.moveToNext()) gamePlayIds.add(gamePlayIdCursor.getLong(0));
		if (gamePlayIds.size() > 0)
		{
			boardGamePlayDataList = VideoGameDbUtility.getGamePlays(dbHelper, gamePlayIds);
			gamePlayIdCursor.close();
			Collections.sort(boardGamePlayDataList, new Comparator<GamePlayData>()
			{
				@Override
				public int compare(GamePlayData lhs, GamePlayData rhs)
				{
					return -lhs.getDate().rawDate().compareTo(rhs.getDate().rawDate());
				}
			});
		}
		return boardGamePlayDataList;
	}

	public static List<GamePlayData> getGamePlaysFromGame(GamesDbHelper dbHelper, String game)
	{
		List<Long> gamePlayIds = new ArrayList<>();
		List<GamePlayData> boardGamePlayDataList;
		Cursor gamePlayIdCursor = dbHelper.getReadableDatabase()
		                                  .query(GamePlayEntry.TABLE_NAME,
		                                         new String[]{GamePlayEntry._ID},
		                                         GamePlayEntry.GAME + " = ?",
		                                         new String[]{game},
		                                         null,
		                                         null,
		                                         null,
		                                         null);
		while (gamePlayIdCursor.moveToNext())
		{
			gamePlayIds.add(gamePlayIdCursor.getLong(0));
		}
		boardGamePlayDataList = VideoGameDbUtility.getGamePlays(dbHelper, gamePlayIds);
		gamePlayIdCursor.close();
		Collections.sort(boardGamePlayDataList, new Comparator<GamePlayData>()
		{
			@Override
			public int compare(GamePlayData lhs, GamePlayData rhs)
			{
				return -lhs.getDate().rawDate().compareTo(rhs.getDate().rawDate());
			}
		});
		return boardGamePlayDataList;
	}

	public static int getNumberGamesPlayed(GamesDbHelper dbHelper)
	{
		Cursor countCursor = dbHelper.getReadableDatabase().query(true,
		                                                          GamePlayEntry.TABLE_NAME,
		                                                          new String[]{GamePlayEntry.GAME},
		                                                          GamePlayEntry.COUNT_FOR_STATS + " = ?",
		                                                          new String[]{"y"},
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null);
		int count = countCursor.getCount();
		countCursor.close();
		return count;
	}

	public static Map<Integer, List<String>> getAllGamesByTimesPlayed(GamesDbHelper dbHelper)
	{
		Map<Integer, List<String>> games = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{"COUNT(" + GamePlayEntry.GAME + ")", GamePlayEntry.GAME},
				GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{"y"},
				GamePlayEntry.GAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			int count = gamesCursor.getInt(0);
			if (!games.containsKey(count))
				games.put(count, new ArrayList<String>());
			games.get(count).add(gamesCursor.getString(1));
		}

		gamesCursor.close();

		return games;
	}

	public static Map<Integer, List<String>> getAllGamesByTimePlayed(GamesDbHelper dbHelper)
	{
		Map<Integer, List<String>> games = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{"SUM(" + GamePlayEntry.TIME_PLAYED + ")", GamePlayEntry.GAME},
				GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{"y"},
				GamePlayEntry.GAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			int count = gamesCursor.getInt(0);
			count = Math.max(count, 1);
			if (!games.containsKey(count))
				games.put(count, new ArrayList<String>());
			games.get(count).add(gamesCursor.getString(1));
		}

		gamesCursor.close();

		return games;
	}

	public static Map<String, Integer> getAllPlayersByTimesPlayed(GamesDbHelper dbHelper)
	{
		Map<String, Integer> players = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(true,
		                                                          PlayerEntry.TABLE_NAME,
		                                                          new String[]{PlayerEntry.NAME},
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null);

		while(gamesCursor.moveToNext())
		{
			String player = gamesCursor.getString(0);
			if (!player.equalsIgnoreCase("master_user") && !player.equalsIgnoreCase("Other"))
				players.put(player, timesPlayedWithPlayer(dbHelper, player));
		}

		gamesCursor.close();

		return players;
	}

	public static Map<String, Integer> getAllPlayersByTimePlayed(GamesDbHelper dbHelper)
	{
		Map<String, Integer> players = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(true,
		                                                          PlayerEntry.TABLE_NAME,
		                                                          new String[]{PlayerEntry.NAME},
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null,
		                                                          null);

		while(gamesCursor.moveToNext())
		{
			String player = gamesCursor.getString(0);
			if (!player.equalsIgnoreCase("master_user") && !player.equalsIgnoreCase("Other"))
				players.put(player, totalTimePlayedWithPlayer(dbHelper, player));
		}

		gamesCursor.close();

		return players;	}

	public static int getTimesWon(GamesDbHelper dbHelper, String game)
	{
		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
				" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"COUNT(" + GamePlayEntry.GAME + ")", GamePlayEntry.GAME},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.WIN + " = ? AND " + PlayerEntry.NAME + " = ?" +
				" AND " + GamePlayEntry.GAME + " = ? AND " +
				GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{"y", "master_user", game, "y"},
				GamePlayEntry.GAME,
				null,
				null);

		int wins = 0;
		if (gamesCursor.moveToFirst()) wins = gamesCursor.getInt(0);
		gamesCursor.close();

		return wins;
	}

	public static Map<String, Integer> getAllWonGames(GamesDbHelper dbHelper)
	{
		Map<String, Integer> games = new TreeMap<>();

		Cursor gamesCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
				" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{"COUNT(" + GamePlayEntry.GAME + ")", GamePlayEntry.GAME},
				GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.COUNT_FOR_STATS + " = ? AND " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.WIN + " = ? AND " +
				PlayerEntry.NAME + " = ?",
				new String[]{"y", "y", "master_user"},
				GamePlayEntry.GAME,
				null,
				null);

		while(gamesCursor.moveToNext())
		{
			games.put(gamesCursor.getString(1), gamesCursor.getInt(0));
		}

		gamesCursor.close();

		return games;
	}

	public static String getMostLostToPlayer(GamesDbHelper dbHelper, String game)
	{
		List<Long> lostGamesIds = new ArrayList<>();
		Cursor lostGamesIdCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
				" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{PlayerEntry.GAME_PLAY_ID},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.WIN + " = ? AND " + PlayerEntry.NAME + " = ?" +
				" AND " + GamePlayEntry.GAME + " = ? AND " +
				GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{"n", "master_user", game, "y"},
				null,
				null,
				null);

		while(lostGamesIdCursor.moveToNext()) lostGamesIds.add(lostGamesIdCursor.getLong(0));
		lostGamesIdCursor.close();

		Map<String, Integer> players = new TreeMap<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		for (long id : lostGamesIds)
		{
			Cursor playersCursor = db.query(PlayerEntry.TABLE_NAME,
			                                new String[]{PlayerEntry.NAME},
			                                PlayerEntry.GAME_PLAY_ID + " = ? AND " + PlayerEntry.WIN + " = ? AND NOT " + PlayerEntry.NAME + " = ?",
			                                new String[]{id + "", "y", "OTHER"},
			                                null,
			                                null,
			                                null);
			while (playersCursor.moveToNext())
			{
				String player = playersCursor.getString(0);
				if (!players.containsKey(player)) players.put(player, 1);
				else players.put(player, players.get(player) + 1);
			}
		}
		db.close();

		int mostLosses = 0;
		for (Integer i : players.values()) mostLosses = Math.max(mostLosses, i);
		List<String> mostLostPlayers = new ArrayList<>();
		for (String player : players.keySet())
			if (players.get(player) == mostLosses)
				mostLostPlayers.add(player);

		if (mostLostPlayers.isEmpty()) return null;
		else
		{
			Random r = new Random();
			String player = mostLostPlayers.get(r.nextInt(mostLostPlayers.size()));
			return player;
		}
	}

	public static Map<String, Integer> getAllPlayersGamesLost(GamesDbHelper dbHelper)
	{
		List<Long> lostGamesIds = new ArrayList<>();
		Cursor lostGamesIdCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
				" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{PlayerEntry.GAME_PLAY_ID},
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.WIN + " = ? AND " + PlayerEntry.NAME + " = ?" +
				" AND " + GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{"n", "master_user", "y"},
				null,
				null,
				null);

		while(lostGamesIdCursor.moveToNext()) lostGamesIds.add(lostGamesIdCursor.getLong(0));
		lostGamesIdCursor.close();

		Map<String, Integer> players = new TreeMap<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		for (long id : lostGamesIds)
		{
			Cursor playersCursor = db.query(PlayerEntry.TABLE_NAME,
			                                new String[]{PlayerEntry.NAME},
			                                PlayerEntry.GAME_PLAY_ID + " = ? AND " + PlayerEntry.WIN + " = ? AND NOT " + PlayerEntry.NAME + " = ?",
			                                new String[]{id + "", "y", "OTHER"},
			                                null,
			                                null,
			                                null);
			while (playersCursor.moveToNext())
			{
				String player = playersCursor.getString(0);
				if (!players.containsKey(player)) players.put(player, 1);
				else players.put(player, players.get(player) + 1);
			}
		}
		db.close();

		return players;
	}

	public static String getMostPlayedWithPlayer(GamesDbHelper dbHelper, String game)
	{
		List<Long> gameIds = new ArrayList<>();
		Cursor gamePlayIdCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME + " INNER JOIN " + PlayerEntry.TABLE_NAME +
				" ON " + GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID + " = " +
				PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
				new String[]{PlayerEntry.GAME_PLAY_ID},
				GamePlayEntry.GAME + " = ? AND " + GamePlayEntry.COUNT_FOR_STATS + " = ?",
				new String[]{game, "y"},
				null,
				null,
				null);

		while(gamePlayIdCursor.moveToNext()) gameIds.add(gamePlayIdCursor.getLong(0));
		gamePlayIdCursor.close();

		Map<String, Integer> players = new TreeMap<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		for (long id : gameIds)
		{
			Cursor playersCursor = db.query(PlayerEntry.TABLE_NAME,
			                                new String[]{PlayerEntry.NAME},
			                                PlayerEntry.GAME_PLAY_ID + " = ? AND NOT " + PlayerEntry.NAME + " = ? AND NOT " + PlayerEntry.NAME + " = ?",
			                                new String[]{id + "", "OTHER", "master_user"},
			                                null,
			                                null,
			                                null);
			while (playersCursor.moveToNext())
			{
				String player = playersCursor.getString(0);
				if (!players.containsKey(player)) players.put(player, 1);
				else players.put(player, players.get(player) + 1);
			}
		}
		db.close();

		int mostPlays = 0;
		for (Integer i : players.values()) mostPlays = Math.max(mostPlays, i);
		List<String> mostPlayedPlayers = new ArrayList<>();
		for (String player : players.keySet())
			if (players.get(player) == mostPlays)
				mostPlayedPlayers.add(player);

		if (mostPlayedPlayers.isEmpty()) return null;
		else
		{
			Random r = new Random();
			String player = mostPlayedPlayers.get(r.nextInt(mostPlayedPlayers.size()));
			return player;
		}
	}

	public static Date lastTimePlayed(GamesDbHelper dbHelper, String game)
	{
		Cursor dateCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry.DATE},
				GamePlayEntry.GAME + " = ?",
				new String[]{game},
				null,
				null,
				GamePlayEntry.DATE + " DESC",
				"1");
		Date date;
		if (dateCursor.moveToFirst())
			date = new Date(dateCursor.getString(0));
		else
			date = null;
		dateCursor.close();

		return date;
	}

	public static int getNumberOfGamesInCollection(GamesDbHelper dbHelper)
	{
		int count = 0;
		Cursor cursor = dbHelper.getReadableDatabase()
		                        .query(VideoGameEntry.TABLE_NAME,
		                               new String[]{"COUNT (" + VideoGameEntry.NAME + ")"},
		                               null,
		                               null,
		                               null,
		                               null,
		                               null);
		if (cursor.moveToNext())
			count = cursor.getInt(0);
		return count;
	}

	public static int getGameScore(GamesDbHelper dbHelper, String game)
	{
		int score = 1;

		score = timesPlayed(dbHelper, game) + Math.max(totalTimePlayed(dbHelper, game) / 60, 1);

		return score;
	}


}
