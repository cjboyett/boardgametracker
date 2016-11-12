package com.cjboyett.boardgamestats.view.ticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cjboyett.boardgamestats.R;

/**
 * Convenience TickerItem used for when a user has not added any games to collection yet.
 * Created by Casey on 5/11/2016.
 */
public class AddBoardGameTickerItem extends TickerItem
{
	public AddBoardGameTickerItem(Context context)
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
		return "<b>Add a new game!</b>";
	}
}
