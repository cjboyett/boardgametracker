package com.cjboyett.boardgamestats.view.ticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cjboyett.boardgamestats.R;

/**
 * Convenience TickerItem used for when a user has not recorded any game plays yet.
 * Created by Casey on 5/27/2016.
 */
public class AddGamePlayTickerItem extends TickerItem
{
	public AddGamePlayTickerItem(Context context)
	{
		super(context);
	}

	@Override
	public String getID()
	{
		return "Yancey";
	}

	@Override
	public Bitmap getImage()
	{
		Bitmap yancey = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
		return yancey;
	}

	@Override
	public String getBlurb()
	{
		return "<b>Add a new game play!</b>";
	}
}
