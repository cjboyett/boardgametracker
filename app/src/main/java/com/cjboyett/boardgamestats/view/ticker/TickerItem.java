package com.cjboyett.boardgamestats.view.ticker;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Abstract class used in a ticker.
 * Created by Casey on 5/8/2016.
 */
public abstract class TickerItem
{
	protected Context context;

	public TickerItem(Context context)
	{
		this.context = context;
	}

	// ID is used to keep TickerItems from being duplicated in a Queue
	public abstract String getID();
	// Image shown in the Ticker
	public abstract Bitmap getImage();
	// Quick bit of text shown in Ticker
	public abstract String getBlurb();
}
