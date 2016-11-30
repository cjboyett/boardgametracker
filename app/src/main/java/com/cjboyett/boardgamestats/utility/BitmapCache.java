package com.cjboyett.boardgamestats.utility;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Casey on 11/29/2016.
 */
public class BitmapCache extends LruCache<String, Bitmap>
{
	public BitmapCache()
	{
		super((int)(Runtime.getRuntime().maxMemory() / 1024) / 4);
	}

	@Override
	protected int sizeOf(String key, Bitmap value)
	{
		return value.getByteCount() / 1024;
	}

	public void addBitmapToCache(String key, Bitmap bitmap)
	{
		if (get(key) == null) put(key, bitmap);
	}
}
