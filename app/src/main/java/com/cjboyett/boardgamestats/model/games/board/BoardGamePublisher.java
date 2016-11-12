package com.cjboyett.boardgamestats.model.games.board;

import com.cjboyett.boardgamestats.model.games.GameExtra;

/**
 * Created by Casey on 3/7/2016.
 */
public class BoardGamePublisher extends GameExtra
{
	public BoardGamePublisher(String name, int bggid)
	{
		super(name, bggid);
	}

	@Override
	public String getType()
	{
		return "Publisher";
	}
}
