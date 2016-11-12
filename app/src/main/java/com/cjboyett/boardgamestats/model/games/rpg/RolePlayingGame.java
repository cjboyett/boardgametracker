package com.cjboyett.boardgamestats.model.games.rpg;

import com.cjboyett.boardgamestats.data.games.rpg.RPGXmlParser;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GameExtra;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/24/2016.
 */
public class RolePlayingGame extends Game
{
	private List<GameExtra> mechanics;
	private List<GameExtra> families;

	public static final int MECHANIC = 0, FAMILY = 1;

	public RolePlayingGame(String name)
	{
		super(name);
	}

	public RolePlayingGame(String name, String description, int bggId)
	{
		super(name, description, bggId);
	}

	public RolePlayingGame(String name, String description, String thumbnailUrl, int bggId)
	{
		super(name, description, thumbnailUrl, bggId);
	}

	public void addMechanic(RPGMechanic mechanic)
	{
		getMechanics().add(mechanic);
	}

	public List<GameExtra> getMechanics()
	{
		if (mechanics == null) mechanics = new ArrayList<>();
		return mechanics;
	}

	public void addFamily(RPGFamily family)
	{
		getFamilies().add(family);
	}

	public List<GameExtra> getFamilies()
	{
		if (families == null) families = new ArrayList<>();
		return families;
	}

	public static RolePlayingGame createGame(RPGXmlParser.Item item)
	{
		RolePlayingGame game = new RolePlayingGame(item.name, item.description, item.thumbnailUrl, item.id);

		for (String[] extra : item.links)
		{
			if (extra[0].equals("rpgmechanic")) game.addMechanic(new RPGMechanic(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("rpgfamily")) game.addFamily(new RPGFamily(extra[2], Integer.parseInt(extra[1])));
		}

		return game;
	}

}
