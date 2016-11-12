package com.cjboyett.boardgamestats.model.achievements;

import android.app.Activity;

import com.cjboyett.boardgamestats.model.stats.StatisticsManager;

/**
 * Created by Casey on 10/4/2016.
 */
public class AchievementManager
{
	private StatisticsManager statisticsManager;

	private final static int HOUR = 60, DAY = 60 * 24, WEEK = 60 * 24 * 7;

	public AchievementManager(Activity activity)
	{
		statisticsManager = StatisticsManager.getInstance(activity);
	}

	public final Achievement[] getAchievements()
	{
		Achievement[] achievements = new Achievement[]{
				new Achievement(0, "First-Timer", "Log your first game", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberGamePlaysPlayed() >= 1;
					}
				},
				new Achievement(10, "Getting Going", "Log 5 total plays", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberGamePlaysPlayed() >= 5;
					}
				},
				new Achievement(11, "Going Strong", "Log 10 total plays", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberGamePlaysPlayed() >= 10;
					}
				},
				new Achievement(12, "On a Roll", "Log 25 total plays", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberGamePlaysPlayed() >= 25;
					}
				},
				new Achievement(13, "You've Been Busy", "Log 100 total plays", 100, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberGamePlaysPlayed() >= 100;
					}
				},
				new Achievement(14, "You've Been Very Busy", "Log 1000 total plays", 200, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberGamePlaysPlayed() >= 1000;
					}
				},
				new Achievement(20, "Just a Quick Game", "Log total of 1 hour", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= HOUR;
					}
				},
				new Achievement(21, "Maybe Another One", "Log total of 5 hours", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 5 * HOUR;
					}
				},
				new Achievement(22, "One More", "Log total of 10 hours", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 10 * HOUR;
					}
				},
				new Achievement(23, "Just Another Day", "Log total of 1 day", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= DAY;
					}
				},
				new Achievement(24, "On Vacation", "Log total of 1 week", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= WEEK;
					}
				},
				new Achievement(25, "Calling in Sick", "Log total of 1 month", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 30 * DAY;
					}
				},
				new Achievement(26, "Do You Sleep?", "Log total of 6 month", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 6 * 30 * DAY;
					}
				},
				new Achievement(30, "Made a Friend", "Log total of 1 player", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberPlayers() >= 1;
					}
				},
				new Achievement(31, "Got a Gaming Group", "Log total of 10 players", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberPlayers() >= 10;
					}
				},
				new Achievement(32, "Getting Around", "Log total of 25 players", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberPlayers() >= 25;
					}
				},
				new Achievement(33, "Quite the Socialite", "Log total of 50 players", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberPlayers() >= 50;
					}
				},
				new Achievement(34, "Did You Host a Convention?", "Log total of 100 players", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getNumberPlayers() >= 100;
					}
				}
/*
				new Achievement(25, "Calling in Sick", "Log total of 1 month", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 30 * DAY;
					}
				},
				new Achievement(25, "Calling in Sick", "Log total of 1 month", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 30 * DAY;
					}
				},
				new Achievement(25, "Calling in Sick", "Log total of 1 month", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 30 * DAY;
					}
				},
				new Achievement(25, "Calling in Sick", "Log total of 1 month", 50, null)
				{
					@Override
					public boolean isCompleted()
					{
						return statisticsManager.getGameTimePlayed() >= 30 * DAY;
					}
				}
*/
		};

		return achievements;
	}
}
