package com.cjboyett.boardgamestats.model.games;

/**
 * Created by Casey on 8/10/2016.
 */
public class PlayerData
{
	private String name;
	private String facebookid;
	private String notes;
	private String imageFilePath;

	private int timesPlayedWith;
	private int timePlayedWith;
	private String mostPlayedGameByTimes;
	private String mostPlayedGameByTime;
	private String mostWonGame;
	private String mostLostGame;
	private double winPercentage;
	private double losePercentage;

	public PlayerData(String name, String facebookid, String notes, String imageFilePath, int timesPlayedWith, int timePlayedWith, String mostPlayedGameByTimes, String mostPlayedGameByTime, String mostWonGame, String mostLostGame, double winPercentage, double losePercentage)
	{
		this.name = name;
		this.facebookid = facebookid;
		this.notes = notes;
		this.imageFilePath = imageFilePath;
		this.timesPlayedWith = timesPlayedWith;
		this.timePlayedWith = timePlayedWith;
		this.mostPlayedGameByTimes = mostPlayedGameByTimes;
		this.mostPlayedGameByTime = mostPlayedGameByTime;
		this.mostWonGame = mostWonGame;
		this.mostLostGame = mostLostGame;
		this.winPercentage = winPercentage;
		this.losePercentage = losePercentage;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getFacebookid()
	{
		return facebookid;
	}

	public void setFacebookid(String facebookid)
	{
		this.facebookid = facebookid;
	}

	public String getNotes()
	{
		return notes;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
	}

	public String getImageFilePath()
	{
		return imageFilePath;
	}

	public void setImageFilePath(String imageFilePath)
	{
		this.imageFilePath = imageFilePath;
	}

	public int getTimesPlayedWith()
	{
		return timesPlayedWith;
	}

	public void setTimesPlayedWith(int timesPlayedWith)
	{
		this.timesPlayedWith = timesPlayedWith;
	}

	public int getTimePlayedWith()
	{
		return timePlayedWith;
	}

	public void setTimePlayedWith(int timePlayedWith)
	{
		this.timePlayedWith = timePlayedWith;
	}

	public String getMostPlayedGameByTimes()
	{
		return mostPlayedGameByTimes;
	}

	public void setMostPlayedGameByTimes(String mostPlayedGameByTimes)
	{
		this.mostPlayedGameByTimes = mostPlayedGameByTimes;
	}

	public String getMostPlayedGameByTime()
	{
		return mostPlayedGameByTime;
	}

	public void setMostPlayedGameByTime(String mostPlayedGameByTime)
	{
		this.mostPlayedGameByTime = mostPlayedGameByTime;
	}

	public String getMostWonGame()
	{
		return mostWonGame;
	}

	public void setMostWonGame(String mostWonGame)
	{
		this.mostWonGame = mostWonGame;
	}

	public String getMostLostGame()
	{
		return mostLostGame;
	}

	public void setMostLostGame(String mostLostGame)
	{
		this.mostLostGame = mostLostGame;
	}

	public double getWinPercentage()
	{
		return winPercentage;
	}

	public void setWinPercentage(double winPercentage)
	{
		this.winPercentage = winPercentage;
	}

	public double getLosePercentage()
	{
		return losePercentage;
	}

	public void setLosePercentage(double losePercentage)
	{
		this.losePercentage = losePercentage;
	}

	@Override
	public String toString()
	{
		return name + ":[" + facebookid + ", " + notes + ", " + timesPlayedWith + ", " + timePlayedWith
		       + ", " + mostPlayedGameByTimes + ", " + mostPlayedGameByTime + ", "
		       + mostWonGame + ", " + mostLostGame + ", " + winPercentage + ", " + losePercentage + "]";
	}
}
