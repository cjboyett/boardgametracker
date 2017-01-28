package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 4/21/2016.
 */
public class HScoreStatistic extends Statistic {
	private StatisticsManager statisticsManager;

	private String hScore = "";

	public HScoreStatistic(Activity activity, StatisticsManager statisticsManager) {
		super(activity);
		this.statisticsManager = statisticsManager;
	}

	@Override
	public String getTitle() {
		return "H-Score";
	}

	@NonNull
	@Override
	public View getView() {
		if (hScore.equalsIgnoreCase("")) {
			Map<Integer, List<String>> gamesPlayed = statisticsManager.getAllGamesByTimesPlayed();
			List<Integer> counts = new ArrayList<>(gamesPlayed.keySet());
			int h = 0;
			int max = counts.get(counts.size() - 1);
			int[] partialCounts = new int[max + 1];
			partialCounts[max] = gamesPlayed.get(max).size();
			for (int i = max - 1; i > 0; i--) {
				partialCounts[i] += partialCounts[i + 1] + (gamesPlayed.containsKey(i) ? gamesPlayed.get(i).size() : 0);
			}
			partialCounts[0] = 0;

			while (h <= max && h <= partialCounts[h]) h++;
			h--;

			hScore = "Your H-score is <b>" + h + "</b>.  This means you have played at least <b>" + h +
					"</b> games <b>" +
					+h + "</b> times.";
		}
		View view = activity.getLayoutInflater().inflate(R.layout.cardview_game_stats, null, false);
		((TextView) view.findViewById(R.id.textview_stats)).setText(Html.fromHtml(hScore));
		view.findViewById(R.id.textview_stats).setVisibility(View.VISIBLE);

		return view;
	}

	@Nullable
	@Override
	public void getMoreStats() {

	}

	@Nullable
	@Override
	public void getFewerStats() {

	}
}