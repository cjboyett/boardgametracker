package com.cjboyett.boardgamestats.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

/**
 * Created by Casey on 9/18/2016.
 */
public class StatsView extends RelativeLayout
{
	public StatsView(Context context)
	{
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.layout_stats_view, this);
	}

	public StatsView(Context context, String title, String description)
	{
		this(context);
		setTitle(title);
		setDescription(description);
	}

	public void colorComponents(int backgroundColor, int foregroundColor)
	{
		setBackgroundColor(backgroundColor);
		((TextView)findViewById(R.id.textview_stat_title)).setTextColor(foregroundColor);
		((TextView)findViewById(R.id.textview_stat_description)).setTextColor(foregroundColor);
	}

	public void setTitle(String title)
	{
		((TextView)findViewById(R.id.textview_stat_title)).setText(title);
	}

	public void setDescription(String description)
	{
		((TextView)findViewById(R.id.textview_stat_description)).setText(description);
	}
}
