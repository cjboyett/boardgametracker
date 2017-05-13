package com.cjboyett.boardgamestats.data.games.board;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.HotnessXmlParser;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGameCategory;
import com.cjboyett.boardgamestats.model.games.board.BoardGameFamily;
import com.cjboyett.boardgamestats.model.games.board.BoardGameMechanic;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePublisher;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import timber.log.Timber;

import static com.cjboyett.boardgamestats.data.games.board.BoardGameContract.BoardGameEntry;
import static com.cjboyett.boardgamestats.data.games.board.BoardGameContract.GamePlayEntry;
import static com.cjboyett.boardgamestats.data.games.board.BoardGameContract.ImageEntry;
import static com.cjboyett.boardgamestats.data.games.board.BoardGameContract.PlayerEntry;

/**
 * Created by Casey on 3/7/2016.
 */
public class BoardGameDbUtility {
	public static void addBoardGame(GamesDbHelper dbHelper, String gameName, String description, int yearPublished,
									int bggid, String thumbnailUrl, String expansions, String categories,
									String families,
									String mechanics, String gameType) {
		ContentValues values = new ContentValues();
		values.put(BoardGameEntry.NAME, gameName);
		if (description != null) values.put(BoardGameEntry.DESCRIPTION, description);
		if (yearPublished > 0) values.put(BoardGameEntry.YEAR_PUBLISHED, yearPublished);
		if (bggid > 0) values.put(BoardGameEntry.BGG_ID, bggid);
		if (thumbnailUrl != null) values.put(BoardGameEntry.THUMBNAIL, thumbnailUrl);
		if (expansions != null) values.put(BoardGameEntry.EXPANSIONS, expansions);
		if (categories != null) values.put(BoardGameEntry.CATEGORIES, categories);
		if (families != null) values.put(BoardGameEntry.FAMILIES, families);
		if (mechanics != null) values.put(BoardGameEntry.MECHANICS, mechanics);
		values.put(BoardGameEntry.GAMETYPE, gameType);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertOrThrow(BoardGameEntry.TABLE_NAME, null, values);
		} catch (Exception e) {
		}
		db.close();
	}

	public static void addBoardGame(GamesDbHelper dbHelper, BoardGame game) {
		String expansions = "";
		for (BoardGame expansion : game.getExpansions())
			expansions += expansion.getName() + "|" + expansion.getBggId() + "|";
		if (expansions.endsWith("|")) expansions = expansions.substring(0, expansions.length() - 1);
		addBoardGame(dbHelper,
					 game.getName(),
					 game.getDescription(),
					 game.getYearPublished(),
					 game.getBggId(),
					 game.getThumbnailUrl(),
					 expansions,
					 game.getCategories().toString(),
					 game.getFamilies().toString(),
					 game.getMechanics().toString(),
					 "boardgame");
	}

	public static void deleteBoardGame(GamesDbHelper dbHelper, BoardGame game) {
		Cursor gamePlayCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry._ID},
				GamePlayEntry.GAME + " = ?",
				new String[]{game.getName()},
				null,
				null,
				null);
		while (gamePlayCursor.moveToNext()) deleteGamePlay(dbHelper, gamePlayCursor.getLong(0));

		dbHelper.getWritableDatabase().delete(BoardGameEntry.TABLE_NAME,
											  BoardGameEntry.NAME + " = ?",
											  new String[]{game.getName()});

		gamePlayCursor.close();
	}

	public static void deleteGamePlay(GamesDbHelper dbHelper, long gamePlayId) {
		deleteImages(dbHelper, gamePlayId);
		deletePlayers(dbHelper, gamePlayId);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL(
				"DELETE FROM " + GamePlayEntry.TABLE_NAME + " WHERE " + GamePlayEntry._ID + " = " + gamePlayId + ";");
		db.close();
	}

	public static boolean updateBoardGame(GamesDbHelper dbHelper, String oldGameName, BoardGame boardGame) {
		String expansions = "";
		for (BoardGame expansion : boardGame.getExpansions())
			expansions += expansion.getName() + "|" + expansion.getBggId() + "|";
		if (expansions.endsWith("|")) expansions = expansions.substring(0, expansions.length() - 1);
		return updateBoardGame(dbHelper,
							   oldGameName,
							   boardGame.getName(),
							   boardGame.getDescription(),
							   boardGame.getYearPublished(),
							   boardGame.getBggId(),
							   boardGame.getThumbnailUrl(),
							   expansions,
							   boardGame.getCategories().toString(),
							   boardGame.getFamilies().toString(),
							   boardGame.getMechanics().toString(),
							   "boardgame");
	}

	public static boolean updateBoardGame(GamesDbHelper dbHelper, String oldGameName, String gameName,
										  String description, int yearPublished,
										  int bggid, String thumbnailUrl, String expansions, String categories,
										  String families,
										  String mechanics, String gameType) {
		ContentValues values = new ContentValues();
		values.put(BoardGameEntry.NAME, gameName);
		if (description != null) values.put(BoardGameEntry.DESCRIPTION, description);
		if (yearPublished > 0) values.put(BoardGameEntry.YEAR_PUBLISHED, yearPublished);
		if (bggid > 0) values.put(BoardGameEntry.BGG_ID, bggid);
		if (thumbnailUrl != null) values.put(BoardGameEntry.THUMBNAIL, thumbnailUrl);
		if (expansions != null) values.put(BoardGameEntry.EXPANSIONS, expansions);
		if (categories != null) values.put(BoardGameEntry.CATEGORIES, categories);
		if (families != null) values.put(BoardGameEntry.FAMILIES, families);
		if (mechanics != null) values.put(BoardGameEntry.MECHANICS, mechanics);
		values.put(BoardGameEntry.GAMETYPE, gameType);

		// Make sure the new game doesn't already exist
		Cursor newGameCursor = dbHelper.getReadableDatabase().query(BoardGameEntry.TABLE_NAME,
																	new String[]{BoardGameEntry.NAME},
																	BoardGameEntry.NAME + " = ?",
																	new String[]{gameName},
																	null,
																	null,
																	null);
		if (oldGameName.equalsIgnoreCase(gameName) || !newGameCursor.moveToNext()) {
			// Update game plays
			Cursor gamePlayCursor = dbHelper.getReadableDatabase().query(GamePlayEntry.TABLE_NAME,
																		 new String[]{GamePlayEntry._ID},
																		 GamePlayEntry.GAME + " = ?",
																		 new String[]{oldGameName},
																		 null,
																		 null,
																		 null);

			while (gamePlayCursor.moveToNext()) {
				long gamePlayId = gamePlayCursor.getLong(0);
				BoardGamePlayData playData = getGamePlay(dbHelper, gamePlayId);
				playData.setGame(new BoardGame(gameName));
				updateGamePlay(dbHelper, gamePlayId, playData);
			}
			gamePlayCursor.close();

			// Update game data
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			try {
				db.update(BoardGameEntry.TABLE_NAME,
						  values,
						  BoardGameEntry.NAME + " = ?",
						  new String[]{oldGameName});
			} catch (Exception e) {
				Timber.e(e);
				return false;
			}
			db.close();
			return true;
		} else return false;
	}


	public static BoardGame getBoardGame(GamesDbHelper dbHelper, String name) {
		String[] columns = new String[]{
				BoardGameEntry.NAME,
				BoardGameEntry.YEAR_PUBLISHED,
				BoardGameEntry.DESCRIPTION,
				BoardGameEntry.THUMBNAIL,
				BoardGameEntry.EXPANSIONS,
				BoardGameEntry.MECHANICS,
				BoardGameEntry.CATEGORIES,
				BoardGameEntry.FAMILIES,
				BoardGameEntry.PUBLISHERS,
				BoardGameEntry.BGG_ID
		};
		Cursor gameCursor = dbHelper.getReadableDatabase()
									.query(BoardGameEntry.TABLE_NAME,
										   columns,
										   BoardGameEntry.NAME + " = ?",
										   new String[]{name},
										   null,
										   null,
										   null);

		if (gameCursor.moveToFirst()) {
			BoardGame game = new BoardGame(gameCursor.getString(0));
			game.setYearPublished(gameCursor.getInt(1));
			game.setDescription(gameCursor.getString(2));
			game.setThumbnailUrl(gameCursor.getString(3));
			game.setBggId(gameCursor.getInt(9));
			String expansionsString = gameCursor.getString(4);
			if (expansionsString != null && !expansionsString.equals("")) {
				String[] expansions = expansionsString.split("\\|");
				for (int i = 0; i < expansions.length; i += 2)
					game.addExpansion(new BoardGame(expansions[i]));
			}

			addMechanics(game, gameCursor.getString(5));
			addCategories(game, gameCursor.getString(6));
			addFamilies(game, gameCursor.getString(7));

			gameCursor.close();
			return game;
		} else return null;
	}

	private static void addMechanics(BoardGame game, String mechanicsString) {
		addExtra(game, mechanicsString, BoardGame.MECHANIC);
	}

	private static void addCategories(BoardGame game, String categoriesString) {
		addExtra(game, categoriesString, BoardGame.CATEGORY);
	}

	private static void addFamilies(BoardGame game, String familiesString) {
		addExtra(game, familiesString, BoardGame.FAMILY);
	}

	private static void addExtra(BoardGame game, String extraString, int type) {
		if (!TextUtils.isEmpty(extraString) && !extraString.equalsIgnoreCase("[]")) {
			String[] extras = extraString.replace("[", "").replace("]", "").split(",");
			String toAdd = "";
			for (String extra : extras) {
				int index = extra.lastIndexOf(" ");
				if (index != -1 && NumberUtils.isParsable(extra.substring(index + 1))) {
					switch (type) {
						case BoardGame.MECHANIC:
							game.addMechanic(new BoardGameMechanic(toAdd + extra.substring(0, index).trim(),
																   Integer.parseInt(extra.substring(index + 1))));
							break;
						case BoardGame.CATEGORY:
							game.addCategory(new BoardGameCategory(toAdd + extra.substring(0, index).trim(),
																   Integer.parseInt(extra.substring(index + 1))));
							break;
						case BoardGame.FAMILY:
							game.addFamily(new BoardGameFamily(toAdd + extra.substring(0, index).trim(),
															   Integer.parseInt(extra.substring(index + 1))));
							break;
						case BoardGame.PUBLISHER:
							game.addPublisher(new BoardGamePublisher(toAdd + extra.substring(0, index).trim(),
																	 Integer.parseInt(extra.substring(index + 1))));
							break;
					}
					toAdd = "";
				} else toAdd += extra.trim() + ", ";
			}
		}
	}

	public static List<String> getAllGames(GamesDbHelper dbHelper) {
		List<String> games = new ArrayList<>();
		Cursor gameCursor = dbHelper.getReadableDatabase().query(
				BoardGameEntry.TABLE_NAME,
				new String[]{BoardGameEntry.NAME},
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

	public static List<String> getAllPlayedGames(GamesDbHelper dbHelper) {
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

	public static void addGamePlay(GamesDbHelper dbHelper, BoardGamePlayData boardGamePlayData) {
		addGamePlay(dbHelper,
					boardGamePlayData.getGame().getName(),
					boardGamePlayData.getScore(),
					boardGamePlayData.isWin() ? 'y' : 'n',
					boardGamePlayData.getTimePlayed(),
					boardGamePlayData.getDate().rawDate(),
					boardGamePlayData.getNotes(),
					boardGamePlayData.getLocation(),
					boardGamePlayData.isCountForStats(),
					boardGamePlayData.getImages(),
					boardGamePlayData.getOtherPlayers().values(),
					boardGamePlayData.getBggPlayId());
	}

	public static void addGamePlay(GamesDbHelper dbHelper, String game, double score, char win,
								   int timePlayed, String date, String notes, String location, boolean countForStats,
								   List<String> images, Collection<GamePlayerData> gamePlayerData, String bggPlayId) {
		ContentValues values = new ContentValues();
		values.put(GamePlayEntry.GAME, game);
		values.put(GamePlayEntry.SCORE, score);
		values.put(GamePlayEntry.WIN, win + "");
		values.put(GamePlayEntry.TIME_PLAYED, timePlayed);
		if (date != null) values.put(GamePlayEntry.DATE, date);
		if (notes != null) values.put(GamePlayEntry.NOTES, notes);
		if (location != null) values.put(GamePlayEntry.LOCATION, location);
		values.put(GamePlayEntry.COUNT_FOR_STATS, countForStats ? "y" : "n");
		if (bggPlayId != null) values.put(GamePlayEntry.BGG_PLAY_ID, bggPlayId);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertOrThrow(GamePlayEntry.TABLE_NAME, null, values);
		} catch (Exception e) {
			Timber.e(e);
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

	public static boolean addGamePlayIfNotPresent(GamesDbHelper dbHelper, BoardGamePlayData boardGamePlayData) {
		if (!containsGamePlayData(dbHelper, boardGamePlayData)) {
			addGamePlay(dbHelper, boardGamePlayData);
			return true;
		}
		return false;
	}

	private static boolean containsGamePlayData(GamesDbHelper dbHelper, BoardGamePlayData boardGamePlayData) {
		Cursor playCursor = dbHelper.getReadableDatabase()
									.query(GamePlayEntry.TABLE_NAME,
										   new String[]{GamePlayEntry._ID},
										   GamePlayEntry.GAME + " = ? AND " +
												   GamePlayEntry.TIME_PLAYED + " = ? AND " +
												   GamePlayEntry.DATE + " = ? AND " +
												   GamePlayEntry.NOTES + " = ? AND " +
												   GamePlayEntry.LOCATION + " = ? AND " +
												   GamePlayEntry.COUNT_FOR_STATS + " = ?",
										   new String[]{boardGamePlayData.getGame().getName(),
														boardGamePlayData.getTimePlayed() + "",
														boardGamePlayData.getDate().rawDate(),
														boardGamePlayData.getNotes(),
														boardGamePlayData.getLocation(),
														boardGamePlayData.isCountForStats() ? "y" : "n"},
										   null,
										   null,
										   null);

		if (playCursor.moveToFirst()) {
			while (playCursor.moveToNext()) {
				BoardGamePlayData gamePlayData = getGamePlay(dbHelper, playCursor.getLong(0));
				if (boardGamePlayData.equals(gamePlayData)) {
					playCursor.close();
					return true;
				}
			}
		}
		playCursor.close();

		return false;

	}

	public static void updateGamePlay(GamesDbHelper dbHelper, long gamePlayId, BoardGamePlayData boardGamePlayData) {
		updateGamePlay(dbHelper,
					   gamePlayId,
					   boardGamePlayData.getGame().getName(),
					   boardGamePlayData.getScore(),
					   boardGamePlayData.isWin() ? 'y' : 'n',
					   boardGamePlayData.getTimePlayed(),
					   boardGamePlayData.getDate().rawDate(),
					   boardGamePlayData.getNotes(),
					   boardGamePlayData.getLocation(),
					   boardGamePlayData.isCountForStats(),
					   boardGamePlayData.getImages(),
					   boardGamePlayData.getOtherPlayers().values());
	}

	public static void updateGamePlay(GamesDbHelper dbHelper, long gamePlayId, String game, double score, char win,
									  int timePlayed, String date, String notes, String location, boolean countForStats,
									  List<String> images, Collection<GamePlayerData> gamePlayerData) {
		ContentValues values = new ContentValues();
		values.put(GamePlayEntry.GAME, game);
		values.put(GamePlayEntry.SCORE, score);
		values.put(GamePlayEntry.WIN, win + "");
		values.put(GamePlayEntry.TIME_PLAYED, timePlayed);
		if (date != null) values.put(GamePlayEntry.DATE, date);
		if (notes != null) values.put(GamePlayEntry.NOTES, notes);
		if (location != null) values.put(GamePlayEntry.LOCATION, location);
		values.put(GamePlayEntry.COUNT_FOR_STATS, countForStats ? "y" : "n");

		Timber.d(!countForStats + " ");

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.update(GamePlayEntry.TABLE_NAME,
					  values,
					  GamePlayEntry._ID + " = ?",
					  new String[]{gamePlayId + ""});
		} catch (Exception e) {
			Timber.e(e);
		}
		db.close();

		deleteImages(dbHelper, gamePlayId);
		addImages(dbHelper, gamePlayId, images);
		deletePlayers(dbHelper, gamePlayId);
		addPlayers(dbHelper, gamePlayId, gamePlayerData);
	}

	public static BoardGamePlayData getGamePlay(GamesDbHelper dbHelper, long gamePlayId) {
		String[] columns = new String[]{
				GamePlayEntry.GAME,
				GamePlayEntry.SCORE,
				GamePlayEntry.WIN,
				GamePlayEntry.TIME_PLAYED,
				GamePlayEntry.DATE,
				GamePlayEntry.EXPANSIONS,
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

		BoardGamePlayData boardGamePlayData = new BoardGamePlayData(getBoardGame(dbHelper, playCursor.getString(0)),
																	playCursor.getDouble(1),
																	playCursor.getString(2).equals("y"),
																	playCursor.getInt(3),
																	new Date(playCursor.getString(4)),
																	playCursor.getString(6),
																	gamePlayId);
		boardGamePlayData.setLocation(playCursor.getString(7));
		boardGamePlayData.setCountForStats(playCursor.getString(8).equalsIgnoreCase("y"));
		playCursor.close();

		Cursor playersCursor = dbHelper.getReadableDatabase()
									   .query(PlayerEntry.TABLE_NAME,
											  new String[]{
													  PlayerEntry.NAME,
													  PlayerEntry.SCORE,
													  PlayerEntry.WIN
											  },
											  PlayerEntry.GAME_PLAY_ID + " = ?",
											  new String[]{gamePlayId + ""},
											  null,
											  null,
											  PlayerEntry.NAME + " ASC");
		while (playersCursor.moveToNext()) {
			GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0),
															   playersCursor.getDouble(1),
															   playersCursor.getString(2).equals("y"));
			boardGamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
		}
		playersCursor.close();
		return boardGamePlayData;
	}

	public static Map<Long, BoardGamePlayData> getGamePlay(GamesDbHelper dbHelper, String from, String to) {
		Map<Long, BoardGamePlayData> gamePlayDataMap = new TreeMap<>();
		String[] columns = new String[]{
				GamePlayEntry.GAME,
				GamePlayEntry.SCORE,
				GamePlayEntry.WIN,
				GamePlayEntry.TIME_PLAYED,
				GamePlayEntry.DATE,
				GamePlayEntry.EXPANSIONS,
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

		while (playCursor.moveToNext()) {
			long gamePlayId = playCursor.getLong(8);
			BoardGamePlayData boardGamePlayData = new BoardGamePlayData(getBoardGame(dbHelper, playCursor.getString(0)),
																		playCursor.getDouble(1),
																		playCursor.getString(2).equals("y"),
																		playCursor.getInt(3),
																		new Date(playCursor.getString(4)),
																		playCursor.getString(6),
																		gamePlayId);
			boardGamePlayData.setLocation(playCursor.getString(7));

			Cursor playersCursor = dbHelper.getReadableDatabase()
										   .query(PlayerEntry.TABLE_NAME,
												  new String[]{
														  PlayerEntry.NAME,
														  PlayerEntry.SCORE,
														  PlayerEntry.WIN
												  },
												  PlayerEntry.GAME_PLAY_ID + " = ?",
												  new String[]{gamePlayId + ""},
												  null,
												  null,
												  PlayerEntry.NAME + " ASC");
			while (playersCursor.moveToNext()) {
				GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0),
																   playersCursor.getDouble(1),
																   playersCursor.getString(2).equals("y"));
				boardGamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
			}
			playersCursor.close();

			gamePlayDataMap.put(gamePlayId, boardGamePlayData);
		}
		playCursor.close();

		return gamePlayDataMap;
	}

	public static Map<Long, Date> getGamePlayDates(GamesDbHelper dbHelper, String from, String to) {
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

		while (playCursor.moveToNext()) {
			Date date = new Date(playCursor.getString(0));
			long gamePlayId = playCursor.getLong(1);
			gamePlayDatesMap.put(gamePlayId, date);
		}
		playCursor.close();

		return gamePlayDatesMap;
	}

	public static void addImages(GamesDbHelper dbHelper, long gamePlayId, List<String> images) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for (String image : images) {
			ContentValues values = new ContentValues();
			values.put(ImageEntry.GAME_PLAY_ID, gamePlayId);
			values.put(ImageEntry.FILE_LOCATION, image);
			try {
				db.insertOrThrow(ImageEntry.TABLE_NAME, null, values);
			} catch (Exception e) {
			}
		}
		db.close();
	}

	public static void addPlayers(GamesDbHelper dbHelper, long gamePlayId, Collection<GamePlayerData> gamePlayerData) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for (GamePlayerData data : gamePlayerData) {
			ContentValues values = new ContentValues();
			values.put(PlayerEntry.GAME_PLAY_ID, gamePlayId);
			values.put(PlayerEntry.NAME, data.getPlayerName());
			values.put(PlayerEntry.SCORE, data.getScore());
			values.put(PlayerEntry.WIN, data.isWin() ? "y" : "n");
			try {
				db.insertOrThrow(PlayerEntry.TABLE_NAME, null, values);
			} catch (Exception e) {
			}
		}
		db.close();
	}

	public static void deleteImages(GamesDbHelper dbHelper, long gamePlayId) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + ImageEntry.TABLE_NAME + " WHERE " + ImageEntry.GAME_PLAY_ID + " = " + gamePlayId +
						   ";");
		db.close();
	}

	public static void deletePlayers(GamesDbHelper dbHelper, long gamePlayId) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + PlayerEntry.TABLE_NAME + " WHERE " + PlayerEntry.GAME_PLAY_ID + " = " + gamePlayId +
						   ";");
		db.close();
	}

	public static List<String> getImages(GamesDbHelper dbHelper, long gamePlayId) {
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

	public static List<String> getAllPlayers(GamesDbHelper dbHelper) {
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

	public static List<String> getAllPlayers(GamesDbHelper dbHelper, Date since) {
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

	public static List<String> getPlayers(GamesDbHelper dbHelper, long gamePlayId) {
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

	public static List<String> getAllLocations(GamesDbHelper dbHelper) {
		Set<String> locations = new TreeSet<>();

		Cursor locationCursor = dbHelper.getReadableDatabase()
										.query(true,
											   GamePlayEntry.TABLE_NAME,
											   new String[]{GamePlayEntry.LOCATION},
											   null,
											   null,
											   null,
											   null,
											   null,
											   null);
		while (locationCursor.moveToNext()) {
			String location = locationCursor.getString(0);
			if (location != null && !location.equals("")) locations.add(location);
		}
		locationCursor.close();

		return new ArrayList<>(locations);
	}

	public static String getThumbnailUrl(GamesDbHelper dbHelper, String game) {
		String thumbnailUrl = "";
		Cursor thumbnailCursor = dbHelper.getReadableDatabase()
										 .query(BoardGameEntry.TABLE_NAME,
												new String[]{BoardGameEntry.THUMBNAIL},
												BoardGameEntry.NAME + " = ?",
												new String[]{game},
												null,
												null,
												null);
		if (thumbnailCursor.moveToNext())
			thumbnailUrl = thumbnailCursor.getString(0);
		thumbnailCursor.close();
		return thumbnailUrl;
	}

	public static Date getDateById(GamesDbHelper dbHelper, long id) {
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

	public static List<GamePlayData> getGamePlays(GamesDbHelper dbHelper, List<Long> gamePlayIds) {
		int batchSize = 100, numberOfIds = gamePlayIds.size();
		List<GamePlayData> boardGamePlayDataList = new ArrayList<>();
		String inClause = "";

		for (int i = 0; i < (int) Math.ceil((double) numberOfIds / batchSize); i++) {
			int upperBound = batchSize;
			if (i == (int) Math.ceil((double) numberOfIds / batchSize) - 1
					&& numberOfIds / batchSize != (int) Math.ceil((double) numberOfIds / batchSize))
				upperBound = numberOfIds % batchSize;

			inClause = "(";
			String[] inIds = new String[upperBound];

			for (int index = 0; index < upperBound; index++) {
				inClause += "?,";
				inIds[index] = gamePlayIds.get(batchSize * i + index) + "";
			}
			inClause = inClause.substring(0, inClause.length() - 1) + ")";

			String[] columns = new String[]{
					GamePlayEntry.GAME,
					GamePlayEntry.SCORE,
					GamePlayEntry.WIN,
					GamePlayEntry.TIME_PLAYED,
					GamePlayEntry.DATE,
					GamePlayEntry.EXPANSIONS,
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
			while (playCursor.moveToNext()) {
				long gamePlayId = gamePlayIds.get(batchSize * i + index++);
				BoardGamePlayData boardGamePlayData =
						new BoardGamePlayData(getBoardGame(dbHelper, playCursor.getString(0)),
											  playCursor.getDouble(1),
											  playCursor.getString(2).equals("y"),
											  playCursor.getInt(3),
											  new Date(playCursor.getString(4)),
											  playCursor.getString(6),
											  gamePlayId);
				boardGamePlayData.setLocation(playCursor.getString(7));
				boardGamePlayData.setCountForStats(playCursor.getString(8).equalsIgnoreCase("Y"));

				Cursor playersCursor = dbHelper.getReadableDatabase()
											   .query(PlayerEntry.TABLE_NAME,
													  new String[]{
															  PlayerEntry.NAME,
															  PlayerEntry.SCORE,
															  PlayerEntry.WIN},
													  PlayerEntry.GAME_PLAY_ID + " = ?",
													  new String[]{gamePlayId + ""},
													  null,
													  null,
													  PlayerEntry.NAME + " ASC");
				while (playersCursor.moveToNext()) {
					GamePlayerData gamePlayerData =
							new GamePlayerData(playersCursor.getString(0),
											   playersCursor.getDouble(1),
											   playersCursor.getString(2).equals("y"));
					boardGamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
				}
				playersCursor.close();

				boardGamePlayDataList.add(boardGamePlayData);
			}
			playCursor.close();
		}

		return boardGamePlayDataList;
	}

	public static long getGameId(GamesDbHelper dbHelper, String gameName) {
		long id = -10000L;
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				BoardGameEntry.TABLE_NAME,
				new String[]{BoardGameEntry.BGG_ID},
				BoardGameEntry.NAME + " = ?",
				new String[]{gameName},
				null,
				null,
				null);
		if (idCursor.moveToNext()) id = idCursor.getLong(0);
		idCursor.close();
		return id;
	}

	public static List<Long> getGameIds(GamesDbHelper dbHelper) {
		List<Long> idList = new ArrayList<>();
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				BoardGameEntry.TABLE_NAME,
				new String[]{BoardGameEntry.BGG_ID},
				null,
				null,
				null,
				null,
				null);
		while (idCursor.moveToNext()) idList.add(idCursor.getLong(0));
		idCursor.close();
		return idList;
	}

	public static boolean gamePlayExists(GamesDbHelper dbHelper, String bggPlayId) {
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry._ID},
				GamePlayEntry.BGG_PLAY_ID + " = ?",
				new String[]{bggPlayId},
				null,
				null,
				null);
		if (idCursor.moveToNext()) {
			idCursor.close();
			return true;
		}
		idCursor.close();
		return false;
	}

	// Hotness table

	public static void clearHotnessTable(GamesDbHelper dbHelper) {
		dbHelper.getWritableDatabase().delete(BoardGameContract.HotnessEntry.TABLE_NAME, null, null);
	}

	public static void populateHotnessTable(GamesDbHelper dbHelper, List<HotnessXmlParser.Item> items) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for (HotnessXmlParser.Item item : items) {
			ContentValues values = new ContentValues();
			values.put(BoardGameContract.HotnessEntry.NAME, item.name);
			values.put(BoardGameContract.HotnessEntry.BGGID, item.id + "");
			values.put(BoardGameContract.HotnessEntry.RANK, item.rank);
			values.put(BoardGameContract.HotnessEntry.THUMBNAIL, item.thumbnailUrl);
			db.insertOrThrow(BoardGameContract.HotnessEntry.TABLE_NAME, null, values);
		}
		db.close();
	}

	public static List<HotnessXmlParser.Item> getHotnessItems(GamesDbHelper dbHelper) {
		List<HotnessXmlParser.Item> items = new ArrayList<>();

		Cursor hotnessCursor = dbHelper.getReadableDatabase().query(
				BoardGameContract.HotnessEntry.TABLE_NAME,
				new String[]{BoardGameContract.HotnessEntry.NAME,
							 BoardGameContract.HotnessEntry.BGGID,
							 BoardGameContract.HotnessEntry.RANK,
							 BoardGameContract.HotnessEntry.THUMBNAIL},
				null,
				null,
				null,
				null,
				null
		);

		while (hotnessCursor.moveToNext()) {
			items.add(new HotnessXmlParser.Item(hotnessCursor.getString(0),
												hotnessCursor.getInt(1),
												hotnessCursor.getInt(2),
												hotnessCursor.getString(3)));
		}
		hotnessCursor.close();

		return items;
	}

	public static String getBggId(GamesDbHelper dbHelper, String gameName) {
		String bggid = null;
		Cursor cursor = dbHelper.getReadableDatabase()
								.query(BoardGameEntry.TABLE_NAME,
									   new String[]{BoardGameEntry.BGG_ID},
									   BoardGameEntry.NAME + " = ?",
									   new String[]{gameName},
									   null,
									   null,
									   null);
		if (cursor.moveToNext()) bggid = cursor.getString(0);
		cursor.close();
		return bggid;
	}
}
