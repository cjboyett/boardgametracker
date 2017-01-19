package com.cjboyett.boardgamestats.utility.firebase;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.firebase.FirebaseGamePlayData;
import com.cjboyett.boardgamestats.model.firebase.FirebasePlayerData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;

import static com.cjboyett.boardgamestats.utility.data.StringUtilities.blankIfNull;

/**
 * Created by Casey on 12/30/2016.
 */

public class FirebaseGamePlayDataUtility
{
	public static BoardGamePlayData makeBoardGamePlayData(FirebaseGamePlayData gamePlayData)
	{
		BoardGamePlayData boardGamePlayData = new BoardGamePlayData(new BoardGame(gamePlayData.game, "", gamePlayData.gameId),
		                                                            -10000,
		                                                            false,
		                                                            gamePlayData.timePlayed,
		                                                            new Date(gamePlayData.date),
		                                                            blankIfNull(gamePlayData.notes),
		                                                            gamePlayData.id);
		for (String player : gamePlayData.otherPlayers.keySet())
			boardGamePlayData.addOtherPlayer(player, makePlayerData(gamePlayData.otherPlayers.get(player)));

		boardGamePlayData.setScore(boardGamePlayData.getOtherPlayers().get("master_user").getScore());
		boardGamePlayData.setWin(boardGamePlayData.getOtherPlayers().get("master_user").isWin());
		boardGamePlayData.setLocation(blankIfNull(gamePlayData.location));
		boardGamePlayData.setBggPlayId(blankIfNull(gamePlayData.bggPlayId));
		boardGamePlayData.setCountForStats(!gamePlayData.ignoreWinData);

		return boardGamePlayData;
	}

	public static RPGPlayData makeRPGPlayData(FirebaseGamePlayData gamePlayData)
	{
		RPGPlayData rpgPlayData = new RPGPlayData(new RolePlayingGame(gamePlayData.game, "", gamePlayData.gameId),
		                                          gamePlayData.timePlayed,
		                                          new Date(gamePlayData.date),
		                                          blankIfNull(gamePlayData.notes),
		                                          gamePlayData.id);
		for (String player : gamePlayData.otherPlayers.keySet())
			rpgPlayData.addOtherPlayer(player, makePlayerData(gamePlayData.otherPlayers.get(player)));

		rpgPlayData.setLocation(blankIfNull(gamePlayData.location));
		rpgPlayData.setBggPlayId(blankIfNull(gamePlayData.bggPlayId));
		rpgPlayData.setCountForStats(!gamePlayData.ignoreWinData);

		return rpgPlayData;
	}

	public static VideoGamePlayData makeVideoGamePlayData(FirebaseGamePlayData gamePlayData)
	{
		VideoGamePlayData videoGamePlayData = new VideoGamePlayData(new VideoGame(gamePlayData.game, "", gamePlayData.gameId),
		                                                            -10000,
		                                                            false,
		                                                            gamePlayData.timePlayed,
		                                                            new Date(gamePlayData.date),
		                                                            blankIfNull(gamePlayData.notes),
		                                                            gamePlayData.id);
		for (String player : gamePlayData.otherPlayers.keySet())
			videoGamePlayData.addOtherPlayer(player, makePlayerData(gamePlayData.otherPlayers.get(player)));

		videoGamePlayData.setScore(videoGamePlayData.getOtherPlayers().get("master_user").getScore());
		videoGamePlayData.setWin(videoGamePlayData.getOtherPlayers().get("master_user").isWin());
		videoGamePlayData.setLocation(blankIfNull(gamePlayData.location));
		videoGamePlayData.setBggPlayId(blankIfNull(gamePlayData.bggPlayId));
		videoGamePlayData.setCountForStats(!gamePlayData.ignoreWinData);

		return videoGamePlayData;
	}

	public static GamePlayerData makePlayerData(FirebasePlayerData playerData)
	{
		String name = playerData.name == null ? "" : playerData.name;
		double score = playerData.score;
		boolean win = playerData.win;

		return new GamePlayerData(name, score, win);
	}
}
