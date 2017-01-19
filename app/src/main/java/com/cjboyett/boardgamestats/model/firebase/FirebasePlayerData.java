package com.cjboyett.boardgamestats.model.firebase;

import com.cjboyett.boardgamestats.model.games.GamePlayerData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Casey on 5/25/2016.
 */
public class FirebasePlayerData
{
	public String name;
	public double score;
	public boolean win;

	public FirebasePlayerData()
	{
	}

	public FirebasePlayerData(String name, double score, boolean win)
	{
		this.name = name;
		this.score = score;
		this.win = win;
	}

	public static FirebasePlayerData makeFirebaseData(GamePlayerData gamePlayerData)
	{
		return new FirebasePlayerData(gamePlayerData.getPlayerName(), gamePlayerData.getScore(), gamePlayerData.isWin());
	}

	public static Map<String, FirebasePlayerData> makeFirebaseData(Map<String, GamePlayerData> playerDataMap)
	{
		Map<String, FirebasePlayerData> firebasePlayerDataMap = new HashMap<>();
		for (String player : playerDataMap.keySet())
			firebasePlayerDataMap.put(player, makeFirebaseData(playerDataMap.get(player)));
		return firebasePlayerDataMap;
	}

	@Override
	public String toString()
	{
		return "[" + name + ", " + score + ", " + win + "]";
	}
}
