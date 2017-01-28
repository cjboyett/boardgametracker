package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

import java.text.NumberFormat;

/**
 * Created by Casey on 4/21/2016.
 */
public class GameMetricStatistic extends Statistic {
	private StatisticsManager statisticsManager;

	public GameMetricStatistic(Activity activity, StatisticsManager statisticsManager) {
		super(activity);
		this.statisticsManager = statisticsManager;
	}

	@NonNull
	@Override
	public String getTitle() {
		return "Metric";
	}

	@NonNull
	@Override
	public View getView() {
//		GamesDbHelper dbHelper = new GamesDbHelper(activity);
		int numberTimesPlayed = statisticsManager.getGamesTimesPlayed();
		int numberGamesPlayed = statisticsManager.getNumberGamesPlayed();
		double metric = (double) numberTimesPlayed / numberGamesPlayed;
//		dbHelper.close();

		String metricString = "You have played " + numberGamesPlayed + " different games.  " +
				"That is an average of " + NumberFormat.getNumberInstance().format(metric) + " plays per game.";

		View view = activity.getLayoutInflater().inflate(R.layout.cardview_game_stats, null, false);
		((TextView) view.findViewById(R.id.textview_stats)).setText(metricString);
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
