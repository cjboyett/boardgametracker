package com.cjboyett.boardgamestats.model.changelog;

import com.cjboyett.boardgamestats.model.Date;

import static com.cjboyett.boardgamestats.model.Date.*;

/**
 * Created by Casey on 4/29/2016.
 */
public class ChangeLog
{
	private ChangeLog(){}

	public static LogEntry[] entries = {new LogEntry("1.1.0", "Major update:\n" +
	                                                          "UI overhaul\n" +
	                                                          "Player avatars\n" +
	                                                          "Facebook integration\n" +
	                                                          "Stats filtering by game type\n" +
	                                                          "Extras:\n" +
	                                                          "- Database backup\n" +
	                                                          "- Board game recommendations\n" +
	                                                          "Various background optimizations", new Date(2016, NOVEMBER, 29)),
	                                    new LogEntry("1.0.9b", "Bug fix related to crash coming from ticker" +
	                                                           "" /* Changed way most won games is handled */, new Date(2016, MAY, 27)),
	                                    new LogEntry("1.0.9", "UI updates:\n" +
	                                                          "- Added ticker to home screen\n" +
	                                                          "-- Ticker scrolls through game plays\n" +
	                                                          "-- as well as BGG Hot List\n" +
	                                                          "- Made buttons clearer on home screen\n" +
	                                                          "- Added number of plays to player list\n" +
	                                                          "Fixed timer to properly keep running\n" +
	                                                          "Added several new themes\n" +
	                                                          "Bug fixes (as always)", new Date(2016, MAY, 11)),
	                                    new LogEntry("1.0.8", "Major bug fix in Add Game Play screen\n" +
	                                                          "Added various notifications", new Date(2016, MAY, 5)),
	                                    new LogEntry("1.0.7", "Added Change Log\n" +
	                                                          "Sync manually added game with BGG\n" +
	                                                          "Several optimizations\n" +
	                                                          "Bug fixes", new Date(2016, APRIL, 29)),
	                                    new LogEntry("1.0.6", "Added Widget\n" +
	                                                          "Allow entry of manually added games\n" +
	                                                          "Various tracked stats added\n" +
	                                                          "Added timer in Add Game Play screen\n" +
	                                                          "Custom themes\n" +
	                                                          "Several optimizations\n" +
	                                                          "Bug fixes", new Date(2016, APRIL, 25)),
	                                    new LogEntry("1.0.5", "Bug fixes", new Date(2016, APRIL, 21)),
	                                    new LogEntry("1.0.4", "Added ability to retrieve game play and collection data from Board Game Geek", new Date(2016, APRIL, 18)),
	                                    new LogEntry("1.0.3", "Bug fixes, various tweaks, and UI improvements\n" +
	                                                          "Added filtering of games in Collection and Game Plays", new Date(2016, APRIL, 17)),
	                                    new LogEntry("1.0.2", "Added functionality to for role playing games and video games", new Date(2016, APRIL, 13)),
	                                    new LogEntry("1.0.1", "Bug fixes\n" +
	                                                          "Various tweaks", new Date(2016, APRIL, 12)),
	                                    new LogEntry("1.0.0", "Initial release", new Date(2016, APRIL, 11))};

	public static class LogEntry
	{
		public String version;
		public String entry;
		public Date date;

		public LogEntry(String version, String entry, Date date)
		{
			this.version = version;
			this.entry = entry;
			this.date = date;
		}
	}
}
