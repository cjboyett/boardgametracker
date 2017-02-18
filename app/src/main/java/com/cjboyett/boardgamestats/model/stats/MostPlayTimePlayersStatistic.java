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

/**
 * Created by Casey on 4/21/2016.
 */
public class MostPlayTimePlayersStatistic extends MultiStatistic {
	public MostPlayTimePlayersStatistic(Activity activity, StatisticsManager statisticsManager) {
		super(activity, statisticsManager);
	}

	@NonNull
	@Override
	public String getTitle() {
		return "Most Played Players by Time";
	}

	void generateStats() {
		Map<Integer, List<String>> playerMap = new TreeMap<>();

		List<Integer> counts = new ArrayList<>(statisticsManager.getAllPlayersByTimePlayed().keySet());
		for (Integer count : counts)
			playerMap.put(count, new ArrayList<>(statisticsManager.getAllPlayersByTimePlayed().get(count)));

		Random r = new Random(Preferences.getUsername(activity).hashCode());

		for (int i = counts.size() - 1; i >= 0; i--) {
			int count = counts.get(i);
			while (!playerMap.get(count).isEmpty()) {
				String player = playerMap.get(count).remove(r.nextInt(playerMap.get(count).size()));
				stats.add(new String[]{player, StringUtilities.convertMinutes(count)});
			}
		}
	}
}
