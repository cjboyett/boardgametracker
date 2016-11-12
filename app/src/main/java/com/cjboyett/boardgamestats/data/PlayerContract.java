package com.cjboyett.boardgamestats.data;

import android.provider.BaseColumns;

/**
 * Created by Casey on 8/10/2016.
 */
public class PlayerContract
{
	public static final class PlayerEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "allplayers";

		public static final String NAME = "name";
		public static final String FACEBOOKID = "facebookid";
		public static final String NOTES = "notes";
		public static final String IMAGE = "image";

		//Various recorded stats
		public static final String TIMESPLAYED = "timesgame";
		public static final String TIMEPLAYED = "timeplayed";
		public static final String MOSTPLAYEDGAMEBYTIME = "mostplayedgametimes";
		public static final String MOSTPLAYEDGAMEBYTIMES = "mostplayedgametime";
		public static final String MOSTWONGAME = "mostwongame";
		public static final String MOSTLOSTGAME = "mostlostgame";
		public static final String WINPERCENTAGE = "winpercentage";
		public static final String LOSEPERCENTAGE = "losepercentage";
	}
}
