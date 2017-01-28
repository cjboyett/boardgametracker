package com.cjboyett.boardgamestats.data;

import android.util.Log;
import android.util.Xml;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/17/2016.
 */
public class PlaysXmlParser {
	private static final String namespace = null;
	private String bggUserName = null, userName = "";
	private Game.GameType gameType = Game.GameType.BOARD;

	public List<GamePlayData> parse(InputStream in) {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} catch (Exception e) {
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return new ArrayList<>();
	}

	private List<GamePlayData> readFeed(XmlPullParser parser) {
		List<GamePlayData> entries = new ArrayList<>();

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "plays");
			bggUserName = parser.getAttributeValue(null, "username");
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				String name = parser.getName();
				if (name.equals("play")) entries.add(readItem(parser));
				else skip(parser);
			}
		} catch (Exception e) {
		}
		return entries;
	}

	private GamePlayData readItem(XmlPullParser parser) {
		Game game = null;
		String date = "0", notes = "", location = "";
		double score = -10000;
		boolean win = true;
		int timePlayed = 0;
		String bggPlayId = "";
		List<GamePlayerData> players = new ArrayList<>();
		try {
			parser.require(XmlPullParser.START_TAG, namespace, "play");
			bggPlayId = parser.getAttributeValue(null, "id");
			String playDate = parser.getAttributeValue(null, "date");
			if (!playDate.equals("")) {
				String[] dateBits = playDate.split("-");
				int month = Integer.parseInt(dateBits[1]) - 1;
				date = dateBits[0] + ""
						+ (month < 10 ? "0" + month : month) + ""
						+ dateBits[2];
			}
			timePlayed = Integer.parseInt(parser.getAttributeValue(null, "length"));
			location = parser.getAttributeValue(null, "location");

			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				String entryName = parser.getName();

				switch (entryName) {
					case "item":
						game = readGame(parser);
						break;
					case "players":
						players = readPlayers(parser);
						break;
					case "comment":
						notes = readNotes(parser);
						break;
					default:
						skip(parser);
						break;
				}
			}
		} catch (Exception e) {
		}

		GamePlayData gamePlayData = null;

		switch (gameType) {
			case BOARD:
				gamePlayData =
						new BoardGamePlayData((BoardGame) game, score, win, timePlayed, new Date(date), notes, -1l);
				break;
			case RPG:
				gamePlayData = new RPGPlayData((RolePlayingGame) game, timePlayed, new Date(date), notes, -1l);
				break;
			case VIDEO:
				gamePlayData =
						new VideoGamePlayData((VideoGame) game, score, win, timePlayed, new Date(date), notes, -1l);
				break;
		}
		if (gamePlayData != null) {
			gamePlayData.setBggPlayId(bggPlayId);
			gamePlayData.setLocation(location);
			for (GamePlayerData player : players) {
				gamePlayData.addOtherPlayer(player.getPlayerName(), player);
				if (gameType == Game.GameType.BOARD)
					if (player.getPlayerName().equals("master_user"))
						((BoardGamePlayData) gamePlayData).setWin(player.isWin());
				if (gameType == Game.GameType.VIDEO)
					if (player.getPlayerName().equals("master_user"))
						((VideoGamePlayData) gamePlayData).setWin(player.isWin());
			}
		}

		return gamePlayData;
	}

	private Game readGame(XmlPullParser parser) {
		String game = "";
		int id = -1;

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "item");
			game = parser.getAttributeValue(null, "name");
			if (parser.getAttributeValue(null, "objectid") != null &&
					!parser.getAttributeValue(null, "objectid").equals(""))
				id = Integer.parseInt(parser.getAttributeValue(null, "objectid"));
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				skip(parser);
			}
			parser.require(XmlPullParser.END_TAG, namespace, "item");
		} catch (Exception e) {
		}

		if (gameType == Game.GameType.BOARD) return new BoardGame(game, "", id);
		else if (gameType == Game.GameType.RPG) return new RolePlayingGame(game, "", id);
		else return new VideoGame(game, "", id);
	}

	private String readNotes(XmlPullParser parser) {
		String notes = null;

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "comments");
			parser.next();
			notes = StringEscapeUtils.unescapeHtml4(parser.getText());
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "comments");
		} catch (Exception e) {
			Log.e("PARSER", e.getMessage());
		}

		return notes;
	}

	private List<GamePlayerData> readPlayers(XmlPullParser parser) {
		List<GamePlayerData> players = new ArrayList<>();

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "players");
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				players.add(readPlayer(parser));
			}
			parser.require(XmlPullParser.END_TAG, namespace, "players");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return players;
	}

	private GamePlayerData readPlayer(XmlPullParser parser) {
		GamePlayerData gamePlayerData = new GamePlayerData("", 0, false);

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "player");
			if (parser.getAttributeValue(null, "username").equals(bggUserName) ||
					parser.getAttributeValue(null, "name").equals(userName))
				gamePlayerData.setPlayerName("master_user");
			else gamePlayerData.setPlayerName(parser.getAttributeValue(null, "name"));
			if (!parser.getAttributeValue(null, "score").equals(""))
				gamePlayerData.setScore(Double.parseDouble(parser.getAttributeValue(null, "score")));
			gamePlayerData.setWin(parser.getAttributeValue(null, "win").equals("1"));
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "player");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return gamePlayerData;
	}

	private void skip(XmlPullParser parser) {
		try {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				throw new IllegalStateException();
			}
			int depth = 1;
			while (depth != 0) {
				switch (parser.next()) {
					case XmlPullParser.END_TAG:
						depth--;
						break;
					case XmlPullParser.START_TAG:
						depth++;
						break;
				}
			}
		} catch (Exception e) {
		}
	}

	public void setGameType(Game.GameType gameType) {
		this.gameType = gameType;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
