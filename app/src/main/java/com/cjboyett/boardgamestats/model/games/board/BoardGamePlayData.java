package com.cjboyett.boardgamestats.model.games.board;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/9/2016.
 */
public class BoardGamePlayData extends GamePlayData {
	private List<BoardGame> expansions;
	private double score;
	private boolean win;

	public BoardGamePlayData(BoardGame game, double score, boolean win, int timePlayed, Date date, String notes,
							 long id) {
		setGame(game);
		setScore(score);
		setWin(win);
		setTimePlayed(timePlayed);
		setDate(date);
		setNotes(notes);
		setId(id);
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

	public List<BoardGame> getExpansions() {
		if (expansions == null) expansions = new ArrayList<>();
		return expansions;
	}

	public boolean addExpansion(BoardGame expansion) {
		return getExpansions().add(expansion);
	}

	@Override
	public String toString() {
		return getGame().getName() + " " + score + " " + win + " " + getTimePlayed() + " " + getDate() + " " +
				getNotes() + " " + getOtherPlayers().values().toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BoardGamePlayData &&
				((BoardGamePlayData) obj).getGame().equals(getGame()) &&
				((BoardGamePlayData) obj).getTimePlayed() == getTimePlayed() &&
				((BoardGamePlayData) obj).getDate().equals(getDate()) &&
				StringUtilities.blankIfNull(((BoardGamePlayData) obj).getLocation())
							   .equalsIgnoreCase(StringUtilities.blankIfNull(getLocation())) &&
				((BoardGamePlayData) obj).isCountForStats() == isCountForStats() &&
				GamePlayerData.equalPlayerMaps(((BoardGamePlayData) obj).getOtherPlayers(), getOtherPlayers());
	}
}
