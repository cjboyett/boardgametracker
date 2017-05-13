package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import timber.log.Timber;

/**
 * Created by Casey on 4/21/2016.
 */
public class MostPlayTimeGamesStatistic extends MultiStatistic {
	public MostPlayTimeGamesStatistic(Activity activity, StatisticsManager statisticsManager) {
		super(activity, statisticsManager);
	}

	@NonNull
	@Override
	public String getTitle() {
		return "Most Played Games by Time";
	}

	void generateStats() {
		Map<Integer, List<String>> games = new TreeMap<>();

		List<Integer> counts = new ArrayList<>(statisticsManager.getAllGamesByTimePlayed().keySet());
		for (Integer count : counts)
			games.put(count, new ArrayList<>(statisticsManager.getAllGamesByTimePlayed().get(count)));

		Random r = new Random(Preferences.getUsername(activity).hashCode());

		for (int i = counts.size() - 1; i >= 0; i--) {
			int count = counts.get(i);
			Timber.d(games.get(count).toString());
			while (!games.get(count).isEmpty()) {
				String game = games.get(count).remove(r.nextInt(games.get(count).size()));
				stats.add(new String[]{game, StringUtilities.convertMinutes(count)});
			}
		}
	}
}
