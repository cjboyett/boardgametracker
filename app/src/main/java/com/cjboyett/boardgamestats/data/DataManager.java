package com.cjboyett.boardgamestats.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.data.games.HotnessXmlParser;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Casey on 4/23/2016.
 */
public class DataManager
{
	private static DataManager instance = null;
	private Context context;

	private List<String> allGamesCombined;
	private List<String> allPlayedGamesCombined;
	private List<String> allPlayers;
	private List<String> allLocations;

	private List<HotnessXmlParser.Item> allHotnessItems;

	private DataManager(Context context)
	{
		this.context = context;
	}

	public static DataManager getInstance(Context context)
	{
		if (instance == null || ActivityUtilities.databaseChanged(context)) instance = new DataManager(context);
		return instance;
	}

	public void initialize()
	{
		instance.getAllGamesCombined();
		instance.getAllPlayedGamesCombined();
		instance.getAllPlayers();
		instance.getAllHotnessItems();
	}

	public List<String> getAllGamesCombined()
	{
		if (allGamesCombined == null)
		{
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allGamesCombined = GamesDbUtility.getAllGamesSortedWithType(dbHelper);
			dbHelper.close();
		}

		return allGamesCombined;
	}

	public List<String> getAllPlayedGamesCombined()
	{
		if (allPlayedGamesCombined == null)
		{
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allPlayedGamesCombined = GamesDbUtility.getAllPlayedGamesSortedWithType(dbHelper);
			dbHelper.close();
		}

		return allPlayedGamesCombined;
	}

	public List<String> getAllPlayers()
	{
		if (allPlayers == null)
		{
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allPlayers = GamesDbUtility.getAllPlayersSorted(dbHelper, threeMonthsAgo());
			dbHelper.close();
		}

		return allPlayers;
	}

	public List<String> getAllLocations()
	{
		if (allLocations == null)
		{
			GamesDbHelper dbHelper = new GamesDbHelper(context);
			allLocations = GamesDbUtility.getAllLocationsSorted(dbHelper, threeMonthsAgo());
			dbHelper.close();
		}

		return allLocations;
	}

	private Date threeMonthsAgo()
	{
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		Date tempDate;
		if (month >= 3) tempDate = new Date(year, month - 3, day);
		else tempDate = new Date(year - 1, month + 9, day);
		return tempDate;
	}

	public List<HotnessXmlParser.Item> getAllHotnessItems()
	{
		if (allHotnessItems == null)
		{
			Log.d("HOTMESS", "1");
			if (Preferences.getLastHotnessDownload(context) < System.currentTimeMillis() - 1000 * 60 * 60 * 24)
			{
				Log.d("HOTMESS", "2");

				new AsyncTask<String, Void, List<HotnessXmlParser.Item>>()
				{
					@Override
					protected List<HotnessXmlParser.Item> doInBackground(String... params)
					{
						Log.d("HOTMESS", "Yes it is");
						List<HotnessXmlParser.Item> items = new ArrayList<>();
						for (String url : params) items.addAll(UrlUtilities.loadHotnessXmlFromNetwork(url));
						return items;
					}

					@Override
					protected void onPostExecute(List<HotnessXmlParser.Item> items)
					{
						allHotnessItems = items;
						GamesDbHelper dbHelper = new GamesDbHelper(context);
						BoardGameDbUtility.clearHotnessTable(dbHelper);
						BoardGameDbUtility.populateHotnessTable(dbHelper, items);
						dbHelper.close();
						Preferences.setLastHotnessDownload(context, System.currentTimeMillis());
					}
				}.execute("https://www.boardgamegeek.com/xmlapi2/hot?type=boardgame",
				          "https://www.boardgamegeek.com/xmlapi2/hot?type=rpg",
				          "https://www.boardgamegeek.com/xmlapi2/hot?type=videogame");

				Log.d("HOTMESS", "3");
			}
			else
			{
				Log.d("HOTMESS", "4");
				GamesDbHelper dbHelper = new GamesDbHelper(context);
				allHotnessItems = BoardGameDbUtility.getHotnessItems(dbHelper);
			    dbHelper.close();
				Log.d("HOTMESS", "5");
			}
		}

		return allHotnessItems;
	}
}
