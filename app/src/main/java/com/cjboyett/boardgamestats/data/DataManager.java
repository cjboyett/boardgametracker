package com.cjboyett.boardgamestats.data;

import android.app.Application;
import android.os.AsyncTask;

import com.cjboyett.boardgamestats.MyApp;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.data.games.HotnessXmlParser;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/23/2016.
 */
public class DataManager {
	private static DataManager instance = null;
	private Application application;

	private List<String> allGamesCombined;
	private List<String> allPlayedGamesCombined;
	private List<String> allPlayers;
	private List<String> allLocations;

	private List<HotnessXmlParser.Item> allHotnessItems;

	private DataManager(Application application) {
		this.application = application;
	}

	public static DataManager getInstance(Application application) {
		if (instance == null || ActivityUtilities.databaseChanged(application)) instance = new DataManager(application);
		return instance;
	}

	public void initialize() {
		instance.getAllGamesCombined();
		instance.getAllPlayedGamesCombined();
		instance.getAllPlayers();
		instance.getAllHotnessItems();
	}

	public List<String> getAllGamesCombined() {
		if (allGamesCombined == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(application);
			allGamesCombined = GamesDbUtility.getAllGamesSortedWithType(dbHelper);
			dbHelper.close();
		}

		return allGamesCombined;
	}

	public List<String> getAllPlayedGamesCombined() {
		if (allPlayedGamesCombined == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(application);
			allPlayedGamesCombined = GamesDbUtility.getAllPlayedGamesSortedWithType(dbHelper);
			dbHelper.close();
		}

		return allPlayedGamesCombined;
	}

	public List<String> getAllPlayers() {
		if (allPlayers == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(application);
			allPlayers = GamesDbUtility.getAllPlayersSorted(dbHelper);
			dbHelper.close();
		}

		return allPlayers;
	}

	public List<String> getAllLocations() {
		if (allLocations == null) {
			GamesDbHelper dbHelper = new GamesDbHelper(application);
			allLocations = GamesDbUtility.getAllLocationsSorted(dbHelper);
			dbHelper.close();
		}

		return allLocations;
	}

	public List<HotnessXmlParser.Item> getAllHotnessItems() {
		if (allHotnessItems == null) {
			if (((MyApp) application).isConnectedToInternet() && Preferences.getLastHotnessDownload(application) <
					System.currentTimeMillis() - 1000 * 60 * 60 * 24) {
				new AsyncTask<String, Void, List<HotnessXmlParser.Item>>() {
					@Override
					protected List<HotnessXmlParser.Item> doInBackground(String... params) {
						List<HotnessXmlParser.Item> items = new ArrayList<>();
						for (String url : params) items.addAll(UrlUtilities.loadHotnessXmlFromNetwork(url));
						return items;
					}

					@Override
					protected void onPostExecute(List<HotnessXmlParser.Item> items) {
						allHotnessItems = items;
						GamesDbHelper dbHelper = new GamesDbHelper(application);
						BoardGameDbUtility.clearHotnessTable(dbHelper);
						BoardGameDbUtility.populateHotnessTable(dbHelper, items);
						dbHelper.close();
						Preferences.setLastHotnessDownload(application, System.currentTimeMillis());
					}
				}.execute("https://www.boardgamegeek.com/xmlapi2/hot?type=boardgame",
						  "https://www.boardgamegeek.com/xmlapi2/hot?type=rpg",
						  "https://www.boardgamegeek.com/xmlapi2/hot?type=videogame");

			} else {
				GamesDbHelper dbHelper = new GamesDbHelper(application);
				allHotnessItems = BoardGameDbUtility.getHotnessItems(dbHelper);
				dbHelper.close();
			}
		}

		return allHotnessItems;
	}
}
