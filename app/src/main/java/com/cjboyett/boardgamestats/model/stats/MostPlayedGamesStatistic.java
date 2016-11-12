package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.cjboyett.boardgamestats.utility.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Casey on 4/21/2016.
 */
public class MostPlayedGamesStatistic extends MultiStatistic
{
	public MostPlayedGamesStatistic(Activity activity, StatisticsManager statisticsManager)
	{
		super(activity, statisticsManager);
	}

	@NonNull
	@Override
	public String getTitle()
	{
		return "Most Played Games";
	}

	void generateStats()
	{
		Map<Integer, List<String>> games = new TreeMap<>();

		List<Integer> counts = new ArrayList<>(statisticsManager.getAllGamesByTimesPlayed().keySet());
		for (Integer count : counts) games.put(count, new ArrayList<>(statisticsManager.getAllGamesByTimesPlayed().get(count)));

		Random r = new Random(Preferences.getUsername(activity).hashCode());

		for (int i=counts.size()-1;i>=0;i--)
		{
			int count = counts.get(i);
			while (!games.get(count).isEmpty())
			{
				String game = games.get(count).remove(r.nextInt(games.get(count).size()));
				stats.add(new String[]{game, count + " play" + (count != 1 ? "s" : "")});
			}
		}
	}
}
