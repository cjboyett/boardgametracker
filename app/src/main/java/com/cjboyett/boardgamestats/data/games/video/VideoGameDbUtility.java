package com.cjboyett.boardgamestats.data.games.video;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract.VideoGameEntry;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGameCompilation;
import com.cjboyett.boardgamestats.model.games.video.VideoGameDeveloper;
import com.cjboyett.boardgamestats.model.games.video.VideoGameFranchise;
import com.cjboyett.boardgamestats.model.games.video.VideoGameGenre;
import com.cjboyett.boardgamestats.model.games.video.VideoGameMode;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlatform;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePublisher;
import com.cjboyett.boardgamestats.model.games.video.VideoGameSeries;
import com.cjboyett.boardgamestats.model.games.video.VideoGameTheme;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.cjboyett.boardgamestats.data.games.video.VideoGameContract.GamePlayEntry;
import static com.cjboyett.boardgamestats.data.games.video.VideoGameContract.ImageEntry;
import static com.cjboyett.boardgamestats.data.games.video.VideoGameContract.PlayerEntry;

/**
 * Created by Casey on 4/13/2016.
 */
public class VideoGameDbUtility {
	public static void addVideoGame(GamesDbHelper dbHelper, String gameName, String description, int yearPublished,
									String releaseDate, int bggid, String thumbnailUrl, String compilations,
									String developer, String franchise, String genre, String mode,
									String platform, String publisher, String series, String theme) {
		ContentValues values = new ContentValues();
		values.put(VideoGameEntry.NAME, gameName);
		if (description != null) values.put(VideoGameEntry.DESCRIPTION, description);
		if (yearPublished > 0) values.put(VideoGameEntry.YEAR_PUBLISHED, yearPublished);
		if (releaseDate != null) values.put(VideoGameEntry.RELEASE_DATE, releaseDate);
		if (bggid > 0) values.put(VideoGameEntry.BGG_ID, bggid);
		if (thumbnailUrl != null) values.put(VideoGameEntry.THUMBNAIL, thumbnailUrl);
		if (compilations != null) values.put(VideoGameEntry.COMPILATION, compilations);
		if (developer != null) values.put(VideoGameEntry.DEVELOPER, developer);
		if (franchise != null) values.put(VideoGameEntry.FRANCHISE, franchise);
		if (genre != null) values.put(VideoGameEntry.GENRE, genre);
		if (mode != null) values.put(VideoGameEntry.MODE, mode);
		if (platform != null) values.put(VideoGameEntry.PLATFORM, platform);
		if (publisher != null) values.put(VideoGameEntry.PUBLISHER, publisher);
		if (series != null) values.put(VideoGameEntry.SERIES, series);
		if (theme != null) values.put(VideoGameEntry.THEME, theme);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertOrThrow(VideoGameEntry.TABLE_NAME, null, values);
		} catch (Exception e) {
		}
		db.close();
	}

	public static void addVideoGame(GamesDbHelper dbHelper, VideoGame game) {
		addVideoGame(dbHelper, game.getName(), game.getDescription(), game.getYearPublished(),
					 game.getReleaseDate(), game.getBggId(), game.getThumbnailUrl(), game.getCompilations().toString(),
					 game.getDevelopers().toString(), game.getFranchises().toString(), game.getGenres().toString(),
					 game.getModes().toString(), game.getPlatforms().toString(), game.getPublishers().toString(),
					 game.getSeries().toString(), game.getThemes().toString());
	}

	public static boolean updateVideoGame(GamesDbHelper dbHelper, String oldGameName, VideoGame videoGame) {
		return updateVideoGame(dbHelper,
							   oldGameName,
							   videoGame.getName(),
							   videoGame.getDescription(),
							   videoGame.getYearPublished(),
							   videoGame.getReleaseDate(),
							   videoGame.getBggId(),
							   videoGame.getThumbnailUrl(),
							   videoGame.getCompilations().toString(),
							   videoGame.getDevelopers().toString(),
							   videoGame.getFranchises().toString(),
							   videoGame.getGenres().toString(),
							   videoGame.getModes().toString(),
							   videoGame.getPlatforms().toString(),
							   videoGame.getPublishers().toString(),
							   videoGame.getSeries().toString(),
							   videoGame.getThemes().toString());
	}

	public static boolean updateVideoGame(GamesDbHelper dbHelper, String oldGameName, String gameName,
										  String description, int yearPublished,
										  String releaseDate, int bggid, String thumbnailUrl, String compilations,
										  String developer, String franchise, String genre, String mode,
										  String platform, String publisher, String series, String theme) {
		ContentValues values = new ContentValues();
		values.put(VideoGameEntry.NAME, gameName);
		if (description != null) values.put(VideoGameEntry.DESCRIPTION, description);
		if (yearPublished > 0) values.put(VideoGameEntry.YEAR_PUBLISHED, yearPublished);
		if (releaseDate != null) values.put(VideoGameEntry.RELEASE_DATE, releaseDate);
		if (bggid > 0) values.put(VideoGameEntry.BGG_ID, bggid);
		if (thumbnailUrl != null) values.put(VideoGameEntry.THUMBNAIL, thumbnailUrl);
		if (compilations != null) values.put(VideoGameEntry.COMPILATION, compilations);
		if (developer != null) values.put(VideoGameEntry.DEVELOPER, developer);
		if (franchise != null) values.put(VideoGameEntry.FRANCHISE, franchise);
		if (genre != null) values.put(VideoGameEntry.GENRE, genre);
		if (mode != null) values.put(VideoGameEntry.MODE, mode);
		if (platform != null) values.put(VideoGameEntry.PLATFORM, platform);
		if (publisher != null) values.put(VideoGameEntry.PUBLISHER, publisher);
		if (series != null) values.put(VideoGameEntry.SERIES, series);
		if (theme != null) values.put(VideoGameEntry.THEME, theme);

		// Make sure the game doesn't already exist
		Cursor newGameCursor = dbHelper.getReadableDatabase().query(VideoGameEntry.TABLE_NAME,
																	new String[]{VideoGameEntry.NAME},
																	VideoGameEntry.NAME + " = ?",
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
				VideoGamePlayData playData = getGamePlay(dbHelper, gamePlayId);
				playData.setGame(new VideoGame(gameName));
				updateGamePlay(dbHelper, gamePlayId, playData);
			}
			gamePlayCursor.close();

			// Update game data
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			try {
				db.update(VideoGameEntry.TABLE_NAME,
						  values,
						  VideoGameEntry.NAME + " = ?",
						  new String[]{oldGameName});
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			db.close();
			return true;
		} else return false;
	}


	public static void deleteVideoGame(GamesDbHelper dbHelper, VideoGame game) {
		Cursor gamePlayCursor = dbHelper.getReadableDatabase().query(
				GamePlayEntry.TABLE_NAME,
				new String[]{GamePlayEntry._ID},
				GamePlayEntry.GAME + " = ?",
				new String[]{game.getName()},
				null,
				null,
				null);
		while (gamePlayCursor.moveToNext()) deleteGamePlay(dbHelper, gamePlayCursor.getLong(0));

		dbHelper.getWritableDatabase().delete(VideoGameEntry.TABLE_NAME,
											  VideoGameEntry.NAME + " = ?",
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

	// TODO Add extras
	public static VideoGame getVideoGame(GamesDbHelper dbHelper, String name) {
		String[] columns = new String[]{
				VideoGameEntry.NAME,
				VideoGameEntry.YEAR_PUBLISHED,
				VideoGameEntry.RELEASE_DATE,
				VideoGameEntry.DESCRIPTION,
				VideoGameEntry.THUMBNAIL,
				VideoGameEntry.COMPILATION,
				VideoGameEntry.DEVELOPER,
				VideoGameEntry.FRANCHISE,
				VideoGameEntry.GENRE,
				VideoGameEntry.MODE,
				VideoGameEntry.PLATFORM,
				VideoGameEntry.PUBLISHER,
				VideoGameEntry.SERIES,
				VideoGameEntry.THEME,
				VideoGameEntry.BGG_ID
		};
		Cursor gameCursor = dbHelper.getReadableDatabase()
									.query(VideoGameEntry.TABLE_NAME,
										   columns,
										   VideoGameEntry.NAME + " = ?",
										   new String[]{name},
										   null,
										   null,
										   null);

		if (gameCursor.moveToFirst()) {
			VideoGame game = new VideoGame(gameCursor.getString(0));
			game.setYearPublished(gameCursor.getInt(1));
			game.setReleaseDate(gameCursor.getString(2));
			game.setDescription(gameCursor.getString(3));
			game.setThumbnailUrl(gameCursor.getString(4));
			game.setBggId(gameCursor.getInt(14));

			addCompilation(game, gameCursor.getString(5));
			addDeveloper(game, gameCursor.getString(6));
			addFranchise(game, gameCursor.getString(7));
			addGenre(game, gameCursor.getString(8));
			addMode(game, gameCursor.getString(9));
			addPlatform(game, gameCursor.getString(10));
			addPublisher(game, gameCursor.getString(11));
			addSeries(game, gameCursor.getString(12));
			addTheme(game, gameCursor.getString(13));

			gameCursor.close();
			return game;
		} else return null;
	}

	private static void addCompilation(VideoGame game, String compilationString) {
		addExtra(game, compilationString, VideoGame.COMPILATION);
	}

	private static void addDeveloper(VideoGame game, String developerString) {
		addExtra(game, developerString, VideoGame.DEVELOPER);
	}

	private static void addFranchise(VideoGame game, String franchiseString) {
		addExtra(game, franchiseString, VideoGame.FRANCHISE);
	}

	private static void addGenre(VideoGame game, String genreString) {
		addExtra(game, genreString, VideoGame.GENRE);
	}

	private static void addMode(VideoGame game, String modeString) {
		addExtra(game, modeString, VideoGame.MODE);
	}

	private static void addPlatform(VideoGame game, String platformString) {
		addExtra(game, platformString, VideoGame.PLATFORM);
	}

	private static void addPublisher(VideoGame game, String publisherString) {
		addExtra(game, publisherString, VideoGame.PUBLISHER);
	}

	private static void addSeries(VideoGame game, String seriesString) {
		addExtra(game, seriesString, VideoGame.SERIES);
	}

	private static void addTheme(VideoGame game, String themeString) {
		addExtra(game, themeString, VideoGame.THEME);
	}

	private static void addExtra(VideoGame game, String extraString, int type) {
		if (!TextUtils.isEmpty(extraString) && !extraString.equalsIgnoreCase("[]")) {
			String[] extras = extraString.replace("[", "").replace("]", "").split(",");
			String toAdd = "";
			for (String extra : extras) {
				int index = extra.lastIndexOf(" ");
				if (index != -1 && NumberUtils.isParsable(extra.substring(index + 1))) {
					switch (type) {
						case VideoGame.COMPILATION:
							game.addCompilation(new VideoGameCompilation(toAdd + extra.substring(0, index).trim(),
																		 Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.DEVELOPER:
							game.addDeveloper(new VideoGameDeveloper(toAdd + extra.substring(0, index).trim(),
																	 Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.FRANCHISE:
							game.addFranchise(new VideoGameFranchise(toAdd + extra.substring(0, index).trim(),
																	 Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.GENRE:
							game.addGenre(new VideoGameGenre(toAdd + extra.substring(0, index).trim(),
															 Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.MODE:
							game.addMode(new VideoGameMode(toAdd + extra.substring(0, index).trim(),
														   Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.PLATFORM:
							game.addPlatform(new VideoGamePlatform(toAdd + extra.substring(0, index).trim(),
																   Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.PUBLISHER:
							game.addPublisher(new VideoGamePublisher(toAdd + extra.substring(0, index).trim(),
																	 Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.SERIES:
							game.addSeries(new VideoGameSeries(toAdd + extra.substring(0, index).trim(),
															   Integer.parseInt(extra.substring(index + 1))));
							break;
						case VideoGame.THEME:
							game.addTheme(new VideoGameTheme(toAdd + extra.substring(0, index).trim(),
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
				VideoGameEntry.TABLE_NAME,
				new String[]{VideoGameEntry.NAME},
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


	// Game play data

	public static void addGamePlay(GamesDbHelper dbHelper, VideoGamePlayData gamePlayData) {
		addGamePlay(dbHelper, gamePlayData.getGame().getName(),
					gamePlayData.getTimePlayed(), gamePlayData.getDate().rawDate(), gamePlayData.getNotes(),
					gamePlayData.getLocation(), gamePlayData.isCountForStats(), gamePlayData.getImages(),
					gamePlayData.getOtherPlayers().values(), gamePlayData.getBggPlayId());
	}

	public static void addGamePlay(GamesDbHelper dbHelper, String game,
								   int timePlayed, String date, String notes, String location, boolean countForStats,
								   List<String> images, Collection<GamePlayerData> gamePlayerData, String bggPlayId) {
		ContentValues values = new ContentValues();
		values.put(GamePlayEntry.GAME, game);
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

	public static boolean addGamePlayIfNotPresent(GamesDbHelper dbHelper, VideoGamePlayData videoGamePlayData) {
		if (!containsGamePlayData(dbHelper, videoGamePlayData)) {
			addGamePlay(dbHelper, videoGamePlayData);
			return true;
		}
		return false;
	}

	private static boolean containsGamePlayData(GamesDbHelper dbHelper, VideoGamePlayData videoGamePlayData) {
		Cursor playCursor = dbHelper.getReadableDatabase()
									.query(GamePlayEntry.TABLE_NAME,
										   new String[]{GamePlayEntry._ID},
										   GamePlayEntry.GAME + " = ? AND " +
												   GamePlayEntry.TIME_PLAYED + " = ? AND " +
												   GamePlayEntry.DATE + " = ? AND " +
												   GamePlayEntry.NOTES + " = ? AND " +
												   GamePlayEntry.LOCATION + " = ? AND " +
												   GamePlayEntry.COUNT_FOR_STATS + " = ?",
										   new String[]{videoGamePlayData.getGame().getName(),
														videoGamePlayData.getTimePlayed() + "",
														videoGamePlayData.getDate().rawDate(),
														videoGamePlayData.getNotes(),
														videoGamePlayData.getLocation(),
														videoGamePlayData.isCountForStats() ? "y" : "n"},
										   null,
										   null,
										   null);

		if (playCursor.moveToFirst()) {
			while (playCursor.moveToNext()) {
				VideoGamePlayData gamePlayData = getGamePlay(dbHelper, playCursor.getLong(0));
				if (videoGamePlayData.equals(gamePlayData)) {
					playCursor.close();
					return true;
				}
			}
		}
		playCursor.close();

		return false;

	}


	public static void updateGamePlay(GamesDbHelper dbHelper, long gamePlayId, VideoGamePlayData gamePlayData) {
		updateGamePlay(dbHelper, gamePlayId, gamePlayData.getGame().getName(),
					   gamePlayData.getTimePlayed(), gamePlayData.getDate().rawDate(),
					   gamePlayData.getNotes(), gamePlayData.getLocation(), gamePlayData.isCountForStats(),
					   gamePlayData.getImages(), gamePlayData.getOtherPlayers().values());
	}

	public static void updateGamePlay(GamesDbHelper dbHelper, long gamePlayId, String game,
									  int timePlayed, String date, String notes, String location, boolean countForStats,
									  List<String> images, Collection<GamePlayerData> gamePlayerData) {
		ContentValues values = new ContentValues();
		values.put(GamePlayEntry.GAME, game);
		values.put(GamePlayEntry.TIME_PLAYED, timePlayed);
		if (date != null) values.put(GamePlayEntry.DATE, date);
		if (notes != null) values.put(GamePlayEntry.NOTES, notes);
		if (location != null) values.put(GamePlayEntry.LOCATION, location);
		values.put(GamePlayEntry.COUNT_FOR_STATS, countForStats ? "y" : "n");

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.update(GamePlayEntry.TABLE_NAME,
					  values,
					  GamePlayEntry._ID + " = ?",
					  new String[]{gamePlayId + ""});
		} catch (Exception e) {
			e.printStackTrace();
		}
		db.close();

		deleteImages(dbHelper, gamePlayId);
		addImages(dbHelper, gamePlayId, images);
		deletePlayers(dbHelper, gamePlayId);
		addPlayers(dbHelper, gamePlayId, gamePlayerData);
	}

	public static VideoGamePlayData getGamePlay(GamesDbHelper dbHelper, long gamePlayId) {
		String[] columns = new String[]{
				GamePlayEntry.GAME,
				GamePlayEntry.SCORE,
				GamePlayEntry.WIN,
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

		VideoGamePlayData videoGamePlayData = new VideoGamePlayData(getVideoGame(dbHelper, playCursor.getString(0)),
																	playCursor.getDouble(1),
																	(playCursor.getString(2) != null &&
																			playCursor.getString(2).equals("y")),
																	playCursor.getInt(3),
																	new Date(playCursor.getString(4)),
																	playCursor.getString(5),
																	gamePlayId);
		videoGamePlayData.setLocation(playCursor.getString(6));
		videoGamePlayData.setCountForStats(playCursor.getString(7).equalsIgnoreCase("y"));
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
															   (playersCursor.getString(2) != null &&
																	   playersCursor.getString(2).equals("y")));
			videoGamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
		}
		playersCursor.close();
		return videoGamePlayData;
	}

	public static Map<Long, VideoGamePlayData> getGamePlay(GamesDbHelper dbHelper, String from, String to) {
		Map<Long, VideoGamePlayData> gamePlayDataMap = new TreeMap<>();
		String[] columns = new String[]{
				GamePlayEntry.GAME,
				GamePlayEntry.SCORE,
				GamePlayEntry.WIN,
				GamePlayEntry.TIME_PLAYED,
				GamePlayEntry.DATE,
				GamePlayEntry.NOTES,
				GamePlayEntry.LOCATION
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
			long gamePlayId = playCursor.getLong(5);
			VideoGamePlayData videoGamePlayData = new VideoGamePlayData(getVideoGame(dbHelper, playCursor.getString(0)),
																		playCursor.getDouble(1),
																		playCursor.getString(2).equals("y"),
																		playCursor.getInt(3),
																		new Date(playCursor.getString(4)),
																		playCursor.getString(5),
																		gamePlayId);
			videoGamePlayData.setLocation(playCursor.getString(6));

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
			while (playersCursor.moveToNext()) {
				GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0));
				videoGamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
			}
			playersCursor.close();

			gamePlayDataMap.put(gamePlayId, videoGamePlayData);
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
										 .query(VideoGameEntry.TABLE_NAME,
												new String[]{VideoGameEntry.THUMBNAIL},
												VideoGameEntry.NAME + " = ?",
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
		List<GamePlayData> gamePlayDataList = new ArrayList<>();
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
				long gamePlayId = gamePlayIds.get(i * batchSize + index++);
				VideoGamePlayData videoGamePlayData =
						new VideoGamePlayData(getVideoGame(dbHelper, playCursor.getString(0)),
											  playCursor.getDouble(1),
											  (playCursor.getString(2) != null && playCursor.getString(2).equals("y")),
											  playCursor.getInt(3),
											  new Date(playCursor.getString(4)),
											  playCursor.getString(5),
											  gamePlayId);
				videoGamePlayData.setLocation(playCursor.getString(6));
				videoGamePlayData.setCountForStats(playCursor.getString(7).equalsIgnoreCase("Y"));

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
					GamePlayerData gamePlayerData = new GamePlayerData(playersCursor.getString(0),
																	   playersCursor.getDouble(1),
																	   (playersCursor.getString(2) != null &&
																			   playersCursor.getString(2).equals("y")));
					videoGamePlayData.addOtherPlayer(gamePlayerData.getPlayerName(), gamePlayerData);
				}
				playersCursor.close();

				gamePlayDataList.add(videoGamePlayData);
			}
			playCursor.close();
		}

		return gamePlayDataList;
	}

	public static long getGameId(GamesDbHelper dbHelper, String gameName) {
		long id = -10000l;
		Cursor idCursor = dbHelper.getReadableDatabase().query(
				VideoGameEntry.TABLE_NAME,
				new String[]{VideoGameEntry.BGG_ID},
				VideoGameEntry.NAME + " = ?",
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
				VideoGameEntry.TABLE_NAME,
				new String[]{VideoGameEntry.BGG_ID},
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

	public static String getBggId(GamesDbHelper dbHelper, String gameName) {
		String bggid = null;
		Cursor cursor = dbHelper.getReadableDatabase()
								.query(VideoGameEntry.TABLE_NAME,
									   new String[]{VideoGameEntry.BGG_ID},
									   VideoGameEntry.NAME + " = ?",
									   new String[]{gameName},
									   null,
									   null,
									   null);
		if (cursor.moveToNext()) bggid = cursor.getString(0);
		cursor.close();
		return bggid;
	}
}
