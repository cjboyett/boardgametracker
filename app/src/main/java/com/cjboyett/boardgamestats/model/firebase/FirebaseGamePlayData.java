package com.cjboyett.boardgamestats.model.firebase;

import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;

import java.util.Map;

/**
 * Created by Casey on 5/25/2016.
 */
public class FirebaseGamePlayData {
	public String game, gameType;
	public int gameId;
	public int timePlayed;
	public String notes;
	public String date;
	public String location;
	//	private List<String> images;
	public Map<String, FirebasePlayerData> otherPlayers;
	public long id;
	public String bggPlayId;
	public boolean ignoreWinData;

	public FirebaseGamePlayData() {
	}

	public FirebaseGamePlayData(String game, String gameType, int gameId, int timePlayed, String notes, String date,
								String location, Map<String, FirebasePlayerData> otherPlayers, long id,
								String bggPlayId, boolean ignoreWinData) {
		this.game = game;
		this.gameType = gameType;
		this.gameId = gameId;
		this.timePlayed = timePlayed;
		this.notes = notes;
		this.date = date;
		this.location = location;
		this.otherPlayers = otherPlayers;
		this.id = id;
		this.bggPlayId = bggPlayId;
		this.ignoreWinData = ignoreWinData;
	}

	public static FirebaseGamePlayData makeFirebaseData(GamePlayData gamePlayData) {
		String gameType = "";
		if (gamePlayData instanceof BoardGamePlayData) gameType = "b";
		else if (gamePlayData instanceof RPGPlayData) gameType = "r";
		else if (gamePlayData instanceof VideoGamePlayData) gameType = "v";

		return new FirebaseGamePlayData(gamePlayData.getGame().getName(),
										gameType,
										gamePlayData.getGame().getBggId(),
										gamePlayData.getTimePlayed(),
										gamePlayData.getNotes(),
										gamePlayData.getDate().rawDate(),
										gamePlayData.getLocation(),
										FirebasePlayerData.makeFirebaseData(gamePlayData.getOtherPlayers()),
										gamePlayData.getId(),
										gamePlayData.getBggPlayId(),
										!gamePlayData.isCountForStats());
	}

	@Override
	public String toString() {
		String toReturn =
				"[" + game + ", " + gameType + ", " + gameId + ", " + timePlayed + ", " + notes + ", " + date + ", " +
						location +
						", " + otherPlayers.toString() + ", " + id + ", " + bggPlayId + ", " + ignoreWinData + "]";
		return toReturn;
	}

}
