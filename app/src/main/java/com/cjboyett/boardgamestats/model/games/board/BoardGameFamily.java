package com.cjboyett.boardgamestats.model.games.board;

import com.cjboyett.boardgamestats.model.games.GameExtra;

/**
 * Created by Casey on 3/5/2016.
 */
public class BoardGameFamily extends GameExtra
{

	public BoardGameFamily(String name, int bggid)
	{
		super(name, bggid);
	}

	@Override
	public String getType()
	{
		return "Family";
	}
}
