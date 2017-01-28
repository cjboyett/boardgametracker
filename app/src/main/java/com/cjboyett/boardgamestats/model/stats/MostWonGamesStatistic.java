package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;

import com.cjboyett.boardgamestats.utility.Preferences;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Casey on 4/22/2016.
 */
public class MostWonGamesStatistic extends MultiStatistic {
	public MostWonGamesStatistic(Activity activity, StatisticsManager statisticsManager) {
		super(activity, statisticsManager);
	}

	@Override
	public String getTitle() {
		return "Most Won Games";
	}

	void generateStats() {
		int THRESHOLD = Preferences.gamePlayThreshold(activity);

		Map<Integer, List<String>> games = statisticsManager.getAllGamesByTimesPlayed();
		Map<String, Integer> wonGames = statisticsManager.getNumberWonGames();
		Map<Double, List<String>> winRatios = new TreeMap<>();

		for (Integer count : games.keySet()) {
			List<String> gameList = games.get(count);
			for (String game : gameList) {
				if (wonGames.containsKey(game)) {
					int wins = wonGames.get(game);
					if (count >= THRESHOLD) {
						double winRatio = ((double) wins) / count;
						if (!winRatios.containsKey(winRatio))
							winRatios.put(winRatio, new ArrayList<String>());
						winRatios.get(winRatio).add(game);
					}
				}
			}
		}

		List<Double> wins = new ArrayList<>(winRatios.keySet());

		Random r = new Random(Preferences.getUsername(activity).hashCode());

		for (int i = wins.size() - 1; i >= 0; i--) {
			double d = wins.get(i);
			while (!winRatios.get(d).isEmpty()) {
				String game = winRatios.get(d).remove(r.nextInt(winRatios.get(d).size()));
				stats.add(new String[]{game, NumberFormat.getPercentInstance().format(d)});
			}
		}
	}
}
