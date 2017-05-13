package com.cjboyett.boardgamestats.data.games;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cjboyett.boardgamestats.data.PlayerContract;
import com.cjboyett.boardgamestats.data.TempContract;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.utility.Preferences;

/**
 * Created by Casey on 3/7/2016.
 */
public class GamesDbHelper extends SQLiteOpenHelper {
	private Context context;

	private final static int DATABASE_VERSION = 11;
	static final String DATABASE_NAME = "boardgames.db";

	final String SQL_CREATE_BOARD_GAME_TABLE = "CREATE TABLE " + BoardGameContract.BoardGameEntry.TABLE_NAME + " (" +
			BoardGameContract.BoardGameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			BoardGameContract.BoardGameEntry.NAME + " TEXT UNIQUE NOT NULL, " +
			BoardGameContract.BoardGameEntry.DESCRIPTION + " TEXT, " +
			BoardGameContract.BoardGameEntry.YEAR_PUBLISHED + " TEXT, " +
			BoardGameContract.BoardGameEntry.BGG_ID + " TEXT, " +
			BoardGameContract.BoardGameEntry.THUMBNAIL + " TEXT, " +
			BoardGameContract.BoardGameEntry.EXPANSIONS + " TEXT, " +
			BoardGameContract.BoardGameEntry.CATEGORIES + " TEXT, " +
			BoardGameContract.BoardGameEntry.FAMILIES + " TEXT, " +
			BoardGameContract.BoardGameEntry.MECHANICS + " TEXT, " +
			BoardGameContract.BoardGameEntry.PUBLISHERS + " TEXT, " +
			BoardGameContract.BoardGameEntry.GAMETYPE + " TEXT NOT NULL);";

	final String SQL_CREATE_BOARD_GAME_PLAYS_TABLE =
			"CREATE TABLE " + BoardGameContract.GamePlayEntry.TABLE_NAME + " (" +
					BoardGameContract.GamePlayEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					BoardGameContract.GamePlayEntry.GAME + " TEXT NOT NULL, " +
					BoardGameContract.GamePlayEntry.DATE + " TEXT NOT NULL, " +
					BoardGameContract.GamePlayEntry.TIME_PLAYED + " INTEGER, " +
					BoardGameContract.GamePlayEntry.WIN + " CHAR(1), " +
					BoardGameContract.GamePlayEntry.SCORE + " REAL, " +
					BoardGameContract.GamePlayEntry.EXPANSIONS + " TEXT, " +
					BoardGameContract.GamePlayEntry.NOTES + " TEXT, " +
					BoardGameContract.GamePlayEntry.LOCATION + " TEXT, " +
					BoardGameContract.GamePlayEntry.BGG_PLAY_ID + " TEXT, " +
					BoardGameContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y', " +
					"FOREIGN KEY(" + BoardGameContract.GamePlayEntry.GAME + ") REFERENCES " +
					BoardGameContract.BoardGameEntry.TABLE_NAME + "(" + BoardGameContract.BoardGameEntry.NAME + ")" +
					" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_BOARD_GAME_IMAGE_TABLE = "CREATE TABLE " + BoardGameContract.ImageEntry.TABLE_NAME + " (" +
			BoardGameContract.ImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			BoardGameContract.ImageEntry.FILE_LOCATION + " TEXT NOT NULL, " +

			// Do I care?  I can append it later
//			BoardGameContract.ImageEntry.EXTERNAL + " CHAR(1) NOT NULL, " +
			BoardGameContract.ImageEntry.GAME_PLAY_ID + " INTEGER NOT NULL," +
			"FOREIGN KEY(" + BoardGameContract.ImageEntry.GAME_PLAY_ID + ") REFERENCES " +
			BoardGameContract.GamePlayEntry.TABLE_NAME + "(" + BoardGameContract.GamePlayEntry._ID + ")" +
			" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_BOARD_GAME_PLAYERS_TABLE =
			"CREATE TABLE " + BoardGameContract.PlayerEntry.TABLE_NAME + " (" +
					BoardGameContract.PlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					BoardGameContract.PlayerEntry.NAME + " TEXT NOT NULL, " +
					BoardGameContract.PlayerEntry.SCORE + " REAL, " +
					BoardGameContract.PlayerEntry.WIN + " CHAR(1), " +
					BoardGameContract.PlayerEntry.GAME_PLAY_ID + " INTEGER NOT NULL," +
					"FOREIGN KEY(" + BoardGameContract.PlayerEntry.GAME_PLAY_ID + ") REFERENCES " +
					BoardGameContract.GamePlayEntry.TABLE_NAME + "(" + BoardGameContract.GamePlayEntry._ID + ")" +
					" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_RPG_TABLE = "CREATE TABLE " + RPGContract.RPGEntry.TABLE_NAME + " (" +
			RPGContract.RPGEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			RPGContract.RPGEntry.NAME + " TEXT UNIQUE NOT NULL, " +
			RPGContract.RPGEntry.DESCRIPTION + " TEXT, " +
			RPGContract.RPGEntry.YEAR_PUBLISHED + " TEXT, " +
			RPGContract.RPGEntry.BGG_ID + " TEXT, " +
			RPGContract.RPGEntry.THUMBNAIL + " TEXT, " +
			RPGContract.RPGEntry.FAMILIES + " TEXT, " +
			RPGContract.RPGEntry.MECHANICS + " TEXT);";

	final String SQL_CREATE_RPG_PLAYS_TABLE = "CREATE TABLE " + RPGContract.GamePlayEntry.TABLE_NAME + " (" +
			RPGContract.GamePlayEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			RPGContract.GamePlayEntry.GAME + " TEXT NOT NULL, " +
			RPGContract.GamePlayEntry.DATE + " TEXT NOT NULL, " +
			RPGContract.GamePlayEntry.TIME_PLAYED + " INTEGER, " +
			RPGContract.GamePlayEntry.NOTES + " TEXT, " +
			RPGContract.GamePlayEntry.LOCATION + " TEXT, " +
			RPGContract.GamePlayEntry.BGG_PLAY_ID + " TEXT, " +
			RPGContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y', " +
			"FOREIGN KEY(" + RPGContract.GamePlayEntry.GAME + ") REFERENCES " +
			RPGContract.RPGEntry.TABLE_NAME + "(" + RPGContract.RPGEntry.NAME + ")" +
			" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_RPG_IMAGE_TABLE = "CREATE TABLE " + RPGContract.ImageEntry.TABLE_NAME + " (" +
			RPGContract.ImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			RPGContract.ImageEntry.FILE_LOCATION + " TEXT NOT NULL, " +

			// Do I care?  I can append it later.
//			BoardGameContract.ImageEntry.EXTERNAL + " CHAR(1) NOT NULL, " +
			RPGContract.ImageEntry.GAME_PLAY_ID + " INTEGER NOT NULL," +
			"FOREIGN KEY(" + RPGContract.ImageEntry.GAME_PLAY_ID + ") REFERENCES " +
			RPGContract.GamePlayEntry.TABLE_NAME + "(" + RPGContract.GamePlayEntry._ID + ")" +
			" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_RPG_PLAYERS_TABLE = "CREATE TABLE " + RPGContract.PlayerEntry.TABLE_NAME + " (" +
			RPGContract.PlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			RPGContract.PlayerEntry.NAME + " TEXT NOT NULL, " +
			RPGContract.PlayerEntry.GAME_PLAY_ID + " INTEGER NOT NULL," +
			"FOREIGN KEY(" + RPGContract.PlayerEntry.GAME_PLAY_ID + ") REFERENCES " +
			RPGContract.GamePlayEntry.TABLE_NAME + "(" + RPGContract.GamePlayEntry._ID + ")" +
			" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_VIDEO_GAME_TABLE = "CREATE TABLE " + VideoGameContract.VideoGameEntry.TABLE_NAME + " (" +
			VideoGameContract.VideoGameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			VideoGameContract.VideoGameEntry.NAME + " TEXT UNIQUE NOT NULL, " +
			VideoGameContract.VideoGameEntry.DESCRIPTION + " TEXT, " +
			VideoGameContract.VideoGameEntry.YEAR_PUBLISHED + " TEXT, " +
			VideoGameContract.VideoGameEntry.RELEASE_DATE + " TEXT, " +
			VideoGameContract.VideoGameEntry.BGG_ID + " TEXT, " +
			VideoGameContract.VideoGameEntry.THUMBNAIL + " TEXT, " +
			VideoGameContract.VideoGameEntry.COMPILATION + " TEXT, " +
			VideoGameContract.VideoGameEntry.DEVELOPER + " TEXT, " +
			VideoGameContract.VideoGameEntry.FRANCHISE + " TEXT, " +
			VideoGameContract.VideoGameEntry.GENRE + " TEXT, " +
			VideoGameContract.VideoGameEntry.MODE + " TEXT, " +
			VideoGameContract.VideoGameEntry.PLATFORM + " TEXT, " +
			VideoGameContract.VideoGameEntry.PUBLISHER + " TEXT, " +
			VideoGameContract.VideoGameEntry.SERIES + " TEXT, " +
			VideoGameContract.VideoGameEntry.THEME + " TEXT);";

	final String SQL_CREATE_VIDEO_GAME_PLAYS_TABLE =
			"CREATE TABLE " + VideoGameContract.GamePlayEntry.TABLE_NAME + " (" +
					VideoGameContract.GamePlayEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					VideoGameContract.GamePlayEntry.GAME + " TEXT NOT NULL, " +
					VideoGameContract.GamePlayEntry.DATE + " TEXT NOT NULL, " +
					VideoGameContract.GamePlayEntry.TIME_PLAYED + " INTEGER, " +
					VideoGameContract.GamePlayEntry.WIN + " CHAR(1), " +
					VideoGameContract.GamePlayEntry.SCORE + " REAL, " +
					VideoGameContract.GamePlayEntry.NOTES + " TEXT, " +
					VideoGameContract.GamePlayEntry.LOCATION + " TEXT, " +
					VideoGameContract.GamePlayEntry.BGG_PLAY_ID + " TEXT, " +
					VideoGameContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y', " +
					"FOREIGN KEY(" + VideoGameContract.GamePlayEntry.GAME + ") REFERENCES " +
					VideoGameContract.VideoGameEntry.TABLE_NAME + "(" + VideoGameContract.VideoGameEntry.NAME + ")" +
					" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_VIDEO_GAME_IMAGE_TABLE = "CREATE TABLE " + VideoGameContract.ImageEntry.TABLE_NAME + " (" +
			VideoGameContract.ImageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			VideoGameContract.ImageEntry.FILE_LOCATION + " TEXT NOT NULL, " +

			// Do I care?
//			BoardGameContract.ImageEntry.EXTERNAL + " CHAR(1) NOT NULL, " +
			VideoGameContract.ImageEntry.GAME_PLAY_ID + " INTEGER NOT NULL," +
			"FOREIGN KEY(" + VideoGameContract.ImageEntry.GAME_PLAY_ID + ") REFERENCES " +
			VideoGameContract.GamePlayEntry.TABLE_NAME + "(" + VideoGameContract.GamePlayEntry._ID + ")" +
			" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_VIDEO_GAME_PLAYERS_TABLE =
			"CREATE TABLE " + VideoGameContract.PlayerEntry.TABLE_NAME + " (" +
					VideoGameContract.PlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					VideoGameContract.PlayerEntry.NAME + " TEXT NOT NULL, " +
					VideoGameContract.PlayerEntry.SCORE + " REAL, " +
					VideoGameContract.PlayerEntry.WIN + " CHAR(1), " +
					VideoGameContract.PlayerEntry.GAME_PLAY_ID + " INTEGER NOT NULL," +
					"FOREIGN KEY(" + VideoGameContract.PlayerEntry.GAME_PLAY_ID + ") REFERENCES " +
					VideoGameContract.GamePlayEntry.TABLE_NAME + "(" + VideoGameContract.GamePlayEntry._ID + ")" +
					" ON UPDATE CASCADE ON DELETE CASCADE);";

	final String SQL_CREATE_ALL_PLAYERS_TABLE = "CREATE TABLE " + PlayerContract.PlayerEntry.TABLE_NAME + " (" +
			PlayerContract.PlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			PlayerContract.PlayerEntry.NAME + " TEXT UNIQUE NOT NULL, " +
			PlayerContract.PlayerEntry.FACEBOOKID + " TEXT UNIQUE, " +
			PlayerContract.PlayerEntry.NOTES + " TEXT, " +
			PlayerContract.PlayerEntry.IMAGE + " TEXT, " +
			PlayerContract.PlayerEntry.TIMESPLAYED + " INTEGER, " +
			PlayerContract.PlayerEntry.TIMEPLAYED + " INTEGER, " +
			PlayerContract.PlayerEntry.MOSTPLAYEDGAMEBYTIMES + " TEXT, " +
			PlayerContract.PlayerEntry.MOSTPLAYEDGAMEBYTIME + " TEXT, " +
			PlayerContract.PlayerEntry.MOSTWONGAME + " TEXT, " +
			PlayerContract.PlayerEntry.MOSTLOSTGAME + " TEXT, " +
			PlayerContract.PlayerEntry.WINPERCENTAGE + " REAL, " +
			PlayerContract.PlayerEntry.LOSEPERCENTAGE + " REAL);";

	final String SQL_CREATE_TEMP_GAME_PLAYS_TABLE = "CREATE TABLE " + TempContract.GamePlayEntry.TABLE_NAME + " (" +
			TempContract.GamePlayEntry.GAME + " TEXT NOT NULL, " +
			TempContract.GamePlayEntry.GAME_TYPE + " TEXT, " +
			TempContract.GamePlayEntry.DATE + " TEXT NOT NULL, " +
			TempContract.GamePlayEntry.TIME_PLAYED + " INTEGER, " +
			TempContract.GamePlayEntry.NOTES + " TEXT, " +
			TempContract.GamePlayEntry.LOCATION + " TEXT, " +
			TempContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y', " +
			TempContract.GamePlayEntry.TIMER_START + " INTEGER DEFAULT 0, " +
			TempContract.GamePlayEntry.LAST_TIMER_START + " INTEGER DEFAULT 0, " +
			TempContract.GamePlayEntry.LAST_TIMER_STOP + " INTEGER DEFAULT 0, " +
			TempContract.GamePlayEntry.TIMER_DIFF + " INTEGER DEFAULT 0);";

	final String SQL_CREATE_TEMP_GAME_PLAYERS_TABLE = "CREATE TABLE " + TempContract.PlayerEntry.TABLE_NAME + " (" +
			TempContract.PlayerEntry.NAME + " TEXT NOT NULL, " +
			TempContract.PlayerEntry.SCORE + " REAL, " +
			TempContract.PlayerEntry.WIN + " CHAR(1));";

	final String SQL_CREATE_BOARD_GAME_HOTNESS_TABLE =
			"CREATE TABLE " + BoardGameContract.HotnessEntry.TABLE_NAME + " (" +
					BoardGameContract.HotnessEntry.NAME + " TEXT NOT NULL, " +
					BoardGameContract.HotnessEntry.BGGID + " TEXT, " +
					BoardGameContract.HotnessEntry.THUMBNAIL + " TEXT, " +
					BoardGameContract.HotnessEntry.RANK + " INTEGER);";


	public GamesDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_BOARD_GAME_TABLE);
		db.execSQL(SQL_CREATE_BOARD_GAME_PLAYS_TABLE);
		db.execSQL(SQL_CREATE_BOARD_GAME_IMAGE_TABLE);
		db.execSQL(SQL_CREATE_BOARD_GAME_PLAYERS_TABLE);

		db.execSQL(SQL_CREATE_RPG_TABLE);
		db.execSQL(SQL_CREATE_RPG_PLAYS_TABLE);
		db.execSQL(SQL_CREATE_RPG_IMAGE_TABLE);
		db.execSQL(SQL_CREATE_RPG_PLAYERS_TABLE);

		db.execSQL(SQL_CREATE_VIDEO_GAME_TABLE);
		db.execSQL(SQL_CREATE_VIDEO_GAME_PLAYS_TABLE);
		db.execSQL(SQL_CREATE_VIDEO_GAME_IMAGE_TABLE);
		db.execSQL(SQL_CREATE_VIDEO_GAME_PLAYERS_TABLE);

		db.execSQL(SQL_CREATE_ALL_PLAYERS_TABLE);

		db.execSQL(SQL_CREATE_TEMP_GAME_PLAYS_TABLE);
		db.execSQL(SQL_CREATE_TEMP_GAME_PLAYERS_TABLE);

		db.execSQL(SQL_CREATE_BOARD_GAME_HOTNESS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Added location entry in game play table in version 2.
		// Added image table for potential images for game plays.
		if (oldVersion < 2) {
			db.execSQL("ALTER TABLE " + BoardGameContract.GamePlayEntry.TABLE_NAME +
							   " ADD " + BoardGameContract.GamePlayEntry.LOCATION + " TEXT;");
			db.execSQL(SQL_CREATE_BOARD_GAME_IMAGE_TABLE);
		}

		// Added separate tables for rpgs and video games
		if (oldVersion < 3) {
			db.execSQL(SQL_CREATE_RPG_TABLE);
			db.execSQL(SQL_CREATE_RPG_PLAYS_TABLE);
			db.execSQL(SQL_CREATE_RPG_IMAGE_TABLE);
			db.execSQL(SQL_CREATE_RPG_PLAYERS_TABLE);

			db.execSQL(SQL_CREATE_VIDEO_GAME_TABLE);
			db.execSQL(SQL_CREATE_VIDEO_GAME_PLAYS_TABLE);
			db.execSQL(SQL_CREATE_VIDEO_GAME_IMAGE_TABLE);
			db.execSQL(SQL_CREATE_VIDEO_GAME_PLAYERS_TABLE);
		}

		// Added bgg play ids in all three play tables
		if (oldVersion < 4) {
			db.execSQL("ALTER TABLE " + BoardGameContract.GamePlayEntry.TABLE_NAME +
							   " ADD " + BoardGameContract.GamePlayEntry.BGG_PLAY_ID + " TEXT;");

			// Coming from version 2, rpg and video game table
			// will already contain the columns
			if (oldVersion > 2) {
				db.execSQL("ALTER TABLE " + RPGContract.GamePlayEntry.TABLE_NAME +
								   " ADD " + RPGContract.GamePlayEntry.BGG_PLAY_ID + " TEXT;");
				db.execSQL("ALTER TABLE " + VideoGameContract.GamePlayEntry.TABLE_NAME +
								   " ADD " + VideoGameContract.GamePlayEntry.BGG_PLAY_ID + " TEXT;");
			}
		}

		// Added temp table for game play data as it is being entered
		// Will likely have to add a temp image table later.
		if (oldVersion < 5) {
			db.execSQL(SQL_CREATE_TEMP_GAME_PLAYS_TABLE);
			db.execSQL(SQL_CREATE_TEMP_GAME_PLAYERS_TABLE);
		}

		// Adjusted temp table to better control game play data
		// In particular, added game type
		if (oldVersion < 6) {
			// Prior to version 5 will get this update from CREATE command
			// in update to version 5 above
			if (oldVersion == 5) {
				db.execSQL("ALTER TABLE " + TempContract.GamePlayEntry.TABLE_NAME +
								   " ADD " + TempContract.GamePlayEntry.GAME_TYPE + " TEXT;");
			}
		}

		// Added hotness table for ticker
		if (oldVersion < 7) {
			db.execSQL(SQL_CREATE_BOARD_GAME_HOTNESS_TABLE);
		}

		// Added "count for stats" column in each table
		if (oldVersion < 8) {
			db.execSQL("ALTER TABLE " + BoardGameContract.GamePlayEntry.TABLE_NAME +
							   " ADD " + BoardGameContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y';");
			db.execSQL("ALTER TABLE " + RPGContract.GamePlayEntry.TABLE_NAME +
							   " ADD " + RPGContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y';");
			db.execSQL("ALTER TABLE " + VideoGameContract.GamePlayEntry.TABLE_NAME +
							   " ADD " + VideoGameContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y';");
			db.execSQL("ALTER TABLE " + TempContract.GamePlayEntry.TABLE_NAME +
							   " ADD " + TempContract.GamePlayEntry.COUNT_FOR_STATS + " CHAR(1) DEFAULT 'Y';");
		}

		// Added AllPlayers table
		if (oldVersion < 9) {
			db.execSQL(SQL_CREATE_ALL_PLAYERS_TABLE);
			Preferences.setNeedAllPlayerTableUpgrade(context, true);
		}

		// Added images to AllPlayers table
		if (oldVersion < 10) {
			db.execSQL("ALTER TABLE " + PlayerContract.PlayerEntry.TABLE_NAME +
							   " ADD " + PlayerContract.PlayerEntry.IMAGE + " TEXT;");
		}

		//Fixed a blaring issue
		if (oldVersion < 11) {
			ContentValues values = new ContentValues();
			values.put(BoardGameContract.PlayerEntry.NAME, "master_user");
			db.update(BoardGameContract.PlayerEntry.TABLE_NAME,
					  values,
					  BoardGameContract.PlayerEntry.NAME + " = ?",
					  new String[]{"Me"});
			db.update(RPGContract.PlayerEntry.TABLE_NAME,
					  values,
					  BoardGameContract.PlayerEntry.NAME + " = ?",
					  new String[]{"Me"});
			db.update(VideoGameContract.PlayerEntry.TABLE_NAME,
					  values,
					  BoardGameContract.PlayerEntry.NAME + " = ?",
					  new String[]{"Me"});
			db.update(PlayerContract.PlayerEntry.TABLE_NAME,
					  values,
					  BoardGameContract.PlayerEntry.NAME + " = ?",
					  new String[]{"Me"});
			db.update(TempContract.PlayerEntry.TABLE_NAME,
					  values,
					  BoardGameContract.PlayerEntry.NAME + " = ?",
					  new String[]{"Me"});
		}
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
// TODO Uncomment this when you rearrange database to actually
//		work with cascading.
//		db.setForeignKeyConstraintsEnabled(true);
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}
}
