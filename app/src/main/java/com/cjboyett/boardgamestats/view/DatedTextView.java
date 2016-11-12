package com.cjboyett.boardgamestats.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Casey on 4/20/2016.
 */
public class DatedTextView extends TextView
{
	private long dateStamp;

	public DatedTextView(Context context)
	{
		super(context);
		dateStamp = SystemClock.elapsedRealtime();
	}

	public DatedTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		dateStamp = SystemClock.elapsedRealtime();
	}

	public DatedTextView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		dateStamp = SystemClock.elapsedRealtime();
	}

	public long getDateStamp()
	{
		return dateStamp;
	}

	@Override
	public void setOnClickListener(OnClickListener l)
	{
		super.setOnClickListener(l);
	}
}
