package com.cjboyett.boardgamestats.model.games.video;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;

/**
 * Created by Casey on 4/10/2016.
 */
public class VideoGamePlayData extends GamePlayData
{
	private double score;
	private boolean win;

	public VideoGamePlayData(VideoGame game, double score, boolean win, int timePlayed, Date date, String notes, long id)
	{
		setGame(game);
		setScore(score);
		setWin(win);
		setTimePlayed(timePlayed);
		setDate(date);
		setNotes(notes);
		setId(id);
	}

	public double getScore()
	{
		return score;
	}

	public void setScore(double score)
	{
		this.score = score;
	}

	public boolean isWin()
	{
		return win;
	}

	public void setWin(boolean win)
	{
		this.win = win;
	}

	@Override
	public String toString()
	{
		return getGame().getName() + " " + score + " " + win + " " + getTimePlayed() + " " + getDate() + " " + getNotes() + " " + getOtherPlayers().values().toString();
	}

}
