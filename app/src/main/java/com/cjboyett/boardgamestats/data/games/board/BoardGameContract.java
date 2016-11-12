package com.cjboyett.boardgamestats.data.games.board;

import android.provider.BaseColumns;

/**
 * Created by Casey on 3/7/2016.
 */
public class BoardGameContract
{
	public static final class BoardGameEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "boardgames";

		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String YEAR_PUBLISHED = "year";
		public static final String BGG_ID = "bggid";
		public static final String THUMBNAIL = "thumbnail";
		public static final String EXPANSIONS = "expansions";
		public static final String CATEGORIES = "categories";
		public static final String MECHANICS = "mechanics";
		public static final String FAMILIES = "families";
		public static final String PUBLISHERS = "publishers";

		//Not used.  Changed to use separate tables now.
		public static final String GAMETYPE = "gametype";
	}

	public static final class GamePlayEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "plays";

		public static final String GAME = "game";
		public static final String WIN = "win";
		public static final String SCORE = "score";
		public static final String TIME_PLAYED = "timeplayed";
		public static final String DATE = "date";
		public static final String EXPANSIONS = "expansions";
		public static final String NOTES = "notes";
		public static final String LOCATION = "location";
		public static final String COUNT_FOR_STATS = "countforstats";

		public static final String BGG_PLAY_ID = "bggplayid";
	}

	public static final class PlayerEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "players";

		public static final String NAME = "name";
		public static final String SCORE = "score";
		public static final String WIN = "win";
		public static final String GAME_PLAY_ID = "gameid";
	}

	public static final class ImageEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "images";

		public static final String FILE_LOCATION = "fileLocation";
		public static final String EXTERNAL = "external";
		public static final String GAME_PLAY_ID = "gameid";
	}

	public static final class HotnessEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "hotness";

		public static final String NAME = "name";
		public static final String BGGID = "bggid";
		public static final String RANK = "rank";
		public static final String THUMBNAIL = "thumbnail";
	}

}
