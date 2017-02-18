package com.cjboyett.boardgamestats.data.games.video;

import android.provider.BaseColumns;

/**
 * Created by Casey on 4/10/2016.
 */
public class VideoGameContract {
	public static final class VideoGameEntry implements BaseColumns {
		public static final String TABLE_NAME = "videogames";

		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String YEAR_PUBLISHED = "year";
		public static final String RELEASE_DATE = "releasedate";
		public static final String BGG_ID = "bggid";
		public static final String THUMBNAIL = "thumbnail";
		public static final String COMPILATION = "compilation";
		public static final String DEVELOPER = "developer";
		public static final String FRANCHISE = "franchise";
		public static final String GENRE = "genre";
		public static final String MODE = "mode";
		public static final String PLATFORM = "platform";
		public static final String PUBLISHER = "publisher";
		public static final String SERIES = "series";
		public static final String THEME = "theme";
	}

	public static final class GamePlayEntry implements BaseColumns {
		public static final String TABLE_NAME = "videogameplays";

		public static final String GAME = "game";
		public static final String WIN = "win";
		public static final String SCORE = "score";
		public static final String TIME_PLAYED = "timeplayed";
		public static final String DATE = "date";
		public static final String NOTES = "notes";
		public static final String LOCATION = "location";
		public static final String COUNT_FOR_STATS = "countforstats";

		public static final String BGG_PLAY_ID = "bggplayid";
	}

	public static final class PlayerEntry implements BaseColumns {
		public static final String TABLE_NAME = "videogameplayers";

		public static final String NAME = "name";
		public static final String SCORE = "score";
		public static final String WIN = "win";
		public static final String GAME_PLAY_ID = "gameid";
	}

	public static final class ImageEntry implements BaseColumns {
		public static final String TABLE_NAME = "videogameimages";

		public static final String FILE_LOCATION = "fileLocation";
		public static final String EXTERNAL = "external";
		public static final String GAME_PLAY_ID = "gameid";
	}

}
