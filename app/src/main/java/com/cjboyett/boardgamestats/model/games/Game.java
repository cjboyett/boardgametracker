package com.cjboyett.boardgamestats.model.games;

/**
 * Created by Casey on 3/24/2016.
 */
public abstract class Game
{
	private String name, description, thumbnailUrl;
	int bggId, yearPublished;

	public Game(String name)
	{
		this.name = name;
	}

	public Game(String name, String description, int bggId)
	{
		this.name = name;
		this.description = description;
		this.bggId = bggId;
	}

	public Game(String name, String description, String thumbnailUrl, int bggId)
	{
		this.name = name;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
		this.bggId = bggId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getThumbnailUrl()
	{
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl)
	{
		this.thumbnailUrl = thumbnailUrl;
	}

	public int getBggId()
	{
		return bggId;
	}

	public void setBggId(int bggId)
	{
		this.bggId = bggId;
	}

	public void setYearPublished(int yearPublished)
	{
		this.yearPublished = yearPublished;
	}

	public int getYearPublished()
	{
		return yearPublished;
	}

	public enum GameType
	{
		BOARD("boardgame"), RPG("rpg"), VIDEO("videogame");
		private String type;
		GameType(String type)
		{
			this.type = type;
		}

		public String getType()
		{
			return type;
		}
	}
}
