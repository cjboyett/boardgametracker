package com.cjboyett.boardgamestats.data.games.rpg;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGameCategory;
import com.cjboyett.boardgamestats.model.games.board.BoardGameFamily;
import com.cjboyett.boardgamestats.model.games.board.BoardGameMechanic;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePublisher;
import com.cjboyett.boardgamestats.model.games.rpg.RPGFamily;
import com.cjboyett.boardgamestats.model.games.rpg.RPGMechanic;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.cjboyett.boardgamestats.data.games.rpg.RPGContract.GamePlayEntry;
import static com.cjboyett.boardgamestats.data.games.rpg.RPGContract.ImageEntry;
import static com.cjboyett.boardgamestats.data.games.rpg.RPGContract.PlayerEntry;
import static com.cjboyett.boardgamestats.data.games.rpg.RPGContract.RPGEntry;

/**
 * Created by Casey on 4/10/2016.
 */
public class RPGDbUtility
{
	// Game data

	public static void addRPG(GamesDbHelper dbHelper, String gameName, String description, int yearPublished,
	                                int bggid, String thumbnailUrl, String families, String mechanics)
	{
		ContentValues values = new ContentValues();
		values.put(RPGEntry.NAME, gameName);
		if (description != null) values.put(RPGEntry.DESCRIPTION, description);
		if (yearPublished > 0) values.put(RPGEntry.YEAR_PUBLISHED, yearPublished);
		if (bggid > 0) values.put(RPGEntry.BGG_ID, bggid);
		if (thumbnailUrl != null) values.put(RPGEntry.THUMBNAIL, thumbnailUrl);
		if (families != null) values.put(RPGEntry.FAMILIES, families);
		if (mechanics != null) values.put(RPGEntry.MECHANICS, mechanics);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.insertOrThrow(RPGEntry.TABLE_NAME, null, values);
		}
		catch (Exception e)
		{
		}
		db.close();
	}

	public static void addRPG(GamesDbHelper dbHelper, RolePlayingGame game)
	{
		addRPG(dbHelper, game.getName(), game.getDescription(), game.getYearPublished(),
				game.getBggId(), game.getThumbnailUrl(), game.getFamilies().toString(),
				game.getMechanics().toString());
	}

	public static boolean updateRPG(GamesDbHelper dbHelper, String oldGameName, RolePlayingGame rpg)
	{
		return updateRPG(dbHelper, oldGameName, rpg.getName(), rpg.getDescription(), rpg.getYearPublished(),
				rpg.getBggId(), rpg.getThumbnailUrl(), rpg.getFamilies().toString(),
				rpg.getMechanics().toString());
	}

	public static boolean updateRPG(GamesDbHelper dbHelper, String oldGameName, String gameName, String description, int yearPublished,
	                             int bggid, String thumbnailUrl, String families, String mechanics)
	{
		ContentValues values = new ContentValues();
		values.put(RPGEntry.NAME, gameName);
		if (description != null) values.put(RPGEntry.DESCRIPTION, description);
		if (yearPublished > 0) values.put(RPGEntry.YEAR_PUBLISHED, yearPublished);
		if (bggid > 0) values.put(RPGEntry.BGG_ID, bggid);
		if (thumbnailUrl != null) values.put(RPGEntry.THUMBNAIL, thumbnailUrl);
		if (families != null) values.put(RPGEntry.FAMILIES, families);
		if (mechanics != null) values.put(RPGEntry.MECHANICS, mechanics);

		// Make sure the game doesn't already exist
		Cursor newGameCursor = dbHelper.getReadableDatabase().query(RPGEntry.TABLE_NAME,
				new String[]{RPGEntry.NAME},
				RPGEntry.NAME + " = ?",
				new String[]{gameName},
				null,
				null,
				null);
		if (oldGameName.equalsIgnoreCase(gameName) || !newGameCursor.moveToNext())
		{
			// Update game plays
			Cursor gamePlayCursor = dbHelper.getReadableDatabase().query(GamePlayEntry.TABLE_NAME,
					new String[]{GamePlayEntry._ID},
					GamePlayEntry.GAME + " = ?",
					new String[]{oldGameName},
					null,
					null,
					null);

			while (gamePlayCursor.moveToNext())
			{
				long gamePlayId = gamePlayCursor.getLong(0);
				RPGPlayData playData = getGamePlay(dbHelper, gamePlayId);
				playData.setGame(new RolePlayingGame(gameName));
				updateGamePlay(dbHelper, gamePlayId, playData);
			}
			gamePlayCursor.close();

			// Update game data
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			try
			{
				db.update(RPGEntry.TABLE_NAME,
						values,
						RPGEntry.NAME + " = ?",
						new String[]{oldGameName});
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
			db.close();
			return true;
		}
		else return false;
	}


	public static void deleteRPG(GamesDbHelper dbHelper, RolePlayingGame game)
	{
		Cursor gamePlayCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry._ID},
				GamePlayEntry.GAME + " = ?",
				new String[]{game.getName()},
				null,
				null,
				null);
		while (gamePlayCursor.moveToNext()) deleteGamePlay(dbHelper, gamePlayCursor.getLong(0));

		dbHelper.getWritableDatabase().delete(RPGEntry.TABLE_NAME,
			RPGEntry.NAME + " = ?",
			new String[]{game.getName()});

		gamePlayCursor.close();
	}

	public static void deleteGamePlay(GamesDbHelper dbHelper, long gamePlayId)
	{
		deleteImages(dbHelper, gamePlayId);
		deletePlayers(dbHelper, gamePlayId);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + GamePlayEntry.TABLE_NAME + " WHERE " + GamePlayEntry._ID + " = " + gamePlayId + ";");
		db.close();
	}

	// TODO Add extras
	public static RolePlayingGame getRPG(GamesDbHelper dbHelper, String name)
	{
		String[] columns = new String[]{
				RPGEntry.NAME,
				RPGEntry.YEAR_PUBLISHED,
				RPGEntry.DESCRIPTION,
				RPGEntry.THUMBNAIL,
				RPGEntry.MECHANICS,
				RPGEntry.FAMILIES,
		};
		Cursor gameCursor = dbHelper.getReadableDatabase()
				.query(RPGEntry.TABLE_NAME,
						columns,
						RPGEntry.NAME + " = ?",
						new String[]{name},
						null,
						null,
						null);

		if (gameCursor.moveToFirst())
		{
			RolePlayingGame game = new RolePlayingGame(gameCursor.getString(0));
			game.setYearPublished(gameCursor.getInt(1));
			game.setDescription(gameCursor.getString(2));
			game.setThumbnailUrl(gameCursor.getString(3));

			addMechanics(game, gameCursor.getString(4));
			addFamilies(game, gameCursor.getString(5));

			gameCursor.close();
			return game;
		}
		else return null;
	}

	private static void addMechanics(RolePlayingGame game, String mechanicsString)
	{
		addExtra(game, mechanicsString, RolePlayingGame.MECHANIC);
	}

	private static void addFamilies(RolePlayingGame game, String familiesString)
	{
		addExtra(game, familiesString, RolePlayingGame.FAMILY);
	}

	private static void addExtra(RolePlayingGame game, String extraString, int type)
	{
		if (!TextUtils.isEmpty(extraString) && !extraString.equalsIgnoreCase("[]"))
		{
			String[] extras = extraString.replace("[", "").replace("]", "").split(",");
			String toAdd = "";
			for (String extra : extras)
			{
				int index = extra.lastIndexOf(" ");
				if (index != -1 && NumberUtils.isParsable(extra.substring(index + 1)))
				{
					switch (type)
					{
						case RolePlayingGame.MECHANIC:
							game.addMechanic(new RPGMechanic(toAdd + extra.substring(0, index).trim(),
							                                 Integer.parseInt(extra.substring(index + 1))));
							break;
						case RolePlayingGame.FAMILY:
							game.addFamily(new RPGFamily(toAdd + extra.substring(0, index).trim(),
							                             Integer.parseInt(extra.substring(index + 1))));
							break;
					}
					toAdd = "";
				}
				else toAdd += extra.trim() + ", ";
			}
		}
	}


	public static List<String> getAllGames(GamesDbHelper dbHelper)
	{
		List<String> games = new ArrayList<>();
		Cursor gameCursor = dbHelper.getReadableDatabase().query(
				RPGEntry.TABLE_NAME,
				new String[]{RPGEntry.NAME},
				null,
				null,
				null,
				null,
				null);
		while (gameCursor.moveToNext()) games.add(gameCursor.getString(0));
		StringUtilities.sortList(games);
		gameCursor.close();
		return games;
	}

	public static List<String> getAllPlayedGames(GamesDbHelper dbHelper)
	{
		List<String> games = new ArrayList<>();
		Cursor gameCursor = dbHelper.getReadableDatabase().query(
				true,
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry.GAME},
				null,
				null,
				null,
				null,
				null,
				null);
		while (gameCursor.moveToNext()) games.add(gameCursor.getString(0));
		StringUtilities.sortList(games);
		gameCursor.close();
		return games;
	}


	// Game play data

	public static void addGamePlay(GamesDbHelper dbHelper, RPGPlayData gamePlayData)
	{
		addGamePlay(dbHelper, gamePlayData.getGame().getName(),
					gamePlayData.getTimePlayed(), gamePlayData.getDate().rawDate(), gamePlayData.getNotes(),
					gamePlayData.getLocation(), gamePlayData.isCountForStats(), gamePlayData.getImages(),
				    gamePlayData.getOtherPlayers().values(), gamePlayData.getBggPlayId());
	}

	public static void addGamePlay(GamesDbHelper dbHelper, String game,
	                               int timePlayed, String date, String notes, String location, boolean countForStats,
	                               List<String> images, Collection<GamePlayerData> gamePlayerData, String bggPlayId)
	{
		ContentValues values = new ContentValues();
		values.put(GamePlayEntry.GAME, game);
		values.put(GamePlayEntry.TIME_PLAYED, timePlayed);
		if (date != null) values.put(GamePlayEntry.DATE, date);
		if (notes != null) values.put(GamePlayEntry.NOTES, notes);
		if (location != null) values.put(GamePlayEntry.LOCATION, location);
		if (bggPlayId != null) values.put(GamePlayEntry.BGG_PLAY_ID, bggPlayId);
		values.put(GamePlayEntry.COUNT_FOR_STATS, countForStats ? "y" : "n");

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.insertOrThrow(GamePlayEntry.TABLE_NAME, null, values);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		db.close();

		Cursor gamePlayCursor = dbHelper.getReadableDatabase()
				.query(GamePlayEntry.TABLE_NAME,
						new String[]{GamePlayEntry._ID},
						GamePlayEntry.GAME + " = ?",
						new String[]{game},
						null,
						null,
						GamePlayEntry._ID + " DESC");
		gamePlayCursor.moveToFirst();
		long id = gamePlayCursor.getLong(0);
		gamePlayCursor.close();
		addImages(dbHelper, id, images);
		addPlayers(dbHelper, id, gamePlayerData);
	}

	public static void updateGamePlay(GamesDbHelper dbHelper, long gamePlayId, RPGPlayData gamePlayData)
	{
		updateGamePlay(dbHelper, gamePlayId, gamePlayData.getGame().getName(),
				gamePlayData.getTimePlayed(), gamePlayData.getDate().rawDate(),
				gamePlayData.getNotes(), gamePlayData.getLocation(), gamePlayData.isCountForStats(),
				gamePlayData.getImages(), gamePlayData.getOtherPlayers().values());
	}

	public static void updateGamePlay(GamesDbHelper dbHelper, long gamePlayId, String game,
	                                  int timePlayed, String date, String notes, String location, boolean countForStats,
	                                  List<String> images, Collection<GamePlayerData> gamePlayerData)
	{
		ContentValues values = new ContentValues();
		values.put(GamePlayEntry.GAME, game);
		values.put(GamePlayEntry.TIME_PLAYED, timePlayed);
		if (date != null) values.put(GamePlayEntry.DATE, date);
		if (notes != null) values.put(GamePlayEntry.NOTES, notes);
		if (location != null) values.put(GamePlayEntry.LOCATION, location);
		values.put(GamePlayEntry.COUNT_FOR_STATS, countForStats ? "y" : "n");

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.update(GamePlayEntry.TABLE_NAME,
					values,
					GamePlayEntry._ID + " = ?",
					new String[]{gamePlayId + ""});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		db.close();

		deleteImages(dbHelper, gamePlayId);
		addImages(dbHelper, gamePlayId, images);
		deletePlayers(dbHelper, gamePlayId);
		addPlayers(dbHelper, gamePlayId, gamePlayerData);
	}

	public static RPGPlayData getGamePlay(GamesDbHelper dbHelper, long gamePlayId)
	{
		String[] columns = new String[]{
				GamePlayEntry.GAME,
				GamePlayEntry.TIME_PLAYED,
				GamePlayEntry.DATE,
				GamePlayEntry.NOTES,
				GamePlayEntry.LOCATION,
				GamePlayEntry.COUNT_FOR_STATS
		};
		Cursor playCursor = dbHelper.getReadableDatabase()
				.query(GamePlayEntry.TABLE_NAME,
						columns,
						GamePlayEntry._ID + " = ?",
						new String[]{gamePlayId + ""},
						null,
						null,
						null);

		playCursor.moveToFirst();

		RPGPlayData gamePlayData = new RPGPlayData(getRPG(dbHelper, playCursor.getString(0)),
				playCursor.getInt(1),
				new Date(playCursor.getString(2)),
				playCursor.getString(3),
				gamePlayId);
		gamePlayData.setLocation(playCursor.getString(4));
		gamePlayData.setCountForStats(playCursor.getString(5).equalsIgnoreCase("y"));
		playCursor.close();

		Cursor playersCursor = dbHelper.getReadableDatabase()
				.query(PlayerEntry.TABLE_NAME,
						new String[]{
								PlayerEntry.NAME,
						},
						PlayerEntry.GAME_PLAY_ID + " = ?",
						new String[]{gamePlayId + ""},
						null,
						null,
						PlayerEntry.NAME + " ASC");
		while (playersCursor.moveToNext())
		{
			GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0));
			gamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
		}
		playersCursor.close();
		return gamePlayData;
	}

	public static Map<Long, RPGPlayData> getGamePlay(GamesDbHelper dbHelper, String from, String to)
	{
		Map<Long, RPGPlayData> gamePlayDataMap = new TreeMap<>();
		String[] columns = new String[]{
				GamePlayEntry.GAME,
				GamePlayEntry.TIME_PLAYED,
				GamePlayEntry.DATE,
				GamePlayEntry.NOTES,
				GamePlayEntry.LOCATION,
				GamePlayEntry._ID
		};
		Cursor playCursor = dbHelper.getReadableDatabase()
				.query(GamePlayEntry.TABLE_NAME,
						columns,
						GamePlayEntry.DATE + " BETWEEN ? AND ?",
						new String[]{from, to},
						null,
						null,
						null);

		while (playCursor.moveToNext())
		{
			long gamePlayId = playCursor.getLong(5);
			RPGPlayData gamePlayData = new RPGPlayData(getRPG(dbHelper, playCursor.getString(0)),
					playCursor.getInt(1),
					new Date(playCursor.getString(2)),
					playCursor.getString(3),
					gamePlayId);
			gamePlayData.setLocation(playCursor.getString(4));

			Cursor playersCursor = dbHelper.getReadableDatabase()
					.query(PlayerEntry.TABLE_NAME,
							new String[]{
									PlayerEntry.NAME,
							},
							PlayerEntry.GAME_PLAY_ID + " = ?",
							new String[]{gamePlayId + ""},
							null,
							null,
							PlayerEntry.NAME + " ASC");
			while (playersCursor.moveToNext())
			{
				GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0));
				gamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
			}
			playersCursor.close();

			gamePlayDataMap.put(gamePlayId, gamePlayData);
		}
		playCursor.close();

		return gamePlayDataMap;
	}



	public static Map<Long, Date> getGamePlayDates(GamesDbHelper dbHelper, String from, String to)
	{
		Map<Long, Date> gamePlayDatesMap = new TreeMap<>();
		String[] columns = new String[]{
				GamePlayEntry.DATE,
				GamePlayEntry._ID
		};
		Cursor playCursor = dbHelper.getReadableDatabase()
				.query(GamePlayEntry.TABLE_NAME,
						columns,
						GamePlayEntry.DATE + " BETWEEN ? AND ?",
						new String[]{from, to},
						null,
						null,
						null);

		while (playCursor.moveToNext())
		{
			Date date = new Date(playCursor.getString(0));
			long gamePlayId = playCursor.getLong(1);
			gamePlayDatesMap.put(gamePlayId, date);
		}
		playCursor.close();

		return gamePlayDatesMap;
	}

	public static void addImages(GamesDbHelper dbHelper, long gamePlayId, List<String> images)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for(String image : images)
		{
			ContentValues values = new ContentValues();
			values.put(ImageEntry.GAME_PLAY_ID, gamePlayId);
			values.put(ImageEntry.FILE_LOCATION, image);
			try
			{
				db.insertOrThrow(ImageEntry.TABLE_NAME, null, values);
			}
			catch (Exception e)
			{
			}
		}
		db.close();
	}

	public static void addPlayers(GamesDbHelper dbHelper, long gamePlayId, Collection<GamePlayerData> gamePlayerData)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for(GamePlayerData data : gamePlayerData)
		{
			ContentValues values = new ContentValues();
			values.put(PlayerEntry.GAME_PLAY_ID, gamePlayId);
			values.put(PlayerEntry.NAME, data.getPlayerName());
			try
			{
				db.insertOrThrow(PlayerEntry.TABLE_NAME, null, values);
			}
			catch (Exception e)
			{
			}
		}
		db.close();
	}

	public static void deleteImages(GamesDbHelper dbHelper, long gamePlayId)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + ImageEntry.TABLE_NAME + " WHERE " + ImageEntry.GAME_PLAY_ID + " = " + gamePlayId + ";");
		db.close();
	}

	public static void deletePlayers(GamesDbHelper dbHelper, long gamePlayId)
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + PlayerEntry.TABLE_NAME + " WHERE " + PlayerEntry.GAME_PLAY_ID + " = " + gamePlayId + ";");
		db.close();
	}

	public static List<String> getImages(GamesDbHelper dbHelper, long gamePlayId)
	{
		List<String> images = new ArrayList<>();
		Cursor cursor = dbHelper.getReadableDatabase()
				.query(ImageEntry.TABLE_NAME,
						new String[]{ImageEntry.FILE_LOCATION},
						ImageEntry.GAME_PLAY_ID + " = ?",
						new String[]{gamePlayId + ""},
						null,
						null,
						null);
		while (cursor.moveToNext()) images.add(cursor.getString(0));
		cursor.close();
		return images;
	}

	public static List<String> getAllPlayers(GamesDbHelper dbHelper)
	{
		Set<String> players = new TreeSet<>();

		Cursor playersCursor = dbHelper.getReadableDatabase()
				.query(true,
						PlayerEntry.TABLE_NAME,
						new String[]{PlayerEntry.NAME},
						null,
						null,
						null,
						null,
						null,
						null);
		while (playersCursor.moveToNext()) players.add(playersCursor.getString(0));
		playersCursor.close();

		return new ArrayList<>(players);
	}

	public static List<String> getAllPlayers(GamesDbHelper dbHelper, Date since)
	{
		Set<String> players = new TreeSet<>();

		Cursor playersCursor = dbHelper.getReadableDatabase()
				.query(true,
						GamePlayEntry.TABLE_NAME + " INNER JOIN " +
								PlayerEntry.TABLE_NAME + " ON " +
								GamePlayEntry.TABLE_NAME + "." + GamePlayEntry._ID +
								" = " + PlayerEntry.TABLE_NAME + "." + PlayerEntry.GAME_PLAY_ID,
						new String[]{PlayerEntry.NAME},
						GamePlayEntry.TABLE_NAME + "." + GamePlayEntry.DATE + " >= ?",
						new String[]{since.rawDate()},
						null,
						null,
						null,
						null);
		while (playersCursor.moveToNext()) players.add(playersCursor.getString(0));
		playersCursor.close();

		return new ArrayList<>(players);
	}

	public static List<String> getPlayers(GamesDbHelper dbHelper, long gamePlayId)
	{
		Set<String> players = new TreeSet<>();

		Cursor playersCursor = dbHelper.getReadableDatabase()
				.query(true,
						PlayerEntry.TABLE_NAME,
						new String[]{PlayerEntry.NAME},
						PlayerEntry.GAME_PLAY_ID + " = ?",
						new String[]{gamePlayId + ""},
						null,
						null,
						null,
						null);
		while (playersCursor.moveToNext()) players.add(playersCursor.getString(0));
		playersCursor.close();

		return new ArrayList<>(players);
	}

	public static List<String> getAllLocations(GamesDbHelper dbHelper, Date since)
	{
		Set<String> locations = new TreeSet<>();

		Cursor locationCursor = dbHelper.getReadableDatabase()
				.query(true,
						GamePlayEntry.TABLE_NAME,
						new String[]{GamePlayEntry.LOCATION},
						GamePlayEntry.DATE + " >= ?",
						new String[]{since.rawDate()},
						null,
						null,
						null,
						null);
		while (locationCursor.moveToNext())
		{
			String location = locationCursor.getString(0);
			if (location != null && !location.equals("")) locations.add(location);
		}
		locationCursor.close();

		return new ArrayList<>(locations);
	}

	public static String getThumbnailUrl(GamesDbHelper dbHelper, String game)
	{
		String thumbnailUrl = "";
		Cursor thumbnailCursor = dbHelper.getReadableDatabase()
				.query(RPGEntry.TABLE_NAME,
						new String[]{RPGEntry.THUMBNAIL},
						RPGEntry.NAME + " = ?",
						new String[]{game},
						null,
						null,
						null);
		if (thumbnailCursor.moveToNext())
			thumbnailUrl = thumbnailCursor.getString(0);
		thumbnailCursor.close();
		return thumbnailUrl;
	}

	public static Date getDateById(GamesDbHelper dbHelper, long id)
	{
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor dateCursor = db.query(GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry.DATE},
				GamePlayEntry._ID + " = ?",
				new String[]{id + ""},
				null,
				null,
				null);
		Date date = null;
		if (dateCursor.moveToNext()) date = new Date(dateCursor.getString(0));
		dateCursor.close();
		db.close();
		return date;
	}

	public static List<GamePlayData> getGamePlays(GamesDbHelper dbHelper, List<Long> gamePlayIds)
	{
		int batchSize = 100, numberOfIds = gamePlayIds.size();
		List<GamePlayData> gamePlayDataList = new ArrayList<>();
		String inClause = "";

		for (int i=0;i<(int)Math.ceil((double)numberOfIds / batchSize);i++)
		{
			int upperBound = batchSize;
			if (i == (int)Math.ceil((double)numberOfIds / batchSize) - 1
			    && numberOfIds / batchSize != (int)Math.ceil((double)numberOfIds / batchSize)) upperBound = numberOfIds % batchSize;

			inClause = "(";
			String[] inIds = new String[upperBound];

			for (int index = 0; index < upperBound; index++)
			{
				inClause += "?,";
				inIds[index] = gamePlayIds.get(batchSize * i + index) + "";
			}
			inClause = inClause.substring(0, inClause.length() - 1) + ")";

			String[] columns = new String[]{
					GamePlayEntry.GAME,
					GamePlayEntry.TIME_PLAYED,
					GamePlayEntry.DATE,
					GamePlayEntry.NOTES,
					GamePlayEntry.LOCATION,
					GamePlayEntry.COUNT_FOR_STATS
			};
			Cursor playCursor = dbHelper.getReadableDatabase()
			                            .query(GamePlayEntry.TABLE_NAME,
			                                   columns,
			                                   GamePlayEntry._ID + " IN " + inClause,
			                                   inIds,
			                                   null,
			                                   null,
			                                   null);

			int index = 0;
			while (playCursor.moveToNext())
			{
				long gamePlayId = gamePlayIds.get(i * batchSize + index++);
				RPGPlayData gamePlayData = new RPGPlayData(getRPG(dbHelper, playCursor.getString(0)),
				                                           playCursor.getInt(1),
				                                           new Date(playCursor.getString(2)),
				                                           playCursor.getString(3),
				                                           gamePlayId);
				gamePlayData.setLocation(playCursor.getString(4));
				gamePlayData.setCountForStats(playCursor.getString(5).equalsIgnoreCase("Y"));

				Cursor playersCursor = dbHelper.getReadableDatabase()
				                               .query(PlayerEntry.TABLE_NAME,
				                                      new String[]{PlayerEntry.NAME},
				                                      PlayerEntry.GAME_PLAY_ID + " = ?",
				                                      new String[]{gamePlayId + ""},
				                                      null,
				                                      null,
				                                      PlayerEntry.NAME + " ASC");
				while (playersCursor.moveToNext())
				{
					GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0));
					gamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
				}

				playersCursor.close();

				gamePlayDataList.add(gamePlayData);
			}
			playCursor.close();
		}

		return gamePlayDataList;
	}

	public static long getGameId(GamesDbHelper dbHelper, String gameName)
	{
		long id = -10000l;
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				RPGEntry.TABLE_NAME,
				new String[]{RPGEntry.BGG_ID},
				RPGEntry.NAME + " = ?",
				new String[]{gameName},
				null,
				null,
				null);
		if (idCursor.moveToNext()) id = idCursor.getLong(0);
		idCursor.close();
		return id;
	}

	public static List<Long> getGameIds(GamesDbHelper dbHelper)
	{
		List<Long> idList = new ArrayList<>();
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				RPGEntry.TABLE_NAME,
				new String[]{RPGEntry.BGG_ID},
				null,
				null,
				null,
				null,
				null);
		while (idCursor.moveToNext()) idList.add(idCursor.getLong(0));
		idCursor.close();
		return idList;
	}

	public static boolean gamePlayExists(GamesDbHelper dbHelper, String bggPlayId)
	{
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry._ID},
				GamePlayEntry.BGG_PLAY_ID + " = ?",
				new String[] {bggPlayId},
				null,
				null,
				null);
		if (idCursor.moveToNext())
		{
			idCursor.close();
			return true;
		}
		idCursor.close();
		return false;
	}

	public static String getBggId(GamesDbHelper dbHelper, String gameName)
	{
		String bggid = null;
		Cursor cursor = dbHelper.getReadableDatabase()
		                        .query(RPGEntry.TABLE_NAME,
		                               new String[]{RPGEntry.BGG_ID},
		                               RPGEntry.NAME + " = ?",
		                               new String[]{gameName},
		                               null,
		                               null,
		                               null);
		if (cursor.moveToNext()) bggid = cursor.getString(0);
		cursor.close();
		return bggid;
	}
}
