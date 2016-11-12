package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

import java.util.Random;

/**
 * Created by Casey on 4/21/2016.
 */
public class TestStatistic extends Statistic
{
	int i;
	public TestStatistic(Activity activity, int i)
	{
		super(activity);
		this.i = i;
	}

	@NonNull
	@Override
	public String getTitle()
	{
		return "Test";
	}

	@NonNull
	@Override
	public View getView()
	{
		String s = "";
		Random r = new Random(1000 + i);
		int wordCount = r.nextInt(25) + 10;
		for (int j=0;j<wordCount;j++)
		{
			String word = "";
			int wordLength = r.nextInt(10) + 1;
			for (int k=0;k<wordLength;k++)
			{
				word += (char)(r.nextInt(26) + 97);
			}
			s += word + " ";
		}
		s = (char)(s.charAt(0)-32) + s.substring(1, s.length()-1) + ".";
		View view = activity.getLayoutInflater().inflate(R.layout.cardview_game_stats, null, false);
		((TextView)view.findViewById(R.id.textview_stats)).setText(s);
		return view;
	}

	@Nullable
	@Override
	public void getMoreStats()
	{

	}

	@Nullable
	@Override
	public void getFewerStats()
	{

	}
}
