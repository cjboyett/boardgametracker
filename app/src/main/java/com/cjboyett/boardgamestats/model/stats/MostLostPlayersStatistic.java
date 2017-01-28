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
public class MostLostPlayersStatistic extends MultiStatistic {
	public MostLostPlayersStatistic(Activity activity, StatisticsManager statisticsManager) {
		super(activity, statisticsManager);
	}

	@Override
	public String getTitle() {
		return "Most Lost Players";
	}

	void generateStats() {
		double THRESHOLD = StatisticsManager.getInstance(activity)
											.getAverageNumberGamesPerPlayer();//Preferences.gamePlayThreshold(activity);

		Map<Integer, List<String>> allPlayersCount = statisticsManager.getAllPlayersByTimesPlayed();
		Map<String, Integer> allLostPlayersCount = statisticsManager.getAllPlayersGamesLost();
		Map<Double, List<String>> loseRatios = new TreeMap<>();

		for (Integer count : allPlayersCount.keySet()) {
			List<String> gameList = allPlayersCount.get(count);
			for (String game : gameList) {
				if (allLostPlayersCount.containsKey(game)) {
					int wins = allLostPlayersCount.get(game);
					if (count >= THRESHOLD) {
						double winRatio = ((double) wins) / count;
						if (!loseRatios.containsKey(winRatio))
							loseRatios.put(winRatio, new ArrayList<String>());
						loseRatios.get(winRatio).add(game);
					}
				}
			}
		}

		List<Double> loses = new ArrayList<>(loseRatios.keySet());

		Random r = new Random(Preferences.getUsername(activity).hashCode());

		for (int i = loses.size() - 1; i >= 0; i--) {
			double d = loses.get(i);
			while (!loseRatios.get(d).isEmpty()) {
				String game = loseRatios.get(d).remove(r.nextInt(loseRatios.get(d).size()));
				stats.add(new String[]{game, NumberFormat.getPercentInstance().format(d)});
			}
		}
	}
}
