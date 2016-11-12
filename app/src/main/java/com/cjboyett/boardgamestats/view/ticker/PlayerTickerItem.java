package com.cjboyett.boardgamestats.view.ticker;

import android.content.Context;
import android.graphics.Bitmap;

import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

/**
 * Ticker Item for random stats about players  (Still in development)
 * Created by Casey on 5/28/2016.
 */
public class PlayerTickerItem extends TickerItem
{
	private String name;
	private String blurb;

	public PlayerTickerItem(Context context, String name)
	{
		super(context);
		this.name = name;
		GamesDbHelper dbHelper = new GamesDbHelper(context);
		this.blurb = name;
		try
		{
			this.blurb = PlayersDbUtility.generateBlurb(dbHelper, name);
		}
		catch (Exception e)
		{

		}
		dbHelper.close();
	}

	@Override
	public String getID()
	{
		return name;
	}

	@Override
	public Bitmap getImage()
	{
		// Creates a nifty little avatar based on the players name and the current theme
		// I'm quite proud of this one
		return ViewUtilities.createAvatar(context, name, false);
	}

	@Override
	public String getBlurb()
	{
		return blurb;
	}
}
