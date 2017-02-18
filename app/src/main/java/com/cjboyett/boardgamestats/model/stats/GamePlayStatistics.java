package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

import java.text.NumberFormat;

/**
 * Created by Casey on 4/21/2016.
 */
public class GamePlayStatistics extends Statistic {
	private StatisticsManager statisticsManager;

	public GamePlayStatistics(Activity activity, StatisticsManager statisticsManager) {
		super(activity);
		this.statisticsManager = statisticsManager;
	}

	@NonNull
	@Override
	public String getTitle() {
		return "Game Play";
	}

	@Override
	@NonNull
	public View getView() {
		String gamePlayStats = "";
		int totalTimePlayed = statisticsManager.getGameTimePlayed();
		int numberTimesPlayed = statisticsManager.getNumberGamePlaysPlayed();
		int numberGamesPlayed = statisticsManager.getNumberGamesPlayed();
		if (numberTimesPlayed > 0) {
			double metric = (double) numberTimesPlayed / numberGamesPlayed;

/*
		String metricString = "You have played " + numberGamesPlayed + " different games.  " +
				"That is an average of " + NumberFormat.getNumberInstance().format(metric) + " plays per game.";


		String gamePlayStats = "You have played a total of " + numberTimesPlayed + " games" +
				" over " + StringUtilities.convertMinutes(totalTimePlayed) + ".  " +
				"That is an average of " + StringUtilities.convertMinutes(totalTimePlayed / numberTimesPlayed) + " per game." ;
*/

			String metricString = "You have played " + numberGamesPlayed + " different games.  " +
					"That is an average of " + NumberFormat.getNumberInstance().format(metric) + " plays per game.";


			gamePlayStats = "You have played <b>" + numberGamesPlayed + "</b> different games a total of <b>" +
					numberTimesPlayed + "</b> times" +
					" over <b>" + StringUtilities.convertMinutes(totalTimePlayed) + "</b>.  " +
					"That is an average of <b>" + NumberFormat.getNumberInstance().format(metric) +
					"</b> plays per game" +
					" and <b>" + StringUtilities.convertMinutes(totalTimePlayed / numberTimesPlayed) + "</b> per game.";
		} else gamePlayStats = "You need to go play some games before I can give you stats.";

		View view = activity.getLayoutInflater().inflate(R.layout.cardview_game_stats, null, false);
		((TextView) view.findViewById(R.id.textview_stats)).setText(Html.fromHtml(gamePlayStats));
		view.findViewById(R.id.textview_stats).setVisibility(View.VISIBLE);
/*
		view.findViewById(R.id.textview_stats).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent detailedStatsIntent = new Intent(activity, StatsDetailedActivity.class);
				activity.startActivity(detailedStatsIntent);
			}
		});
*/
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
