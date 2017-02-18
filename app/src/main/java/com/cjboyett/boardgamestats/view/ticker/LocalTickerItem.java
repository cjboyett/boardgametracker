package com.cjboyett.boardgamestats.view.ticker;

import android.content.Context;
import android.graphics.Bitmap;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameStatsDbUtility;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.utility.view.ImageController;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Ticker Item for random stats about games played, such as time played or times won
 * Created by Casey on 5/8/2016.
 */
public class LocalTickerItem extends TickerItem {
	private String blurb, gameName, gameType;
	private Bitmap image;

	public LocalTickerItem(Context context) {
		super(context);

		generateImageAndBlurb();
	}

	private void generateImageAndBlurb() {
		Game game = generateGameAndThumbnail();
		blurb = generateBlurb(game);
	}

	private Game generateGameAndThumbnail() {
		// StatisticsManager caches various stats in memory that are either lengthy to compute
		// or are accessed frequently, such as number of games played and times each have been played
		StatisticsManager statisticsManager = StatisticsManager.getInstance(context);
		Map<Integer, List<String>> games = statisticsManager.getAllGamesByAverageTimePlayedWithType();
		GamesDbHelper dbHelper = new GamesDbHelper(context);

		// Randomization is based on average game play length of games
		// Longer games have higher priority
		// This will later be changed to take in times played and last
		// time played...but as always, I'm lazy
		int total = 0;
		for (Integer count : games.keySet())
			total += count * games.get(count).size();

		gameName = "";
		gameType = "";
		String thumbnailUrl = "";

		// Checking to make sure the game actually has a thumbnail for the image
		while (thumbnailUrl.length() <= 11) {
			int choice = new Random().nextInt(total);

			for (Integer count : games.keySet()) {
				if (count * games.get(count).size() <= choice)
					choice -= count * games.get(count).size();
				else {
					gameName = games.get(count).get(choice / count);
					break;
				}
			}

			gameType = gameName.substring(gameName.length() - 1);
			gameName = gameName.substring(0, gameName.lastIndexOf(":"));
			switch (gameType) {
				case "b":
					thumbnailUrl = "http://" + BoardGameDbUtility.getThumbnailUrl(dbHelper, gameName);
					break;
				case "r":
					thumbnailUrl = "http://" + RPGDbUtility.getThumbnailUrl(dbHelper, gameName);
					break;
				case "v":
					thumbnailUrl = "http://" + VideoGameDbUtility.getThumbnailUrl(dbHelper, gameName);
					break;
			}
		}

		// Since thumbnail does exist, we load it from memory...
		image = new ImageController(context).setDirectoryName("thumbnails")
											.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
											.load();
		// And then load the game
		Game game = null;
		switch (gameType) {
			case "b":
				game = BoardGameDbUtility.getBoardGame(dbHelper, gameName);
				break;
			case "r":
				game = RPGDbUtility.getRPG(dbHelper, gameName);
				break;
			case "v":
				game = VideoGameDbUtility.getVideoGame(dbHelper, gameName);
				break;
		}
		dbHelper.close();

		return game;
	}

	private String generateBlurb(Game game) {
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		String gameBlurb = "";
		String gameType = "";

		if (game instanceof BoardGame) gameType = "b";
		else if (game instanceof RolePlayingGame) gameType = "r";
		else gameType = "v";

		// Magically generate some quick blurb about the game that was loaded above
		switch (gameType) {
			case "b":
				gameBlurb = BoardGameStatsDbUtility.getRandomBlurb(dbHelper, game.getName());
				break;
			case "r":
				gameBlurb = RPGStatsDbUtility.getRandomBlurb(dbHelper, game.getName());
				break;
			case "v":
				gameBlurb = VideoGameStatsDbUtility.getRandomBlurb(dbHelper, game.getName());
				break;
		}

		return gameBlurb;
	}

	@Override
	public String getID() {
		return gameName;
	}

	public String getGameType() {
		return gameType;
	}

	@Override
	public Bitmap getImage() {
		return image;
	}

	@Override
	public String getBlurb() {
		return blurb;
	}
}
