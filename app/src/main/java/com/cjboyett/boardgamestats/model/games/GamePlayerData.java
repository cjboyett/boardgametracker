package com.cjboyett.boardgamestats.model.games;

import java.util.Map;

/**
 * Created by Casey on 3/10/2016.
 */
public class GamePlayerData {
	private String playerName;
	private double score;
	private boolean win;

	public GamePlayerData(String playerName) {
		this(playerName, -10000, true);
	}

	public GamePlayerData(String playerName, double score, boolean win) {
		this.playerName = playerName;
		this.score = score;
		this.win = win;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	@Override
	public String toString() {
		String toReturn = "";
		toReturn += playerName;
		if (score > -10000) {
			if ((int) score == score) toReturn += " " + (int) score;
			else toReturn += " " + score;
		}
		toReturn += " " + (win ? "win" : "lose");
		return toReturn;
	}

	public static boolean equalPlayerMaps(Map<String, GamePlayerData> map1, Map<String, GamePlayerData> map2) {
		boolean equal = true;
		for (String player : map1.keySet()) {
			if (map2.containsKey(player))
				equal = equal && map1.get(player).equals(map2.get(player));
			else equal = false;
			if (!equal) break;
		}
		return equal;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GamePlayerData &&
				((GamePlayerData) obj).getPlayerName().equalsIgnoreCase(getPlayerName()) &&
				((GamePlayerData) obj).getScore() == getScore() &&
				((GamePlayerData) obj).isWin() == isWin();
	}
}
