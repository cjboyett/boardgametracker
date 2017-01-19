package com.cjboyett.boardgamestats.model.games.board;

import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GameExtra;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/5/2016.
 */
public class BoardGame extends Game
{
	private List<BoardGame> expansions = new ArrayList<>();
	private List<GameExtra> mechanics = new ArrayList<>();
	private List<GameExtra> categories = new ArrayList<>();
	private List<GameExtra> families = new ArrayList<>();
	private List<GameExtra> publishers = new ArrayList<>();

	public static final int MECHANIC = 0, CATEGORY = 1, FAMILY = 2, PUBLISHER = 3;

	public BoardGame(String name)
	{
		super(name);
	}

	public BoardGame(String name, String description, int bggId)
	{
		super(name, description, bggId);
	}

	public BoardGame(String name, String description, String thumbnailUrl, int bggId)
	{
		super(name, description, thumbnailUrl, bggId);
	}

	public List<BoardGame> getExpansions()
	{
		return expansions;
	}

	public boolean addExpansion(BoardGame expansion)
	{
		if (expansions == null) expansions = new ArrayList<>();
		return expansions.add(expansion);
	}

	public List<GameExtra> getMechanics()
	{
		return mechanics;
	}

	public boolean addMechanic(BoardGameMechanic mechanic)
	{
		if (mechanics == null) mechanics = new ArrayList<>();
		return mechanics.add(mechanic);
	}

	public List<GameExtra> getCategories()
	{
		return categories;
	}

	public boolean addCategory(BoardGameCategory category)
	{
		if (categories == null) categories = new ArrayList<>();
		return categories.add(category);
	}

	public List<GameExtra> getFamilies()
	{
		return families;
	}

	public boolean addFamily(BoardGameFamily family)
	{
		if (families == null) families = new ArrayList<>();
		return  families.add(family);
	}

	public List<GameExtra> getPublishers()
	{
		return publishers;
	}

	public boolean addPublisher(BoardGamePublisher publisher)
	{
		if (publishers == null) publishers = new ArrayList<>();
		return  publishers.add(publisher);
	}

	public static BoardGame createGame(BoardGameXmlParser.Item item)
	{
		BoardGame game = new BoardGame(item.name, item.description, item.thumbnailUrl, item.id);

		game.setYearPublished(item.yearPublished);

		for (String[] extra : item.links)
		{
			if (extra[0].equals("boardgamecategory")) game.addCategory(new BoardGameCategory(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("boardgamefamily")) game.addFamily(new BoardGameFamily(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("boardgamemechanic")) game.addMechanic(new BoardGameMechanic(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("boardgamepublisher")) game.addPublisher(new BoardGamePublisher(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("boardgameexpansion")) game.addExpansion(new BoardGame(extra[2], null, Integer.parseInt(extra[1])));
		}

		return game;
	}

	@Override
	public String toString()
	{
		String toReturn = getName();
		if (getYearPublished() > 0) toReturn += "(" + getYearPublished() + ")";
		toReturn += " " + getBggId() + "\n" + getThumbnailUrl() +
				"\n" + getDescription() + "\n";
		for (BoardGame boardGame : getExpansions()) toReturn += "expansion:\n[" + boardGame + "]\n";
		for (GameExtra boardGameMechanic : getMechanics()) toReturn += "mechanic: " + boardGameMechanic + "\n";
		for (GameExtra boardGameCategory : getCategories()) toReturn += "category: " + boardGameCategory + "\n";
		for (GameExtra boardGameFamily : getFamilies()) toReturn += "family: " + boardGameFamily + "\n";
		for (GameExtra boardGamePublisher : getPublishers()) toReturn += "publisher: " + boardGamePublisher + "\n";
		return toReturn;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BoardGame &&
		       ((BoardGame) obj).getBggId() == getBggId() &&
		       ((BoardGame) obj).getName().equalsIgnoreCase(getName());
	}

}
