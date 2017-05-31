package com.cjboyett.boardgamestats.utility.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;

import com.cjboyett.boardgamestats.data.PlaysXmlParser;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGXmlParser;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameXmlParser;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ImageController;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

/**
 * Created by Casey on 4/18/2016.
 */
public class GameDownloadUtilities {
	public static void downloadThumbnail(final String thumbnailUrl, final Context context) {
		new AsyncTask<String, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(String... url) {
				Bitmap bitmap = null;
				InputStream in = null;
				try {
					if (!url[0].startsWith("http://") && !url[0].startsWith("https://")) url[0] = "https://" + url[0];
					URL thumbnailUrl = new URL(url[0]);
					HttpURLConnection connection = (HttpURLConnection) thumbnailUrl.openConnection();
					connection.setReadTimeout(10000);
					connection.setConnectTimeout(15000);
					connection.setDoInput(true);
					connection.connect();
					in = connection.getInputStream();
					bitmap = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					Timber.e(e);
				} finally {
					try {
						in.close();
					} catch (Exception e) {
						Timber.e(e);
					}
				}

				return bitmap;
			}

			@Override
			protected void onPostExecute(final Bitmap bitmap) {
				new ImageController(context)
						.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
						.setDirectoryName("thumbnails")
						.save(bitmap);
			}
		}.execute(thumbnailUrl);
	}

	public static void downloadGamePlays(final Context context, final String bggUserName) {
		final ProgressDialog progressDialog = new ProgressDialog(context);
		final GamesDbHelper dbHelper = new GamesDbHelper(context);

		final List<Long> boardGameIds, rpgIds, videoGameIds;
		final boolean[] doneSearching = new boolean[3];

		progressDialog.setMessage("Searching for your games...");
		progressDialog.show();

		boardGameIds = BoardGameDbUtility.getGameIds(dbHelper);
		rpgIds = RPGDbUtility.getGameIds(dbHelper);
		videoGameIds = VideoGameDbUtility.getGameIds(dbHelper);

		class DownloadXmlPlaysTask extends AsyncTask<String, Void, List<GamePlayData>> {
			private String url = "";
			private Game.GameType gameType;
			private int page = 1;

			public DownloadXmlPlaysTask(Game.GameType gameType) {
				this(gameType, 1);
			}

			public DownloadXmlPlaysTask(Game.GameType gameType, int page) {
				this.gameType = gameType;
				this.page = page;
			}

			@Override
			protected List<GamePlayData> doInBackground(String... urls) {
				url = urls[0];
				try {
					return loadGamePlayDataXmlFromNetwork(context, urls[0] + "&page=" + page, gameType);
				} catch (Exception e) {
					Timber.e(e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<GamePlayData> result) {
				if (result != null && !result.isEmpty()) {
					for (GamePlayData gamePlayData : result) {
						try {
							if (gameType == Game.GameType.BOARD) {
								if (!BoardGameDbUtility.gamePlayExists(dbHelper, gamePlayData.getBggPlayId())) {
									if (!boardGameIds.contains((long) gamePlayData.getGame().getBggId())) {
										gamePlayData.setGame(new DownloadXmlGameTask(gameType)
																	 .execute(
																			 "https://www.boardgamegeek.com/xmlapi2/thing?id=" +
																					 gamePlayData.getGame().getBggId())
																	 .get());
										GameDownloadUtilities.downloadThumbnail(gamePlayData.getGame()
																							.getThumbnailUrl(),
																				context);
										BoardGameDbUtility.addBoardGame(dbHelper, (BoardGame) gamePlayData.getGame());
									}
									BoardGameDbUtility.addGamePlay(dbHelper, (BoardGamePlayData) gamePlayData);
								}
							} else if (gameType == Game.GameType.RPG) {
								if (!RPGDbUtility.gamePlayExists(dbHelper, gamePlayData.getBggPlayId())) {
									if (!rpgIds.contains((long) gamePlayData.getGame().getBggId())) {
										gamePlayData.setGame(new DownloadXmlGameTask(gameType)
																	 .execute(
																			 "https://www.boardgamegeek.com/xmlapi2/family?id=" +
																					 gamePlayData.getGame().getBggId())
																	 .get());
										GameDownloadUtilities.downloadThumbnail(gamePlayData.getGame()
																							.getThumbnailUrl(),
																				context);
										RPGDbUtility.addRPG(dbHelper, (RolePlayingGame) gamePlayData.getGame());
									}
									RPGDbUtility.addGamePlay(dbHelper, (RPGPlayData) gamePlayData);
								}
							} else if (gameType == Game.GameType.VIDEO) {
								if (!VideoGameDbUtility.gamePlayExists(dbHelper, gamePlayData.getBggPlayId())) {
									if (!videoGameIds.contains((long) gamePlayData.getGame().getBggId())) {
										gamePlayData.setGame(new DownloadXmlGameTask(gameType)
																	 .execute(
																			 "https://www.boardgamegeek.com/xmlapi2/thing?id=" +
																					 gamePlayData.getGame().getBggId())
																	 .get());
										GameDownloadUtilities.downloadThumbnail(gamePlayData.getGame()
																							.getThumbnailUrl(),
																				context);
										VideoGameDbUtility.addVideoGame(dbHelper, (VideoGame) gamePlayData.getGame());
									}
									VideoGameDbUtility.addGamePlay(dbHelper, (VideoGamePlayData) gamePlayData);
								}
							}
						} catch (InterruptedException e) {
							Timber.e(e);
						} catch (ExecutionException e) {
							Timber.e(e);
						}

						new DownloadXmlPlaysTask(gameType, page + 1).execute(url);
					}
				}

				switch (gameType) {
					case BOARD:
						doneSearching[0] = true;
						break;
					case RPG:
						doneSearching[1] = true;
						break;
					case VIDEO:
						doneSearching[2] = true;
						break;
				}

				if (doneSearching[0] && doneSearching[1] && doneSearching[2]) {
					progressDialog.dismiss();
					dbHelper.close();
				}
			}
		}

		new DownloadXmlPlaysTask(Game.GameType.BOARD)
				.execute("https://www.boardgamegeek.com/xmlapi2/plays?username=" + URLEncoder.encode(bggUserName) +
								 "&subtype=boardgame");
		new DownloadXmlPlaysTask(Game.GameType.RPG)
				.execute("https://www.boardgamegeek.com/xmlapi2/plays?username=" + URLEncoder.encode(bggUserName) +
								 "&subtype=rpg");
		new DownloadXmlPlaysTask(Game.GameType.VIDEO)
				.execute("https://www.boardgamegeek.com/xmlapi2/plays?username=" + URLEncoder.encode(bggUserName) +
								 "&subtype=videogame");
	}

	public static class DownloadXmlGameTask extends AsyncTask<String, Void, Game> {
		private Game.GameType gameType;

		public DownloadXmlGameTask(Game.GameType gameType) {
			this.gameType = gameType;
		}

		@Override
		protected Game doInBackground(String... urls) {
			if (gameType == Game.GameType.BOARD) {
				try {
					List<BoardGameXmlParser.Item> items = UrlUtilities.loadBoardGameXmlFromNetwork(urls[0]);
					return BoardGame.createGame(items.get(0));
				} catch (Exception e) {
					Timber.e(e);
				}
			} else if (gameType == Game.GameType.RPG) {
				InputStream inputStream = null;
				try {
					inputStream = UrlUtilities.downloadUrl(urls[0]);
					List<RPGXmlParser.Item> items = new RPGXmlParser().parse(inputStream);
					return RolePlayingGame.createGame(items.get(0));
				} catch (Exception e) {
					Timber.e(e);
				} finally {
					try {
						inputStream.close();
					} catch (Exception e) {
						Timber.e(e);
					}
				}
			} else if (gameType == Game.GameType.VIDEO) {
				try {
					List<VideoGameXmlParser.Item> items = UrlUtilities.loadVideoGameXmlFromNetwork(urls[0]);
					return VideoGame.createGame(items.get(0));
				} catch (Exception e) {
					Timber.e(e);
				}
			}

			return null;
		}
	}

	private static List<GamePlayData> loadGamePlayDataXmlFromNetwork(Context context, String urlString,
																	 Game.GameType gameType) {
		InputStream in = null;
		PlaysXmlParser parser = new PlaysXmlParser();
		parser.setGameType(gameType);
		parser.setUserName(Preferences.getUsername(context));
		List<GamePlayData> items = null;

		try {
			in = UrlUtilities.downloadUrl(urlString);
			items = parser.parse(in);
		} catch (Exception e) {
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return items;
	}

	public static void downloadCollections(final Context context, final String bggUserName) {
		final ProgressDialog progressDialog = new ProgressDialog(context);
		final GamesDbHelper dbHelper = new GamesDbHelper(context);

		final List<Long> boardGameIds, rpgIds, videoGameIds;
		final boolean[] doneSearching = new boolean[3];

		progressDialog.setMessage("Searching for your games...");
		progressDialog.show();

		boardGameIds = BoardGameDbUtility.getGameIds(dbHelper);
		rpgIds = RPGDbUtility.getGameIds(dbHelper);
		videoGameIds = VideoGameDbUtility.getGameIds(dbHelper);

		class DownloadXmlCollectionTask extends AsyncTask<String, Void, List<Long>> {
			private Game.GameType gameType;

			public DownloadXmlCollectionTask(Game.GameType gameType) {
				this.gameType = gameType;
			}

			@Override
			protected List<Long> doInBackground(String... urls) {
				try {
					UrlUtilities.downloadUrl(urls[0]);
					return loadCollectionFromNetwork(urls[0]);
				} catch (Exception e) {
					Timber.e(e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<Long> result) {
				if (result != null && !result.isEmpty()) {
					for (Long id : result) {
						try {
							if (gameType == Game.GameType.BOARD) {
								if (!boardGameIds.contains(id)) {
									Game boardGame = new DownloadXmlGameTask(gameType)
											.execute("https://www.boardgamegeek.com/xmlapi2/thing?id=" + id)
											.get();
									if (boardGame != null) {
										GameDownloadUtilities.downloadThumbnail(boardGame.getThumbnailUrl(), context);
										BoardGameDbUtility.addBoardGame(dbHelper, (BoardGame) boardGame);
									}
								}
							} else if (gameType == Game.GameType.RPG) {
								if (!rpgIds.contains(id)) {
									Game rpg = new DownloadXmlGameTask(gameType)
											.execute("https://www.boardgamegeek.com/xmlapi2/family?id=" + id)
											.get();
									if (rpg != null) {
										GameDownloadUtilities.downloadThumbnail(rpg.getThumbnailUrl(), context);
										RPGDbUtility.addRPG(dbHelper, (RolePlayingGame) rpg);
									}
								}
							} else if (gameType == Game.GameType.VIDEO) {
								if (!videoGameIds.contains(id)) {
									Game videoGame = new DownloadXmlGameTask(gameType)
											.execute("https://www.boardgamegeek.com/xmlapi2/thing?id=" + id)
											.get();
									if (videoGame != null) {
										GameDownloadUtilities.downloadThumbnail(videoGame.getThumbnailUrl(), context);
										VideoGameDbUtility.addVideoGame(dbHelper, (VideoGame) videoGame);
									}
								}
							}
						} catch (InterruptedException e) {
							Timber.e(e);
						} catch (ExecutionException e) {
							Timber.e(e);
						}
					}
				}

				switch (gameType) {
					case BOARD:
						doneSearching[0] = true;
						break;
					case RPG:
						doneSearching[1] = true;
						break;
					case VIDEO:
						doneSearching[2] = true;
						break;
				}

				if (doneSearching[0] && doneSearching[1] && doneSearching[2]) {
					progressDialog.dismiss();
					dbHelper.close();
				}
			}
		}

		new DownloadXmlCollectionTask(Game.GameType.BOARD)
				.execute("https://www.boardgamegeek.com/xmlapi2/collection?username=" + URLEncoder.encode(bggUserName) +
								 "&subtype=boardgame");
		new DownloadXmlCollectionTask(Game.GameType.RPG)
				.execute("https://www.boardgamegeek.com/xmlapi2/collection?username=" + URLEncoder.encode(bggUserName) +
								 "&subtype=rpg");
		new DownloadXmlCollectionTask(Game.GameType.VIDEO)
				.execute("https://www.boardgamegeek.com/xmlapi2/collection?username=" + URLEncoder.encode(bggUserName) +
								 "&subtype=videogame");
	}

	private static List<Long> loadCollectionFromNetwork(String urlString) {
		InputStream in;
		List<Long> gameIds = new ArrayList<>();
		in = UrlUtilities.downloadUrl(urlString);

		try {
			String result = IOUtils.toString(in);
			int index = 0;
			while ((index = result.indexOf("objectid=\"", index + 10)) > -1) {
				String id = result.substring(index + 10, result.indexOf("\"", index + 10));
				gameIds.add(Long.parseLong(id));
			}
		} catch (IOException e) {
			Timber.e(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				Timber.e(e);
			}
		}
		return gameIds;
	}

	public static void syncWithBgg(final Context context, final String bggUserName) {
		new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				Looper.prepare();
				GameDownloadUtilities.downloadCollections(context, bggUserName);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				GameDownloadUtilities.downloadGamePlays(context, bggUserName);
			}
		}.execute("");
	}

}
