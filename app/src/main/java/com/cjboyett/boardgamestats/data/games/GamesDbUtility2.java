package com.cjboyett.boardgamestats.data.games;

import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 5/30/2016.
 */
public class GamesDbUtility2
{
	/* Returns a list of GamePlayDatas
	 * who - List of player names
	 * what - List of games
	 * when - List of dates
	 * range - whether dates are exact or are ranges
	 * range requires exactly two Dates
	 * where - List of locations
	 *
	 * In all cases, a null value means search all.
	 */
	public static List<GamePlayData> query(GamesDbHelper dbHelper, List<String> who, List<String> what, List<Date> when, boolean range, List<String> where)
	{
		List<GamePlayData> gamePlayDataList = new ArrayList<>();

		String table;
		String[] columns = new String[1];
		String selections;
		String[] selectionArgs;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String limit = null;

		selections = buildSelectionsString(who, what, when, range, where);
		selectionArgs = buildSelectionArgs(who, what, when, range, where);

		table = BoardGameContract.GamePlayEntry.TABLE_NAME;
		columns[0] = BoardGameContract.GamePlayEntry._ID;
		dbHelper.getReadableDatabase().query(table, columns, selections, selectionArgs, groupBy, having, orderBy, limit);

		table = RPGContract.GamePlayEntry.TABLE_NAME;
//		columns[0] = RPGContract.GamePlayEntry._ID;
		dbHelper.getReadableDatabase().query(table, columns, selections, selectionArgs, groupBy, having, orderBy, limit);

		table = VideoGameContract.GamePlayEntry.TABLE_NAME;
//		columns[0] = VideoGameContract.GamePlayEntry._ID;
		dbHelper.getReadableDatabase().query(table, columns, selections, selectionArgs, groupBy, having, orderBy, limit);
		return gamePlayDataList;
	}

	private static String buildSelectionsString(List<String> who, List<String> what, List<Date> when, boolean range, List<String> where)
	{
		String selection = "";
//		for (String person : who) selection +=
		return selection;
	}

	private static String[] buildSelectionArgs(List<String> who, List<String> what, List<Date> when, boolean range, List<String> where)
	{
		return null;
	}

}
