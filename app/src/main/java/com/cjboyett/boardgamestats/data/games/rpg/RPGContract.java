package com.cjboyett.boardgamestats.data.games.rpg;

import android.provider.BaseColumns;

/**
 * Created by Casey on 4/10/2016.
 */
public class RPGContract {
	public static final class RPGEntry implements BaseColumns {
		public static final String TABLE_NAME = "rpgs";

		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String YEAR_PUBLISHED = "year";
		public static final String BGG_ID = "bggid";
		public static final String THUMBNAIL = "thumbnail";
		public static final String MECHANICS = "mechanics";
		public static final String FAMILIES = "families";
	}

	public static final class GamePlayEntry implements BaseColumns {
		public static final String TABLE_NAME = "rpgplays";

		public static final String GAME = "game";
		public static final String TIME_PLAYED = "timeplayed";
		public static final String DATE = "date";
		public static final String NOTES = "notes";
		public static final String LOCATION = "location";
		public static final String COUNT_FOR_STATS = "countforstats";

		public static final String BGG_PLAY_ID = "bggplayid";
	}

	public static final class PlayerEntry implements BaseColumns {
		public static final String TABLE_NAME = "rpgplayers";

		public static final String NAME = "name";
		public static final String GAME_PLAY_ID = "gameid";
	}

	public static final class ImageEntry implements BaseColumns {
		public static final String TABLE_NAME = "rpgimages";

		public static final String FILE_LOCATION = "fileLocation";
		public static final String EXTERNAL = "external";
		public static final String GAME_PLAY_ID = "gameid";
	}

}
