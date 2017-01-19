package com.cjboyett.boardgamestats.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cjboyett.boardgamestats.data.games.GameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.PlayerData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static com.cjboyett.boardgamestats.data.PlayerContract.PlayerEntry;

/**
 * Created by Casey on 8/10/2016.
 */
public class PlayersDbUtility
{
	public static PlayerData getPlayerData(GamesDbHelper dbHelper, String player)
	{
		PlayerData playerData = null;
		Cursor playerCursor = dbHelper.getReadableDatabase()
		                              .query(PlayerEntry.TABLE_NAME,
		                                     new String[]{PlayerEntry.NAME,
		                                                  PlayerEntry.FACEBOOKID,
		                                                  PlayerEntry.NOTES,
		                                                  PlayerEntry.IMAGE,
		                                                  PlayerEntry.TIMESPLAYED,
		                                                  PlayerEntry.TIMEPLAYED,
		                                                  PlayerEntry.MOSTPLAYEDGAMEBYTIMES,
		                                                  PlayerEntry.MOSTPLAYEDGAMEBYTIME,
		                                                  PlayerEntry.MOSTWONGAME,
		                                                  PlayerEntry.MOSTLOSTGAME,
		                                                  PlayerEntry.WINPERCENTAGE,
		                                                  PlayerEntry.LOSEPERCENTAGE},
		                                     PlayerEntry.NAME + " = ?",
		                                     new String[]{player},
		                                     null,
		                                     null,
		                                     null);

		if (playerCursor.moveToNext())
		{
			String facebookid = "";
			if (playerCursor.getString(1) != null) facebookid = playerCursor.getString(1);
			playerData = new PlayerData(playerCursor.getString(0),
			                            facebookid,
			                            playerCursor.getString(2),
			                            playerCursor.getString(3),
			                            playerCursor.getInt(4),
			                            playerCursor.getInt(5),
			                            playerCursor.getString(6),
			                            playerCursor.getString(7),
			                            playerCursor.getString(8),
			                            playerCursor.getString(9),
			                            playerCursor.getDouble(10),
			                            playerCursor.getDouble(11));
		}
		else playerData = new PlayerData("", "", "", "", 0, 0, "", "", "", "", 0, 0);

		playerCursor.close();
		return playerData;
	}

	// TODO Fix to test whether conflict is from facebookid or name
	public static void addPlayerData(GamesDbHelper dbHelper, PlayerData playerData)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.NAME, playerData.getName());
		values.put(PlayerEntry.FACEBOOKID, playerData.getFacebookid());
		values.put(PlayerEntry.NOTES, playerData.getNotes());
		values.put(PlayerEntry.IMAGE, playerData.getImageFilePath());
		values.put(PlayerEntry.TIMEPLAYED, playerData.getTimePlayedWith());
		values.put(PlayerEntry.TIMESPLAYED, playerData.getTimesPlayedWith());
		values.put(PlayerEntry.MOSTPLAYEDGAMEBYTIME, playerData.getMostPlayedGameByTime());
		values.put(PlayerEntry.MOSTPLAYEDGAMEBYTIMES, playerData.getMostPlayedGameByTimes());
		values.put(PlayerEntry.MOSTWONGAME, playerData.getMostWonGame());
		values.put(PlayerEntry.MOSTLOSTGAME, playerData.getMostLostGame());
		values.put(PlayerEntry.WINPERCENTAGE, playerData.getWinPercentage());
		values.put(PlayerEntry.LOSEPERCENTAGE, playerData.getLosePercentage());
		try
		{
			dbHelper.getWritableDatabase().insertWithOnConflict(PlayerEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		}
		catch (Exception e)
		{
			dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME, values, PlayerEntry.NAME + " = ?", new String[]{playerData.getName()});
		}
	}

	public static String facebookID(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getFacebookid();
	}

	// TODO Fix to updateOnConflict
	public static void setFacebookID(GamesDbHelper dbHelper, String player, String facebookid)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.FACEBOOKID, facebookid);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static String getPlayerNotes(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getNotes();
	}

	public static void setPlayerNotes(GamesDbHelper dbHelper, String player, String playerNotes)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.NOTES, playerNotes);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static String getPlayerImageFilePath(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getImageFilePath();
	}

	public static void setPlayerImageFilePath(GamesDbHelper dbHelper, String player, boolean hasFilePath)
	{
		ContentValues values = new ContentValues();
		if (hasFilePath)
		{
			if (!player.equals("master_user")) values.put(PlayerEntry.IMAGE, getPlayerId(dbHelper, player));
			else values.put(PlayerEntry.IMAGE, "master_user");
		}
		else
			values.put(PlayerEntry.IMAGE, "");
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static int timesPlayedWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getTimesPlayedWith();
	}

	public static void setTimesPlayedWith(GamesDbHelper dbHelper, String player, int timesPlayedWith)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.TIMESPLAYED, timesPlayedWith);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static int timePlayedWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getTimePlayedWith();
	}

	public static void setTimePlayedWith(GamesDbHelper dbHelper, String player, int timePlayedWith)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.TIMEPLAYED, timePlayedWith);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static String mostPlayedGameByTimeWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getMostPlayedGameByTime();
	}

	public static void setMostPlayedGameByTimeWith(GamesDbHelper dbHelper, String player, String mostPlayedGameByTime)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.MOSTPLAYEDGAMEBYTIME, mostPlayedGameByTime);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static String mostPlayedGameByTimesWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getMostPlayedGameByTimes();
	}

	public static void setMostPlayedGameByTimesWith(GamesDbHelper dbHelper, String player, String mostPlayedGameByTimes)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.MOSTPLAYEDGAMEBYTIMES, mostPlayedGameByTimes);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static String mostWonGameWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getMostWonGame();
	}

	public static void setMostWonGameWith(GamesDbHelper dbHelper, String player, String mostWonGame)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.MOSTWONGAME, mostWonGame);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static String mostLostGameWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getMostLostGame();
	}

	public static void setMostLostGameWith(GamesDbHelper dbHelper, String player, String mostLostGame)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.MOSTLOSTGAME, mostLostGame);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static double winPercentageWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getWinPercentage();
	}

	public static void setWinPercentageWith(GamesDbHelper dbHelper, String player, double winPercentage)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.WINPERCENTAGE, winPercentage);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static double losePercentageWith(GamesDbHelper dbHelper, String player)
	{
		return getPlayerData(dbHelper, player).getLosePercentage();
	}

	public static void setLostPercentageWith(GamesDbHelper dbHelper, String player, double losePercentage)
	{
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.LOSEPERCENTAGE, losePercentage);
		dbHelper.getWritableDatabase().update(PlayerEntry.TABLE_NAME,
		                                      values,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{player});
	}

	public static long getPlayerId(GamesDbHelper dbHelper, String player)
	{
		long id = 0;
		Cursor cursor = dbHelper.getReadableDatabase().query(PlayerEntry.TABLE_NAME,
		                                                     new String[]{PlayerEntry._ID},
		                                                     PlayerEntry.NAME + " = ?",
		                                                     new String[]{player},
		                                                     null,
		                                                     null,
		                                                     null);
		if (cursor.moveToNext()) id = cursor.getLong(0);
		cursor.close();
		return id;
	}

	public static void generateNewPlayer(GamesDbHelper dbHelper, String player)
	{
		List<GamePlayData> gamePlayDataList = GameStatsDbUtility.getGamePlaysWithPlayer(dbHelper, player, true, true, true);
		Map<String, Integer> gamesCountMap = new HashMap<>();
		Map<String, Integer> gamesTimeMap = new HashMap<>();
		Map<String, Integer> gamesWinMap = new HashMap<>();
		Map<String, Integer> gamesLoseMap = new HashMap<>();
		int gamesCount = 0, winnableGamesCount = 0, totalTime = 0, winCount = 0, loseCount = 0;

		for (GamePlayData gamePlayData : gamePlayDataList)
		{
			if (gamePlayData.isCountForStats() && gamePlayData.getGame() != null)
			{
				String gameName = gamePlayData.getGame().getName();

				gamesCount++;

				if (!gamesCountMap.containsKey(gameName))
					gamesCountMap.put(gameName, 0);
				gamesCountMap.put(gameName, gamesCountMap.get(gameName) + 1);

				if (!gamesTimeMap.containsKey(gameName))
					gamesTimeMap.put(gameName, 0);
				gamesTimeMap.put(gameName, gamesTimeMap.get(gameName) + gamePlayData.getTimePlayed());
				totalTime += gamePlayData.getTimePlayed();

				boolean userWin = gamePlayData.getOtherPlayers().get("master_user").isWin();
				boolean playerWin = gamePlayData.getOtherPlayers().get(player).isWin();

				if (!(gamePlayData instanceof RPGPlayData))
				{
					winnableGamesCount++;

					if (!gamesWinMap.containsKey(gameName))
						gamesWinMap.put(gameName, 0);
					if (userWin && !playerWin)
					{
						gamesWinMap.put(gameName, gamesWinMap.get(gameName) + 1);
						winCount++;
					}

					if (!gamesLoseMap.containsKey(gameName))
						gamesLoseMap.put(gameName, 0);
					if (!userWin && playerWin)
					{
						gamesLoseMap.put(gameName, gamesLoseMap.get(gameName) + 1);
						loseCount++;
					}
				}
			}
		}

		int maxPlay = 0, maxTime = 0, maxWin = 0, maxLose = 0;
		for (String game : gamesCountMap.keySet())
		{
			maxPlay = Math.max(maxPlay, gamesCountMap.get(game));
			maxTime = Math.max(maxTime, gamesTimeMap.get(game));
			if (gamesWinMap.containsKey(game))
				maxWin = Math.max(maxWin, gamesWinMap.get(game));
			if (gamesLoseMap.containsKey(game))
				maxLose = Math.max(maxLose, gamesLoseMap.get(game));
		}

		List<String> mostPlayedGamesByTimes = new ArrayList<>();
		List<String> mostPlayedGamesByTime = new ArrayList<>();
		List<String> mostWonGames = new ArrayList<>();
		List<String> mostLostGames = new ArrayList<>();

		for (String game : gamesCountMap.keySet())
		{
			if (gamesCountMap.get(game) == maxPlay) mostPlayedGamesByTimes.add(game);
			if (gamesTimeMap.get(game) == maxTime) mostPlayedGamesByTime.add(game);
			if (gamesWinMap.containsKey(game))
				if (maxWin > 0 && gamesWinMap.get(game) == maxWin) mostWonGames.add(game);
			if (gamesLoseMap.containsKey(game))
				if (maxLose > 0 && gamesLoseMap.get(game) == maxLose) mostLostGames.add(game);
		}

		Random r = new Random();

		String mostPlayedGameByTimes = "", mostPlayedGameByTime = "", mostWonGame = "", mostLostGame = "";
		if (!mostPlayedGamesByTimes.isEmpty()) mostPlayedGameByTimes = mostPlayedGamesByTimes.get(r.nextInt(mostPlayedGamesByTimes.size()));
		if (!mostPlayedGamesByTime.isEmpty()) mostPlayedGameByTime = mostPlayedGamesByTime.get(r.nextInt(mostPlayedGamesByTime.size()));
		if (!mostWonGames.isEmpty()) mostWonGame = mostWonGames.get(r.nextInt(mostWonGames.size()));
		if (!mostLostGames.isEmpty()) mostLostGame = mostLostGames.get(r.nextInt(mostLostGames.size()));

		String facebookId, avatarImagePath, notes;
		try
		{
			// FIXME
			facebookId = "NO ID: " + player;//facebookID(dbHelper, player);
		}
		catch (Exception e)
		{
			facebookId = "NO ID: " + player;
		}

		try
		{
			notes = getPlayerNotes(dbHelper, player);
		}
		catch (Exception e)
		{
			notes = "";
		}

		try
		{
			avatarImagePath = getPlayerImageFilePath(dbHelper, player);
		}
		catch (Exception e)
		{
			avatarImagePath = "";
		}

		PlayerData playerData = new PlayerData(player,
		                                       facebookId,
		                                       notes,
		                                       avatarImagePath,
		                                       gamesCount,
		                                       totalTime,
		                                       mostPlayedGameByTimes,
		                                       mostPlayedGameByTime,
		                                       mostWonGame,
		                                       mostLostGame,
		                                       (100. * winCount) / winnableGamesCount,
		                                       (100. * loseCount) / winnableGamesCount);

		addPlayerData(dbHelper, playerData);
	}

	public static void populateAllPlayersTable(GamesDbHelper dbHelper)
	{
		List<String> allPlayers;
		Set<String> players = new TreeSet<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor boardGamePlayers = db.query(true,
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

		Cursor rpgPlayers = db.query(true,
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

		Cursor videoGamePlayers = db.query(true,
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
		db.close();

		allPlayers = new ArrayList<>(players);
//		for (String player : allPlayers) Log.d("PLAYER", player);
		for (String player : allPlayers) PlayersDbUtility.generateNewPlayer(dbHelper, player);
	}

	public static void combinePlayers(GamesDbHelper dbHelper, String oldPlayer, String newPlayer)
	{
		dbHelper.getWritableDatabase().delete(PlayerEntry.TABLE_NAME,
		                                      PlayerEntry.NAME + " = ?",
		                                      new String[]{oldPlayer});
		generateNewPlayer(dbHelper, newPlayer);
	}

	public static String generateBlurb(GamesDbHelper dbHelper, String player)
	{
		String blurb = "";
		Random r = new Random();
		int selection = r.nextInt(8);
		switch (selection)
		{
			case 0:
				int timesPlayed = timesPlayedWith(dbHelper, player);
				blurb = "<b>Times played with " + player + ":</b><br/>" + timesPlayed + " play" + (timesPlayed != 1 ? "s" : "");
				break;
			case 1:
				blurb = "<b>Time played with " + player + ":</b><br/>" + StringUtilities.convertMinutes(timePlayedWith(dbHelper, player));
				break;
			case 2:
				blurb = "<b>Favorite game (by count) with " + player + ":</b><br/>" + mostPlayedGameByTimesWith(dbHelper, player);
				break;
			case 3:
				blurb = "<b>Favorite game (by time) with " + player + ":</b><br/>" + mostPlayedGameByTimeWith(dbHelper, player);
				break;
			case 4:
				String mostWonGame = mostWonGameWith(dbHelper, player);
				if (!TextUtils.isEmpty(mostWonGame))
					blurb = "<b>Most won game against " + player + ":</b><br/>" + mostWonGame;
				else
				{
					timesPlayed = timesPlayedWith(dbHelper, player);
					blurb = "<b>Times played with " + player + ":</b><br/>" + timesPlayed + " play" + (timesPlayed != 1 ? "s" : "");
				}
				break;
			case 5:
				String mostLostGame = mostLostGameWith(dbHelper, player);
				if (!TextUtils.isEmpty(mostLostGame))
					blurb = "<b>Most lost game against " + player + ":</b><br/>" + mostLostGame;
				else
					blurb = "<b>Time played with " + player + ":</b><br/>" + StringUtilities.convertMinutes(timePlayedWith(dbHelper, player));
				break;
			case 6:
				blurb = "<b>Win percentage against " + player + ":</b><br/>" + NumberFormat.getPercentInstance().format(winPercentageWith(dbHelper, player) / 100);
				break;
			case 7:
				blurb = "<b>Lose percentage against " + player + ":</b><br/>" + NumberFormat.getPercentInstance().format(losePercentageWith(dbHelper, player) / 100);
				break;
		}
		return blurb;
	}
}
