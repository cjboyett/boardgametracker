package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.view.StatsView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 9/19/2016.
 */
public abstract class MultiStatistic extends Statistic
{
	private View view;
	protected StatisticsManager statisticsManager;

	protected int gamesCount = 3, initSize = 3, stepSize = 5;

	protected List<String[]> stats;

	private int backgroundColor, foregroundColor, hintTextColor;

	public MultiStatistic(Activity activity, StatisticsManager statisticsManager)
	{
		super(activity);
		this.statisticsManager = statisticsManager;
		stats = new ArrayList<>();
		generateStats();
	}

	public abstract String getTitle();

	@NonNull
	@Override
	public View getView()
	{
		view = activity.getLayoutInflater().inflate(R.layout.cardview_game_stats, null, false);

		backgroundColor = Preferences.getBackgroundColor(activity);
		foregroundColor = Preferences.getForegroundColor(activity);
		hintTextColor = Preferences.getHintTextColor(activity);

		view.setBackgroundColor(Preferences.getBackgroundColor(activity));
		view.findViewById(R.id.linearlayout_stats).setBackgroundColor(Preferences.getBackgroundColor(activity));

		((TextView) view.findViewById(R.id.textview_more_stats)).setTextColor(hintTextColor);
		view.findViewById(R.id.textview_more_stats).setBackgroundColor(backgroundColor);

		((TextView) view.findViewById(R.id.textview_more_stats)).setTextColor(hintTextColor);
		view.findViewById(R.id.textview_more_stats).setBackgroundColor(backgroundColor);

		view.findViewById(R.id.linearlayout_more_less_bar).setBackgroundColor(backgroundColor);

		generateView();

		return view;
	}

	@Nullable
	@Override
	public void getMoreStats()
	{
		if (gamesCount < initSize) gamesCount = initSize;
		else gamesCount += stepSize;
		generateView();
	}

	@Nullable
	@Override
	public void getFewerStats()
	{
		gamesCount -= stepSize;
		generateView();
	}

	@Override
	public boolean hasMoreStats()
	{
		return gamesCount < stats.size();
	}

	@Override
	public boolean hasFewerStats()
	{
		return gamesCount > 0;
	}

	abstract void generateStats();

	protected void generateView()
	{
		gamesCount = Math.min(gamesCount, stats.size());
		gamesCount = Math.max(gamesCount, 0);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			TransitionManager.beginDelayedTransition((ViewGroup)view);
		}

		LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.linearlayout_stats);

		if (linearLayout.getChildCount() > gamesCount)
		{
			linearLayout.removeViews(gamesCount, linearLayout.getChildCount() - gamesCount);
		}

		else
		{
			for (int i=linearLayout.getChildCount();i<gamesCount;i++)
			{
				StatsView statsView = new StatsView(activity);
				statsView.setTitle(stats.get(i)[0]);
				statsView.setDescription(stats.get(i)[1]);
				statsView.colorComponents(backgroundColor, foregroundColor);
				linearLayout.addView(statsView, i);
			}
		}
	}

}
