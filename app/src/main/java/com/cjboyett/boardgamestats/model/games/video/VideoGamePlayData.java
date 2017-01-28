package com.cjboyett.boardgamestats.model.games.video;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

/**
 * Created by Casey on 4/10/2016.
 */
public class VideoGamePlayData extends GamePlayData {
	private double score;
	private boolean win;

	public VideoGamePlayData(VideoGame game, double score, boolean win, int timePlayed, Date date, String notes,
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

	@Override
	public String toString() {
		return getGame().getName() + " " + score + " " + win + " " + getTimePlayed() + " " + getDate() + " " +
				getNotes() + " " + getOtherPlayers().values().toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof VideoGamePlayData &&
				((VideoGamePlayData) obj).getGame().equals(getGame()) &&
				((VideoGamePlayData) obj).getTimePlayed() == getTimePlayed() &&
				((VideoGamePlayData) obj).getDate().equals(getDate()) &&
				StringUtilities.blankIfNull(((VideoGamePlayData) obj).getLocation())
							   .equalsIgnoreCase(StringUtilities.blankIfNull(getLocation())) &&
				((VideoGamePlayData) obj).isCountForStats() == isCountForStats() &&
				GamePlayerData.equalPlayerMaps(((VideoGamePlayData) obj).getOtherPlayers(), getOtherPlayers());
	}


}
