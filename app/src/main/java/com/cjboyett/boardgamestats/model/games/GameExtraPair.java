package com.cjboyett.boardgamestats.model.games;

/**
 * Created by Casey on 8/31/2016.
 */
public class GameExtraPair
{
	private GameExtra[] extras;

	public GameExtraPair(GameExtra gameExtra1, GameExtra gameExtra2)
	{
		extras = new GameExtra[2];
		extras[0] = gameExtra1;
		extras[1] = gameExtra2;
	}

	@Override
	public String toString()
	{
		return "(" + extras[0].getName() + ", " + extras[1].getName() + ")";
	}
}
