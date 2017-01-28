package com.cjboyett.boardgamestats.data;

import android.provider.BaseColumns;

/**
 * Created by Casey on 4/20/2016.
 */
public class TempContract {
	public static final class GamePlayEntry implements BaseColumns {
		public static final String TABLE_NAME = "tempplays";

		public static final String GAME = "game";
		public static final String GAME_TYPE = "gametype";
		public static final String TIME_PLAYED = "timeplayed";
		public static final String DATE = "date";
		public static final String NOTES = "notes";
		public static final String LOCATION = "location";
		public static final String COUNT_FOR_STATS = "countforstats";

		public static final String TIMER_START = "timerstart";
		public static final String LAST_TIMER_START = "lasttimerstart";
		public static final String LAST_TIMER_STOP = "lasttimerstop";
		public static final String TIMER_DIFF = "timerdiff";
	}

	public static final class PlayerEntry implements BaseColumns {
		public static final String TABLE_NAME = "tempplayers";

		public static final String NAME = "name";
		public static final String SCORE = "score";
		public static final String WIN = "win";
	}

}
